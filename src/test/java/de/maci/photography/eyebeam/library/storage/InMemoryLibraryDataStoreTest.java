package de.maci.photography.eyebeam.library.storage;

import de.maci.photography.eyebeam.library.model.PhotoLocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.util.NoSuchElementException;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 11.10.15
 */
public class InMemoryLibraryDataStoreTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void containsNoData_IfNewInstance() throws Exception {
        InMemoryLibraryIndexDataStore sut = InMemoryLibraryIndexDataStore.empty();

        assertThat(sut.photos().collect(toSet()), emptyIterable());
        assertThat(sut.size(), equalTo(0L));
    }

    @Test
    public void aPhotoCanBeAdded_IfTheDataStoreIsEmpty() throws Exception {
        InMemoryLibraryIndexDataStore sut = InMemoryLibraryIndexDataStore.empty();
        PhotoLocation photoLocation = somePhoto();

        assertTrue(sut.add(photoLocation));
        assertTrue(sut.contains(photoLocation));
        assertThat(sut.photos().collect(toSet()), equalTo(singleton(photoLocation)));
    }

    @Test
    public void aPhotoIsNotAdded_IfAlreadyContainedInTheDataStore() throws Exception {
        InMemoryLibraryIndexDataStore sut = InMemoryLibraryIndexDataStore.empty();
        PhotoLocation photoLocation = somePhoto();

        sut.add(photoLocation);
        assertFalse(sut.add(photoLocation));
    }

    @Test
    public void metadataCannotBeSet_IfTheCorrespondingPhotoIsNotContainedInTheDataStore() throws Exception {
        InMemoryLibraryIndexDataStore sut = InMemoryLibraryIndexDataStore.empty();

        String path = "/some/path.jpg";

        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage("Data store does not contain '" + path + "'.");

        sut.storeMetadata(photoWithPath(path), Metadata.empty());
    }

    @Test
    public void metadataCanBeSet_IfTheCorrespondingPhotoIsContainedInTheDataStore() throws Exception {
        InMemoryLibraryIndexDataStore sut = InMemoryLibraryIndexDataStore.empty();
        PhotoLocation photoLocation = somePhoto();
        sut.add(photoLocation);
        Metadata metadata = Metadata.empty();
        sut.storeMetadata(photoLocation, metadata);

        assertThat(sut.metadataOf(photoLocation).get(), equalTo(metadata));
    }

    @Test
    public void metadataCannotBeRead_IfTheCorrespondingPhotoIsNotContainedInTheDataStore() throws Exception {
        InMemoryLibraryIndexDataStore sut = InMemoryLibraryIndexDataStore.empty();

        String path = "/some/photo.jpg";

        expectedException.expect(NoSuchElementException.class);
        expectedException.expectMessage("Data store does not contain '" + path + "'.");

        sut.metadataOf(photoWithPath(path));
    }

    @Test
    public void metadataExistsEvaluatesToFalse_IfMetadataIsNotPresent() throws Exception {
        PhotoLocation photoLocation = somePhoto();

        InMemoryLibraryIndexDataStore sut = InMemoryLibraryIndexDataStore.empty();
        sut.add(photoLocation);

        assertFalse(sut.metadataExists(photoLocation));
    }

    @Test
    public void metadataExistsEvaluatesToTrue_IfMetadataIsPresent() throws Exception {
        PhotoLocation photoLocation = somePhoto();
        Metadata metadata = Metadata.empty();

        InMemoryLibraryIndexDataStore sut = InMemoryLibraryIndexDataStore.empty();
        sut.add(photoLocation);
        sut.storeMetadata(photoLocation, metadata);

        assertTrue(sut.metadataExists(photoLocation));
    }

    @Test
    public void emptyDataStoreIsEmpty_IfCleared() throws Exception {
        InMemoryLibraryIndexDataStore sut = InMemoryLibraryIndexDataStore.empty();
        sut.clear();

        assertThat(sut.size(), equalTo(0L));
    }

    @Test
    public void nonEmptyDataStoreIsEmpty_IfCleared() throws Exception {
        InMemoryLibraryIndexDataStore sut = InMemoryLibraryIndexDataStore.empty();
        sut.add(photoWithPath("/some/photo.jpg"));
        sut.add(photoWithPath("/some/other/photo.jpg"));
        sut.clear();

        assertThat(sut.size(), equalTo(0L));
    }

    private static PhotoLocation somePhoto() {
        return PhotoLocation.locatedAt(new File("").toPath());
    }

    private static PhotoLocation photoWithPath(String path) {
        return PhotoLocation.locatedAt(new File(path).toPath());
    }
}