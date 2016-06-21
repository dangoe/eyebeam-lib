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

import de.maci.photography.eyebeam.library.metadata.ExifData;
import de.maci.photography.eyebeam.library.metadata.Metadata;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Optional;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 06.11.15
 */
public class StorableMetadata implements Storable<Metadata> {

    private Long fileSize;

    private Double fnumber;
    private Integer focalLength;
    private Integer focalLengthFullFrameEquivalent;
    private Integer iso;
    private Instant takenAt;

    @SuppressWarnings("unused")
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
        return new Metadata(fileSize, null,
                            ExifData.empty().withFnumber(fnumber).withFocalLength(focalLength)
                                    .withFocalLengthFullFrameEquivalent(focalLengthFullFrameEquivalent).withIso(iso)
                                    .withTakenAt(takenAt));
    }

    public static StorableMetadata of(@Nonnull Metadata metadata) {
        return new StorableMetadata(metadata);
    }
}
