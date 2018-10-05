import javax.swing.SwingUtilities;

public class Controller
{
	private final byte MIN_SIZE = 5;
	private final byte MAX_SIZE = 10;
	private State state;
	private View view;
	private byte computer_player;
	private Search search;
	private byte size;
	private byte num_player;
	// TODO: also track past movements here

	public Controller() {
		this.size = 6;
		this.num_player = 2;
        this.computer_player = 2;
		state = new State(this, size, num_player);
		search = new Search(computer_player);
//		search.alphaBeta(state, state.numMoves(), Integer.MAX_VALUE, Integer.MIN_VALUE);
  	}

	
	public short getCellColor(short cell_index) {
		return state.getCellContent(cell_index);
	}
	
	public void processCellClick(short cell_index) throws IllegalArgumentException{
//		System.out.println(state.used_cells);
		System.out.println("Human player placed cell " + cell_index);

//        System.out.println("Player " + state.nextPlayer() + "'s turn");

		// TODO: check termination
        if (state.isTerminal()) {
            throw new IllegalArgumentException("Game has terminated");
        }

		// if click is outside board, or cell is already occupied --> illegal
		if (cell_index < 0 || state.getCellContent(cell_index) != 0) {
			throw new IllegalArgumentException("Cell index out of bound");
		} else if (computer_player == state.nextPlayer()) {
            throw new IllegalArgumentException("========It is computer player (player " + state.nextPlayer() + ")'s turn. ========");
        } else { // If legal, update state, return the player index
			state.placePiece(cell_index);
		}

//		System.out.println(state.totalRounds() + ", " + state.totalTurns() + ", " + state.currentRound() + ", " + state.currentTurn() + ", " + state.turnsLeft());

        if (state.nextPlayer() == computer_player) { // when it's computer's turn
            System.out.println("Entering a-b with " + state.turnsLeft() + " turns left");
            search.alphaBeta(state, state.turnsLeft(), Integer.MAX_VALUE, Integer.MIN_VALUE);
        }
	}

	public byte numPlayers() {
		return num_player;
	}

	public void setView(View v) {
	    this.view = v;
    }

	public void notifyChange() {
        view.repaint();
    }

    public byte getMinSize() {
		return MIN_SIZE;
	}

	public void setBoardSize(byte size) {
		this.size = size;
		state.reset(size);
		view.reset();
	}

	public byte getMaxSize() {
		return MAX_SIZE;
	}

	public byte getBoardSize() {
		return size;
	}
}