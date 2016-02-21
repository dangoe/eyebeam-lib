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
package de.maci.photography.eyebeam.library;

import de.maci.photography.eyebeam.library.indexing.FilesystemScanner;
import de.maci.photography.eyebeam.library.metadata.Metadata;
import de.maci.photography.eyebeam.library.metadata.MetadataReader;
import de.maci.photography.eyebeam.library.storage.LibraryDataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 16.02.16
 */
public class LibraryReindexer {

    @FunctionalInterface
    public interface ReindexingNecessaryDecision {

        boolean check(Photo photo);
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Library library;
    private final LibraryConfiguration libraryConfiguration;

    private final ReindexingNecessaryDecision reindexingNecessaryDecision;

    // For testing purposes only
    protected LibraryReindexer(Library library,
                               LibraryConfiguration libraryConfiguration,
                               ReindexingNecessaryDecision reindexingNecessaryDecision) {
        this.library = library;
        this.libraryConfiguration = libraryConfiguration;
        this.reindexingNecessaryDecision = reindexingNecessaryDecision;
    }

    // For testing purposes only
    protected FilesystemScanner createScanner(Predicate<Path> fileFilter) {
        return FilesystemScanner.newInstance(fileFilter, FilesystemScanner.Options.newInstance().followSymlinks(true));
    }

    private Path rootFolder() {
        return libraryConfiguration.rootFolder();
    }

    private Predicate<Path> fileFilter() {
        return libraryConfiguration.fileFilter().orElse(p -> true);
    }

    public void reindexLibrary() {
        if (library.lockForReindexing()) {
            try {
                Path rootFolder = rootFolder();
                MetadataReader metadataReader = libraryConfiguration.metadataReader().get();

                createScanner(fileFilter())
                        .scan(rootFolder,
                              path -> library.dataStore().store(Photo.locatedAt(rootFolder.relativize(path))));

                photosWithMetadataToBeRefreshed()
                        .forEach(photo -> {
                            try {
                                Metadata metadata = metadataReader.readFrom(rootFolder.resolve(photo.path()));
                                library.dataStore().replaceMetadata(photo, metadata);
                            } catch (Exception e) {
                                // Process should not be interrupted if a single exception occurs
                                logger.warn("Failed to read metadata for '" + photo.path() + "'.", e);
                            }
                        });
            } catch (IOException e) {
                throw new IllegalStateException(e);
            } finally {
                library.unlock();
            }
        }
    }

    private Stream<Photo> photosWithMetadataToBeRefreshed() {
        return library.photos().filter(photo -> reindexingNecessaryDecision.check(photo));
    }

    public LibraryReindexer withCustomReindexingNecessaryDecision(@Nonnull ReindexingNecessaryDecision decision) {
        return new LibraryReindexer(library, libraryConfiguration, decision);
    }

    static LibraryReindexer newInstance(@Nonnull Library library,
                                        @Nonnull LibraryConfiguration libraryConfiguration) {
        return new LibraryReindexer(library,
                                    libraryConfiguration,
                                    refreshIfMetadataOrExifIsMissing(library.dataStore()));

    }

    private static ReindexingNecessaryDecision refreshIfMetadataOrExifIsMissing(LibraryDataStore dataStore) {
        return photo -> !dataStore.metadataExists(photo) || !dataStore.metadataOf(photo).get().exifData().isPresent();
    }
}
