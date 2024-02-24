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