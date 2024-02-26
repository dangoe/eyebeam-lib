/**
 * Copyright 2024 Daniel Götten
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
package de.maci.photography.eyebeam.library.indexing

import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class FilesystemScanner(
    private val options: Options,
    private val fileFilter: (Path) -> Boolean = { _ -> true }
) {

    data class Options(val maxDepth: UInt, val fileVisitOptions: Set<FileVisitOption>) {

        companion object {

            private const val DEFAULT_MAX_DEPTH: UInt = 42u

            fun default(): Options {
                return Options(DEFAULT_MAX_DEPTH, emptySet())
            }
        }

        fun withFollowSymlinks(flag: Boolean): Options {
            return Options(maxDepth, if (flag) setOf(FileVisitOption.FOLLOW_LINKS) else emptySet())
        }

        fun withMaxDepth(maxDepth: UInt): Options {
            return Options(maxDepth, fileVisitOptions)
        }
    }

    private inner class PathVisitor(private val matchingPathHandler: (Path) -> Unit) :
        SimpleFileVisitor<Path>() {

        @Throws(IOException::class)
        override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
            if (fileFilter(file)) {
                matchingPathHandler(file)
            }
            return super.visitFile(file, attributes)
        }
    }

    @Throws(IOException::class)
    fun scan(rootFolder: Path, matchingPathHandler: (Path) -> Unit) {
        Files.walkFileTree(
            rootFolder,
            options.fileVisitOptions,
            options.maxDepth.toInt(),
            PathVisitor(matchingPathHandler)
        )
    }
}
