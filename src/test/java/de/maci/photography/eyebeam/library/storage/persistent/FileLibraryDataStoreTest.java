package de.maci.photography.eyebeam.library.storage.persistent;

import de.maci.photography.eyebeam.library.model.PhotoLocation;
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
public class FileLibraryDataStoreTest {

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
        PhotoLocation photoLocation = somePhoto();

        assertTrue(sut.store(photoLocation));
        assertTrue(sut.contains(photoLocation));
        assertThat(sut.photos().collect(toSet()), equalTo(singleton(photoLocation)));
    }

    @Test
    public void aPhotoIsNotAdded_IfAlreadyContainedInTheDataStore() throws Exception {
        FileDataStore sut = newFileDataStore();
        PhotoLocation photoLocation = somePhoto();
        sut.store(photoLocation);

        assertFalse(sut.store(photoLocation));
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
        PhotoLocation photoLocation = somePhoto();
        sut.store(photoLocation);
        Metadata metadata = Metadata.empty();
        sut.updateMetadata(photoLocation, metadata);

        assertThat(sut.metadataOf(photoLocation).get(), equalTo(metadata));
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
        PhotoLocation photoLocation = somePhoto();

        FileDataStore sut = newFileDataStore();
        sut.store(photoLocation);

        assertFalse(sut.metadataExists(photoLocation));
    }

    @Test
    public void metadataExistsEvaluatesToTrue_IfMetadataIsPresent() throws Exception {
        PhotoLocation photoLocation = somePhoto();
        Metadata metadata = Metadata.empty();

        FileDataStore sut = newFileDataStore();
        sut.store(photoLocation);
        sut.updateMetadata(photoLocation, metadata);

        assertTrue(sut.metadataExists(photoLocation));
    }

    @Test
    public void aDataStoreCanBeFlushedAndRestored() throws Exception {
        FileDataStore sut = newFileDataStore();
        PhotoLocation photoLocationWithMetadata = photoWithPath("/some/photo.jpg");
        sut.store(photoLocationWithMetadata);
        Instant now = Instant.now();
        Metadata metadata = new Metadata(42L,
                                         null,
                                         ExifData.empty().withFnumber(1d).withFocalLength(2)
                                                 .withFocalLengthFullFrameEquivalent(3).withIso(4).withTakenAt(now));
        sut.updateMetadata(photoLocationWithMetadata, metadata);
        PhotoLocation photoLocationWithoutMetadata = photoWithPath("/some/other/photo.jpg");
        sut.store(photoLocationWithoutMetadata);
        sut.flush();
        sut.clear();
        sut.restore();

        assertThat(sut.photos().collect(toSet()), containsInAnyOrder(photoLocationWithMetadata,
            photoLocationWithoutMetadata));
        assertThat(sut.metadataOf(photoLocationWithMetadata).get(), new MetadataMatcher(metadata));
        assertFalse(sut.metadataExists(photoLocationWithoutMetadata));
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

    private static PhotoLocation somePhoto() {
        return PhotoLocation.locatedAt(new File("").toPath());
    }

    private static PhotoLocation photoWithPath(String path) {
        return PhotoLocation.locatedAt(new File(path).toPath());
    }

    private FileDataStore newFileDataStore() {
        return new FileDataStore(temporaryFolder.getRoot().toPath());
    }
}