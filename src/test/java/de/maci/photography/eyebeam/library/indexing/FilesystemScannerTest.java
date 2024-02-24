package de.maci.photography.eyebeam.library.indexing;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.createTempFile;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Daniel Götten <daniel.goetten@googlemail.com>
 * @since 04.11.15
 */
@SuppressWarnings("unchecked")
public class FilesystemScannerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Path directoryToBeScanned;

    @Before
    public void setUp() throws IOException {
        directoryToBeScanned =
                createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "directoryToBeScanned");
    }

    @After
    public void cleanUp() throws IOException {
        FileUtils.deleteDirectory(directoryToBeScanned.toFile());
    }

    @Test
    public void aFilterCanBeUsedToFilterFilesInADirectory() throws Exception {
        Path jpgFile = createTempFile(directoryToBeScanned, "someFile", ".jpg");
        Path pngFile = createTempFile(directoryToBeScanned, "someFile", ".png");
        Path txtFile = createTempFile(directoryToBeScanned, "someFile", ".txt");

        Consumer<Path> pathConsumer = mock(Consumer.class);
        Predicate<Path> filter = fileNameEndsWith(".jpg");

        FilesystemScanner.newInstance(filter).scan(directoryToBeScanned, pathConsumer);

        verify(filter).test(jpgFile);
        verify(filter).test(pngFile);
        verify(filter).test(txtFile);
    }

    @Test
    public void aPathConsumerCanBeUsedToProcessFiles() throws Exception {
        Path jpgFile = createTempFile(directoryToBeScanned, "someFile", ".jpg");
        Path anotherJpgFile = createTempFile(directoryToBeScanned, "someFile", ".jpg");

        Consumer<Path> pathConsumer = mock(Consumer.class);

        FilesystemScanner.newInstance(path -> true).scan(directoryToBeScanned, pathConsumer);

        verify(pathConsumer).accept(jpgFile);
        verify(pathConsumer).accept(anotherJpgFile);
    }

    @Test
    public void onlyMatchingFilesCanBeConsumed() throws Exception {
        Path jpgFile = createTempFile(directoryToBeScanned, "someFile", ".jpg");
        Path pngFile = createTempFile(directoryToBeScanned, "someFile", ".png");
        Path txtFile = createTempFile(directoryToBeScanned, "someFile", ".txt");

        Consumer<Path> pathConsumer = mock(Consumer.class);

        FilesystemScanner.newInstance(fileNameEndsWith(".jpg")).scan(directoryToBeScanned, pathConsumer);

        verify(pathConsumer).accept(jpgFile);
        verify(pathConsumer, never()).accept(pngFile);
        verify(pathConsumer, never()).accept(txtFile);
    }

    @Test
    public void directoriesAreScannedRecursively() throws Exception {
        Path someSubfolder = createTempDirectory(directoryToBeScanned, "someSubfolder");
        Path jpgFile = createTempFile(directoryToBeScanned, "someFile", ".jpg");
        Path jpgFileInSubfolder = createTempFile(someSubfolder, "someFile", ".jpg");

        Consumer<Path> pathConsumer = mock(Consumer.class);

        FilesystemScanner.newInstance(fileNameEndsWith(".jpg")).scan(directoryToBeScanned, pathConsumer);

        verify(pathConsumer).accept(jpgFile);
        verify(pathConsumer).accept(jpgFileInSubfolder);
    }

    @Test
    public void scanningDepthCanBeLimited() throws Exception {
        Path someSubfolder = createTempDirectory(directoryToBeScanned, "someSubfolder");
        Path jpgFile = createTempFile(directoryToBeScanned, "someFile", ".jpg");
        Path jpgFileInSubfolder = createTempFile(someSubfolder, "someFile", ".jpg");

        Consumer<Path> pathConsumer = mock(Consumer.class);

        FilesystemScanner.newInstance(fileNameEndsWith(".jpg"), Options.newInstance().withLimitDepthTo(1))
                         .scan(directoryToBeScanned, pathConsumer);

        verify(pathConsumer).accept(jpgFile);
        verify(pathConsumer, never()).accept(jpgFileInSubfolder);
    }

    @Test
    public void maxScanningDepthMustNotBeZero() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Max depth must be larger than 0.");

        Options.newInstance().withLimitDepthTo(0);
    }

    @Test
    public void maxScanningDepthMustNotBeNegative() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Max depth must be larger than 0.");

        Options.newInstance().withLimitDepthTo(-1);
    }

    private static Predicate<Path> fileNameEndsWith(String suffix) {
        Predicate<Path> filter = mock(Predicate.class);
        when(filter.test(any(Path.class))).thenAnswer(
                i -> ((Path) i.getArguments()[0]).getFileName().toString().endsWith(suffix));
        return filter;
    }
}
