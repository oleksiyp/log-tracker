package log_tracker;

import java.util.Date;

public class Util {
    public static String now() {
        Date now = new Date();
        return String.format("%tH:%tM:%tS", now, now, now);
    }
}
