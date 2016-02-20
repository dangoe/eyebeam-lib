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
package de.maci.photography.eyebeam.library.indexing;

import javax.annotation.Nonnull;
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

import static java.nio.file.Files.walkFileTree;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

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
            if (maxDepth <= 0) {
                throw new IllegalArgumentException("Max depth must be a positive number.");
            }
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
