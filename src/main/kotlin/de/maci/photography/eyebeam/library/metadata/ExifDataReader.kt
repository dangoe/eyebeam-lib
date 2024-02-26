/**
 * Copyright 2024 Daniel Götten
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
package de.maci.photography.eyebeam.library.metadata

import arrow.core.Either
import com.drew.imaging.ImageMetadataReader
import com.drew.imaging.ImageProcessingException
import com.drew.metadata.Metadata
import com.drew.metadata.MetadataException
import com.drew.metadata.exif.ExifDirectoryBase
import com.drew.metadata.exif.ExifSubIFDDirectory
import de.maci.photography.eyebeam.library.metadata.model.ExifData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path

internal class ExifDataReader {

    private val logger: Logger = LoggerFactory.getLogger(ExifDataReader::class.java)

    fun read(path: Path): Either<String, ExifData?> {
        return try {
            Either.Right(createExifData(ImageMetadataReader.readMetadata(path.toFile())))
        } catch (e: ImageProcessingException) {
            Either.Left("Failed to read EXIF data: ${e.message}")
        }
    }

    private fun createExifData(metadata: Metadata): ExifData {
        val directory = metadata.getFirstDirectoryOfType(
            ExifSubIFDDirectory::class.java
        )

        return ExifData(
            tryGetOrNull {
                directory?.getDouble(
                    ExifDirectoryBase.TAG_FNUMBER
                )
            },
            tryGetOrNull {
                directory?.getInt(
                    ExifDirectoryBase.TAG_FOCAL_LENGTH
                )?.toUInt()
            },
            tryGetOrNull {
                directory?.getInt(
                    ExifDirectoryBase.TAG_35MM_FILM_EQUIV_FOCAL_LENGTH
                )?.toUInt()
            },
            tryGetOrNull {
                directory?.getInt(
                    ExifDirectoryBase.TAG_ISO_EQUIVALENT
                )?.toUInt()
            },
            tryGetOrNull {
                directory?.getDate(
                    ExifDirectoryBase.TAG_DATETIME_ORIGINAL
                )?.toInstant()
            }
        )
    }

    private fun <T> tryGetOrNull(producer: () -> T?): T? {
        try {
            return producer()
        } catch (e: MetadataException) {
            logger.debug("Failed to read EXIF value.", e)
            return null
        }
    }
}