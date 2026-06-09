package com.bizflow.adminreportservice.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.bizflow.adminreportservice.dto.DailySalesDto;
import com.bizflow.adminreportservice.dto.LowStockDto;
import com.bizflow.adminreportservice.dto.SalesOverviewDto;

public final class ReportExportRenderer {

	private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;

	private ReportExportRenderer() {
	}

	public static byte[] renderSales(ExportFormat format, LocalDate from, LocalDate to, SalesOverviewDto overview,
			List<DailySalesDto> daily) throws IOException {
		return switch (format) {
			case XLSX -> buildSalesXlsx(from, to, overview, daily);
			case PDF -> buildSalesPdf(from, to, overview, daily);
			case CSV -> buildSalesCsv(from, to, overview, daily);
		};
	}

	public static byte[] renderLowStock(ExportFormat format, int threshold, int limit, List<LowStockDto> items)
			throws IOException {
		return switch (format) {
			case XLSX -> buildLowStockXlsx(threshold, limit, items);
			case PDF -> buildLowStockPdf(threshold, limit, items);
			case CSV -> buildLowStockCsv(threshold, limit, items);
		};
	}

	private static String nullSafe(LocalDate d) {
		return d == null ? "" : d.format(DATE);
	}

	private static String nullSafe(BigDecimal v) {
		return v == null ? "0" : v.toPlainString();
	}

	private static byte[] buildSalesCsv(LocalDate from, LocalDate to, SalesOverviewDto overview, List<DailySalesDto> daily) {
		StringBuilder csv = new StringBuilder();
		csv.append("from,to,totalOrders,paidOrders,revenue,averageOrderValue\n");
		csv.append(nullSafe(from)).append(',')
				.append(nullSafe(to)).append(',')
				.append(overview.totalOrders()).append(',')
				.append(overview.paidOrders()).append(',')
				.append(nullSafe(overview.revenue())).append(',')
				.append(nullSafe(overview.averageOrderValue())).append("\n\n");

		csv.append("date,orderCount,revenue\n");
		for (DailySalesDto d : daily) {
			csv.append(d.date() != null ? d.date().format(DATE) : "").append(',')
					.append(d.orderCount()).append(',')
					.append(nullSafe(d.revenue())).append('\n');
		}
		return csv.toString().getBytes(StandardCharsets.UTF_8);
	}

	private static byte[] buildLowStockCsv(int threshold, int limit, List<LowStockDto> items) {
		StringBuilder csv = new StringBuilder();
		csv.append("threshold,limit\n");
		csv.append(threshold).append(',').append(limit).append("\n\n");
		csv.append("productId,productName,stock\n");
		for (LowStockDto i : items) {
			csv.append(i.productId()).append(',')
					.append(escapeCsv(i.productName())).append(',')
					.append(i.stock()).append('\n');
		}
		return csv.toString().getBytes(StandardCharsets.UTF_8);
	}

