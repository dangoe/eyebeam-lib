package de.maci.photography.eyebeam.library;

import com.google.common.util.concurrent.Uninterruptibles;
import de.maci.photography.eyebeam.library.indexing.FilesystemScanner;
import de.maci.photography.eyebeam.library.metadata.Metadata;
import de.maci.photography.eyebeam.library.metadata.MetadataReader;
import de.maci.photography.eyebeam.library.storage.InMemoryDataStore;
import de.maci.photography.eyebeam.library.storage.LibraryDataStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.jayway.awaitility.Awaitility.await;
import static de.maci.photography.eyebeam.library.testhelper.MockingHelper.mockFileScanner;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 07.11.15
 */
public class LibraryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void photosReturnsEmptySet_IfCorrespondingDataStoreIsEmpty() throws Exception {
        LibraryDataStore dataStore = new InMemoryDataStore();
        Library sut = Library.newInstance(anyConfig(), () -> dataStore);

        assertTrue(sut.photos().isEmpty());
    }

    @Test
    public void photosReturnsSetOfPhotosContainedInTheDatastore_IfCorrespondingDataStoreIsNotEmpty() throws Exception {
        LibraryDataStore dataStore = dataStoreContainingThreePhotos();
        Library sut = Library.newInstance(anyConfig(), () -> dataStore);

        List<Photo> expectedPhotos = new ArrayList<>(dataStore.photos());

        assertThat(sut.photos(),
                   containsInAnyOrder(expectedPhotos.get(0), expectedPhotos.get(1), expectedPhotos.get(2)));
    }

    @Test
    public void countPhotosReturnsZero_IfCorrespondingDataStoreIsEmpty() throws Exception {
        Library sut = Library.newInstance(anyConfig(), () -> new InMemoryDataStore());

        assertThat(sut.countPhotos(), equalTo(0L));
    }

    @Test
    public void countPhotosReturnsNumberOfPhotosContainedInTheDatastore_IfCorrespondingDataStoreIsNotEmpty() throws Exception {
        Library sut = Library.newInstance(anyConfig(), () -> dataStoreContainingThreePhotos());

        assertThat(sut.countPhotos(), equalTo(3L));
    }

    @Test
    public void dataStoreCanBeCleared() throws Exception {
        Library sut = Library.newInstance(anyConfig(), () -> dataStoreContainingThreePhotos());
        sut.clear();

        assertThat(sut.countPhotos(), equalTo(0L));
    }

    @Test
    public void aNoSuchElementExceptionIsThrownWhenTryingToReadMetadata_IfTheDataStoreDoesNotContainTheCorrespondingPhoto() throws Exception {
        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage("Data store does not contain 'aSecondPhoto.jpg'.");

        LibraryDataStore dataStore = new InMemoryDataStore();
        dataStore.store(new Photo(Paths.get("somePhoto.jpg")));

        Library sut = Library.newInstance(anyConfig(), () -> dataStore);

        sut.metadataOf(new Photo(Paths.get("aSecondPhoto.jpg")));
    }

    @Test
    public void metadataIsAbsent_IfTheDataStoreContainsTheCorrespondingPhotoButNoMetadataInformationIsAvailable() throws Exception {
        LibraryDataStore dataStore = new InMemoryDataStore();
        dataStore.store(new Photo(Paths.get("somePhoto.jpg")));

        Library sut = Library.newInstance(anyConfig(), () -> dataStore);

        assertFalse(sut.metadataOf(new Photo(Paths.get("somePhoto.jpg"))).isPresent());
    }

    @Test
    public void metadataIsReturned_IfTheDataStoreContainsTheCorrespondingPhotoButNoMetadataInformationIsAvailable() throws Exception {
        Photo photo = new Photo(Paths.get("somePhoto.jpg"));
        Metadata metadata = Metadata.empty();

        LibraryDataStore dataStore = new InMemoryDataStore();
        dataStore.store(photo);
        dataStore.replaceMetadata(photo, metadata);

        Library sut = Library.newInstance(anyConfig(), () -> dataStore);

        assertThat(sut.metadataOf(photo).get(), is(metadata));
    }

    @Test
    public void refreshingFlagIsSet_IfLibraryIsRefreshing() throws Exception {
        Library sut = new Library(anyConfig(), () -> new InMemoryDataStore()) {

            @Override
            protected FilesystemScanner createScanner(Predicate<Path> fileFilter) {
                int sleepFor = 500;
                return sleepingFilesystemScanner(sleepFor);
            }
        };

        new Thread(() -> sut.refresh()).start();

        await().atMost(250, MILLISECONDS).until(() -> sut.isRefreshing());
        await().atMost(1, SECONDS).until(() -> !sut.isRefreshing());
    }

    @Test
    public void refreshingFlagIsNotSet_IfLibraryInstanceJustHasBeenCreated() throws Exception {
        Library sut = Library.newInstance(anyConfig(), () -> new InMemoryDataStore());

        assertFalse(sut.isRefreshing());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void photosAreAddedWithRelativizedPathToTheDataStore_WhileRefresing() throws Exception {
        Path rootFolder = Paths.get("/some/folder");
        Path somePhotoPath = rootFolder.resolve("somePhoto.jpg");
        Path aSecondPhotoPath = rootFolder.resolve("aSecondPhoto.jpg");
        Path aThirdPhotoPath = rootFolder.resolve("aThirdPhoto.jpg");

        LibraryDataStore dataStore = mock(LibraryDataStore.class);

        Library sut = new Library(new LibraryConfiguration() {
            @Override
            public Path rootFolder() {
                return rootFolder;
            }

            @Override
            public Optional<Predicate<Path>> fileFilter() {
                return Optional.empty();
            }
        }, () -> dataStore) {
            @Override
            protected FilesystemScanner createScanner(Predicate<Path> fileFilter) {
                return scannerOnPaths(asList(somePhotoPath, aSecondPhotoPath, aThirdPhotoPath));
            }
        };

        sut.refresh();

        verify(dataStore).store(new Photo(rootFolder.relativize(somePhotoPath)));
        verify(dataStore).store(new Photo(rootFolder.relativize(aSecondPhotoPath)));
        verify(dataStore).store(new Photo(rootFolder.relativize(aThirdPhotoPath)));
    }

    @Test
    public void metadataIsResolved_WhileRefreshing() throws Exception {
        Path rootFolder = Paths.get("/some/folder");
        Path somePhotoPath = rootFolder.resolve("somePhoto.jpg");
        Path aSecondPhotoPath = rootFolder.resolve("aSecondPhoto.jpg");
        Path aThirdPhotoPath = rootFolder.resolve("aThirdPhoto.jpg");

        LibraryDataStore dataStore = new InMemoryDataStore();
        MetadataReader metadataReader = mock(MetadataReader.class);
        when(metadataReader.readFrom(any(Path.class))).thenReturn(Optional.of(Metadata.empty()));

        Library sut = new Library(new LibraryConfiguration() {
            @Override
            public Path rootFolder() {
                return rootFolder;
            }

            @Override
            public Optional<Predicate<Path>> fileFilter() {
                return Optional.empty();
            }
        }, () -> dataStore) {
            @Override
            protected FilesystemScanner createScanner(Predicate<Path> fileFilter) {
                return scannerOnPaths(asList(somePhotoPath, aSecondPhotoPath, aThirdPhotoPath));
            }

            @Override
            protected MetadataReader createMetadataReader() {
                return metadataReader;
            }
        };

        sut.refresh();

        verify(metadataReader).readFrom(somePhotoPath);
        verify(metadataReader).readFrom(aSecondPhotoPath);
        verify(metadataReader).readFrom(aThirdPhotoPath);

        dataStore.photos().forEach(photo -> assertTrue(dataStore.metadataOf(photo).isPresent()));
    }

    @SuppressWarnings("unchecked")
    private static FilesystemScanner scannerOnPaths(Collection<Path> paths) {
        return mockFileScanner(invocation -> paths
                .forEach(path -> ((Consumer<Path>) invocation.getArguments()[1]).accept(path)));
    }

    @SuppressWarnings("unchecked")
    private static FilesystemScanner sleepingFilesystemScanner(int sleepFor) {
        return mockFileScanner(invocation -> Uninterruptibles.sleepUninterruptibly(sleepFor, MILLISECONDS));
    }

    private static LibraryDataStore dataStoreContainingThreePhotos() {
        LibraryDataStore dataStore = new InMemoryDataStore();
        dataStore.store(new Photo(Paths.get("somePhoto.jpg")));
        dataStore.store(new Photo(Paths.get("aSecondPhoto.jpg")));
        dataStore.store(new Photo(Paths.get("aThirdPhoto.jpg")));
        return dataStore;
    }

    private static LibraryConfiguration anyConfig() {
        return config(anyPath(), path -> true);
    }

    private static Path anyPath() {
        return Paths.get(".");
    }

    private static LibraryConfiguration config(Path rootFolder, Predicate<Path> fileFilter) {
        return new LibraryConfiguration() {

            @Override
            public Path rootFolder() {
                return rootFolder;
            }

            @Override
            public Optional<Predicate<Path>> fileFilter() {
                return Optional.of(fileFilter);
            }
        };
    }
}
