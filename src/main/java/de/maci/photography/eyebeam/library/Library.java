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
import de.maci.photography.eyebeam.library.metadata.Metadata;
import de.maci.photography.eyebeam.library.storage.LibraryDataStore;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.requireNonNull;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 30.09.15
 */
public class Library {

    private final LibraryDataStore dataStore;
    private final LibraryConfiguration configuration;

    private final ReentrantLock reindexingLock = new ReentrantLock();

    @VisibleForTesting
    Library(@Nonnull LibraryDataStore dataStore, @Nonnull LibraryConfiguration configuration) {
        requireNonNull(dataStore, "Data store must not be null.");
        requireNonNull(configuration, "Configuration must not be null.");

        this.dataStore = dataStore;
        this.configuration = configuration;
    }

    public Set<Photo> photos() {
        return new HashSet<>(dataStore.photos());
    }

    public long countPhotos() {
        return dataStore.size();
    }

    boolean lockForReindexing() {
        return reindexingLock.tryLock();
    }

    void unlock() {
        reindexingLock.unlock();
    }

    public boolean isReindexing() {
        return reindexingLock.isLocked();
    }

    public LibraryReindexer createReindexer() {
        return LibraryReindexer.newInstance(this, dataStore, configuration);
    }

    public void clear() {
        dataStore.clear();
    }

    public Optional<Metadata> metadataOf(@Nonnull Photo photo) {
        return dataStore.metadataOf(photo);
    }

    public static Library newInstance(@Nonnull LibraryDataStore dataStore,
                                      @Nonnull LibraryConfiguration configuration) {
        return new Library(dataStore, configuration);
    }
}
