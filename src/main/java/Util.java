import java.awt.*;

public final class Util {
    private Util() {
        /* This utility class should not be instantiated */
    }

    public static final Color TEMP_COLOR = Color.RED;
    public static final Color MR_COLOR = Color.BLUE;
    public static final Color RH_COLOR = Color.GREEN;
    public static final Color TD_COLOR = Color.BLUE;
    public static final Color PP_COLOR = Color.BLACK;


    /**
     * Parses a numeric string, accepting comma as decimal separator.
     */
    public static double parseDouble(String text) {
        return Double.parseDouble(text.trim().replace(',', '.'));
    }
}
