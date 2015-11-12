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
package de.maci.photography.eyebeam.library.testhelper;

import de.maci.photography.eyebeam.library.indexing.FilesystemScanner;
import org.mockito.invocation.InvocationOnMock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 12.11.15
 */
public class MockingHelper {

    private MockingHelper() {
        super();
    }

    @SuppressWarnings("unchecked")
    public static FilesystemScanner mockFileScanner(Consumer<InvocationOnMock> operation) throws IOException {
        FilesystemScanner scanner = mock(FilesystemScanner.class);
        doAnswer(invocation -> {
            operation.accept(invocation);
            return null;
        }).when(scanner).scan(any(Path.class), any(Consumer.class));
        return scanner;
    }
}
