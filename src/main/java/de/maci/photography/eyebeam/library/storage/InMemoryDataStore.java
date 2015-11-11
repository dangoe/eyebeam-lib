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
package de.maci.photography.eyebeam.library.storage;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.maci.photography.eyebeam.library.Photo;
import de.maci.photography.eyebeam.library.metadata.Metadata;
import de.maci.photography.eyebeam.library.storage.LibraryDataStore;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 10.10.15
 */
public final class InMemoryDataStore implements LibraryDataStore {

    private final Map<Photo, Metadata> photos = new TreeMap<>(Photo::compareTo);

    @Override
    public Optional<Metadata> metadataOf(@Nonnull Photo photo) {
        requireNonNull(photo, "Corresponding photo must not be null.");
        checkContained(photo);
        return Optional.ofNullable(photos.get(photo));
    }

    @Override
    public Set<Photo> photos() {
        return unmodifiableSet(photos.keySet());
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
