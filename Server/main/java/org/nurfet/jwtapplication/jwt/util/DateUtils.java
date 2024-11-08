package org.nurfet.jwtapplication.jwt.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtils {

    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

    public static Date toDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(DEFAULT_ZONE_ID).toInstant());
    }
}
