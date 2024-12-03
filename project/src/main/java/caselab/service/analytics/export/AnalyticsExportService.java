package caselab.service.analytics.export;

import caselab.service.analytics.AnalyticsService;
import caselab.service.analytics.export.mapper.ExportMapper;
import caselab.service.analytics.export.mapper.LocalDateEntry;
import caselab.service.analytics.export.mapper.StringEntry;
import java.io.ByteArrayOutputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsExportService {
    private final AnalyticsService analyticsService;
    private final ExportMapper exportMapper;
    private final ChartService chartService;

    private void fillAndPlotString(List<StringEntry> entries, XSSFSheet sheet, Plotter plotter) {
        int n = entries.size();

        if (n == 0) {
            return;
        }
        int m = entries.getFirst().getValues().size();

        for (int i = 0; i < n; i++) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(entries.get(i).getCategory());
            for (int j = 0; j < m; j++) {
                row.createCell(j + 1).setCellValue(entries.get(i).getValues().get(j));
            }
        }

        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(
            sheet, new CellRangeAddress(0, n - 1, 0, 0)
        );
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(
            sheet, new CellRangeAddress(0, n - 1, 1, 1)
        );
        plotter.plot(categories, List.of(values));
    }

    private void fillAndPlotLocalDate(List<LocalDateEntry> entries, XSSFSheet sheet, Plotter plotter, XSSFWorkbook wb) {
        int n = entries.size();

        if (n == 0) {
            return;
        }
        int m = entries.getFirst().getValues().size();

        for (int i = 0; i < n; i++) {
            Row row = sheet.createRow(i);
            Cell cell = row.createCell(0);

            CellStyle style = wb.createCellStyle();
            CreationHelper createHelper = wb.getCreationHelper();
            style.setDataFormat(createHelper.createDataFormat().getFormat("d/m/yy"));

            cell.setCellStyle(style);
            cell.setCellValue(entries.get(i).getCategory());
            for (int j = 0; j < m; j++) {
                row.createCell(j + 1).setCellValue(entries.get(i).getValues().get(j));
            }
        }

        XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(
            sheet, new CellRangeAddress(0, n - 1, 0, 0)
        );
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(
            sheet, new CellRangeAddress(0, n - 1, 1, 1)
        );
        plotter.plot(categories, List.of(values));
    }

    @SneakyThrows
    public byte[] getReportDocuments(String period) {
        var res = analyticsService.getReportDocuments(period);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            String title = "Количество созданных документов";
            XSSFSheet sheet = wb.createSheet(title);
            var toMap = res.stream().map(exportMapper::map).toList();
            //CHECKSTYLE:OFF
            String axisName = "Количество документов";
            //CHECKSTYLE:ON
            fillAndPlotLocalDate(toMap, sheet, (category, values) -> chartService.createLineChart(
                sheet.createDrawingPatriarch(),
                category,
                values,
                title,
                "Дата",
                axisName,
                List.of(axisName)
            ), wb);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    @SneakyThrows
    public byte[] getUserSignaturesReport(String period) {
        var res = analyticsService.getUserSignaturesReport(period);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            String title = "Среднее время обработки документов пользователями";
            XSSFSheet sheet = wb.createSheet(title);
            var toMap = res.stream().map(exportMapper::map).toList();
            fillAndPlotString(toMap, sheet, (category, values) -> chartService.createBarChart(
                sheet.createDrawingPatriarch(),
                category,
                values,
                title,
                "Пользователь",
                "Время отклика (мин.)",
                List.of(title)
            ));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    @SneakyThrows
    public byte[] getDocumentTypesReport() {
        var res = analyticsService.getDocumentTypesReport();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            String title = "Среднее время обработки типов документов";
            XSSFSheet sheet = wb.createSheet(title);
            var toMap = res.stream().map(exportMapper::map).toList();
            fillAndPlotString(toMap, sheet, (category, values) -> chartService.createBarChart(
                sheet.createDrawingPatriarch(),
                category,
                values,
                title,
                "Тип документа",
                "Среднее время обработки (мин.)",
                List.of(title)
            ));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    @SneakyThrows
    public byte[] getDocumentTrends(String period) {
        var res = analyticsService.getDocumentTrends(period);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            String title = "Количество подписанных и отклонённых документов";
            XSSFSheet sheet = wb.createSheet(title);
            var toMap = res.stream().map(exportMapper::map).toList();
            fillAndPlotLocalDate(toMap, sheet, (category, values) -> chartService.createLineChart(
                sheet.createDrawingPatriarch(),
                category,
                values,
                title,
                "Неделя",
                "Количество документов ",
                List.of("Подписаны", "Отклонены")
            ), wb);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    @SneakyThrows
    public byte[] getDocumentTypeDistribution() {
        var res = analyticsService.getDocumentTypeDistribution();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            String title = "Распределение типов документов";
            XSSFSheet sheet = wb.createSheet(title);
            var toMap = res.stream().map(exportMapper::map).toList();
            fillAndPlotString(toMap, sheet, (category, values) -> chartService.createPieChart(
                sheet.createDrawingPatriarch(),
                category,
                values,
                title
            ));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    @SneakyThrows
    public byte[] getStageProcessingTimes() {
        var res = analyticsService.getStageProcessingTimes();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            String title = "Среднее время на каждом этапе обработки документа";
            XSSFSheet sheet = wb.createSheet(title);
            var toMap = res.stream().map(exportMapper::map).toList();
            fillAndPlotString(toMap, sheet, (category, values) -> chartService.createBarChart(
                sheet.createDrawingPatriarch(),
                category,
                values,
                title,
                "Этап",
                "Время (мин.)",
                List.of(title)
            ));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    @SneakyThrows
    public byte[] getVotingTimeDistribution() {
        var res = analyticsService.getVotingTimeDistribution();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            String title = "Распределение времени на голосование";
            XSSFSheet sheet = wb.createSheet(title);
            var toMap = res.stream().map(exportMapper::map).toList();
            fillAndPlotString(toMap, sheet, (category, values) -> chartService.createBarChart(
                sheet.createDrawingPatriarch(),
                category,
                values,
                title,
                "Время голосования",
                "Количество документов",
                List.of(title)
            ));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    @SneakyThrows
    public byte[] getSystemLoadByHour() {
        var res = analyticsService.getSystemLoadByHour();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            String title = "Нагрузка на систему по часам";
            XSSFSheet sheet = wb.createSheet(title);
            var toMap = res.stream().map(exportMapper::map).toList();
            fillAndPlotString(toMap, sheet, (category, values) -> chartService.createLineChart(
                sheet.createDrawingPatriarch(),
                category,
                values,
                title,
                "Час",
                "Активность (кол-во документов)",
                List.of(title)
            ));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }
}
