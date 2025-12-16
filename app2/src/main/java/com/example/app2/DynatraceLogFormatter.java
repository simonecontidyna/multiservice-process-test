package com.example.app2;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DynatraceLogFormatter extends Formatter {

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    private final String serviceName;
    private final String serviceId;

    public DynatraceLogFormatter(String serviceName, String serviceId) {
        this.serviceName = serviceName;
        this.serviceId = serviceId;
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        // Dynatrace enrichment prefix in standard format [!dt key=value,key=value]
        sb.append("[!dt dt.entity.service=").append(serviceId).append("] ");

        // Timestamp
        sb.append(DATE_FORMATTER.format(Instant.ofEpochMilli(record.getMillis())));
        sb.append(" ");

        // Log level
        sb.append("[").append(record.getLevel()).append("] ");

        // Logger name
        sb.append(record.getLoggerName()).append(" - ");

        // Message
        sb.append(formatMessage(record));

        // New line
        sb.append(System.lineSeparator());

        // Exception if present
        if (record.getThrown() != null) {
            sb.append("Exception: ");
            sb.append(record.getThrown().toString());
            sb.append(System.lineSeparator());
            for (StackTraceElement element : record.getThrown().getStackTrace()) {
                sb.append("    at ").append(element.toString());
                sb.append(System.lineSeparator());
            }
        }

        return sb.toString();
    }
}
