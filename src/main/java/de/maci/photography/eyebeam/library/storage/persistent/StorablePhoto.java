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
package de.maci.photography.eyebeam.library.storage.persistent;

import de.maci.photography.eyebeam.library.Photo;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Objects;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 06.11.15
 */
@SuppressWarnings("unused")
public class StorablePhoto implements Storable<Photo>, Comparable<StorablePhoto> {

    private String path;

    public StorablePhoto() {
        super();
    }

    private StorablePhoto(Photo photo) {
        this.path = photo.path().toString();
    }

    @Nonnull
    @Override
    public Photo unbox() {
        return new Photo(new File(path).toPath());
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
