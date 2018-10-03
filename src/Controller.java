import javax.swing.SwingUtilities;

public class Controller
{
	private State state;
	private View view;

	private Controller() {
		byte size = 10;
		byte num_player = 2;
		view = new View(this, size);
//		view.createAndShowGUI();
		state = new State(this, size, num_player);
		Search search = new Search();
		search.alphaBeta(state, state.numMoves(), Integer.MAX_VALUE, Integer.MIN_VALUE);
  	}

	public static void main(String[] args)
	{
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
				new Controller();
//			}
//		});
	}
	
	public short getCellColor(short cell_index) {
		return state.getCellContent(cell_index);
	}
	
	public short processCellClick(short cell_index) {
		System.out.println("cell " + cell_index);

		// if click is outside board, or cell is already occupied 
		// illegal, return -1
		if (cell_index < 0 || state.getCellContent(cell_index) != 0) {
			return -1;
		} else { // If legal, update state, return the player index
			state.placePiece(cell_index);
			return state.getCellContent(cell_index);
		}
	}

	public void notifyChange() {
        view.repaint();
    }
}