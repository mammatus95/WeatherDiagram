import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {


    public static void main(String[] args) {

        Path file = Path.of(args.length > 0 ? args[0] : "Task4/data.csv");

        // Read CSV outside Swing's event dispatch thread.
        final List<Meteo.Measurement> data;

        try {
            data = CsvReader.read(file);
            if (data.isEmpty()) {
                System.err.println("No data rows found in "+ file.toAbsolutePath());
                return;
            }
        } catch (IOException | RuntimeException e) {
            System.err.println("Could not read " + file.toAbsolutePath()+ "\n" + e.getMessage());
            return;
        }

        // Create and show Swing components on the EDT.
        SwingUtilities.invokeLater(() -> {
            try {
                new GUI(data).setVisible(true);
            } catch (RuntimeException e) {
                System.err.println("Could not display chart.\n"+ e.getMessage());
            }
        });
    }

}
