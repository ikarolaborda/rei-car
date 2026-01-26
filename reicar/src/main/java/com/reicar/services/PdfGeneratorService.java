package com.reicar.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
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

        // Cabeçalho com Estilo Reicar
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("RELATÓRIO DE ORDEM DE SERVIÇO", fontTitle);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("OS Nº: " + order.getOrderNumber()));
        document.add(new Paragraph("Cliente: " + order.getCustomer().getName()));
        document.add(new Paragraph("Data: " + order.getEntryDate()));
        document.add(new Paragraph("--------------------------------------------------"));

        // Tabela de Itens (Peças)
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.addCell("Quant.");
        table.addCell("Descrição");
        table.addCell("Preço Unit.");
        table.addCell("Total (c/ 30%)");

        for (ServiceItem item : order.getItems()) {
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(item.getDescription());
            table.addCell("R$ " + item.getUnitPrice());
            // Mostra o valor já com o markup aplicado no service
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())).multiply(new BigDecimal("1.30"));
            table.addCell("R$ " + lineTotal.setScale(2, RoundingMode.HALF_UP));
        }
        document.add(table);

        // Resumo Financeiro com destaque para Mão de Obra
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Valor Mão de Obra: R$ " + order.getServiceValue()));

        Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph total = new Paragraph("VALOR TOTAL FINAL: R$ " + order.getTotalValue(), fontTotal);
        document.add(total);

        document.close();
    }
}
