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

import java.nio.file.Path;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 19.10.15
 */
public class DefaultMetadataReader implements MetadataReader {

    private final ExifDataReader exifDataReader = new ExifDataReader();

    @Override
    public Optional<Metadata> readFrom(@Nonnull Path path) {
        return exifDataReader.readFrom(path).map(exifData -> new Metadata(path.toFile().length(), exifData));
    }
}
