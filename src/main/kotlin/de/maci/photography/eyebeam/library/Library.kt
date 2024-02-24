package de.maci.photography.eyebeam.library

import de.maci.photography.eyebeam.library.metadata.model.Metadata
import de.maci.photography.eyebeam.library.storage.LibraryDataStore
import java.util.concurrent.locks.ReentrantLock

class Library(val dataStore: LibraryDataStore) {

    private val reindexingLock = ReentrantLock()

    fun photos(): Sequence<Photo> {
        return dataStore.photos()
    }

    fun countPhotos(): Long {
        return dataStore.size()
    }

    internal fun lockForReindexing(): Boolean {
        return reindexingLock.tryLock()
    }

    internal fun unlock() {
        reindexingLock.unlock()
    }

    fun isReindexing(): Boolean {
        return reindexingLock.isLocked
    }

    fun clear() {
        dataStore.clear()
    }

    fun metadataExists(photo: Photo): Boolean {
        return dataStore.metadataOf(photo) != null
    }

    fun metadataOf(photo: Photo): Metadata? {
        return dataStore.metadataOf(photo)
    }
}
