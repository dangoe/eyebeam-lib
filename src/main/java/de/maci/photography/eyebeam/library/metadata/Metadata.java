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
package de.maci.photography.eyebeam.library.metadata;

import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 10.10.15
 */
public final class Metadata {

    private final Long fileSize;
    private final ExifData exifData;

    public Metadata(@Nullable Long fileSize, @Nullable ExifData exifData) {
        this.fileSize = fileSize;
        this.exifData = exifData;
    }

    public Optional<ExifData> exifData() {
        return Optional.ofNullable(exifData);
    }

    public Optional<Long> fileSize() {
        return Optional.ofNullable(fileSize);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("fileSize", fileSize()).add("exifData", exifData()).toString();
    }

    public static Metadata empty() {
        return new Metadata(null, null);
    }
}
