package de.maci.photography.eyebeam.library

import de.maci.photography.eyebeam.library.metadata.Metadata
import de.maci.photography.eyebeam.library.storage.LibraryDataStore
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import java.util.stream.Stream

class Library(val dataStore: LibraryDataStore, private val configuration: LibraryConfiguration) {

    private val reindexingLock = ReentrantLock()

    fun photos(): Stream<Photo> {
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
        return dataStore.metadataExists(photo)
    }

    fun metadataOf(photo: Photo): Optional<Metadata> {
        return dataStore.metadataOf(photo)
    }
}
