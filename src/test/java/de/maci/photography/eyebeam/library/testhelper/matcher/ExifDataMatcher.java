/**
 * Copyright (c) 2015 Daniel Götten
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to
 * do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.maci.photography.eyebeam.library.testhelper.matcher;

import de.maci.photography.eyebeam.library.metadata.ExifData;
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
