import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        
        // 1. Try to Apply the FlatLaf Dark Theme
        try {
            // We use reflection here so the code still compiles even if 
            // the FlatLaf jar file is missing from your project.
            Class<?> flatLaf = Class.forName("com.formdev.flatlaf.FlatDarkLaf");
            java.lang.reflect.Method setupMethod = flatLaf.getMethod("setup");
            setupMethod.invoke(null);
            System.out.println("Dark Theme applied successfully!");
        } catch (Exception e) {
            // This runs if the jar is missing
            System.err.println("FlatLaf library not found. Falling back to default.");
            System.err.println("To enable Dark Mode, ensure flatlaf-3.5.4.jar is in classpath.");
            try {
                // Fallback to the standard Windows/Mac style
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) { ex.printStackTrace(); }
        }

        // 2. Launch the GUI
        // We run this on the Swing Event Dispatch Thread (EDT) for thread safety.
        SwingUtilities.invokeLater(() -> {
            new HuffmanGUI().setVisible(true);
        });
    }
}