/**
 * Copyright (c) 2015 Daniel Götten
 * <p/>
 * Permission is hereby granted, free fromMetadata charge, to any person obtaining a copy
 * fromMetadata this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * fromMetadata the Software, and to permit persons to whom the Software is furnished to
 * do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions fromMetadata the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.maci.photography.eyebeam.library;

import com.google.common.base.Stopwatch;
import de.maci.photography.eyebeam.library.indexing.FilesystemScanner;
import de.maci.photography.eyebeam.library.indexing.FilesystemScanner.Options;
import de.maci.photography.eyebeam.library.metadata.DefaultMetadataReader;
import de.maci.photography.eyebeam.library.metadata.Metadata;
import de.maci.photography.eyebeam.library.metadata.MetadataReader;
import de.maci.photography.eyebeam.library.storage.LibraryDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 30.09.15
 */
public final class Library {

    private static final Logger logger = LoggerFactory.getLogger(Library.class);

    private final LibraryDataStore dataStore;
    private final Path rootFolder;

    private final FilesystemScanner scanner;
    private final MetadataReader metadataReader;

    private final AtomicBoolean refreshing = new AtomicBoolean(false);

    private Library(@Nonnull LibraryConfiguration configuration,
                    @Nonnull Supplier<LibraryDataStore> newDataStoreInstanceSupplier) {
        requireNonNull(configuration, "Configuration must not be null.");
        requireNonNull(newDataStoreInstanceSupplier, "New data store instance supplier must not be null.");

        this.dataStore = newDataStoreInstanceSupplier.get();
        requireNonNull(dataStore, "Data store instance must not be null.");

        this.rootFolder = configuration.rootFolder();

        this.scanner = createScanner(configuration.fileFilter().orElse(path -> true));
        this.metadataReader = new DefaultMetadataReader();
    }

    public Set<Photo> photos() {
        return dataStore.photos();
    }

    public long countPhotos() {
        return dataStore.size();
    }

    public boolean isRefreshing() {
        return refreshing.get();
    }

    public void refresh() {
        logger.info("Refreshing library ...");

        if (!refreshing.getAndSet(true)) {
            try {
                Stopwatch stopwatch = Stopwatch.createStarted();

                scanner.scan(rootFolder, path -> dataStore.store(new Photo(rootFolder.relativize(path))));

                photosWithoutExifData()
                        .forEach(photo -> metadataReader.readFrom(rootFolder.resolve(photo.path()))
                                                        .ifPresent(metadata -> dataStore
                                                                .replaceMetadata(photo, metadata)));

                logger.info(String.format("The library has been refreshed in %d second(s).",
                                          stopwatch.elapsed(TimeUnit.SECONDS)));
            } catch (IOException e) {
                logger.error("Refresh failed.", e);
            } finally {
                refreshing.set(false);
            }
        }
    }

    private Stream<Photo> photosWithoutExifData() {
        return photos().stream().filter(photo -> !dataStore.metadataOf(photo).isPresent());
    }

    private FilesystemScanner createScanner(Predicate<Path> fileFilter) {
        return FilesystemScanner.newInstance(fileFilter, Options.newInstance().followSymlinks(true));
    }

    public void clear() {
        dataStore.clear();
    }

    public Optional<Metadata> metadataOf(@Nonnull Photo photo) {
        return dataStore.metadataOf(photo);
    }

    public static Library newInstance(@Nonnull LibraryConfiguration configuration,
                                      @Nonnull Supplier<LibraryDataStore> newDataStoreInstanceSupplier) {
        return new Library(configuration, newDataStoreInstanceSupplier);
    }
}
