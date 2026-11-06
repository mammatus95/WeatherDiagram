import java.time.LocalDateTime;

public final class Meteo {
    private Meteo() {
        /* This utility class should not be instantiated */
    }

    /** approximation of the specific gas constant of dry air J/(kg*K) */
    public static final double RD = 286.90;
    /** approximation of the specific gas constant of water vapor J/(kg*K) */
    public static final double RV = 461.27;
    /** ratio of specific gas constants of dry air and water vapor (mixing ratio factor) */
    public static final double RD_OVER_RV = RD / RV;

    /**
     * Calculate the vapor pressure.
     * @param mixingRatio in kg/kg
     * @param pressure in hPa
     * @return vapor pressure in hPa
     */
    public static double mixingRatio2VaporPressure(double mixingRatio, double pressure) {
        return (pressure*100f * mixingRatio / (mixingRatio + RD_OVER_RV))/100f;
    }

    /**
     * Calculate the saturation vapor pressure using the Magnus approximation over liquid water.
     * @param temperature in Celsius
     * @return saturation vapor pressure in hPa
     */
    public static double saturationPressure(double temperature) {
        return 6.112 * Math.exp((17.62 * temperature) / (243.12 + temperature));
    }

    /** One measurement record from a single CSV row. */
    public record Measurement(
            LocalDateTime time,
            double pressure,
            double temperature,
            double mixingRatio) {

        /**
         * Calculate the relative humidity.
         * @return relative humidity in percent
         */
        public double relativeHumidity() {
            return 100.0 * mixingRatio2VaporPressure(mixingRatio, pressure) / saturationPressure(temperature);
        }

        /**
         * Calculate the dew point.
         * @return dew point in C
         */
        public double dewPoint() {
            double x = Math.log(mixingRatio2VaporPressure(mixingRatio, pressure) / 6.112);
            return 243.5 * x / (17.67 - x);
        }
    }
}
