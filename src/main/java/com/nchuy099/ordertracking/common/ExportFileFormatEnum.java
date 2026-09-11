package com.nchuy099.ordertracking.common;

import java.util.Locale;

public enum ExportFileFormatEnum {
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    CSV("text/csv; charset=UTF-8", "csv");

    private final String contentType;
    private final String extension;

    ExportFileFormatEnum(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String getContentType() {
        return contentType;
    }

    public String getExtension() {
        return extension;
    }

    public static ExportFileFormatEnum fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return ExportFileFormatEnum.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
