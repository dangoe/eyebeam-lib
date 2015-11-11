package de.maci.photography.eyebeam.library;

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
        Photo photo = new Photo(Paths.get(getClass().getResource("sample.jpg").toURI()));

        assertThat(photo.filename(), equalTo("sample.jpg"));
    }
}
