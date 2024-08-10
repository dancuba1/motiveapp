package com.example.calendarapp.Utils;

import androidx.room.TypeConverter;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class LocalDateTimeConverter {

    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @TypeConverter
    public static LocalDateTime toLocalDateTime(String value) {
        return (value == null) ? null : LocalDateTime.parse(value, formatter);
    }

    @TypeConverter
    public static String fromLocalDateTime(LocalDateTime dateTime) {
        return (dateTime == null) ? null : dateTime.format(formatter);
    }

    public static String toTimeString(LocalDateTime dateTime) {
        return (dateTime == null) ? null : dateTime.format(TIME_FORMATTER);
    }

    public static String convertLocalDateTimeToFormattedString(LocalDateTime dateTime) {
        // Adjusted pattern to include time "HH:mm" at the beginning
        String pattern = "HH:mm d'PLACEHOLDER' MMMM yyyy";

        // Extracting the day of month to determine the suffix
        int day = dateTime.getDayOfMonth();
        String suffix = getDayOfMonthSuffix(day);

        // Creating a DateTimeFormatter with the correct suffix
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern.replace("PLACEHOLDER", suffix), Locale.ENGLISH);

        // Formatting the LocalDateTime

        return dateTime.format(formatter);
    }
    public static String convertDateToFormattedString(Date date) {
        String pattern = "d'PLACEHOLDER' MMMM yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);

        // Extracting the day of month to determine the suffix
        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.ENGLISH);
        int day = Integer.parseInt(dayFormat.format(date));
        String suffix = getDayOfMonthSuffix(day);

        // Replacing the placeholder with the correct suffix
        String formattedDate = simpleDateFormat.format(date).replace("PLACEHOLDER", suffix);

        return formattedDate;
    }

    public static long localDateTimeToEpochMilli(LocalDateTime localDateTime, ZoneId zoneId) {
        // Convert LocalDateTime to ZonedDateTime
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);

        // Convert ZonedDateTime to Instant
        Instant instant = zonedDateTime.toInstant();

        // Return epoch millisecond from the Instant
        return instant.toEpochMilli();
    }

    private static String getDayOfMonthSuffix(final int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }
}

