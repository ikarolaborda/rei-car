package com.reicar.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.reicar.entities.MechanicServiceOrder; // Importação para verificação de tipo
import com.reicar.entities.ServiceItem;
import com.reicar.entities.ServiceOrder;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PdfGeneratorService {

    public void export(HttpServletResponse response, ServiceOrder order) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Estilos de Fonte
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        // Cabeçalho
        Paragraph title = new Paragraph("RELATÓRIO DE ORDEM DE SERVIÇO", fontTitle);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("OS Nº: " + order.getOrderNumber()));
        document.add(new Paragraph("Cliente: " + order.getCustomer().getName()));
        document.add(new Paragraph("Data: " + order.getEntryDate()));
        document.add(new Paragraph("Setor: " + (order instanceof MechanicServiceOrder ? "Mecânica" : "Borracharia")));
        document.add(new Paragraph("--------------------------------------------------"));

        // Tabela de Itens
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        table.addCell(new Phrase("Quant.", fontLabel));
        table.addCell(new Phrase("Descrição", fontLabel));
        table.addCell(new Phrase("Preço Unit.", fontLabel));
        table.addCell(new Phrase("Total (c/ Markup)", fontLabel));

        // Define markup baseado no tipo
        BigDecimal markup = (order instanceof MechanicServiceOrder) ? new BigDecimal("1.30") : BigDecimal.ONE;

        for (ServiceItem item : order.getItems()) {
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(item.getDescription());
            table.addCell("R$ " + item.getUnitPrice());

            BigDecimal lineTotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .multiply(markup);

            table.addCell("R$ " + lineTotal.setScale(2, RoundingMode.HALF_UP));
        }
        document.add(table);

        // Resumo Financeiro
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Valor Mão de Obra: R$ " + order.getServiceValue().setScale(2, RoundingMode.HALF_UP)));

        Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph total = new Paragraph("VALOR TOTAL FINAL: R$ " + order.getTotalValue(), fontTotal);
        total.setSpacingBefore(10f);
        document.add(total);

        document.close();
    }
}