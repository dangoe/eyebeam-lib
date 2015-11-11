package de.maci.photography.eyebeam.library.metadata;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 09.10.15
 */
public class ExifDataReaderTest {

    private final ExifDataReader sut = new ExifDataReader();

    @Test
    public void testReadExifData_ReturnsEmpty_IfFileDoesNotExist() throws Exception {
        Optional<ExifData> exifData =
                sut.readFrom(Paths.get(getClass().getResource(".").toString(), UUID.randomUUID().toString()));

        assertFalse(exifData.isPresent());
    }

    @Test
    public void testReadExifData_ReturnsEmpty_IfFileIsNotASupportedImage() throws Exception {
        Optional<ExifData> exifData = sut.readFrom(Paths.get(getClass().getResource("test.txt").toURI()));

        assertFalse(exifData.isPresent());
    }

    @Test
    public void testReadExifData_ReturnsValidExifData_IfFileIsASupportedImage() throws Exception {
        ExifData exifData = sut.readFrom(Paths.get(getClass().getResource("sample.jpg").toURI())).get();

        assertThat(exifData.fnumber().get(), equalTo(8d));
        assertThat(exifData.focalLength().get(), equalTo(25));
        assertThat(exifData.focalLengthFullFrameEquivalent().get(), equalTo(37));
        assertThat(exifData.iso().get(), equalTo(100));
        assertThat(exifData.takenAt().get(), equalTo(Instant.parse("2015-07-07T21:12:37Z")));
    }
}
