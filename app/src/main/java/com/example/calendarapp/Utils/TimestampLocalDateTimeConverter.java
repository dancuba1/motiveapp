package com.example.calendarapp.Utils;

import com.google.firebase.Timestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimestampLocalDateTimeConverter {
    public static LocalDateTime convertTimestampToLocalDateTime(Timestamp timestamp) {
        Instant instant = timestamp.toDate().toInstant(); // Convert Timestamp to Instant
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime(); // Convert Instant to LocalDateTime
    }
    public static Timestamp convertLocalDateTimeToTimeStamp(LocalDateTime localDateTime){
        return new Timestamp(java.util.Date.from(localDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()));
    }
}
