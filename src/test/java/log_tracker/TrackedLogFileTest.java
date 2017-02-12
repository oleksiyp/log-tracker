//package log_tracker;
//
//import bytes_string.ByteString;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.runners.MockitoJUnitRunner;
//
//import java.io.IOException;
//import java.io.PrintStream;
//import java.nio.charset.Charset;
//import java.util.Collections;
//import java.util.function.Consumer;
//
//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.junit.Assert.assertThat;
//
//@RunWith(MockitoJUnitRunner.class)
//public class TrackedLogFileTest {
//    TestTempDirectory tempDir;
//
//    @Mock
//    Consumer<String> consumer;
//
//    @Before
//    public void setUp() throws Exception {
//        tempDir = new TestTempDirectory();
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        tempDir.cleanup();
//    }
//
//    @Test(expected = IOException.class)
//    public void testNoFile() throws Exception {
//        new TrackedLogFile(tempDir.pathTo("abc.txt"),
//                Collections.singleton(new ByteString("CRITICAL", Charset.defaultCharset())),
//                System.out);
//    }
//
//    @Test
//    public void testEmptyFile() throws Exception {
//        tempDir.appendFile("abc.txt", "\n");
//        TrackedLogFile logFile = new TrackedLogFile(tempDir.pathTo("abc.txt"), Collections.singleton(new ByteString("CRITICAL", Charset.defaultCharset())), output);
//        assertThat(logFile.getPossibleToRead(), equalTo(1));
//    }
//
//    @Test
//    public void testRemovedFile() throws Exception {
//        tempDir.appendFile("abc.txt", "\n");
//        TrackedLogFile logFile = new TrackedLogFile(tempDir.pathTo("abc.txt"), Collections.singleton(new ByteString("CRITICAL", Charset.defaultCharset())), output);
//        tempDir.removeFile("abc.txt");
//        assertThat(logFile.getPossibleToRead(), equalTo(-1));
//    }
//
//    @Test
//    public void testFileWithData() throws Exception {
//        tempDir.appendFile("abc.txt", "data\n");
//        TrackedLogFile logFile = new TrackedLogFile(tempDir.pathTo("abc.txt"), Collections.singleton(new ByteString("CRITICAL", Charset.defaultCharset())), output);
//        assertThat(logFile.getPossibleToRead(), equalTo(5));
//    }
//
//    @Test
//    public void testFileWithDataSkipped() throws Exception {
//        tempDir.appendFile("abc.txt", "data\n");
//        TrackedLogFile logFile = new TrackedLogFile(tempDir.pathTo("abc.txt"), Collections.singleton(new ByteString("CRITICAL", Charset.defaultCharset())), output);
//        logFile.skip(5);
//        assertThat(logFile.getPossibleToRead(), equalTo(0));
//    }
//
//    @Test
//    public void testFileWithDataRead() throws Exception {
//        tempDir.appendFile("abc.txt", "data\n");
//        PrintStream stream = Mockito.mock(PrintStream.class);
//        TrackedLogFile logFile = new TrackedLogFile(tempDir.pathTo("abc.txt"), Collections.singleton(new ByteString("CRITICAL", Charset.defaultCharset())), stream);
//
//        logFile.readNext(5);
//        Thread.sleep(200);
//        Mockito.verify(consumer).accept("data");
//        Mockito.verifyNoMoreInteractions(consumer);
//    }
//
//
//    @Test
//    public void testFileWithDataReadRead() throws Exception {
//        tempDir.appendFile("abc.txt", "data\n");
//        TrackedLogFile logFile = new TrackedLogFile(tempDir.pathTo("abc.txt"), Collections.singleton(new ByteString("CRITICAL", Charset.defaultCharset())), output);
//
//        logFile.setConsumer(consumer);
//        logFile.readNext(5);
//        Thread.sleep(200);
//        Mockito.verify(consumer).accept("data");
//        Mockito.reset(consumer);
//
//        tempDir.appendFile("abc.txt", "more\n");
//        logFile.readNext(5);
//        Thread.sleep(200);
//        Mockito.verify(consumer).accept("more");
//    }
//
//    @Test
//    public void testFileWithDataSkipRead() throws Exception {
//        tempDir.appendFile("abc.txt", "data\n");
//        TrackedLogFile logFile = new TrackedLogFile(tempDir.pathTo("abc.txt"), Collections.singleton(new ByteString("CRITICAL", Charset.defaultCharset())), output);
//
//        logFile.setConsumer(consumer);
//        logFile.skip(5);
//        tempDir.appendFile("abc.txt", "more\n");
//        logFile.readNext(5);
//        Thread.sleep(200);
//        Mockito.verify(consumer).accept("more");
//    }
//
//}
