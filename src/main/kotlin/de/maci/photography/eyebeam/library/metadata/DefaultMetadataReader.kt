package de.maci.photography.eyebeam.library.metadata

import arrow.core.Either
import de.maci.photography.eyebeam.library.metadata.model.Metadata
import java.nio.file.Path
import java.time.Instant

class DefaultMetadataReader : MetadataReader {

    private val exifDataReader = ExifDataReader()

    override fun readMetadata(path: Path): Either<String, Metadata> =
        exifDataReader.read(path).map { exifData ->
            Metadata(
                path.toFile().length().toULong(),
                null, // TODO To be implemented ...
                exifData,
                Instant.now()
            )
        }
}