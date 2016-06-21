/**
 * Copyright 2016 Daniel Götten
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

import de.maci.photography.eyebeam.library.Photo;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 20.02.16
 */
public interface MetadataAccessor {

    /**
     * Checks if metadata for the given photo is present without loading the metadata information.
     *
     * @param photo The corresponding photo.
     * @return <code>true</code>, if metadata for the given photo is present.
     */
    boolean metadataExists(@Nonnull Photo photo);

    @Nonnull
    Optional<Metadata> metadataOf(@Nonnull Photo photo);
}
