package com.conveyal.gtfs.error;

/** Represents a problem parsing a date field from a GTFS feed. */
public class DateParseError extends GTFSError {

    public DateParseError(String file, long line, String field) {
        super(file, line, field);
    }

    @Override public String getMessage() {
        return "";
    }

}
