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
package de.maci.photography.eyebeam.library.metadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Optional;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 02.10.15
 */
public class ExifData {

    private final Double fnumber;
    private final Integer focalLength;
    private final Integer focalLengthFullFrameEquivalent;
    private final Integer iso;
    private final Instant takenAt;

    private ExifData(@Nullable Double fnumber,
                     @Nullable Integer focalLength,
                     @Nullable Integer focalLengthFullFrameEquivalent,
                     @Nullable Integer iso,
                     @Nullable Instant takenAt) {
        this.fnumber = fnumber;
        this.focalLength = focalLength;
        this.focalLengthFullFrameEquivalent = focalLengthFullFrameEquivalent;
        this.iso = iso;
        this.takenAt = takenAt;
    }

    @Nonnull
    public Optional<Double> fnumber() {
        return Optional.ofNullable(fnumber);
    }

    @Nonnull
    public Optional<Integer> focalLength() {
        return Optional.ofNullable(focalLength);
    }

    @Nonnull
    public Optional<Integer> focalLengthFullFrameEquivalent() {
        return Optional.ofNullable(focalLengthFullFrameEquivalent);
    }

    @Nonnull
    public Optional<Integer> iso() {
        return Optional.ofNullable(iso);
    }

    @Nonnull
    public Optional<Instant> takenAt() {
        return Optional.ofNullable(takenAt);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ExifData{");
        sb.append("fnumber=").append(fnumber);
        sb.append(", focalLength=").append(focalLength);
        sb.append(", focalLengthFullFrameEquivalent=").append(focalLengthFullFrameEquivalent);
        sb.append(", iso=").append(iso);
        sb.append(", takenAt=").append(takenAt);
        sb.append('}');
        return sb.toString();
    }

    public ExifData withFnumber(@Nullable Double fnumber) {
        return new ExifData(fnumber, focalLength, focalLengthFullFrameEquivalent, iso, takenAt);
    }

    public ExifData withFocalLength(@Nullable Integer focalLength) {
        return new ExifData(fnumber, focalLength, focalLengthFullFrameEquivalent, iso, takenAt);
    }

    public ExifData withFocalLengthFullFrameEquivalent(@Nullable Integer focalLengthFullFrameEquivalent) {
        return new ExifData(fnumber, focalLength, focalLengthFullFrameEquivalent, iso, takenAt);
    }

    public ExifData withIso(@Nullable Integer iso) {
        return new ExifData(fnumber, focalLength, focalLengthFullFrameEquivalent, iso, takenAt);
    }

    public ExifData withTakenAt(@Nullable Instant takenAt) {
        return new ExifData(fnumber, focalLength, focalLengthFullFrameEquivalent, iso, takenAt);
    }

    public static ExifData empty() {
        return new ExifData(null, null, null, null, null);
    }
}
