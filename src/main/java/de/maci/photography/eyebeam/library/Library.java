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
package de.maci.photography.eyebeam.library;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import de.maci.photography.eyebeam.library.indexing.FilesystemScanner;
import de.maci.photography.eyebeam.library.indexing.FilesystemScanner.Options;
import de.maci.photography.eyebeam.library.metadata.Metadata;
import de.maci.photography.eyebeam.library.metadata.MetadataReader;
import de.maci.photography.eyebeam.library.storage.LibraryDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 30.09.15
 */
public class Library {

    private static final Logger logger = LoggerFactory.getLogger(Library.class);

    private final LibraryDataStore dataStore;
    private final Path rootFolder;

    private final FilesystemScanner scanner;
    private final MetadataReader metadataReader;

    private final ReentrantLock refreshLock = new ReentrantLock();

    @VisibleForTesting
    Library(@Nonnull LibraryConfiguration configuration,
            @Nonnull Supplier<LibraryDataStore> newDataStoreInstanceSupplier) {
        requireNonNull(configuration, "Configuration must not be null.");
        requireNonNull(newDataStoreInstanceSupplier, "New data store instance supplier must not be null.");

        this.dataStore = newDataStoreInstanceSupplier.get();
        requireNonNull(dataStore, "Data store instance must not be null.");

        this.rootFolder = configuration.rootFolder();

        this.scanner = createScanner(configuration.fileFilter().orElse(path -> true));
        this.metadataReader = configuration.metadataReader().get();
    }

    @VisibleForTesting
    protected FilesystemScanner createScanner(Predicate<Path> fileFilter) {
        return FilesystemScanner.newInstance(fileFilter, Options.newInstance().followSymlinks(true));
    }

    public Set<Photo> photos() {
        return new HashSet<>(dataStore.photos());
    }

    public long countPhotos() {
        return dataStore.size();
    }

    public boolean isRefreshing() {
        return refreshLock.isLocked();
    }

    public void refresh() {
        logger.info("Refreshing library ...");

        if (refreshLock.tryLock()) {
            try {
                Stopwatch stopwatch = Stopwatch.createStarted();

                scanner.scan(rootFolder, path -> dataStore.store(new Photo(rootFolder.relativize(path))));

                photosWithoutExifData()
                        .forEach(photo -> {
                            try {
                                dataStore
                                        .replaceMetadata(photo,
                                                         metadataReader.readFrom(rootFolder.resolve(photo.path())));
                            } catch (Exception e) {
                                logger.error("Failed to update metadata.", e);
                            }
                        });

                logger.info(String.format("The library has been refreshed in %d second(s).",
                                          stopwatch.elapsed(TimeUnit.SECONDS)));
            } catch (IOException e) {
                logger.error("Refresh failed.", e);
            } finally {
                refreshLock.unlock();
            }
        }
    }

    private Stream<Photo> photosWithoutExifData() {
        return photos().stream().filter(photo -> !dataStore.metadataOf(photo).isPresent());
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
