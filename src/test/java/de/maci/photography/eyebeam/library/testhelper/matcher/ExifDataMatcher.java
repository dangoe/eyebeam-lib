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

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 10.11.15
 */
public class ExifDataMatcher extends TypeSafeMatcher<ExifData> {

    private final ExifData expected;

    public ExifDataMatcher(ExifData expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(ExifData exifData) {
        return Objects.equals(expected.fnumber(), exifData.fnumber())
                && Objects.equals(expected.focalLength(), exifData.focalLength())
                && Objects.equals(expected.focalLengthFullFrameEquivalent(), exifData.focalLengthFullFrameEquivalent())
                && Objects.equals(expected.iso(), exifData.iso())
                && Objects.equals(expected.takenAt(), exifData.takenAt());
    }

    @Override
    public void describeTo(Description description) {
        // Not implemented
    }
}
