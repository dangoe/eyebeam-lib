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
package de.maci.photography.eyebeam.library.storage;

import de.maci.photography.eyebeam.library.Photo;
import de.maci.photography.eyebeam.library.metadata.Metadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 10.10.15
 */
public final class InMemoryDataStore implements LibraryDataStore {

    private final Map<Photo, Metadata> photos = new TreeMap<>(Photo::compareTo);

    private InMemoryDataStore() {
        super();
    }

    public static InMemoryDataStore empty() {
        return new InMemoryDataStore();
    }

    @Override
    public boolean metadataExists(@Nonnull Photo photo) {
        return metadataOf(photo).isPresent();
    }

    @Override
    public Optional<Metadata> metadataOf(@Nonnull Photo photo) {
        requireNonNull(photo, "Corresponding photo must not be null.");
        checkContained(photo);
        return Optional.ofNullable(photos.get(photo));
    }

    @Override
    public Stream<Photo> photos() {
        return photos.keySet().stream();
    }

    @Override
    public boolean contains(@Nullable Photo photo) {
        return photos.containsKey(photo);
    }

    @Override
    public long size() {
        return photos.size();
    }

    @Override
    public boolean remove(@Nullable Photo photo) {
        if (photo == null) {
            return false;
        }
        boolean returnValue = photos.containsKey(photo);
        photos.remove(photo);
        return returnValue;
    }

    @Override
    public boolean store(@Nonnull Photo photo) {
        requireNonNull(photo, "Photo to be added must not be null.");
        if (!photos.containsKey(photo)) {
            photos.put(photo, null);
            return true;
        }
        return false;
    }

    @Override
    public void replaceMetadata(@Nonnull Photo photo, @Nonnull Metadata metadata) {
        requireNonNull(photo, "Corresponding photo must not be null.");
        requireNonNull(metadata, "Metadata to be set must not be null.");
        checkContained(photo);
        photos.replace(photo, metadata);
    }

    @Override
    public void clear() {
        photos.clear();
    }

    private void checkContained(Photo photo) {
        if (!contains(photo)) {
            throw new NoSuchElementException(String.format("Data store does not contain '%s'.", photo.path()));
        }
    }
}
