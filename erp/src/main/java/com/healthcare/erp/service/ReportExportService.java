package com.healthcare.erp.service;

import com.healthcare.erp.dto.report.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Generates downloadable PDF and Excel reports from existing report DTOs.
 */
@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final ReportService reportService;

    // ────────────────────── EXCEL EXPORTS ──────────────────────

    public byte[] exportDashboardExcel(UUID hospitalId) throws IOException {
        DashboardSummaryDTO d = reportService.getDashboardSummary(hospitalId);
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("Dashboard");
            CellStyle header = createHeaderStyle(wb);

            String[][] data = {
                    {"Metric", "Value"},
                    {"Total Patients", String.valueOf(d.totalPatients())},
                    {"Active Patients", String.valueOf(d.activePatients())},
                    {"Total Doctors", String.valueOf(d.totalDoctors())},
                    {"Total Staff", String.valueOf(d.totalStaff())},
                    {"Total Appointments", String.valueOf(d.totalAppointments())},
                    {"Pending Appointments", String.valueOf(d.pendingAppointments())},
                    {"Completed Appointments", String.valueOf(d.completedAppointments())},
                    {"Cancelled Appointments", String.valueOf(d.cancelledAppointments())},
                    {"Total Revenue", d.totalRevenue().toPlainString()},
                    {"Total Paid", d.totalPaid().toPlainString()},
                    {"Outstanding", d.totalOutstanding().toPlainString()},
                    {"Low Stock Medicines", String.valueOf(d.lowStockMedicines())},
                    {"Total Prescriptions", String.valueOf(d.totalPrescriptions())},
                    {"Total Invoices", String.valueOf(d.totalInvoices())}
            };

            for (int i = 0; i < data.length; i++) {
                XSSFRow row = sheet.createRow(i);
                for (int j = 0; j < data[i].length; j++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(j);
                    cell.setCellValue(data[i][j]);
                    if (i == 0) cell.setCellStyle(header);
                }
            }
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            wb.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportRevenueExcel(UUID hospitalId) throws IOException {
        RevenueReportDTO r = reportService.getRevenueReport(hospitalId);
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("Revenue");
            CellStyle header = createHeaderStyle(wb);

            int rowNum = 0;
            XSSFRow titleRow = sheet.createRow(rowNum++);
            titleRow.createCell(0).setCellValue("Revenue Report");

            rowNum++; // blank row

            String[][] summary = {
                    {"Metric", "Amount"},
                    {"Total Revenue", r.totalRevenue().toPlainString()},
                    {"Total GST", r.totalGst().toPlainString()},
                    {"Total Paid", r.totalPaid().toPlainString()},
                    {"Outstanding", r.totalOutstanding().toPlainString()},
                    {"Insurance Claimed", r.totalClaimedAmount().toPlainString()},
                    {"Insurance Approved", r.totalApprovedAmount().toPlainString()}
            };

            int headerRowIdx = rowNum;
            for (String[] datum : summary) {
                XSSFRow row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(datum[0]);
                row.createCell(1).setCellValue(datum[1]);
                if (rowNum == headerRowIdx + 1) {
                    row.getCell(0).setCellStyle(header);
                    row.getCell(1).setCellStyle(header);
                }
            }

            rowNum++; // blank row

            // Invoice breakdown
            XSSFRow invHeader = sheet.createRow(rowNum++);
            invHeader.createCell(0).setCellValue("Invoice Status");
            invHeader.createCell(1).setCellValue("Count");
            invHeader.getCell(0).setCellStyle(header);
            invHeader.getCell(1).setCellStyle(header);

            for (Map.Entry<String, Long> e : r.invoicesByStatus().entrySet()) {
                XSSFRow row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(e.getKey());
                row.createCell(1).setCellValue(e.getValue());
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            wb.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportDoctorWorkloadExcel(UUID hospitalId) throws IOException {
        List<DoctorWorkloadDTO> docs = reportService.getDoctorWorkloads(hospitalId);
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("Doctor Workload");
            CellStyle header = createHeaderStyle(wb);

            String[] headers = {"Doctor", "Specialization", "Total Appts", "Completed", "Cancelled", "Prescriptions"};
            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(header);
            }

            int rowNum = 1;
            for (DoctorWorkloadDTO doc : docs) {
                XSSFRow row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(doc.doctorName());
                row.createCell(1).setCellValue(doc.specialization());
                row.createCell(2).setCellValue(doc.totalAppointments());
                row.createCell(3).setCellValue(doc.completedAppointments());
                row.createCell(4).setCellValue(doc.cancelledAppointments());
                row.createCell(5).setCellValue(doc.totalPrescriptions());
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ────────────────────── PDF EXPORTS ──────────────────────

    public byte[] exportDashboardPdf(UUID hospitalId) {
        DashboardSummaryDTO d = reportService.getDashboardSummary(hospitalId);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(41, 65, 122));
            Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
            Font bodyFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

            doc.add(new Paragraph("Dashboard Summary", titleFont));
            doc.add(new Paragraph("Generated: " + LocalDate.now(), bodyFont));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(80);
            table.setWidths(new float[]{3, 2});

            addPdfHeaderCell(table, "Metric", headerFont);
            addPdfHeaderCell(table, "Value", headerFont);

            addPdfRow(table, "Total Patients", String.valueOf(d.totalPatients()), bodyFont);
            addPdfRow(table, "Active Patients", String.valueOf(d.activePatients()), bodyFont);
            addPdfRow(table, "Total Doctors", String.valueOf(d.totalDoctors()), bodyFont);
            addPdfRow(table, "Total Staff", String.valueOf(d.totalStaff()), bodyFont);
            addPdfRow(table, "Total Appointments", String.valueOf(d.totalAppointments()), bodyFont);
            addPdfRow(table, "Pending", String.valueOf(d.pendingAppointments()), bodyFont);
            addPdfRow(table, "Completed", String.valueOf(d.completedAppointments()), bodyFont);
            addPdfRow(table, "Cancelled", String.valueOf(d.cancelledAppointments()), bodyFont);
            addPdfRow(table, "Total Revenue", "₹" + d.totalRevenue().toPlainString(), bodyFont);
            addPdfRow(table, "Total Paid", "₹" + d.totalPaid().toPlainString(), bodyFont);
            addPdfRow(table, "Outstanding", "₹" + d.totalOutstanding().toPlainString(), bodyFont);
            addPdfRow(table, "Low Stock Medicines", String.valueOf(d.lowStockMedicines()), bodyFont);
            addPdfRow(table, "Total Prescriptions", String.valueOf(d.totalPrescriptions()), bodyFont);
            addPdfRow(table, "Total Invoices", String.valueOf(d.totalInvoices()), bodyFont);

            doc.add(table);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    public byte[] exportRevenuePdf(UUID hospitalId) {
        RevenueReportDTO r = reportService.getRevenueReport(hospitalId);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(41, 65, 122));
            Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
            Font bodyFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD);

            doc.add(new Paragraph("Revenue Report", titleFont));
            doc.add(new Paragraph("Generated: " + LocalDate.now(), bodyFont));
            doc.add(new Paragraph(" "));

            PdfPTable summary = new PdfPTable(2);
            summary.setWidthPercentage(80);
            addPdfHeaderCell(summary, "Metric", headerFont);
            addPdfHeaderCell(summary, "Amount", headerFont);
            addPdfRow(summary, "Total Revenue", "₹" + r.totalRevenue().toPlainString(), bodyFont);
            addPdfRow(summary, "Total GST", "₹" + r.totalGst().toPlainString(), bodyFont);
            addPdfRow(summary, "Total Paid", "₹" + r.totalPaid().toPlainString(), bodyFont);
            addPdfRow(summary, "Outstanding", "₹" + r.totalOutstanding().toPlainString(), bodyFont);
            addPdfRow(summary, "Insurance Claimed", "₹" + r.totalClaimedAmount().toPlainString(), bodyFont);
            addPdfRow(summary, "Insurance Approved", "₹" + r.totalApprovedAmount().toPlainString(), bodyFont);
            doc.add(summary);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Invoice Breakdown", sectionFont));
            doc.add(new Paragraph(" "));

            PdfPTable invoices = new PdfPTable(2);
            invoices.setWidthPercentage(60);
            addPdfHeaderCell(invoices, "Status", headerFont);
            addPdfHeaderCell(invoices, "Count", headerFont);
            for (Map.Entry<String, Long> e : r.invoicesByStatus().entrySet()) {
                addPdfRow(invoices, e.getKey(), String.valueOf(e.getValue()), bodyFont);
            }
            doc.add(invoices);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    public byte[] exportDoctorWorkloadPdf(UUID hospitalId) {
        List<DoctorWorkloadDTO> docs = reportService.getDoctorWorkloads(hospitalId);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(41, 65, 122));
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font bodyFont = new Font(Font.HELVETICA, 9, Font.NORMAL);

            doc.add(new Paragraph("Doctor Workload Report", titleFont));
            doc.add(new Paragraph("Generated: " + LocalDate.now(), bodyFont));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 2, 1.5f, 1.5f, 1.5f, 1.5f});

            String[] headers = {"Doctor", "Specialization", "Total Appts", "Completed", "Cancelled", "Prescriptions"};
            for (String h : headers) addPdfHeaderCell(table, h, headerFont);

            for (DoctorWorkloadDTO dw : docs) {
                addPdfBodyCell(table, dw.doctorName(), bodyFont);
                addPdfBodyCell(table, dw.specialization(), bodyFont);
                addPdfBodyCell(table, String.valueOf(dw.totalAppointments()), bodyFont);
                addPdfBodyCell(table, String.valueOf(dw.completedAppointments()), bodyFont);
                addPdfBodyCell(table, String.valueOf(dw.cancelledAppointments()), bodyFont);
                addPdfBodyCell(table, String.valueOf(dw.totalPrescriptions()), bodyFont);
            }

            doc.add(table);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    // ────────────────────── HELPERS ──────────────────────

    private CellStyle createHeaderStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void addPdfHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(41, 65, 122));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addPdfRow(PdfPTable table, String label, String value, Font font) {
        table.addCell(new Phrase(label, font));
        table.addCell(new Phrase(value, font));
    }

    private void addPdfBodyCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4);
        table.addCell(cell);
    }
}
