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
package de.maci.photography.eyebeam.library.indexing;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.file.Files.walkFileTree;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 02.10.15
 */
public class FilesystemScanner {

    public static final class Options {

        public static final Integer DEFAULT_MAX_DEPTH = 42;

        protected final int maxDepth;
        protected final Set<FileVisitOption> fileVisitOptions;

        private Options(int maxDepth, Set<FileVisitOption> fileVisitOptions) {
            checkArgument(maxDepth > 0, "Max depth must be a positive number.");
            this.maxDepth = maxDepth;
            this.fileVisitOptions = fileVisitOptions;
        }

        public static Options newInstance() {
            return new Options(DEFAULT_MAX_DEPTH, Collections.emptySet());
        }

        public Options followSymlinks(boolean flag) {
            return new Options(maxDepth, flag ? singleton(FileVisitOption.FOLLOW_LINKS) : emptySet());
        }

        public Options limitDepthTo(int maxDepth) {
            if (maxDepth < 1) {
                throw new IllegalArgumentException("Max depth must be larger than 0.");
            }
            return new Options(maxDepth, fileVisitOptions);
        }
    }

    private class PathVisitor extends SimpleFileVisitor<Path> {

        private final Consumer<Path> matchingPathHandler;

        private PathVisitor(Consumer<Path> matchingPathHandler) {
            this.matchingPathHandler = matchingPathHandler;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
            if (filter.test(file)) {
                matchingPathHandler.accept(file);
            }
            return super.visitFile(file, attributes);
        }
    }

    private final Predicate<Path> filter;
    private final Options options;

    private FilesystemScanner(Predicate<Path> filter, Options options) {
        this.filter = filter;
        this.options = options;
    }

    public static FilesystemScanner newInstance(@Nonnull Predicate<Path> filter) {
        return newInstance(filter, Options.newInstance());
    }

    public static FilesystemScanner newInstance(@Nonnull Predicate<Path> filter, @Nonnull Options options) {
        requireNonNull(filter, "Matcher must not be null!");
        requireNonNull(options, "Options must not be null!");
        return new FilesystemScanner(filter, options);
    }

    public void scan(Path rootFolder, Consumer<Path> matchingPathHandler) throws IOException {
        walkFileTree(rootFolder, options.fileVisitOptions, options.maxDepth, new PathVisitor(matchingPathHandler));
    }
}
