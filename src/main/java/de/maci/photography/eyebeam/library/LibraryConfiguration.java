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
package de.maci.photography.eyebeam.library;

import de.maci.photography.eyebeam.library.metadata.DefaultMetadataReader;
import de.maci.photography.eyebeam.library.metadata.MetadataReader;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 08.10.15
 */
public interface LibraryConfiguration {

    Path rootFolder();

    Optional<Predicate<Path>> fileFilter();

    default Supplier<MetadataReader> metadataReader() {
        return () -> new DefaultMetadataReader();
    }
}
