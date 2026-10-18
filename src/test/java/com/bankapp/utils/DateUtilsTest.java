package com.bankapp.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("DateUtils")
public class DateUtilsTest {

    @ParameterizedTest(name = "{0} -> \"{1}\"")
    @CsvSource({
            "2024-01-01T00:00:00, 2024-01-01 00:00:00",
            "1999-12-31T23:59:59, 1999-12-31 23:59:59",
            "2024-02-29T12:05:09, 2024-02-29 12:05:09",
            "2026-10-16T15:30:45, 2026-10-16 15:30:45"
    })
    @DisplayName("formatDate uses the yyyy-MM-dd HH:mm:ss format")
    void formatDateUsesStatementFormat(String input, String expected) {
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.parse(input));

        assertEquals(expected, DateUtils.formatDate(timestamp));
    }

    @Test
    @DisplayName("fractions of a second are dropped, not rounded")
    void fractionsOfSecondAreDropped() {
        Timestamp timestamp = Timestamp.valueOf("2026-10-16 10:01:02.987");

        assertEquals("2026-10-16 10:01:02", DateUtils.formatDate(timestamp));
    }

    @Test
    @DisplayName("the formatter can parse its own output back to the same date-time")
    void formatterRoundTrips() {
        LocalDateTime original = LocalDateTime.of(2026, 3, 8, 9, 15, 0);

        String text = DateUtils.formatDate(Timestamp.valueOf(original));

        assertEquals(original, LocalDateTime.parse(text, DateUtils.YYYYMMDDHHMMSS_FORMATTER));
    }

    @Test
    @DisplayName("a null timestamp throws NullPointerException")
    void nullTimestampThrows() {
        assertThrows(NullPointerException.class, () -> DateUtils.formatDate(null));
    }
}