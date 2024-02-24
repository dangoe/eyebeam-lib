package de.maci.photography.eyebeam.library.storage.persistent.file.model

import de.maci.photography.eyebeam.library.metadata.model.ExifData
import java.time.Instant
import de.maci.photography.eyebeam.library.metadata.model.Metadata as ModelMetadata

data class Metadata(
    val fileSize: ULong,
    val fNumber: Double?,
    val focalLength: UInt?,
    val focalLengthFullFrameEquivalent: UInt?,
    val iso: UInt?,
    val takenAt: String?
) {

    constructor(metadata: ModelMetadata) :
            this(
                metadata.fileSize,
                metadata.exifData?.fNumber,
                metadata.exifData?.focalLength,
                metadata.exifData?.focalLengthFullFrameEquivalent,
                metadata.exifData?.iso,
                metadata.exifData?.takenAt.toString()
            )

    fun toModel(): ModelMetadata =
        ModelMetadata(
            fileSize,
            null,
            ExifData(
                fNumber,
                focalLength,
                focalLengthFullFrameEquivalent,
                iso,
                Instant.parse(takenAt)
            ),
            Instant.now()
        )
}
