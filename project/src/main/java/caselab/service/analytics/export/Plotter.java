package caselab.service.analytics.export;

import java.util.List;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;

interface Plotter {
    void plot(XDDFDataSource<?> category, List<XDDFNumericalDataSource<? extends Number>> values);
}
