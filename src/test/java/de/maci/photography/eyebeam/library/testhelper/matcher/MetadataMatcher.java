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
