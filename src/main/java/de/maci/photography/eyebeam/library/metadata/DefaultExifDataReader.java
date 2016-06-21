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

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;

import static com.drew.metadata.exif.ExifDirectoryBase.TAG_35MM_FILM_EQUIV_FOCAL_LENGTH;
import static com.drew.metadata.exif.ExifDirectoryBase.TAG_DATETIME_ORIGINAL;
import static com.drew.metadata.exif.ExifDirectoryBase.TAG_FNUMBER;
import static com.drew.metadata.exif.ExifDirectoryBase.TAG_FOCAL_LENGTH;
import static com.drew.metadata.exif.ExifDirectoryBase.TAG_ISO_EQUIVALENT;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 09.10.15
 */
class DefaultExifDataReader {

    @FunctionalInterface
    private interface ThrowingMetadataException<T> {

        T perform() throws MetadataException;
    }

    private static final Logger logger = LoggerFactory.getLogger(DefaultExifDataReader.class);

    public ExifData readFrom(@Nonnull Path path) {
        File file = path.toFile();
        try {
            return fromMetadata(ImageMetadataReader.readMetadata(file));
        } catch (Exception e) {
            throw new MetadataReadingException(String.format("Failed to read metadata of '%s'.",
                                                             file.getAbsolutePath()), e);
        }
    }

    private static ExifData fromMetadata(@Nonnull Metadata metadata) {
        ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        return ExifData.empty()
                       .withFnumber(tryExecute(() -> directory.getDouble(TAG_FNUMBER)).orElse(null))
                       .withFocalLength(tryExecute(() -> directory.getInteger(TAG_FOCAL_LENGTH)).orElse(null))
                       .withFocalLengthFullFrameEquivalent(tryExecute(() -> directory
                               .getInteger(TAG_35MM_FILM_EQUIV_FOCAL_LENGTH)).orElse(null))
                       .withIso(tryExecute(() -> directory.getInteger(TAG_ISO_EQUIVALENT)).orElse(null))
                       .withTakenAt(tryExecute(() -> directory.getDate(TAG_DATETIME_ORIGINAL)).map(Date::toInstant)
                                                                                              .orElse(null));
    }

    private static <T> Optional<T> tryExecute(ThrowingMetadataException<T> action) {
        try {
            return Optional.ofNullable(action.perform());
        } catch (MetadataException e) {
            logger.debug("Failed to read EXIF value.", e);
            return Optional.empty();
        }
    }
}
