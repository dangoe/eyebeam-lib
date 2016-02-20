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
package de.maci.photography.eyebeam.library.storage.persistent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.maci.photography.eyebeam.library.Photo;
import de.maci.photography.eyebeam.library.metadata.Metadata;
import de.maci.photography.eyebeam.library.storage.InMemoryDataStore;
import de.maci.photography.eyebeam.library.storage.LibraryDataStore;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 20.10.15
 */
public class FileDataStore implements LibraryDataStore, Persistable {

    private static final Type STORABLE_MAP_TYPE = new TypeToken<Map<StorablePhoto, StorableMetadata>>() {}.getType();

    private static final String dbFileName = "photos.dat";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final LibraryDataStore delegate;

    private final Path dataDirectory;

    public FileDataStore(@Nonnull Path dataDirectory) {
        requireNonNull(dataDirectory, "Data directory path must not be null!");

        this.delegate = InMemoryDataStore.empty();

        this.dataDirectory = dataDirectory;
    }

    @Override
    public boolean metadataExists(@Nonnull Photo photo) {
        return delegate.metadataExists(photo);
    }

    @Nonnull
    @Override
    public Optional<Metadata> metadataOf(@Nonnull Photo photo) {
        return delegate.metadataOf(photo);
    }

    @Override
    public Stream<Photo> photos() {
        return delegate.photos();
    }

    @Override
    public boolean contains(@Nullable Photo photo) {
        return delegate.contains(photo);
    }

    @Override
    public long size() {
        return delegate.size();
    }

    @Override
    public boolean remove(@Nullable Photo photo) {
        return delegate.remove(photo);
    }

    @Override
    public boolean store(@Nonnull Photo photo) {
        return delegate.store(photo);
    }

    @Override
    public void replaceMetadata(@Nonnull Photo photo, @Nonnull Metadata metadata) {
        delegate.replaceMetadata(photo, metadata);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public void flush() throws IOException {
        try (OutputStream os = createCompressedFileOutputStream()) {
            try {
                os.write(createGson().toJson(mapDataToStorables()).getBytes(Charsets.UTF_8));
            } catch (JAXBException e) {
                logger.error("Failed to serialize data.", e);
            }
            os.flush();
        }
    }

    private Map<StorablePhoto, StorableMetadata> mapDataToStorables() throws JAXBException {
        Map<StorablePhoto, StorableMetadata> result = new TreeMap<>();
        photos().forEach(photo -> {
            Optional<Metadata> metadata = metadataOf(photo);
            result.put(StorablePhoto.of(photo), metadata.isPresent() ? StorableMetadata.of(metadata.get()) : null);
        });
        return result;
    }

    @Override
    public void restore() throws IOException {
        try (InputStream is = createCompressedFileInputStream()) {
            setDataFromStorables(createGson().fromJson(IOUtils.toString(is, Charsets.UTF_8), STORABLE_MAP_TYPE));
        }
    }

    private void setDataFromStorables(Map<StorablePhoto, StorableMetadata> data) {
        clear();

        for (Map.Entry<StorablePhoto, StorableMetadata> entry : data.entrySet()) {
            Photo photo = entry.getKey().unbox();
            store(photo);

            Optional<Metadata> metadata = Storables.unboxNullSafe(entry.getValue());
            if (metadata.isPresent()) {
                replaceMetadata(photo, metadata.get());
            }
        }
    }

    private static Gson createGson() {
        return new GsonBuilder().enableComplexMapKeySerialization().create();
    }

    private InputStream createCompressedFileInputStream() {
        try {
            return new GzipCompressorInputStream(new FileInputStream(dataDirectory.resolve(dbFileName).toFile()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private OutputStream createCompressedFileOutputStream() {
        try {
            return new GzipCompressorOutputStream(new FileOutputStream(dataDirectory.resolve(dbFileName).toFile()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
