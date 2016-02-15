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

import com.google.common.base.MoreObjects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Optional;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 10.10.15
 */
public class Metadata {

    public static final class ImageSize {

        private final int width;
        private final int height;

        public ImageSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                              .add("height", height)
                              .add("width", width)
                              .toString();
        }
    }

    private final Long fileSize;
    private final ImageSize imageSize;
    private final ExifData exifData;

    private final Instant gatheredAt;

    public Metadata(@Nullable Long fileSize,
                    @Nullable ImageSize imageSize,
                    @Nullable ExifData exifData) {
        this.fileSize = fileSize;
        this.imageSize = imageSize;
        this.exifData = exifData;

        this.gatheredAt = Instant.now();
    }

    @Nonnull
    public Optional<Long> fileSize() {
        return Optional.ofNullable(fileSize);
    }

    @Nonnull
    public Optional<ImageSize> imageSize() {
        return Optional.ofNullable(imageSize);
    }

    @Nonnull
    public Optional<ExifData> exifData() {
        return Optional.ofNullable(exifData);
    }

    @Nonnull
    public Instant extractedAt() {
        return gatheredAt;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("fileSize", fileSize())
                          .add("imageSize", imageSize())
                          .add("exifData", exifData())
                          .toString();
    }

    public static Metadata empty() {
        return new Metadata(null, null, null);
    }
}
