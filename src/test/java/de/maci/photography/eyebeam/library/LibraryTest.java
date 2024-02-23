package de.maci.photography.eyebeam.library;

import de.maci.photography.eyebeam.library.indexing.FilesystemScanner;
import de.maci.photography.eyebeam.library.metadata.Metadata;
import de.maci.photography.eyebeam.library.metadata.MetadataReader;
import de.maci.photography.eyebeam.library.storage.InMemoryDataStore;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.jayway.awaitility.Awaitility.await;
import static de.maci.photography.eyebeam.library.testhelper.MockingHelper.mockFileScanner;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
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

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path temporaryFolderPath;

    private Path firstSampleFile;
    private Path secondSampleFile;
    private Path thirdSampleFile;

    @Before
    public void setUp() throws Exception {
        Path sampleFilePath = Paths.get(getClass().getResource("sample.jpg").toURI());

        this.temporaryFolderPath = temporaryFolder.getRoot().toPath();
        this.firstSampleFile = temporaryFolderPath.resolve("firstSampleFile.jpg");
        this.secondSampleFile = temporaryFolderPath.resolve("secondSampleFile.jpg");
        this.thirdSampleFile = temporaryFolderPath.resolve("thirdSampleFile.jpg");

        Files.copy(sampleFilePath, firstSampleFile);
        Files.copy(sampleFilePath, secondSampleFile);
        Files.copy(sampleFilePath, thirdSampleFile);
    }

    @Test
    public void photosReturnsEmptySet_IfCorrespondingDataStoreIsEmpty() throws Exception {
        LibraryDataStore dataStore = InMemoryDataStore.empty();
        Library sut = Library.newInstance(dataStore, anyConfig());

        assertThat(sut.photos().count(), equalTo(0L));
    }

    @Test
    public void photosReturnsSetOfPhotosContainedInTheDatastore_IfCorrespondingDataStoreIsNotEmpty() throws Exception {
        LibraryDataStore dataStore = dataStoreContainingThreePhotos();
        Library sut = Library.newInstance(dataStore, anyConfig());

        List<Photo> expectedPhotos = dataStore.photos().collect(toList());

        MatcherAssert.assertThat(sut.photos().collect(toList()),
                                 containsInAnyOrder(expectedPhotos.get(0),
                                                    expectedPhotos.get(1),
                                                    expectedPhotos.get(2)));
    }

    @Test
    public void countPhotosReturnsZero_IfCorrespondingDataStoreIsEmpty() throws Exception {
        Library sut = Library.newInstance(InMemoryDataStore.empty(), anyConfig());

        MatcherAssert.assertThat(sut.countPhotos(), equalTo(0L));
    }

    @Test
    public void countPhotosReturnsNumberOfPhotosContainedInTheDatastore_IfCorrespondingDataStoreIsNotEmpty() throws Exception {
        Library sut = Library.newInstance(dataStoreContainingThreePhotos(), anyConfig());

        MatcherAssert.assertThat(sut.countPhotos(), equalTo(3L));
    }

    @Test
    public void dataStoreCanBeCleared() throws Exception {
        Library sut = Library.newInstance(dataStoreContainingThreePhotos(), anyConfig());
        sut.clear();

        MatcherAssert.assertThat(sut.countPhotos(), equalTo(0L));
    }

    @Test
    public void aNoSuchElementExceptionIsThrownWhenTryingToReadMetadata_IfTheDataStoreDoesNotContainTheCorrespondingPhoto() throws Exception {
        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage("Data store does not contain 'aSecondPhoto.jpg'.");

        LibraryDataStore dataStore = InMemoryDataStore.empty();
        dataStore.store(Photo.locatedAt(Paths.get("somePhoto.jpg")));

        Library sut = Library.newInstance(dataStore, anyConfig());

        sut.metadataOf(Photo.locatedAt(Paths.get("aSecondPhoto.jpg")));
    }

    @Test
    public void metadataIsAbsent_IfTheDataStoreContainsTheCorrespondingPhotoButNoMetadataInformationIsAvailable() throws Exception {
        LibraryDataStore dataStore = InMemoryDataStore.empty();
        dataStore.store(Photo.locatedAt(Paths.get("somePhoto.jpg")));

        Library sut = Library.newInstance(dataStore, anyConfig());

        assertFalse(sut.metadataOf(Photo.locatedAt(Paths.get("somePhoto.jpg"))).isPresent());
    }

    @Test
    public void metadataIsReturned_IfTheDataStoreContainsTheCorrespondingPhotoButNoMetadataInformationIsAvailable() throws Exception {
        Photo photo = Photo.locatedAt(Paths.get("somePhoto.jpg"));
        Metadata metadata = Metadata.empty();

        LibraryDataStore dataStore = InMemoryDataStore.empty();
        dataStore.store(photo);
        dataStore.replaceMetadata(photo, metadata);

        Library sut = Library.newInstance(dataStore, anyConfig());

        MatcherAssert.assertThat(sut.metadataOf(photo).get(), is(metadata));
    }

    @Test
    public void metadataExistsEvaluatesToFalse_IfMetadataIsNotPresent() throws Exception {
        Photo photo = Photo.locatedAt(Paths.get("somePhoto.jpg"));

        InMemoryDataStore dataStore = InMemoryDataStore.empty();
        dataStore.store(photo);

        Library sut = Library.newInstance(dataStore, anyConfig());

        assertFalse(sut.metadataExists(photo));
    }

    @Test
    public void metadataExistsEvaluatesToTrue_IfMetadataIsPresent() throws Exception {
        Photo photo = Photo.locatedAt(Paths.get("somePhoto.jpg"));
        Metadata metadata = Metadata.empty();

        InMemoryDataStore dataStore = InMemoryDataStore.empty();
        dataStore.store(photo);
        dataStore.replaceMetadata(photo, metadata);

        Library sut = Library.newInstance(dataStore, anyConfig());

        assertTrue(sut.metadataExists(photo));
    }

    @Test
    public void refreshingFlagIsSet_IfLibraryIsReindexed() throws Exception {
        InMemoryDataStore dataStore = InMemoryDataStore.empty();
        LibraryConfiguration configuration = anyConfig();

        Library sut = Library.newInstance(dataStore, configuration);

        LibraryReindexer reindexer = new LibraryReindexer(sut, configuration, photo -> true) {

            @Override
            protected FilesystemScanner createScanner(Predicate<Path> fileFilter) {
                int sleepFor = 500;
                return sleepingFilesystemScanner(sleepFor);
            }
        };

        new Thread(() -> reindexer.reindexLibrary()).start();

        await().atMost(250, MILLISECONDS).until(() -> sut.isReindexing());
        await().atMost(1, SECONDS).until(() -> !sut.isReindexing());
    }

    @Test
    public void reindexingFlagIsNotSet_IfLibraryInstanceJustHasBeenCreated() throws Exception {
        Library sut = Library.newInstance(InMemoryDataStore.empty(), anyConfig());

        assertFalse(sut.isReindexing());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void photosAreAddedWithRelativizedPathToTheDataStore_WhileReindexing() throws Exception {
        LibraryDataStore dataStore = mock(LibraryDataStore.class);
        when(dataStore.photos()).thenReturn(Collections.<Photo>emptySet().stream());

        Library sut = Library.newInstance(dataStore, new LibraryConfiguration() {
            @Override
            public Path rootFolder() {
                return temporaryFolderPath;
            }

            @Override
            public Optional<Predicate<Path>> fileFilter() {
                return Optional.empty();
            }
        });

        sut.createReindexer().reindexLibrary();

        verify(dataStore).store(Photo.locatedAt(temporaryFolderPath.relativize(firstSampleFile)));
        verify(dataStore).store(Photo.locatedAt(temporaryFolderPath.relativize(secondSampleFile)));
        verify(dataStore).store(Photo.locatedAt(temporaryFolderPath.relativize(thirdSampleFile)));
    }

    @Test
    public void metadataIsResolved_WhileReindexing() throws Exception {
        LibraryDataStore dataStore = InMemoryDataStore.empty();

        MetadataReader metadataReader = mock(MetadataReader.class);
        when(metadataReader.readFrom(any(Path.class))).thenReturn(Metadata.empty());

        Library sut = Library.newInstance(dataStore, new LibraryConfiguration() {
            @Override
            public Path rootFolder() {
                return temporaryFolderPath;
            }

            @Override
            public Optional<Predicate<Path>> fileFilter() {
                return Optional.empty();
            }

            @Override
            public Supplier<MetadataReader> metadataReader() {
                return () -> metadataReader;
            }
        });

        sut.createReindexer().reindexLibrary();

        verify(metadataReader).readFrom(firstSampleFile);
        verify(metadataReader).readFrom(secondSampleFile);
        verify(metadataReader).readFrom(thirdSampleFile);

        dataStore.photos().forEach(photo -> assertTrue(dataStore.metadataOf(photo).isPresent()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void aCustomMetadataReaderCanBeConfigured() throws Exception {
        Supplier<MetadataReader> metadataReaderFactory = mock(Supplier.class);
        when(metadataReaderFactory.get()).thenAnswer(i -> {
            MetadataReader metadataReader = mock(MetadataReader.class);
            when(metadataReader.readFrom(any(Path.class))).thenAnswer(t -> mock(Metadata.class));
            return metadataReader;
        });

        Library sut = Library.newInstance(InMemoryDataStore.empty(), new LibraryConfiguration() {
            @Override
            public Path rootFolder() {
                return temporaryFolderPath;
            }

            @Override
            public Optional<Predicate<Path>> fileFilter() {
                return Optional.empty();
            }

            @Override
            public Supplier<MetadataReader> metadataReader() {
                return metadataReaderFactory;
            }
        });

        sut.createReindexer().reindexLibrary();

        verify(metadataReaderFactory).get();
    }

    @Test
    public void presentMetadataIsNotRefreshed() throws Exception {
        LibraryDataStore dataStore = InMemoryDataStore.empty();

        Library sut = Library.newInstance(dataStore, new LibraryConfiguration() {
            @Override
            public Path rootFolder() {
                return temporaryFolderPath;
            }

            @Override
            public Optional<Predicate<Path>> fileFilter() {
                return Optional.empty();
            }
        });

        sut.createReindexer().reindexLibrary();

        Metadata firstMetadata = sut.metadataOf(photo(firstSampleFile)).get();
        Metadata secondMetadata = sut.metadataOf(photo(secondSampleFile)).get();
        Metadata thirdMetadata = sut.metadataOf(photo(thirdSampleFile)).get();

        sut.createReindexer().reindexLibrary();

        assertThat(sut.metadataOf(photo(firstSampleFile)).get().extractedAt(), equalTo(firstMetadata.extractedAt()));
        assertThat(sut.metadataOf(photo(secondSampleFile)).get().extractedAt(), equalTo(secondMetadata.extractedAt()));
        assertThat(sut.metadataOf(photo(thirdSampleFile)).get().extractedAt(), equalTo(thirdMetadata.extractedAt()));
    }

    @Test
    public void presentMetadataIsRefreshed_IfTheConfiguredRefreshDeciderEvaluatesToTrue() throws Exception {
        LibraryDataStore dataStore = InMemoryDataStore.empty();

        Library sut = Library.newInstance(dataStore, new LibraryConfiguration() {
            @Override
            public Path rootFolder() {
                return temporaryFolderPath;
            }

            @Override
            public Optional<Predicate<Path>> fileFilter() {
                return Optional.empty();
            }
        });

        sut.createReindexer().reindexLibrary();

        Metadata firstMetadata = sut.metadataOf(photo(firstSampleFile)).get();
        Metadata secondMetadata = sut.metadataOf(photo(secondSampleFile)).get();
        Metadata thirdMetadata = sut.metadataOf(photo(thirdSampleFile)).get();

        sut.createReindexer().withCustomReindexingNecessaryDecision(photo -> true).reindexLibrary();

        assertTrue(sut.metadataOf(photo(firstSampleFile)).get().extractedAt().isAfter(firstMetadata.extractedAt()));
        assertTrue(sut.metadataOf(photo(secondSampleFile)).get().extractedAt().isAfter(secondMetadata.extractedAt()));
        assertTrue(sut.metadataOf(photo(thirdSampleFile)).get().extractedAt().isAfter(thirdMetadata.extractedAt()));
    }

    @Test
    public void aNewUpdaterInstanceCanBeCreated() throws Exception {
        Library sut = Library.newInstance(mock(LibraryDataStore.class), new LibraryConfiguration() {
            @Override
            public Path rootFolder() {
                return temporaryFolderPath;
            }

            @Override
            public Optional<Predicate<Path>> fileFilter() {
                return Optional.empty();
            }
        });

        assertThat(sut.createReindexer(), notNullValue());
    }

    @SuppressWarnings("unchecked")
    private static FilesystemScanner sleepingFilesystemScanner(int sleepFor) {
        return mockFileScanner(invocation -> {
            try {
                Thread.sleep(sleepFor);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private static LibraryDataStore dataStoreContainingThreePhotos() {
        LibraryDataStore dataStore = InMemoryDataStore.empty();
        dataStore.store(Photo.locatedAt(Paths.get("somePhoto.jpg")));
        dataStore.store(Photo.locatedAt(Paths.get("aSecondPhoto.jpg")));
        dataStore.store(Photo.locatedAt(Paths.get("aThirdPhoto.jpg")));
        return dataStore;
    }

    private static Photo photo(Path path) {
        return Photo.locatedAt(path.getFileName());
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
