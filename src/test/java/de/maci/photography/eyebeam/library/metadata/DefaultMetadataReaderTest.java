package de.maci.photography.eyebeam.library.metadata;

import org.junit.Test;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 19.10.15
 */
public class DefaultMetadataReaderTest {

    private final MetadataReader sut = new DefaultMetadataReader();

    @Test
    public void testReadExifData_ReturnsEmpty_IfFileDoesNotExist() throws Exception {
        Optional<Metadata> metadata =
                sut.readFrom(Paths.get(getClass().getResource(".").toString(), UUID.randomUUID().toString()));

        assertFalse(metadata.isPresent());
    }

    @Test
    public void testReadExifData_ReturnsEmpty_IfFileIsNotASupportedImage() throws Exception {
        Optional<Metadata> metadata = sut.readFrom(Paths.get(getClass().getResource("test.txt").toURI()));

        assertFalse(metadata.isPresent());
    }

    @Test
    public void testReadExifData_ReturnsValidMetadata_IfFileIsASupportedImage() throws Exception {
        Metadata metadata = sut.readFrom(Paths.get(getClass().getResource("sample.jpg").toURI())).get();
        ExifData exifData = metadata.exifData().get();

        assertThat(metadata.fileSize().get(), equalTo(157933L));
        assertThat(exifData.fnumber().get(), equalTo(8d));
        assertThat(exifData.focalLength().get(), equalTo(25));
        assertThat(exifData.focalLengthFullFrameEquivalent().get(), equalTo(37));
        assertThat(exifData.iso().get(), equalTo(100));
        assertThat(exifData.takenAt().get(), equalTo(Instant.parse("2015-07-07T21:12:37Z")));
    }
}
