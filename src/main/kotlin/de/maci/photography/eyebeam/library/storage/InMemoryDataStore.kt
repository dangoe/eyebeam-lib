package de.maci.photography.eyebeam.library.storage

import arrow.core.Either
import arrow.core.right
import de.maci.photography.eyebeam.library.Photo
import de.maci.photography.eyebeam.library.metadata.Metadata
import de.maci.photography.eyebeam.library.storage.LibraryDataStore.Errors.PhotoAlreadyExists
import de.maci.photography.eyebeam.library.storage.LibraryDataStore.Errors.PhotoDoesNotExist
import de.maci.photography.eyebeam.library.storage.LibraryDataStore.Errors.RemovePhotoError
import de.maci.photography.eyebeam.library.storage.LibraryDataStore.Errors.StoreMetadataError
import de.maci.photography.eyebeam.library.storage.LibraryDataStore.Errors.StorePhotoError
import java.util.*

class InMemoryDataStore private constructor() : LibraryDataStore {

    companion object {

        fun empty(): InMemoryDataStore {
            return InMemoryDataStore()
        }
    }

    private val photos: MutableMap<Photo, Metadata?> = TreeMap { obj: Photo, other: Photo -> obj.compareTo(other) }

    override fun metadataOf(photo: Photo): Metadata? = photos[photo]

    override fun photos(): Sequence<Photo> = photos.keys.asSequence()

    override fun contains(photo: Photo): Boolean = photos.containsKey(photo)

    override fun size(): Long = photos.size.toLong()

    override fun remove(photo: Photo): Either<RemovePhotoError, Unit> {
        if (!contains(photo)) {
            return Either.Left(PhotoDoesNotExist(photo.path))
        }
        photos.remove(photo)
        return Unit.right()
    }

    override fun store(photo: Photo): Either<StorePhotoError, Unit> {
        if (contains(photo)) {
            return Either.Left(PhotoAlreadyExists(photo.path))
        }
        photos[photo] = null
        return Unit.right()
    }

    override fun updateMetadata(photo: Photo, metadata: Metadata): Either<StoreMetadataError, Unit> {
        if (!contains(photo)) {
            return Either.Left(PhotoDoesNotExist(photo.path))
        }
        photos.replace(photo, metadata)
        return Unit.right()
    }

    override fun clear() {
        photos.clear()
    }
}
