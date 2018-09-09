import java.lang.reflect.Array;
import java.util.Arrays;

import javax.swing.SwingUtilities;

public class Controller
{
	private Model model;
	private Controller() {
		View view = new View(this);
		view.createAndShowGUI();
		model = new Model(3);
  	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {	
				new Controller();
			}
		});
	}
	
	public boolean processCellClick(int index) {
		System.out.println("processing movement on " + index);
		model.getNeighbors(index);
		// illegal?
		
		// If legal, update model 
		// 
		// return true to view
		return true;
	}

	//constants and global variables
//	final static Color COLOURBACK =  Color.WHITE;
//	final static Color COLOURCELL =  Color.WHITE;	 
//	final static Color COLOURGRID =  Color.BLACK;	 
//	final static Color COLOURONE = new Color(255,255,255,200);
//	final static Color COLOURONETXT = Color.BLUE;
//	final static Color COLOURTWO = new Color(0,0,0,200);
//	final static Color COLOURTWOTXT = new Color(255,100,255);
//	final static int EMPTY = 0;
//	final static int BSIZE = 3; //board size.
//	final static int NUM_ROWS = 2 * BSIZE - 1;

//	static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//	static int height = (int) screenSize.getHeight();
//	final static int SCRSIZE = height; //HEXSIZE * (BSIZE + 1) + BORDERS*3; //screen size (vertical dimension).
//	final static int BORDERS = SCRSIZE / NUM_ROWS / 2;
//	final static int HEXSIZE = BORDERS;	//hex size in pixels

//	int[][] board = new int[BSIZE][BSIZE];

}