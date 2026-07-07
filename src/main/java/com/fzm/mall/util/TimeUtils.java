package com.fzm.mall.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    public static final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter yyyyMMddHHmmss = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter RFC3339 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public static long nowTimestamp() {
        return OffsetDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    public static OffsetDateTime nowDateTime() {
        return OffsetDateTime.now(ZoneOffset.ofHours(8));
    }

    public static OffsetDateTime timestampMilliToDateTime(long timestamp) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.ofHours(8));
    }

    public static OffsetDateTime RFC3339ToDateTime(String timeStr) {
        return LocalDateTime.parse(timeStr, RFC3339).atOffset(ZoneOffset.ofHours(8));
    }
}
