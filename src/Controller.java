import javax.swing.SwingUtilities;

public class Controller
{
	private State state;
	private View view;
	private byte computer_player;
	private Search search;
	private byte size;
	// TODO: also track past movements here

	private Controller() {
		this.size = 6;
		byte num_player = 2;
        computer_player = 2;
//		view = new View(this, size);
//		view.initShowUI();
		state = new State(this, size, num_player);

		search = new Search(computer_player);
//		search.alphaBeta(state, state.numMoves(), Integer.MAX_VALUE, Integer.MIN_VALUE);
  	}

	public static void main(String[] args)
	{
	    Controller c = new Controller();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new View(c, c.size).initShowUI();  //just take the idea of this line
            }
        });

	}
	
	public short getCellColor(short cell_index) {
		return state.getCellContent(cell_index);
	}
	
	public void processCellClick(short cell_index) throws IllegalArgumentException{
		System.out.println("Human player placed cell " + cell_index);

//        System.out.println("Player " + state.currentTurn() + "'s turn");

        System.out.println(state.turnsLeft() + " turns left");

        // TODO: check termination
        if (state.isTerminal()) {
            throw new IllegalArgumentException("Game has terminated");
        }

		// if click is outside board, or cell is already occupied --> illegal
		if (cell_index < 0 || state.getCellContent(cell_index) != 0) {
			throw new IllegalArgumentException("Cell index out of bound");
		} else if (computer_player == state.currentTurn()) {
            throw new IllegalArgumentException("========It is computer player (player " + state.currentTurn() + ")'s turn. ========");
        } else { // If legal, update state, return the player index
			state.placePiece(cell_index);
		}

        if (state.currentTurn() == computer_player) { // when it's computer's turn
            System.out.println("Entering a-b with " + state.turnsLeft() + " left");
            search.alphaBeta(state, state.turnsLeft(), Integer.MAX_VALUE, Integer.MIN_VALUE);
        }
	}

	public void setView(View v) {
	    this.view = v;
    }

	public void notifyChange() {
        view.repaint();
    }
}