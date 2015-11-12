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
