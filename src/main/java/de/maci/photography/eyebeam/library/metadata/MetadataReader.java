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
import java.nio.file.Path;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 19.10.15
 */
@FunctionalInterface
public interface MetadataReader {

    /**
     * Reads a photo's {@link Metadata} for a specific path.
     *
     * @param path The photo's path.
     * @return The read {@link Metadata}.
     * @throws MetadataReadingException Is thrown a the photo's metadata for the specific path could not be read.
     */
    Metadata readFrom(@Nonnull Path path);
}
