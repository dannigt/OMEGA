import javax.swing.*;

public class Main {
    public static void main(String[] args)
    {
        Controller c = new Controller();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                View.createAndShowGUI(c);
            }
        });
    }
}
