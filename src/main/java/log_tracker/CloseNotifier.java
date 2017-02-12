package log_tracker;

import java.nio.file.Path;

public interface CloseNotifier {
    void closed(Path path, TrackedLogFile replacement);
}