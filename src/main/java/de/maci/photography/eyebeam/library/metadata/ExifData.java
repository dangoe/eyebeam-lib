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

import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static com.drew.metadata.exif.ExifDirectoryBase.*;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 02.10.15
 */
public class ExifData {

    @FunctionalInterface
    private interface ThrowingMetadataException<T> {
        T perform() throws MetadataException;
    }

    private static final Logger logger = LoggerFactory.getLogger(ExifData.class);

    private final Double fnumber;
    private final Integer focalLength;
    private final Integer focalLengthFullFrameEquivalent;
    private final Integer iso;
    private final Instant takenAt;

    private ExifData(Double fnumber,
                     Integer focalLength,
                     Integer focalLengthFullFrameEquivalent,
                     Integer iso,
                     Instant takenAt) {
        this.fnumber = fnumber;
        this.focalLength = focalLength;
        this.focalLengthFullFrameEquivalent = focalLengthFullFrameEquivalent;
        this.iso = iso;
        this.takenAt = takenAt;
    }

    public Optional<Double> fnumber() {
        return Optional.ofNullable(fnumber);
    }

    public Optional<Integer> focalLength() {
        return Optional.ofNullable(focalLength);
    }

    public Optional<Integer> focalLengthFullFrameEquivalent() {
        return Optional.ofNullable(focalLengthFullFrameEquivalent);
    }

    public Optional<Integer> iso() {
        return Optional.ofNullable(iso);
    }

    public Optional<Instant> takenAt() {
        return Optional.ofNullable(takenAt);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("takenAt", takenAt()).add("focalLength", focalLength())
                          .add("focalLengthFullFrameEquivalent", focalLengthFullFrameEquivalent())
                          .add("fnumber", fnumber())
                          .add("iso", iso()).toString();
    }

    private static <T> Optional<T> tryExecute(ThrowingMetadataException<T> action) {
        try {
            return Optional.ofNullable(action.perform());
        } catch (MetadataException e) {
            logger.debug("Failed to read EXIF value.", e);
            return Optional.empty();
        }
    }

    public static ExifData fromMetadata(@Nonnull Metadata metadata) {
        ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        return new ExifData(tryExecute(() -> directory.getDouble(TAG_FNUMBER)).orElse(null),
                            tryExecute(() -> directory.getInteger(TAG_FOCAL_LENGTH)).orElse(null),
                            tryExecute(() -> directory.getInteger(TAG_35MM_FILM_EQUIV_FOCAL_LENGTH)).orElse(null),
                            tryExecute(() -> directory.getInteger(TAG_ISO_EQUIVALENT)).orElse(null),
                            tryExecute(() -> directory.getDate(TAG_DATETIME_ORIGINAL)).map(Date::toInstant)
                                                                                      .orElse(null));
    }

    public static ExifData fromFields(@Nullable Double fnumber,
                                      @Nullable Integer focalLength,
                                      @Nullable Integer focalLengthFullFrameEquivalent,
                                      @Nullable Integer iso,
                                      @Nullable Instant takenAt) {
        return new ExifData(fnumber, focalLength, focalLengthFullFrameEquivalent, iso, takenAt);
    }
}
