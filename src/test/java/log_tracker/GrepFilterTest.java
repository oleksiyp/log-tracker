package log_tracker;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.*;

public class GrepFilterTest {

    private GrepFilter filter;

    @Before
    public void setUp() throws Exception {
        HashSet<String> strings = new HashSet<>();
        strings.add("abcdef");
        strings.add("ab");
        strings.add("cdef");
        strings.add("acdef");
        strings.add("def");
        filter = new GrepFilter(strings);
    }

    @Test
    public void testSearch() throws Exception {
        String in =
                "afdssdfabcdefasdfacdefsdf";
        String expectedOut =
                "0000000010001000000001000";

        for (int i = 0; i < in.length(); i++) {
            boolean result = filter.search(in.charAt(i));
            assertThat("testSearch " + i,
                    result,
                    CoreMatchers.equalTo(expectedOut.charAt(i) == '1'));
        }
    }

}