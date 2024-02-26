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