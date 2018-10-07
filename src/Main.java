import javax.swing.*;

public class Main {
    public static void main(String[] args)
    {
        Controller c = new Controller();

        SwingUtilities.invokeLater(() -> View.createAndShowGUI(c));
    }
}
