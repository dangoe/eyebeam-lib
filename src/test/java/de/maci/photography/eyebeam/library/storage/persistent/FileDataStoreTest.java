package de.maci.photography.eyebeam.library.storage.persistent;

import de.maci.photography.eyebeam.library.Photo;
import de.maci.photography.eyebeam.library.metadata.ExifData;
import de.maci.photography.eyebeam.library.metadata.Metadata;
import de.maci.photography.eyebeam.library.testhelper.matcher.MetadataMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.NoSuchElementException;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 28.10.15
 */
public class FileDataStoreTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void containsNoData_IfNewInstance() throws Exception {
        FileDataStore sut = newFileDataStore();

        assertThat(sut.photos().collect(toSet()), emptyIterable());
        assertThat(sut.size(), equalTo(0L));
    }

    @Test
    public void aPhotoCanBeAdded_IfTheDataStoreIsEmpty() throws Exception {
        FileDataStore sut = newFileDataStore();
        Photo photo = somePhoto();

        assertTrue(sut.store(photo));
        assertTrue(sut.contains(photo));
        assertThat(sut.photos().collect(toSet()), equalTo(singleton(photo)));
    }

    @Test
    public void aPhotoIsNotAdded_IfAlreadyContainedInTheDataStore() throws Exception {
        FileDataStore sut = newFileDataStore();
        Photo photo = somePhoto();
        sut.store(photo);

        assertFalse(sut.store(photo));
    }

    @Test
    public void metadataCannotBeSet_IfTheCorrespondingPhotoIsNotContainedInTheDataStore() throws Exception {
        FileDataStore sut = newFileDataStore();

        String path = "/some/path.jpg";

        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage("Data store does not contain '" + path + "'.");

        sut.updateMetadata(photoWithPath(path), Metadata.empty());
    }

    @Test
    public void metadataCanBeSet_IfTheCorrespondingPhotoIsContainedInTheDataStore() throws Exception {
        FileDataStore sut = newFileDataStore();
        Photo photo = somePhoto();
        sut.store(photo);
        Metadata metadata = Metadata.empty();
        sut.updateMetadata(photo, metadata);

        assertThat(sut.metadataOf(photo).get(), equalTo(metadata));
    }

    @Test
    public void metadataCannotBeRead_IfTheCorrespondingPhotoIsNotContainedInTheDataStore() throws Exception {
        FileDataStore sut = newFileDataStore();

        String path = "/some/photo.jpg";

        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage("Data store does not contain '" + path + "'.");

        sut.metadataOf(photoWithPath(path));
    }

    @Test
    public void emptyDataStoreIsEmpty_IfCleared() throws Exception {
        FileDataStore sut = newFileDataStore();
        sut.clear();
        assertThat(sut.size(), equalTo(0L));
    }

    @Test
    public void nonEmptyDataStoreIsEmpty_IfCleared() throws Exception {
        FileDataStore sut = newFileDataStore();
        sut.store(photoWithPath("/some/photo.jpg"));
        sut.store(photoWithPath("/some/other/photo.jpg"));
        sut.clear();

        assertThat(sut.size(), equalTo(0L));
    }

    @Test
    public void metadataExistsEvaluatesToFalse_IfMetadataIsNotPresent() throws Exception {
        Photo photo = somePhoto();

        FileDataStore sut = newFileDataStore();
        sut.store(photo);

        assertFalse(sut.metadataExists(photo));
    }

    @Test
    public void metadataExistsEvaluatesToTrue_IfMetadataIsPresent() throws Exception {
        Photo photo = somePhoto();
        Metadata metadata = Metadata.empty();

        FileDataStore sut = newFileDataStore();
        sut.store(photo);
        sut.updateMetadata(photo, metadata);

        assertTrue(sut.metadataExists(photo));
    }

    @Test
    public void aDataStoreCanBeFlushedAndRestored() throws Exception {
        FileDataStore sut = newFileDataStore();
        Photo photoWithMetadata = photoWithPath("/some/photo.jpg");
        sut.store(photoWithMetadata);
        Instant now = Instant.now();
        Metadata metadata = new Metadata(42L,
                                         null,
                                         ExifData.empty().withFnumber(1d).withFocalLength(2)
                                                 .withFocalLengthFullFrameEquivalent(3).withIso(4).withTakenAt(now));
        sut.updateMetadata(photoWithMetadata, metadata);
        Photo photoWithoutMetadata = photoWithPath("/some/other/photo.jpg");
        sut.store(photoWithoutMetadata);
        sut.flush();
        sut.clear();
        sut.restore();

        assertThat(sut.photos().collect(toSet()), containsInAnyOrder(photoWithMetadata, photoWithoutMetadata));
        assertThat(sut.metadataOf(photoWithMetadata).get(), new MetadataMatcher(metadata));
        assertFalse(sut.metadataExists(photoWithoutMetadata));
    }

    @Test
    public void aDataStoreCanBeRestored() throws Exception {
        Files.copy(getClass().getResourceAsStream("photos.dat"),
                   Paths.get(temporaryFolder.getRoot().toPath().toString(), "photos.dat"));

        FileDataStore sut = newFileDataStore();
        sut.restore();

        assertThat(sut.photos().collect(toSet()),
                   containsInAnyOrder(photoWithPath("/some/photo.jpg"), photoWithPath("/some/other/photo.jpg")));
        assertThat(sut.metadataOf(photoWithPath("/some/photo.jpg")).get(), new MetadataMatcher(
                new Metadata(42L, null,
                             ExifData.empty().withFnumber(1d).withFocalLength(2)
                                     .withFocalLengthFullFrameEquivalent(3).withIso(4)
                                     .withTakenAt(Instant.ofEpochSecond(1446905814, 284000000)))));
        assertFalse(sut.metadataOf(photoWithPath("/some/other/photo.jpg")).isPresent());
    }

    private static Photo somePhoto() {
        return Photo.locatedAt(new File("").toPath());
    }

    private static Photo photoWithPath(String path) {
        return Photo.locatedAt(new File(path).toPath());
    }

    private FileDataStore newFileDataStore() {
        return new FileDataStore(temporaryFolder.getRoot().toPath());
    }
}