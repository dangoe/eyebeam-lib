/**
 * Copyright (c) 2015 Daniel Götten
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to
 * do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.maci.photography.eyebeam.library.metadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 09.10.15
 */
public final class ExifDataReader {

    private static final Logger logger = LoggerFactory.getLogger(ExifDataReader.class);

    /**
     * Reads the {@link Metadata} from the given file without throwing an
     * exception.
     *
     * @param path The file's path.
     * @return The read {@link Metadata}, if present and readable.
     */
    public Optional<ExifData> readFrom(@Nonnull Path path) {
        File file = path.toFile();
        try {
            return Optional.of(ExifData.fromMetadata(ImageMetadataReader.readMetadata(file)));
        } catch (ImageProcessingException | IOException e) {
            logger.error(String.format("Failed to read metadata of '%s'.", file.getAbsolutePath()), e);
        }
        return Optional.empty();
    }
}
