package com.reicar.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.reicar.dtos.*;
import com.reicar.entities.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL);
    private static final Color HEADER_BG = new Color(52, 73, 94);
    private static final Color ALT_ROW_BG = new Color(236, 240, 241);

    private final ReportService reportService;

    public byte[] generateRevenueReportPdf(LocalDate startDate, LocalDate endDate) {
        RevenueReportDTO report = reportService.generateRevenueReport(startDate, endDate);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            addTitle(document, "Relatório de Receitas");
            addPeriodInfo(document, startDate, endDate, report.generatedAt());

            addSummarySection(document, report);
            addRevenueByMethodSection(document, report.revenueByMethod());
            addInvoicesSection(document, report);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do relatório de receitas", e);
        }
    }

    public byte[] generateCustomerStatementPdf(Long customerId, LocalDate startDate, LocalDate endDate) {
        CustomerStatementDTO statement = reportService.generateCustomerStatement(customerId, startDate, endDate);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            addTitle(document, "Extrato do Cliente");
            addCustomerInfo(document, statement);
            addPeriodInfo(document, startDate, endDate, statement.generatedAt());

            addStatementSummary(document, statement);
            addStatementInvoices(document, statement);
            addStatementPayments(document, statement);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do extrato do cliente", e);
        }
    }

    private void addTitle(Document document, String title) throws DocumentException {
        Paragraph titleParagraph = new Paragraph(title, TITLE_FONT);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        titleParagraph.setSpacingAfter(20);
        document.add(titleParagraph);
    }

    private void addPeriodInfo(Document document, LocalDate startDate, LocalDate endDate, LocalDate generatedAt) throws DocumentException {
        Paragraph period = new Paragraph(
            String.format("Período: %s a %s | Gerado em: %s",
                startDate.format(DATE_FORMATTER),
                endDate.format(DATE_FORMATTER),
                generatedAt.format(DATE_FORMATTER)),
            NORMAL_FONT
        );
        period.setAlignment(Element.ALIGN_CENTER);
        period.setSpacingAfter(20);
        document.add(period);
    }

    private void addCustomerInfo(Document document, CustomerStatementDTO statement) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        addInfoRow(table, "Cliente:", statement.customerName());
        addInfoRow(table, "Telefone:", statement.customerPhone() != null ? statement.customerPhone() : "-");
        addInfoRow(table, "Localização:", statement.customerCity() + "/" + statement.customerState());

        document.add(table);
    }

    private void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, HEADER_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private void addSummarySection(Document document, RevenueReportDTO report) throws DocumentException {
        Paragraph header = new Paragraph("Resumo", HEADER_FONT);
        header.setSpacingBefore(10);
        header.setSpacingAfter(10);
        document.add(header);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingAfter(20);

        addSummaryRow(table, "Total Faturado:", formatCurrency(report.totalInvoiced()));
        addSummaryRow(table, "Total Recebido:", formatCurrency(report.totalReceived()));
        addSummaryRow(table, "Saldo Pendente:", formatCurrency(report.outstandingBalance()));
        addSummaryRow(table, "Total de Faturas:", String.valueOf(report.invoiceCount()));
        addSummaryRow(table, "Faturas Pagas:", String.valueOf(report.paidInvoiceCount()));
        addSummaryRow(table, "Faturas Pendentes:", String.valueOf(report.unpaidInvoiceCount()));
        addSummaryRow(table, "Faturas Parciais:", String.valueOf(report.partialInvoiceCount()));

        document.add(table);
    }

    private void addStatementSummary(Document document, CustomerStatementDTO statement) throws DocumentException {
        Paragraph header = new Paragraph("Resumo Financeiro", HEADER_FONT);
        header.setSpacingBefore(10);
        header.setSpacingAfter(10);
        document.add(header);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingAfter(20);

        addSummaryRow(table, "Total Faturado:", formatCurrency(statement.totalInvoiced()));
        addSummaryRow(table, "Total Pago:", formatCurrency(statement.totalPaid()));
        addSummaryRow(table, "Saldo Devedor:", formatCurrency(statement.balance()));

        document.add(table);
    }

    private void addSummaryRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL_FONT));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setBorderColor(Color.LIGHT_GRAY);
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addRevenueByMethodSection(Document document, Map<PaymentMethod, BigDecimal> revenueByMethod) throws DocumentException {
        Paragraph header = new Paragraph("Receitas por Método de Pagamento", HEADER_FONT);
        header.setSpacingBefore(10);
        header.setSpacingAfter(10);
        document.add(header);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingAfter(20);

        for (Map.Entry<PaymentMethod, BigDecimal> entry : revenueByMethod.entrySet()) {
            addSummaryRow(table, entry.getKey().getDisplayName(), formatCurrency(entry.getValue()));
        }

        document.add(table);
    }

    private void addInvoicesSection(Document document, RevenueReportDTO report) throws DocumentException {
        Paragraph header = new Paragraph("Faturas", HEADER_FONT);
        header.setSpacingBefore(10);
        header.setSpacingAfter(10);
        document.add(header);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{15, 25, 15, 15, 15, 15});

        addTableHeader(table, "Número", "Cliente", "Data", "Valor", "Pago", "Status");

        int row = 0;
        for (InvoiceDTO invoice : report.invoices()) {
            boolean altRow = row % 2 == 1;
            addInvoiceRow(table, invoice, altRow);
            row++;
        }

        document.add(table);
    }

    private void addStatementInvoices(Document document, CustomerStatementDTO statement) throws DocumentException {
        Paragraph header = new Paragraph("Faturas", HEADER_FONT);
        header.setSpacingBefore(10);
        header.setSpacingAfter(10);
        document.add(header);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{20, 20, 20, 20, 20});

        addTableHeader(table, "Número", "Data", "Valor", "Pago", "Status");

        int row = 0;
        for (InvoiceDTO invoice : statement.invoices()) {
            boolean altRow = row % 2 == 1;
            addCell(table, invoice.invoiceNumber(), altRow);
            addCell(table, invoice.issueDate().format(DATE_FORMATTER), altRow);
            addCell(table, formatCurrency(invoice.totalValue()), altRow);
            addCell(table, formatCurrency(invoice.paidAmount()), altRow);
            addCell(table, translateStatus(invoice.status().name()), altRow);
            row++;
        }

        document.add(table);
    }

    private void addStatementPayments(Document document, CustomerStatementDTO statement) throws DocumentException {
        if (statement.payments().isEmpty()) {
            return;
        }

        Paragraph header = new Paragraph("Pagamentos", HEADER_FONT);
        header.setSpacingBefore(20);
        header.setSpacingAfter(10);
        document.add(header);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{25, 25, 25, 25});

        addTableHeader(table, "Data", "Fatura", "Valor", "Método");

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        int row = 0;
        for (PaymentDTO payment : statement.payments()) {
            boolean altRow = row % 2 == 1;
            addCell(table, payment.paymentDate().format(dateTimeFormatter), altRow);
            addCell(table, payment.invoiceNumber(), altRow);
            addCell(table, formatCurrency(payment.amount()), altRow);
            addCell(table, payment.paymentMethodDisplay(), altRow);
            row++;
        }

        document.add(table);
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
            cell.setBackgroundColor(HEADER_BG);
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addInvoiceRow(PdfPTable table, InvoiceDTO invoice, boolean altRow) {
        addCell(table, invoice.invoiceNumber(), altRow);
        addCell(table, invoice.customerName(), altRow);
        addCell(table, invoice.issueDate().format(DATE_FORMATTER), altRow);
        addCell(table, formatCurrency(invoice.totalValue()), altRow);
        addCell(table, formatCurrency(invoice.paidAmount()), altRow);
        addCell(table, translateStatus(invoice.status().name()), altRow);
    }

    private void addCell(PdfPTable table, String text, boolean altRow) {
        PdfPCell cell = new PdfPCell(new Phrase(text, SMALL_FONT));
        if (altRow) {
            cell.setBackgroundColor(ALT_ROW_BG);
        }
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private String formatCurrency(BigDecimal value) {
        return String.format("R$ %.2f", value != null ? value : BigDecimal.ZERO);
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "PAID" -> "Pago";
            case "UNPAID" -> "Pendente";
            case "PARTIAL" -> "Parcial";
            case "CANCELLED" -> "Cancelado";
            default -> status;
        };
    }
}
