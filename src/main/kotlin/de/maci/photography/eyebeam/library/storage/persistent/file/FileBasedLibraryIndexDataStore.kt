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
package de.maci.photography.eyebeam.library.storage.persistent.file

import arrow.core.Either
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.maci.photography.eyebeam.library.model.PhotoLocation
import de.maci.photography.eyebeam.library.metadata.model.Metadata
import de.maci.photography.eyebeam.library.storage.InMemoryLibraryIndexDataStore
import de.maci.photography.eyebeam.library.storage.LibraryIndexDataStore
import de.maci.photography.eyebeam.library.storage.persistent.Persistable
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.lang.reflect.Type
import java.nio.file.Path
import javax.xml.bind.JAXBException
import de.maci.photography.eyebeam.library.storage.persistent.file.model.Metadata as FileModelMetadata
import de.maci.photography.eyebeam.library.storage.persistent.file.model.Photo as FileModelPhoto

class FileBasedLibraryIndexDataStore(private val dbFile: Path) : LibraryIndexDataStore,
    Persistable {

    companion object {

        private val MAP_TYPE: Type =
            object : TypeToken<Map<FileModelPhoto, FileModelMetadata?>?>() {}.type
    }

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val delegate: LibraryIndexDataStore = InMemoryLibraryIndexDataStore.empty()

    override fun metadataOf(photoLocation: PhotoLocation): Metadata? = delegate.metadataOf(photoLocation)

    override fun photos(): Sequence<PhotoLocation> = delegate.photos()

    override fun contains(photoLocation: PhotoLocation): Boolean = delegate.contains(photoLocation)

    override fun size(): Long = delegate.size()

    override fun remove(photoLocation: PhotoLocation): Either<LibraryIndexDataStore.Errors.RemovePhotoError, Unit> =
        delegate.remove(photoLocation)

    override fun add(photoLocation: PhotoLocation): Either<LibraryIndexDataStore.Errors.StorePhotoError, Unit> =
        delegate.add(photoLocation)

    override fun storeMetadata(
        photoLocation: PhotoLocation, metadata: Metadata
    ): Either<LibraryIndexDataStore.Errors.StoreMetadataError, Unit> =
        delegate.storeMetadata(photoLocation, metadata)

    override fun clear() = delegate.clear()

    @Throws(IOException::class)
    override fun flush() {
        createCompressedFileOutputStream().use { os ->
            try {
                os.write(
                    createGson().toJson(mapToFileModel()).toByteArray(Charsets.UTF_8)
                )
            } catch (e: JAXBException) {
                logger.error("Failed to serialize data.", e)
            }
            os.flush()
        }
    }

    @Throws(JAXBException::class)
    private fun mapToFileModel(): Map<FileModelPhoto, FileModelMetadata?> {
        return photos().fold(mutableMapOf()) { a, b ->
            val metadata = delegate.metadataOf(b)
            a[FileModelPhoto(b.path.toString())] =
                if (metadata != null) FileModelMetadata(metadata) else null
            return@fold a
        }
    }

    @Throws(IOException::class)
    override fun restore() {
        createCompressedFileInputStream().use { inputStream ->
            restoreFromFileModel(
                createGson().fromJson(
                    IOUtils.toString(
                        inputStream, Charsets.UTF_8
                    ), MAP_TYPE
                )
            )
        }
    }

    private fun restoreFromFileModel(data: Map<FileModelPhoto, FileModelMetadata?>) {
        clear()

        for ((key, value) in data) {
            val photoLocation = PhotoLocation(File(key.path).toPath())

            add(photoLocation)

            if (value != null) {
                storeMetadata(photoLocation, value.toModel())
            }
        }
    }

    private fun createGson(): Gson = GsonBuilder().enableComplexMapKeySerialization().create()

    private fun createCompressedFileInputStream(): InputStream {
        try {
            return GzipCompressorInputStream(
                FileInputStream(
                    dbFile.toFile()
                )
            )
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    private fun createCompressedFileOutputStream(): OutputStream {
        try {
            return GzipCompressorOutputStream(
                FileOutputStream(
                    dbFile.toFile()
                )
            )
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }
}