	private static byte[] buildSalesXlsx(LocalDate from, LocalDate to, SalesOverviewDto overview, List<DailySalesDto> daily)
			throws IOException {
		try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sheet = wb.createSheet("Sales");
			int r = 0;
			Row meta = sheet.createRow(r++);
			meta.createCell(0).setCellValue("From");
			meta.createCell(1).setCellValue(from == null ? "" : from.format(DATE));
			meta.createCell(2).setCellValue("To");
			meta.createCell(3).setCellValue(to == null ? "" : to.format(DATE));

			Row sumH = sheet.createRow(r++);
			sumH.createCell(0).setCellValue("Total Orders");
			sumH.createCell(1).setCellValue("Paid Orders");
			sumH.createCell(2).setCellValue("Revenue");
			sumH.createCell(3).setCellValue("Avg Order Value");
			Row sum = sheet.createRow(r++);
			sum.createCell(0).setCellValue(overview.totalOrders());
			sum.createCell(1).setCellValue(overview.paidOrders());
			sum.createCell(2).setCellValue(overview.revenue() == null ? 0d : overview.revenue().doubleValue());
			sum.createCell(3).setCellValue(overview.averageOrderValue() == null ? 0d : overview.averageOrderValue().doubleValue());

			r++; // blank row

			Row header = sheet.createRow(r++);
			header.createCell(0).setCellValue("Date");
			header.createCell(1).setCellValue("Order Count");
			header.createCell(2).setCellValue("Revenue");
			for (DailySalesDto d : daily) {
				Row row = sheet.createRow(r++);
				Cell c0 = row.createCell(0);
				c0.setCellValue(d.date() == null ? "" : d.date().format(DATE));
				row.createCell(1).setCellValue(d.orderCount());
				row.createCell(2).setCellValue(d.revenue() == null ? 0d : d.revenue().doubleValue());
			}

			for (int i = 0; i < 4; i++) sheet.autoSizeColumn(i);
			wb.write(out);
			return out.toByteArray();
		}
	}

	private static byte[] buildLowStockXlsx(int threshold, int limit, List<LowStockDto> items) throws IOException {
		try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sheet = wb.createSheet("Low Stock");
			int r = 0;
			Row meta = sheet.createRow(r++);
			meta.createCell(0).setCellValue("Threshold");
			meta.createCell(1).setCellValue(threshold);
			meta.createCell(2).setCellValue("Limit");
			meta.createCell(3).setCellValue(limit);
			r++;
			Row header = sheet.createRow(r++);
			header.createCell(0).setCellValue("Product ID");
			header.createCell(1).setCellValue("Product Name");
			header.createCell(2).setCellValue("Stock");
			for (LowStockDto i : items) {
				Row row = sheet.createRow(r++);
				row.createCell(0).setCellValue(i.productId() == null ? "" : String.valueOf(i.productId()));
				row.createCell(1).setCellValue(i.productName() == null ? "" : i.productName());
				row.createCell(2).setCellValue(i.stock());
			}
			sheet.autoSizeColumn(0);
			sheet.autoSizeColumn(1);
			sheet.autoSizeColumn(2);
			wb.write(out);
			return out.toByteArray();
		}
	}

	private static byte[] buildSalesPdf(LocalDate from, LocalDate to, SalesOverviewDto overview, List<DailySalesDto> daily)
			throws IOException {
		try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			PDPage page = new PDPage(PDRectangle.A4);
			doc.addPage(page);
			try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
				float margin = 50f;
				float currentY = page.getMediaBox().getHeight() - margin;
				float leading = 14f;
				cs.beginText();
				cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
				cs.newLineAtOffset(margin, currentY);
				cs.showText("Sales Report");
				currentY -= leading * 1.5f;
				cs.newLineAtOffset(0, -leading * 1.5f);
				cs.setFont(PDType1Font.HELVETICA, 11);
				cs.showText("From: " + (from == null ? "" : from.format(DATE)) + "  To: " + (to == null ? "" : to.format(DATE)));
				currentY -= leading;
				cs.newLineAtOffset(0, -leading);
				cs.showText("Total Orders: " + overview.totalOrders() + " | Paid: " + overview.paidOrders());
				currentY -= leading;
				cs.newLineAtOffset(0, -leading);
				cs.showText("Revenue: " + nullSafe(overview.revenue()) + " | Avg: " + nullSafe(overview.averageOrderValue()));
				currentY -= leading * 1.5f;
				cs.newLineAtOffset(0, -leading * 1.5f);
				cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
				cs.showText("Daily");
				currentY -= leading;
				cs.newLineAtOffset(0, -leading);
				cs.setFont(PDType1Font.HELVETICA, 10);
				for (DailySalesDto d : daily) {
					String line = (d.date() == null ? "" : d.date().format(DATE))
							+ " | orders=" + d.orderCount()
							+ " | revenue=" + nullSafe(d.revenue());
					cs.showText(toAscii(line));
					currentY -= leading;
					cs.newLineAtOffset(0, -leading);
					if (currentY < margin) break;
				}
				cs.endText();
			}
			doc.save(out);
			return out.toByteArray();
		}
	}

	private static byte[] buildLowStockPdf(int threshold, int limit, List<LowStockDto> items) throws IOException {
		try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			PDPage page = new PDPage(PDRectangle.A4);
			doc.addPage(page);
			try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
				float margin = 50f;
				float currentY = page.getMediaBox().getHeight() - margin;
				float leading = 14f;
				cs.beginText();
				cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
				cs.newLineAtOffset(margin, currentY);
				cs.showText("Low Stock Report");
				currentY -= leading * 1.5f;
				cs.newLineAtOffset(0, -leading * 1.5f);
				cs.setFont(PDType1Font.HELVETICA, 11);
				cs.showText("Threshold: " + threshold + "  Limit: " + limit);
				currentY -= leading * 1.5f;
				cs.newLineAtOffset(0, -leading * 1.5f);
				cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
				cs.showText("productId | stock | productName");
				currentY -= leading;
				cs.newLineAtOffset(0, -leading);
				cs.setFont(PDType1Font.HELVETICA, 10);
				for (LowStockDto i : items) {
					String line = String.valueOf(i.productId()) + " | " + i.stock() + " | "
							+ (i.productName() == null ? "" : i.productName());
					cs.showText(toAscii(line));
					currentY -= leading;
					cs.newLineAtOffset(0, -leading);
					if (currentY < margin) break;
				}
				cs.endText();
			}
			doc.save(out);
			return out.toByteArray();
		}
	}

	private static String toAscii(String s) {
		if (s == null) return "";
		String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
		String stripped = normalized.replaceAll("\\p{M}+", "");
		return stripped.replace('Đ', 'D').replace('đ', 'd');
	}

	private static String escapeCsv(String s) {
		if (s == null) return "";
		boolean mustQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
		if (!mustQuote) return s;
		return '"' + s.replace("\"", "\"\"") + '"';
	}
}
