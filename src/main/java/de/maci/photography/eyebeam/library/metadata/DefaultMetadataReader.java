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
import java.io.File;
import java.nio.file.Path;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 19.10.15
 */
public class DefaultMetadataReader implements MetadataReader {

    private final DefaultExifDataReader exifDataReader = new DefaultExifDataReader();

    @Override
    public Metadata readFrom(@Nonnull Path path) {
        File file = path.toFile();
        try {
            return new Metadata(file.length(), null, exifDataReader.readFrom(path));
        } catch (SecurityException e) {
            throw new MetadataReadingException(String.format("Failed to read metadata of '%s'.",
                                                             file.getAbsolutePath()), e);
        }
    }
}
