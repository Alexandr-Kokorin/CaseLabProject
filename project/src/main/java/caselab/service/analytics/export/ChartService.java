package caselab.service.analytics.export;

import java.util.List;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.springframework.stereotype.Service;

@Service
public class ChartService {
    private XSSFChart createChart(XSSFDrawing drawing, String title, int n) {
        //CHECKSTYLE:OFF
        int width = 9;
        int height = 20;
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, n + 1, 0, n + width, height);
        //CHECKSTYLE:ON

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title);
        chart.setTitleOverlay(false);
        return chart;
    }

    public void createLineChart(
        XSSFDrawing drawing,
        XDDFDataSource<?> category,
        List<XDDFNumericalDataSource<?>> values,
        String title,
        String bottomAxisName,
        String leftAxisName,
        List<String> seriesName
    ) {
        int n = values.size();
        XSSFChart chart = createChart(drawing, title, n);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle(bottomAxisName);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle(leftAxisName);

        XDDFChartData data = chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
        data.setVaryColors(true);

        for (int i = 0; i < n; i++) {
            XDDFChartData.Series series = data.addSeries(category, values.get(i));
            series.setTitle(seriesName.get(i));
        }
        chart.plot(data);
    }

    public void createPieChart(
        XSSFDrawing drawing,
        XDDFDataSource<?> category,
        List<XDDFNumericalDataSource<?>> values,
        String title
    ) {
        int n = values.size();
        XSSFChart chart = createChart(drawing, title, n);

        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);

        XDDFChartData data = chart.createData(ChartTypes.PIE, null, null);
        data.setVaryColors(true);
        for (int i = 0; i < n; ++i) {
            data.addSeries(category, values.get(i));
        }
        chart.plot(data);
    }

    public void createBarChart(
        XSSFDrawing drawing,
        XDDFDataSource<?> category,
        List<XDDFNumericalDataSource<?>> values,
        String title,
        String bottomAxisName,
        String leftAxisName,
        List<String> seriesName
    ) {
        int n = values.size();
        XSSFChart chart = createChart(drawing, title, n);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle(bottomAxisName);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle(leftAxisName);

        XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        data.setVaryColors(false);
        data.setBarDirection(BarDirection.COL);
        for (int i = 0; i < n; ++i) {
            var series = data.addSeries(category, values.get(i));
            series.setTitle(seriesName.get(i));
        }
        chart.plot(data);
    }
}
