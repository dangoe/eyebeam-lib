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
