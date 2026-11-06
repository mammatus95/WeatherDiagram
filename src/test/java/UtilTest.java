import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UtilTest {

    @Test
    void testNumberWithDecimalPoint() {
        assertEquals(12.5, Util.parseDouble("12.5"), 1e-9);
    }

    @Test
    void testNumberWithDecimalComma() {
        assertEquals(12.5, Util.parseDouble("12,5"), 1e-9);
    }

    @Test
    void testTrimsWhitespace() {
        assertEquals(12.5, Util.parseDouble("  12.5  "), 1e-9);
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(NumberFormatException.class,
                () -> Util.parseDouble("not a number"));
    }

    @Test
    void rejectsNullInput() {
        assertThrows(NullPointerException.class,
                () -> Util.parseDouble(null));
    }
}
