// Report service
package com.procurement.procurement.service.report;

import com.procurement.procurement.dto.report.ReportRequestDTO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    // ===================== Common Jasper Generator =====================
    public JasperPrint generateReport(String reportPath, List<?> data, Map<String, Object> parameters)
            throws JRException {

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);

        Map<String, Object> mutableParameters = (parameters == null) ? new HashMap<>() : new HashMap<>(parameters);

        InputStream reportStream = getClass().getResourceAsStream(reportPath);
        if (reportStream == null) {
            throw new JRException("Report template not found at: " + reportPath);
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        return JasperFillManager.fillReport(jasperReport, mutableParameters, dataSource);
    }

    // ===================== Export PDF =====================
    public byte[] exportReportToPdf(JasperPrint jasperPrint) throws JRException {
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    // ===================== Export Excel =====================
    public byte[] exportReportToExcel(JasperPrint jasperPrint) throws JRException {
        JRXlsxExporter exporter = new JRXlsxExporter();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        exporter.exportReport();
        return outputStream.toByteArray();
    }

    // ===================== Vendor Report Generator =====================
    public byte[] generateVendorReport(ReportRequestDTO request, String format) {
        try {
            // 🔹 Dummy data for now (replace with DB later)
            List<Map<String, Object>> data = List.of(
                    Map.of("vendorName", "ABC Traders", "rating", 4.5, "status", "ACTIVE"),
                    Map.of("vendorName", "XYZ Supplies", "rating", 4.2, "status", "ACTIVE"));

            JasperPrint jasperPrint = generateReport(
                    "/jasper/vendor_report.jrxml",
                    data,
                    Map.of("title", "Vendor Report"));

            if ("excel".equalsIgnoreCase(format)) {
                return exportReportToExcel(jasperPrint);
            }
            return exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace for debugging
            throw new RuntimeException("Report generation failed: " + e.getMessage(), e);
        }
    }
}
