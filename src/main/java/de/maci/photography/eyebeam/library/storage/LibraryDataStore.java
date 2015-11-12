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
package de.maci.photography.eyebeam.library.storage;

import de.maci.photography.eyebeam.library.Photo;
import de.maci.photography.eyebeam.library.metadata.Metadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 09.10.15
 */
public interface LibraryDataStore {

    Optional<Metadata> metadataOf(@Nonnull Photo photo);

    Set<Photo> photos();

    boolean contains(@Nullable Photo photo);

    long size();

    boolean remove(@Nullable Photo photo);

    boolean store(@Nonnull Photo photo);

    void replaceMetadata(@Nonnull Photo photo, @Nonnull Metadata metadata);

    void clear();
}
