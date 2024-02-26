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

import de.maci.photography.eyebeam.library.metadata.MetadataReader
import de.maci.photography.eyebeam.library.metadata.model.Metadata
import de.maci.photography.eyebeam.library.model.PhotoLocation
import de.maci.photography.eyebeam.library.storage.LibraryIndexDataStore

class Library(
    private val metadataReader: MetadataReader,
    private val indexDataStore: LibraryIndexDataStore,
    private val configuration: LibraryConfiguration
) {

    /**
     * Returns the sequence of all photos contained in this library.
     * @return The sequence of all photos.
     */
    fun photos(): Sequence<PhotoLocation> = indexDataStore.photos()

    /**
     * Calculates and returns the total count of photos contained in this library.
     * @return The total count of photos contained in this library.
     */
    fun size(): Long = indexDataStore.size()

    fun metadataOf(photoLocation: PhotoLocation): Metadata? =
        indexDataStore.metadataOf(photoLocation)

    fun reindex() {

        val indexRebuildingConfiguration = LibraryIndexer.IndexRebuildingConfiguration(
            configuration.rootFolder,
            configuration.fileFilter
        )

        LibraryIndexer(metadataReader).rebuildIndex(
            indexDataStore,
            indexRebuildingConfiguration
        )
    }
}
