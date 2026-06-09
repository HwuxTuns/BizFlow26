package com.bizflow.adminreportservice.export;

import java.util.Locale;

public enum ExportFormat {
	CSV("csv", "text/csv; charset=utf-8"),
	XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
	PDF("pdf", "application/pdf");

	private final String extension;
	private final String contentType;

	ExportFormat(String extension, String contentType) {
		this.extension = extension;
		this.contentType = contentType;
	}

	public String extension() {
		return extension;
	}

	public String contentType() {
		return contentType;
	}

	public static ExportFormat parse(String format) {
		String f = format == null ? "csv" : format.trim().toLowerCase(Locale.ROOT);
		if (f.equals("excel") || f.equals("xls")) return XLSX;
		if (f.equals("xlsx")) return XLSX;
		if (f.equals("pdf")) return PDF;
		return CSV;
	}
}
