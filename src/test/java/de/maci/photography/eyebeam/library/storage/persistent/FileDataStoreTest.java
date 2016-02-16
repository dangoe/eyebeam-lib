package de.maci.photography.eyebeam.library.storage.persistent;

import com.google.common.base.Charsets;
import de.maci.photography.eyebeam.library.Photo;
import de.maci.photography.eyebeam.library.metadata.ExifData;
import de.maci.photography.eyebeam.library.metadata.Metadata;
import de.maci.photography.eyebeam.library.testhelper.matcher.MetadataMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 28.10.15
 */
public class FileDataStoreTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static Photo somePhoto() {
        return Photo.locatedAt(new File("").toPath());
    }

    private static Photo photoWithPath(String path) {
        return Photo.locatedAt(new File(path).toPath());
    }

    @Test
    public void containsNoData_IfNewInstance() throws Exception {
        FileDataStore sut = someFileDataStore();

        assertThat(sut.photos().collect(toSet()), emptyIterable());
        assertThat(sut.size(), equalTo(0L));
    }

    @Test
    public void aPhotoCanBeAdded_IfTheDataStoreIsEmpty() throws Exception {
        FileDataStore sut = someFileDataStore();
        Photo photo = somePhoto();

        assertTrue(sut.store(photo));
        assertTrue(sut.contains(photo));
        assertThat(sut.photos().collect(toSet()), equalTo(singleton(photo)));
    }

    @Test
    public void aPhotoIsNotAdded_IfAlreadyContainedInTheDataStore() throws Exception {
        FileDataStore sut = someFileDataStore();
        Photo photo = somePhoto();
        sut.store(photo);

        assertFalse(sut.store(photo));
    }

    @Test
    public void metadataCannotBeSet_IfTheCorrespondingPhotoIsNotContainedInTheDataStore() throws Exception {
        FileDataStore sut = someFileDataStore();

        String path = "/some/path.jpg";

        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage("Data store does not contain '" + path + "'.");

        sut.replaceMetadata(photoWithPath(path), Metadata.empty());
    }

    @Test
    public void metadataCanBeSet_IfTheCorrespondingPhotoIsContainedInTheDataStore() throws Exception {
        FileDataStore sut = someFileDataStore();
        Photo photo = somePhoto();
        sut.store(photo);
        Metadata metadata = Metadata.empty();
        sut.replaceMetadata(photo, metadata);

        assertThat(sut.metadataOf(photo).get(), equalTo(metadata));
    }

    @Test
    public void metadataCannotBeRead_IfTheCorrespondingPhotoIsNotContainedInTheDataStore() throws Exception {
        FileDataStore sut = someFileDataStore();

        String path = "/some/photo.jpg";

        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage("Data store does not contain '" + path + "'.");

        sut.metadataOf(photoWithPath(path));
    }

    @Test
    public void emptyDataStoreIsEmpty_IfCleared() throws Exception {
        FileDataStore sut = someFileDataStore();
        sut.clear();
        assertThat(sut.size(), equalTo(0L));
    }

    @Test
    public void nonEmptyDataStoreIsEmpty_IfCleared() throws Exception {
        FileDataStore sut = someFileDataStore();
        sut.store(photoWithPath("/some/photo.jpg"));
        sut.store(photoWithPath("/some/other/photo.jpg"));
        sut.clear();

        assertThat(sut.size(), equalTo(0L));
    }

    @Test
    public void metadataExistsEvaluatesToFalse_IfMetadataIsNotPresent() throws Exception {
        Photo photo = somePhoto();

        FileDataStore sut = someFileDataStore();
        sut.store(photo);

        assertFalse(sut.metadataExists(photo));
    }

    @Test
    public void metadataExistsEvaluatesToTrue_IfMetadataIsPresent() throws Exception {
        Photo photo = somePhoto();
        Metadata metadata = Metadata.empty();

        FileDataStore sut = someFileDataStore();
        sut.store(photo);
        sut.replaceMetadata(photo, metadata);

        assertTrue(sut.metadataExists(photo));
    }

    @Test
    public void aDataStoreCanBeFlushedAndRestored() throws Exception {
        File dbFile = temporaryFolder.newFile("fileStore.dat");
        FileOutputStream outputStream = new FileOutputStream(dbFile);
        FileInputStream inputStream = new FileInputStream(dbFile);
        FileDataStore sut = new FileDataStore(() -> inputStream, () -> outputStream);
        Photo photoWithMetadata = photoWithPath("/some/photo.jpg");
        sut.store(photoWithMetadata);
        Instant now = Instant.now();
        Metadata metadata = new Metadata(42L,
                                         null,
                                         ExifData.empty().withFnumber(1d).withFocalLength(2)
                                                 .withFocalLengthFullFrameEquivalent(3).withIso(4).withTakenAt(now));
        sut.replaceMetadata(photoWithMetadata, metadata);
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
        InputStream inputStream = getClass().getResourceAsStream("fileStore.dat");

        FileDataStore sut = new FileDataStore(() -> inputStream, () -> mock(OutputStream.class));
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

    @Test
    public void emptyMapIsWrittenToOutputStream_IfEmpty() throws Exception {
        FileOutputStream outputStream = mock(FileOutputStream.class);
        FileDataStore sut = new FileDataStore(() -> mock(InputStream.class), () -> outputStream);
        sut.flush();

        verify(outputStream).write("{}".getBytes(Charsets.UTF_8));
        verify(outputStream).flush();
        verify(outputStream).close();
    }

    private static FileDataStore someFileDataStore() {
        return new FileDataStore(() -> mock(InputStream.class), () -> mock(OutputStream.class));
    }
}