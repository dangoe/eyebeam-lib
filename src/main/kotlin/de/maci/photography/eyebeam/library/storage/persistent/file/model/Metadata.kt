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
