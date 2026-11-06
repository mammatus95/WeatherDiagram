import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

/**
 * Reads a CSV file containing three columns:
 *   1. Time (ISO 8601, e.g. 2026-01-01T12:00:00)
 *   2. Temperature (deg C)
 *   3. Mixing ratio (kg/kg)
 * <p>
 * Expected delimiter: semicolon (;)
 * A header row is present and skipped.
 * </p>
 */
public class CsvReader {

    /**
     * Reads the CSV file and returns all measurements.
     *
     * @param path  path to the CSV file
     * @return      list of measurements in file order
     * @throws IOException  if the file cannot be read
     */
    public static List<Meteo.Measurement> read(Path path) throws IOException {

        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            return lines
                    .skip(1)              // skip header row
                    .filter(l -> !l.isBlank())
                    .map(CsvReader::parseLine)
                    .toList();
        }
    }

    /**
     * Parses a single CSV line into a Measurement.
     * Accepts both dot and comma as decimal separator.
     */
    private static Meteo.Measurement parseLine(String line) {
        // split line at ; into columns
        String[] cols = line.split(";");

        if (cols.length != 4) {
            throw new IllegalArgumentException(
                    "Expected 4 columns, found " + cols.length + " in line: " + line
            );
        }

        try {
            // parse the time
            LocalDateTime time = LocalDateTime.parse(cols[0]);
            // parse the values
            double pressure = Util.parseDouble(cols[1]);
            double temperature = Util.parseDouble(cols[2]);
            double mixingRatio = Util.parseDouble(cols[3]);

            return new Meteo.Measurement(time, pressure, temperature, mixingRatio);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid value in line: " + line, e
            );
        }
    }

    /** Quick runner. */
    public static void main(String[] args) throws IOException {
        Path file = Path.of(args.length > 0 ? args[0] : "data.csv");
        List<Meteo.Measurement> data = read(file);
        data.forEach(m -> System.out.println(m));
    }
}
