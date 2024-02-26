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
package de.maci.photography.eyebeam.library.storage

import arrow.core.Either
import arrow.core.right
import de.maci.photography.eyebeam.library.model.PhotoLocation
import de.maci.photography.eyebeam.library.metadata.model.Metadata
import de.maci.photography.eyebeam.library.storage.LibraryIndexDataStore.Errors.PhotoAlreadyExists
import de.maci.photography.eyebeam.library.storage.LibraryIndexDataStore.Errors.PhotoDoesNotExist
import de.maci.photography.eyebeam.library.storage.LibraryIndexDataStore.Errors.RemovePhotoError
import de.maci.photography.eyebeam.library.storage.LibraryIndexDataStore.Errors.StoreMetadataError
import de.maci.photography.eyebeam.library.storage.LibraryIndexDataStore.Errors.StorePhotoError
import java.util.*

class InMemoryLibraryIndexDataStore private constructor() : LibraryIndexDataStore {

    companion object {

        fun empty(): InMemoryLibraryIndexDataStore {
            return InMemoryLibraryIndexDataStore()
        }
    }

    private val photos: MutableMap<PhotoLocation, Metadata?> =
        TreeMap { obj: PhotoLocation, other: PhotoLocation -> obj.compareTo(other) }

    override fun metadataOf(photoLocation: PhotoLocation): Metadata? = photos[photoLocation]

    override fun photos(): Sequence<PhotoLocation> = photos.keys.toSet().asSequence()

    override fun contains(photoLocation: PhotoLocation): Boolean = photos.containsKey(photoLocation)

    override fun size(): Long = photos.size.toLong()

    override fun remove(photoLocation: PhotoLocation): Either<RemovePhotoError, Unit> {
        if (!contains(photoLocation)) {
            return Either.Left(PhotoDoesNotExist(photoLocation.path))
        }
        photos.remove(photoLocation)
        return Unit.right()
    }

    override fun add(photoLocation: PhotoLocation): Either<StorePhotoError, Unit> {
        if (contains(photoLocation)) {
            return Either.Left(PhotoAlreadyExists(photoLocation.path))
        }
        photos[photoLocation] = null
        return Unit.right()
    }

    override fun storeMetadata(
        photoLocation: PhotoLocation, metadata: Metadata
    ): Either<StoreMetadataError, Unit> {
        if (!contains(photoLocation)) {
            return Either.Left(PhotoDoesNotExist(photoLocation.path))
        }
        photos.replace(photoLocation, metadata)
        return Unit.right()
    }

    override fun clear() {
        photos.clear()
    }
}
