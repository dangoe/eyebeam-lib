package de.maci.photography.eyebeam.library.storage;

import de.maci.photography.eyebeam.library.Photo;
import de.maci.photography.eyebeam.library.metadata.Metadata;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.util.NoSuchElementException;

import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.junit.Assert.*;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 11.10.15
 */
public class InMemoryDataStoreTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void containsNoData_IfNewInstance() throws Exception {
        InMemoryDataStore sut = new InMemoryDataStore();

        assertThat(sut.photos(), emptyIterable());
        assertThat(sut.size(), equalTo(0L));
    }

    @Test
    public void aPhotoCanBeAdded_IfTheDataStoreIsEmpty() throws Exception {
        InMemoryDataStore sut = new InMemoryDataStore();
        Photo photo = somePhoto();

        assertTrue(sut.store(photo));
        assertTrue(sut.contains(photo));
        assertThat(sut.photos(), equalTo(singleton(photo)));
    }

    @Test
    public void aPhotoIsNotAdded_IfAlreadyContainedInTheDataStore() throws Exception {
        InMemoryDataStore sut = new InMemoryDataStore();
        Photo photo = somePhoto();

        sut.store(photo);
        assertFalse(sut.store(photo));
    }

    @Test
    public void metadataCannotBeSet_IfTheCorrespondingPhotoIsNotContainedInTheDataStore() throws Exception {
        InMemoryDataStore sut = new InMemoryDataStore();

        String path = "/some/path.jpg";

        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage("Data store does not contain '" + path + "'.");

        sut.replaceMetadata(photoWithPath(path), Metadata.empty());
    }

    @Test
    public void metadataCanBeSet_IfTheCorrespondingPhotoIsContainedInTheDataStore() throws Exception {
        InMemoryDataStore sut = new InMemoryDataStore();
        Photo photo = somePhoto();
        sut.store(photo);
        Metadata metadata = Metadata.empty();
        sut.replaceMetadata(photo, metadata);

        assertThat(sut.metadataOf(photo).get(), equalTo(metadata));
    }

    @Test
    public void metadataCannotBeRead_IfTheCorrespondingPhotoIsNotContainedInTheDataStore() throws Exception {
        InMemoryDataStore sut = new InMemoryDataStore();

        String path = "/some/photo.jpg";

        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage("Data store does not contain '" + path + "'.");

        sut.metadataOf(photoWithPath(path));
    }

    @Test
    public void emptyDataStoreIsEmpty_IfCleared() throws Exception {
        InMemoryDataStore sut = new InMemoryDataStore();
        sut.clear();

        assertThat(sut.size(), equalTo(0L));
    }

    @Test
    public void nonEmptyDataStoreIsEmpty_IfCleared() throws Exception {
        InMemoryDataStore sut = new InMemoryDataStore();
        sut.store(photoWithPath("/some/photo.jpg"));
        sut.store(photoWithPath("/some/other/photo.jpg"));
        sut.clear();

        assertThat(sut.size(), equalTo(0L));
    }

    private static Photo somePhoto() {
        return new Photo(new File("").toPath());
    }

    private static Photo photoWithPath(String path) {
        return new Photo(new File(path).toPath());
    }
}