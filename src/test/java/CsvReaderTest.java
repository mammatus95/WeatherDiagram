import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Unit tests for CsvReader.read(Path). */
class CsvReaderTest {

    @TempDir
    Path tempDir;

    /** Helper: writes a CSV file into the temp dir and returns its path. */
    private Path csv(String... lines) throws IOException {
        Path file = tempDir.resolve("test.csv");
        Files.write(file, List.of(lines));
        return file;
    }

    @Test
    void testParsesValidRows() throws Exception {
        Path file = csv(
                "Time;Temperature;MixingRatio",
                "2026-01-01T12:00:00;20.5;0.008",
                "2026-01-01T13:00:00;21.2;0.009"
        );

        List<Meteo.Measurement> result = CsvReader.read(file);

        assertEquals(2, result.size());

        Meteo.Measurement first = result.getFirst();
        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 0), first.time());
        assertEquals(20.5, first.temperature(), 1e-9);
        assertEquals(0.008, first.mixingRatio(), 1e-9);
    }

    @Test
    void testDecimalSeparator() throws Exception {
        Path file = csv(
                "Time;Temperature;MixingRatio",
                "2026-01-01T12:00:00;20,5;0,008"
        );

        List<Meteo.Measurement> result = CsvReader.read(file);

        assertEquals(2, result.size());

        Meteo.Measurement first = result.getFirst();
        assertEquals(LocalDateTime.of(2026, 1, 1, 12, 0), first.time());
        assertEquals(20.5, first.temperature(), 1e-9);
        assertEquals(0.008, first.mixingRatio(), 1e-9);
    }

    @Test
    void testSkipsBlankLines() throws Exception {
        Path file = csv(
                "Time;Temperature;MixingRatio",
                "",
                "2026-01-01T12:00:00;20.5;0.008"
        );

        assertEquals(1, CsvReader.read(file).size());
    }

    @Test
    void testRejectsWrongColumnCount() throws Exception {
        Path file = csv(
                "Time;Temperature;MixingRatio",
                "2026-01-01T12:00:00;20.5"   // only 2 columns -> invalid
        );

        assertThrows(IllegalArgumentException.class,
                () -> CsvReader.read(file));
    }
}
