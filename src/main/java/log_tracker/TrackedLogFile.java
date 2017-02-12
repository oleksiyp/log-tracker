package log_tracker;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.RunnableFuture;
import java.util.function.Consumer;

public class TrackedLogFile implements Runnable {
    final Path path;
    final SeekableByteChannel channel;
    private Set<String> grepPhrases;
    private Consumer<String> output;
    private CloseNotifier closeNotifier;

    int readLength;
    private final ByteBuffer bufObj;
    private final byte[] bufData;

    private volatile Consumer<String> consumer;
    private final CharsetDecoder decoder;

    public TrackedLogFile(Path path,
                          Set<String> grepPhrases,
                          Consumer<String> output,
                          CloseNotifier closeNotifier) throws IOException {
        this.path = path;
        channel = Files.newByteChannel(path, StandardOpenOption.READ);
        this.grepPhrases = grepPhrases;
        this.output = output;
        this.closeNotifier = closeNotifier;
        bufData = new byte[64 * 1024];
        bufObj = ByteBuffer.wrap(bufData);
        decoder = Charset.defaultCharset().newDecoder();
    }

    public int getPossibleToRead() {
        try {
            return (int) (Files.size(path) - readLength);
        } catch (IOException e) {
            return -1;
        }
    }

    public void skip(int n) throws IOException {
        channel.position(readLength + n);
        readLength += n;
    }

    public void readNext(int toRead) throws IOException {
        while (toRead > 0) {
            bufObj.clear();
            if (bufObj.limit() > toRead) {
                bufObj.limit(toRead);
            }

            int nRead = channel.read(bufObj);
            if (nRead <= 0) {
                break;
            }

            toRead -= nRead;
            readLength += nRead;

            bufObj.flip();

            CharBuffer buffer = decoder.decode(bufObj);
            for (int i = 0; i < buffer.length(); i++) {
                char c = buffer.get(i);
                if (c == '\n') {
//                    buffer.
                }
            }
        }
    }


    public void stopTracking() {
        try {
            channel.close();
        } catch (IOException e) {
            // skip
        }
        TrackedLogFile logFile = null;
        try {
            logFile = new TrackedLogFile(path, grepPhrases, output, closeNotifier);
        } catch (IOException e) {
            // skip
        }
        if (closeNotifier != null) {
            closeNotifier.closed(path, logFile);
        }

    }

    @Override
    public void run() {
        while (Thread.interrupted()) {
            int toRead = getPossibleToRead();
            try {
                readNext(toRead);
            } catch (IOException e) {
                System.out.println(Util.now() + " error reading '" + path);
                stopTracking();
            }
        }
    }
}
