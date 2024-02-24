package de.maci.photography.eyebeam.library.storage.persistent.file

import arrow.core.Either
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import de.maci.photography.eyebeam.library.Photo
import de.maci.photography.eyebeam.library.metadata.model.Metadata
import de.maci.photography.eyebeam.library.storage.InMemoryLibraryDataStore
import de.maci.photography.eyebeam.library.storage.LibraryDataStore
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


class FileLibraryDataStore(private val dbFile: Path) : LibraryDataStore, Persistable {

    companion object {

        private val MAP_TYPE: Type =
            object : TypeToken<Map<FileModelPhoto, FileModelMetadata?>?>() {}.type
    }

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val delegate: LibraryDataStore = InMemoryLibraryDataStore.empty()

    override fun metadataOf(photo: Photo): Metadata? = delegate.metadataOf(photo)

    override fun photos(): Sequence<Photo> = delegate.photos()

    override fun contains(photo: Photo): Boolean = delegate.contains(photo)

    override fun size(): Long = delegate.size()

    override fun remove(photo: Photo): Either<LibraryDataStore.Errors.RemovePhotoError, Unit> =
        delegate.remove(photo)

    override fun store(photo: Photo): Either<LibraryDataStore.Errors.StorePhotoError, Unit> =
        delegate.store(photo)

    override fun updateMetadata(
        photo: Photo, metadata: Metadata
    ): Either<LibraryDataStore.Errors.StoreMetadataError, Unit> =
        delegate.updateMetadata(photo, metadata)

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
    private fun mapToFileModel(): Map<FileModelPhoto, FileModelMetadata?> =
        photos().fold(HashMap()) { a, b ->
            val metadata = delegate.metadataOf(b)
            a[FileModelPhoto(b.path.toString())] =
                if (metadata != null) FileModelMetadata(metadata) else null
            return a
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
            val photo = Photo(File(key.path).toPath())

            store(photo)

            if (value != null) {
                updateMetadata(photo, value.toModel())
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
