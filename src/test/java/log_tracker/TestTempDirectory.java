package log_tracker;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestTempDirectory {
    public static final Charset CHARSET = Charset.forName("UTF-8");

    final Path tempDir;
    final List<Path> tempFiles;

    public TestTempDirectory() throws IOException {
        tempDir = Files.createTempDirectory("testDir");
        tempFiles = new ArrayList<>();
        tempFiles.add(tempDir);
    }

    public void appendFile(String name, String someValue) throws IOException {
        Path file = pathTo(name);

        Files.write(file, someValue.getBytes(CHARSET),
                StandardOpenOption.APPEND,
                StandardOpenOption.CREATE);

        if (!tempFiles.contains(file)) {
            tempFiles.add(file);
        }
    }

    public void removeFile(String name) {
        Path file = tempDir.resolve(name);
        deleteFile(file);
        tempFiles.remove(file);
    }

    public void cleanup() {
        Collections.reverse(tempFiles);
        tempFiles.forEach(TestTempDirectory::deleteFile);
        tempFiles.clear();
    }

    private static void deleteFile(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            // skip
        }
    }

    public Path getPath() {
        return tempDir;
    }

    public Path pathTo(String name) {
        return tempDir.resolve(name);
    }
}
