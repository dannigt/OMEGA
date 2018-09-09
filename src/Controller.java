import java.lang.reflect.Array;
import java.util.Arrays;

import javax.swing.SwingUtilities;

public class Controller
{
	private Model model;
	private Controller() {
		View view = new View(this, 2);
		view.createAndShowGUI();
		model = new Model(3, 2);
  	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {	
				new Controller();
			}
		});
	}
	
	public int getCellColor(int cell_index) {
		System.out.println(cell_index + " has color " + model.getCellContent(cell_index));
		return model.getCellContent(cell_index);
	}
	
	public int processCellClick(int cell_index, int player_index) {
		System.out.println("processing movement on cell " + cell_index);
		
//		model.getNeighbors(index);
		
		int cell_content = model.getCellContent(cell_index);
		// illegal, return -1
		if (cell_content != 0) { // if cell is already occupied TODO: implement swap here
			return -1;
		} else { // If legal, update model, return the player index
			model.setCellContent(cell_index, player_index);
			return model.nextPlayer();
		}

		// return true to view
//		return 1;
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