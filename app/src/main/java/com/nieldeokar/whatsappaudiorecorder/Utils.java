package com.nieldeokar.whatsappaudiorecorder;

import java.util.concurrent.TimeUnit;

public class Utils {

    public static String parseTime(long milliseconds) {

        if (TimeUnit.MILLISECONDS.toHours(milliseconds) == 0) {
            // append only min and hours
            String FORMAT = "%02d:%02d";
            return String.format(FORMAT, TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(milliseconds)),
                    TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
        } else {
            String FORMAT = "%02d:%02d:%02d";

            return String.format(FORMAT,
                    TimeUnit.MILLISECONDS.toHours(milliseconds),
                    TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(milliseconds)),
                    TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
        }
    }
}
