/**
 * Copyright (c) 2015 Daniel Götten
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to
 * do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.maci.photography.eyebeam.library.storage.persistent;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.maci.photography.eyebeam.library.Photo;
import de.maci.photography.eyebeam.library.metadata.Metadata;
import de.maci.photography.eyebeam.library.storage.InMemoryDataStore;
import de.maci.photography.eyebeam.library.storage.LibraryDataStore;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 20.10.15
 */
public class FileDataStore implements LibraryDataStore {

    private static final Type STORABLE_MAP_TYPE = new TypeToken<Map<StorablePhoto, StorableMetadata>>() {}.getType();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Supplier<InputStream> inputStreamSupplier;
    private final Supplier<OutputStream> outputStreamSupplier;

    private final LibraryDataStore delegate;

    public FileDataStore(@Nonnull Path dataDirectory) {
        this(() -> createCompressedFileInputStream(dataDirectory),
             () -> createCompressedFileOutputStream(dataDirectory));
    }

    @VisibleForTesting
    FileDataStore(@Nonnull Supplier<InputStream> inputStreamSupplier,
                  @Nonnull Supplier<OutputStream> outputStreamSupplier) {
        requireNonNull(inputStreamSupplier, "InputStream supplier must not be null");
        requireNonNull(outputStreamSupplier, "OutputStream supplier must not be null");
        this.inputStreamSupplier = inputStreamSupplier;
        this.outputStreamSupplier = outputStreamSupplier;
        this.delegate = new InMemoryDataStore();
    }

    private static InputStream createCompressedFileInputStream(@Nonnull Path dataDirectory) {
        try {
            return new GzipCompressorInputStream(new FileInputStream(dataDirectory.resolve("photos.dat").toFile()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static OutputStream createCompressedFileOutputStream(@Nonnull Path dataDirectory) {
        try {
            return new GzipCompressorOutputStream(new FileOutputStream(dataDirectory.resolve("photos.dat").toFile()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Optional<Metadata> metadataOf(@Nonnull Photo photo) {
        return delegate.metadataOf(photo);
    }

    @Override
    public Set<Photo> photos() {
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

    public void flush() throws IOException {
        try (OutputStream os = outputStreamSupplier.get()) {
            try {
                os.write(createGson().toJson(mapDataToStorables()).getBytes(UTF_8));
            } catch (JAXBException e) {
                logger.error("Failed to serialize data.", e);
            }
            os.flush();
        }
    }

    private Map<StorablePhoto, StorableMetadata> mapDataToStorables() throws JAXBException {
        Map<StorablePhoto, StorableMetadata> result = new TreeMap<>();
        for (Photo photo : photos()) {
            Optional<Metadata> metadata = metadataOf(photo);
            result.put(StorablePhoto.of(photo), metadata.isPresent() ? StorableMetadata.of(metadata.get()) : null);
        }
        return result;
    }

    public void restore() throws IOException {
        try (InputStream is = inputStreamSupplier.get()) {
            setDataFromStorables(createGson().fromJson(IOUtils.toString(is, UTF_8), STORABLE_MAP_TYPE));
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
}
