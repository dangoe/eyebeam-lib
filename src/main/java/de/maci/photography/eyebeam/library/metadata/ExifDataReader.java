/**
 * Copyright 2015 Daniel Götten
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
