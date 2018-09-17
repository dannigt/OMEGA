import javax.swing.SwingUtilities;

public class Controller
{
	private Model model;
	private Controller() {
		View view = new View(this, 2);
		view.createAndShowGUI();
		model = new Model(10, 2);
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
//		System.out.println(cell_index + " has color " + model.getCellContent(cell_index));
		return model.getCellContent(cell_index);
	}
	
	public int processCellClick(int cell_index) {
		System.out.println("processing movement on cell " + cell_index);

		// if click is outside board, or cell is already occupied 
		// illegal, return -1
		if (cell_index < 0 || model.getCellContent(cell_index) != 0) { 
			return -1;
		} else { // If legal, update model, return the player index
			model.placePiece(cell_index);
			return model.getCellContent(cell_index);
		}
	}
}