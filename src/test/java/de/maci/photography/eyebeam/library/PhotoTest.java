package de.maci.photography.eyebeam.library;

import de.maci.photography.eyebeam.library.model.PhotoLocation;
import org.junit.Test;

import java.nio.file.Paths;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 07.11.15
 */
public class PhotoTest {

    @Test
    public void filenameIsExtractedCorrectly() throws Exception {
        PhotoLocation photoLocation = PhotoLocation.locatedAt(Paths.get(getClass().getResource("sample.jpg").toURI()));

        assertThat(photoLocation.filename(), equalTo("sample.jpg"));
    }
}
