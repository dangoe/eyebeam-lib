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

import de.maci.photography.eyebeam.library.Photo;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Objects;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 06.11.15
 */
public class StorablePhoto implements Storable<Photo>, Comparable<StorablePhoto> {

    private String path;

    @SuppressWarnings("unused")
    public StorablePhoto() {
        super();
    }

    private StorablePhoto(Photo photo) {
        this.path = photo.path().toString();
    }

    @Nonnull
    @Override
    public Photo unbox() {
        return Photo.locatedAt(new File(path).toPath());
    }

    @Override
    public int compareTo(@Nonnull StorablePhoto other) {
        return path.compareTo(other.path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorablePhoto that = (StorablePhoto) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    public static StorablePhoto of(Photo photo) {
        return new StorablePhoto(photo);
    }
}
