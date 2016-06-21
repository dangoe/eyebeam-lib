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

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 09.10.15
 */
public final class Photo implements Comparable<Photo> {

    private final Path path;
    private final String filename;

    private Photo(@Nonnull Path path) {
        requireNonNull(path, "Path must not be null.");
        this.path = path;
        this.filename = path.getFileName().toString();
    }

    public Path path() {
        return path;
    }

    public String filename() {
        return filename;
    }

    @Override
    public int compareTo(@Nonnull Photo other) {
        return path.compareTo(other.path());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return Objects.equals(path, photo.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Photo{");
        sb.append("path=").append(path);
        sb.append('}');
        return sb.toString();
    }

    public static Photo locatedAt(Path path) {
        return new Photo(path);
    }
}
