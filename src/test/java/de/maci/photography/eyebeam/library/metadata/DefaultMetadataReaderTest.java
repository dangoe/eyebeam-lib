package de.maci.photography.eyebeam.library.metadata;

import de.maci.photography.eyebeam.library.metadata.model.ExifData;
import de.maci.photography.eyebeam.library.metadata.model.Metadata;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 19.10.15
 */
public class DefaultMetadataReaderTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final MetadataReader sut = new DefaultMetadataReader();

    @Test
    public void testReadExifData_ReturnsEmpty_IfFileDoesNotExist() throws Exception {
        expectedException.expect(MetadataReadingException.class);

        sut.read(Paths.get(getClass().getResource(".").toString(), UUID.randomUUID().toString()));
    }

    @Test
    public void testReadExifData_ReturnsEmpty_IfFileIsNotASupportedImage() throws Exception {
        expectedException.expect(MetadataReadingException.class);

        sut.read(Paths.get(getClass().getResource("test.txt").toURI()));
    }

    @Test
    public void testReadExifData_ReturnsValidMetadata_IfFileIsASupportedImage() throws Exception {
        Metadata metadata = sut.read(Paths.get(getClass().getResource("sample.jpg").toURI()));
        ExifData exifData = metadata.exifData().get();

        assertThat(metadata.fileSize().get(), equalTo(157933L));
        assertThat(exifData.fnumber().get(), equalTo(8d));
        assertThat(exifData.focalLength().get(), equalTo(25));
        assertThat(exifData.focalLengthFullFrameEquivalent().get(), equalTo(37));
        assertThat(exifData.iso().get(), equalTo(100));
        assertThat(exifData.takenAt().get(), equalTo(Instant.parse("2015-07-07T21:12:37Z")));
    }
}
