package com.nieldeokar.whatsappaudiorecorder;

import java.util.concurrent.TimeUnit

object Utils {

    fun parseTime(milliseconds: Long): String {

        if (TimeUnit.MILLISECONDS.toHours(milliseconds) == 0L) {
            // append only min and hours
            val FORMAT = "%02d:%02d"
            return String.format(FORMAT, TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(milliseconds)),
                    TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(milliseconds)))
        } else {
            val FORMAT = "%02d:%02d:%02d"

            return String.format(FORMAT,
                    TimeUnit.MILLISECONDS.toHours(milliseconds),
                    TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(milliseconds)),
                    TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(milliseconds)))
        }
    }
}
