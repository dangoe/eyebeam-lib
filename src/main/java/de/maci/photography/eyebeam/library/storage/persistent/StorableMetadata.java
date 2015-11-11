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

import de.maci.photography.eyebeam.library.metadata.ExifData;
import de.maci.photography.eyebeam.library.metadata.Metadata;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Optional;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 06.11.15
 */
@SuppressWarnings("unused")
public class StorableMetadata implements Storable<Metadata> {

    private Long fileSize;

    private Double fnumber;
    private Integer focalLength;
    private Integer focalLengthFullFrameEquivalent;
    private Integer iso;
    private Instant takenAt;

    public StorableMetadata() {
        super();
    }

    private StorableMetadata(Metadata metadata) {
        this.fileSize = metadata.fileSize().orElse(null);

        Optional<ExifData> exifData = metadata.exifData();
        if (exifData.isPresent()) {
            this.fnumber = exifData.get().fnumber().orElse(null);
            this.focalLength = exifData.get().focalLength().orElse(null);
            this.focalLengthFullFrameEquivalent = exifData.get().focalLengthFullFrameEquivalent().orElse(null);
            this.iso = exifData.get().iso().orElse(null);
            this.takenAt = exifData.get().takenAt().orElse(null);
        }
    }

    @Nonnull
    @Override
    public Metadata unbox() {
        return new Metadata(fileSize,
                            ExifData.fromFields(fnumber, focalLength, focalLengthFullFrameEquivalent, iso, takenAt));
    }

    public static StorableMetadata of(@Nonnull Metadata metadata) {
        return new StorableMetadata(metadata);
    }
}
