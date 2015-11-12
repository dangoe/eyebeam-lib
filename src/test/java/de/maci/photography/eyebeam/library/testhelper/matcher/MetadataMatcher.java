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
package de.maci.photography.eyebeam.library.testhelper.matcher;

import de.maci.photography.eyebeam.library.metadata.ExifData;
import de.maci.photography.eyebeam.library.metadata.Metadata;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 10.11.15
 */
public class MetadataMatcher extends TypeSafeMatcher<Metadata> {

    private final Metadata expected;

    public MetadataMatcher(Metadata expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(Metadata metadata) {
        //formatter:off
        Optional<ExifData> expectedExifData = expected.exifData();
        Optional<ExifData> exifData = metadata.exifData();

        return Objects.equals(expected.fileSize(), metadata.fileSize())
                && (!expectedExifData.isPresent() && !exifData.isPresent()
                || expectedExifData.isPresent() && exifData.isPresent()
                && new ExifDataMatcher(expectedExifData.get()).matchesSafely(exifData.get()));
        // formatter:on
    }

    @Override
    public void describeTo(Description description) {
        // Not implemented
    }
}
