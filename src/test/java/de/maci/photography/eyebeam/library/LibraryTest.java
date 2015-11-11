package de.maci.photography.eyebeam.library;

import de.maci.photography.eyebeam.library.metadata.ExifData;
import de.maci.photography.eyebeam.library.metadata.Metadata;
import de.maci.photography.eyebeam.library.storage.InMemoryDataStore;
import de.maci.photography.eyebeam.library.storage.LibraryDataStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

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
