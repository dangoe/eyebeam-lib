/**
 * Copyright 2015 Daniel Götten
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
import de.maci.photography.eyebeam.library.Photo
import de.maci.photography.eyebeam.library.metadata.Metadata
import java.nio.file.Path

interface LibraryDataStore {

    companion object Errors {

        interface StorePhotoError
        interface RemovePhotoError
        interface StoreMetadataError

        data class PhotoAlreadyExists(val path: Path) : StorePhotoError
        data class PhotoDoesNotExist(val path: Path) : RemovePhotoError, StoreMetadataError
    }

    /**
     * Returns the sequence of all photos contained in this data store.
     * @return The sequence of all photos.
     */
    fun photos(): Sequence<Photo>

    /**
     * Checks if the given photo is contained in this data store.
     * @return `true`, if the photo is contained. `false` otherwise.
     */
    fun contains(photo: Photo): Boolean

    /**
     * Checks if metadata exists for the given photo.
     * @return `true`, if metadata exists. `false` otherwise.
     */
    fun metadataExists(photo: Photo): Boolean = metadataOf(photo) != null

    /**
     * Return the metadata assigned to the given photo.
     * @return The assigned metadata, if stored.
     */
    fun metadataOf(photo: Photo): Metadata?

    /**
     * Calculates the size of this data store and returns the corresponding value.
     * @return The total number of photos contained in this data store.
     */
    fun size(): Long

    /**
     * Stores the given photo, if it is not already contained in the store.
     * @return An error or `Unit`, if the photo has been stored or not.
     */
    fun store(photo: Photo): Either<StorePhotoError, Unit>

    /**
     * Removes the given photo from the store. if it exists in the store.
     * @return An error or `Unit`, if the photo exists in the store or not.
     */
    fun remove(photo: Photo): Either<RemovePhotoError, Unit>

    /**
     * Updates the metadata assigned to the given photo, if the photo exists in the store.
     * @return An error or `Unit`, if the photo exists in the store or not.
     */
    fun updateMetadata(photo: Photo, metadata: Metadata): Either<StoreMetadataError, Unit>

    /**
     * Removes all photos and metadata from this store.
     */
    fun clear()
}
