import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Displays temperature and mixing ratio over time.
 * <p>
 * Left axis: temperature in degrees Celsius
 * Right axis: relative humidity in %
 * </p>
 * Input timestamps are interpreted as UTC.
 */
public class GUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private final List<Meteo.Measurement> measurements;

    private final XYLineAndShapeRenderer tempRenderer =
            new XYLineAndShapeRenderer(true, false);

    private final XYLineAndShapeRenderer rhRenderer =
            new XYLineAndShapeRenderer(true, false);

    private final XYLineAndShapeRenderer tdRenderer =
            new XYLineAndShapeRenderer(true, false);

    public GUI(List<Meteo.Measurement> measurements) {
        super("Weather Time Series");

        this.measurements = List.copyOf(
                Objects.requireNonNull(measurements, "measurements"));

        if (this.measurements.isEmpty()) {
            throw new IllegalArgumentException("No measurements supplied.");
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 5));

        add(buildChartPanel(), BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.NORTH);

        setSize(1000, 600);
        setLocationRelativeTo(null);
    }

    private JPanel buildButtonBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JToggleButton tempButton =
                new JToggleButton("Temperature", true);
        tempButton.setForeground(Util.TEMP_COLOR);
        tempButton.addActionListener(e ->
                tempRenderer.setSeriesVisible(
                        0, tempButton.isSelected()));

        JToggleButton rhButton =
                new JToggleButton("Relative Humidity", true);
        rhButton.setForeground(Util.RH_COLOR);
        rhButton.addActionListener(e ->
                rhRenderer.setSeriesVisible(
                        0, rhButton.isSelected()));

        JToggleButton tdButton =
                new JToggleButton("Dew Point", true);
        tdButton.setForeground(Util.TD_COLOR);
        tdButton.addActionListener(e ->
                tdRenderer.setSeriesVisible(
                        0, tdButton.isSelected()));

        panel.add(tempButton);
        panel.add(rhButton);
        panel.add(tdButton);
        return panel;
    }

    private ChartPanel buildChartPanel() {
        XYSeriesCollection[] datasets = buildDatasets();

        XYSeriesCollection tempDataset = datasets[0];
        XYSeriesCollection rhDataset = datasets[1];
        XYSeriesCollection tdDataset = datasets[2];

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Weather Diagram",
                "Time (UTC)",
                "Temperature (deg C)",
                tempDataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        XYPlot plot = chart.getXYPlot();

        // Dataset 0 -> temperature axis on the left.
        NumberAxis tempAxis =
                new NumberAxis("Temperature (deg C)");
        applyRange(tempAxis, tempDataset.getSeries(0));

        plot.setRangeAxis(0, tempAxis);
        plot.setRangeAxisLocation(
                0, AxisLocation.BOTTOM_OR_LEFT);

        tempRenderer.setSeriesPaint(0, Util.TEMP_COLOR);
        tempRenderer.setSeriesStroke(0, new BasicStroke(2f));

        plot.setRenderer(0, tempRenderer);
        plot.mapDatasetToRangeAxis(0, 0);

        // Dataset 1 -> rh axis on the right.
        NumberAxis rhAxis =
                new NumberAxis("Relative Humidity ( % )");
        applyRange(rhAxis, rhDataset.getSeries(0));

        plot.setRangeAxis(1, rhAxis);
        plot.setRangeAxisLocation(
                1, AxisLocation.BOTTOM_OR_RIGHT);

        rhRenderer.setSeriesPaint(0, Util.RH_COLOR);
        rhRenderer.setSeriesStroke(0, new BasicStroke(2f));

        plot.setDataset(1, rhDataset);
        plot.setRenderer(1, rhRenderer);
        plot.mapDatasetToRangeAxis(1, 1);

        // Dataset 2 -> dew point axis on the left.
        tdRenderer.setSeriesPaint(0, Util.TD_COLOR);
        tdRenderer.setSeriesStroke(0, new BasicStroke(2f));

        plot.setDataset(2, tdDataset);
        plot.setRenderer(2, tdRenderer);
        plot.mapDatasetToRangeAxis(2, 0);

        // Both datasets share the UTC time axis.
        DateAxis timeAxis = new DateAxis("Time (UTC)");
        timeAxis.setTimeZone(TimeZone.getTimeZone("UTC"));
        plot.setDomainAxis(timeAxis);

        chart.setBackgroundPaint(Color.WHITE);

        return new ChartPanel(chart);
    }

    /**
     * Creates one dataset per range axis.
     * <p>
     * NaN values represent missing data and break the lines.
     * Timestamps must be strictly increasing and distinct at
     * millisecond precision.
     * </p>
     */
    private XYSeriesCollection[] buildDatasets() {
        XYSeries tempSeries =
                new XYSeries("Temperature", true, false);
        XYSeries rhSeries =
                new XYSeries("Relative Humidity", true, false);
        XYSeries tdSeries =
                new XYSeries("Dew Point", true, false);

        LocalDateTime previousTime = null;
        long previousMillis = 0;

        for (Meteo.Measurement measurement : measurements) {
            LocalDateTime time = Objects.requireNonNull(
                    measurement.time(), "Measurement timestamp");

            if (previousTime != null
                    && !time.isAfter(previousTime)) {
                throw new IllegalArgumentException(
                        "Duplicate or non-increasing timestamp: " + time);
            }

            long millis = time.toInstant(ZoneOffset.UTC).toEpochMilli();

            if (previousTime != null && millis <= previousMillis) {
                throw new IllegalArgumentException(
                        "Timestamps must be distinct at millisecond "
                                + "precision: " + time);
            }

            double temperature = measurement.temperature();
            double relativehumidity = measurement.relativeHumidity();
            double dewPoint = measurement.dewPoint();

            if (Double.isInfinite(temperature)
                    || Double.isInfinite(relativehumidity) || Double.isInfinite(dewPoint)) {
                throw new IllegalArgumentException(
                        "Infinite value at " + time);
            }

            if (relativehumidity < 0) {
                throw new IllegalArgumentException(
                        "Negative relative humidity at " + time);
            }

            if (temperature < dewPoint) {
                throw new IllegalArgumentException(
                        "dew point greater then temperature at " + time);
            }

            tempSeries.add(millis, temperature);
            rhSeries.add(millis, relativehumidity);
            tdSeries.add(millis, dewPoint);

            previousTime = time;
            previousMillis = millis;
        }

        return new XYSeriesCollection[] {
                new XYSeriesCollection(tempSeries),
                new XYSeriesCollection(rhSeries),
                new XYSeriesCollection(tdSeries)
        };
    }

    /**
     * Sets a fixed range so toggling a series does not
     * automatically rescale its axis.
     */
    private static void applyRange(NumberAxis axis, XYSeries series) {
        double min = series.getMinY();
        double max = series.getMaxY();

        // Fallback when every value in the series is missing.
        if (!Double.isFinite(min) || !Double.isFinite(max)) {
            axis.setRange(0.0, 1.0);
            return;
        }

        double span = max - min;
        double padding = Math.max(span * 0.05, 1.1);

        axis.setRange(min - padding, max + padding);
    }


}
