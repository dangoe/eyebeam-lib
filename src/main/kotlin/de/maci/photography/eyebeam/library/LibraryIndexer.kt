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
package de.maci.photography.eyebeam.library

import arrow.core.flatMap
import de.maci.photography.eyebeam.library.indexing.FilesystemScanner
import de.maci.photography.eyebeam.library.metadata.MetadataReader
import de.maci.photography.eyebeam.library.model.PhotoLocation
import de.maci.photography.eyebeam.library.storage.LibraryIndexDataStore
import java.io.File
import java.nio.file.Path

internal class LibraryIndexer(private val metadataReader: MetadataReader) {

    data class IndexRebuildingConfiguration(
        val folder: Path,
        val fileFilter: (Path) -> Boolean
    )

    fun rebuildIndex(
        dataStore: LibraryIndexDataStore,
        configuration: IndexRebuildingConfiguration
    ) {
        dataStore.photos().forEach {
            if (it.path.toFile().exists()) {
                dataStore.remove(it)
            }
        }

        val scanner = createScanner(configuration.fileFilter)
        scanner.scan(configuration.folder) { path ->
            val photoLocation = PhotoLocation(path)
            dataStore.add(photoLocation)
            // TODO Error handling
            metadataReader.readMetadata(photoLocation.path)
                .flatMap { dataStore.storeMetadata(photoLocation, it) }
        }
    }

    private fun createScanner(fileFilter: (Path) -> Boolean): FilesystemScanner =
        FilesystemScanner(
            FilesystemScanner.Options.default().withFollowSymlinks(true), fileFilter
        )
}
