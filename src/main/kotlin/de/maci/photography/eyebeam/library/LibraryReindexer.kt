/**
 * Copyright 2016 Daniel Götten
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

import de.maci.photography.eyebeam.library.indexing.FilesystemScanner
import de.maci.photography.eyebeam.library.storage.LibraryDataStore
import java.io.IOException
import java.nio.file.Path

class LibraryReindexer(
    private val library: Library, private val libraryConfiguration: LibraryConfiguration
) {

    companion object {

        private fun createScanner(fileFilter: (Path) -> Boolean): FilesystemScanner =
            FilesystemScanner(
                FilesystemScanner.Options.default().withFollowSymlinks(true), fileFilter
            )

        private fun refreshIfMetadataOrExifIsMissing(dataStore: LibraryDataStore): (Photo) -> Boolean =
            { photo ->
                dataStore.metadataOf(photo)?.exifData == null
            }
    }

    private val reindexingNecessaryDecision: (Path) -> Boolean =
        { path -> refreshIfMetadataOrExifIsMissing(library.dataStore).invoke((Photo(path))) }

    private fun rootFolder(): Path = this.libraryConfiguration.rootFolder()

    private fun fileFilter(): (Path) -> Boolean = this.libraryConfiguration.fileFilter()

    fun reindexLibrary() {
        if (library.lockForReindexing()) {
            try {
                checkForNewPhotos()
                updateMetadata()
            } catch (e: IOException) {
                throw IllegalStateException(e)
            } finally {
                library.unlock()
            }
        }
    }

    @Throws(IOException::class)
    private fun checkForNewPhotos() {
        val rootFolder = rootFolder()

        createScanner(fileFilter()).scan(
            rootFolder
        ) { path -> library.dataStore.store(Photo(rootFolder.relativize(path))) }
    }

    private fun updateMetadata() {
        val rootFolder = rootFolder()
        val metadataReader = libraryConfiguration.metadataReader()

        photosWithMetadataToBeRefreshed().forEach { photo ->
            metadataReader.invoke().readMetadata(rootFolder.resolve(photo.path))
                .onRight { library.dataStore.updateMetadata(photo, it) }
        }
    }

    private fun photosWithMetadataToBeRefreshed(): Sequence<Photo> {
        return library.photos().filter { photo -> reindexingNecessaryDecision.invoke(photo.path) }
    }
}
