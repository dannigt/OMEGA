
import org.apache.commons.lang3.time.StopWatch;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.Stack;

public class Controller implements Serializable
{
	private final byte MIN_SIZE = 5;
	private final byte MAX_SIZE = 10;
	private State state;
	private View view;
	private byte computer_player;
	private SearchRandom search;
	// TODO: timer for players
	private long[] timer;
	// TODO: also track past movements here
	private ArrayList<Short> placementOrder;
	private Stack<Short> pastPlacements;
	private String log_dir;
	private StopWatch stopwatch = new StopWatch();
	private String timestamp;
	private SearchStrategy[] strategies;

	public Controller() {
        this.computer_player = 1;
		state = new State(this);
		search = new SearchRandom(this, "Random");
		log_dir = System.getProperty("user.dir") + "\\log";
		strategies = new SearchStrategy[]{search};
  	}

	public short getCellColor(short cell_index) {
		return state.getCellContent(cell_index);
	}
	
	public void processCellClick(short cell_index, boolean from_UI) throws IllegalArgumentException{
		if (state.isTerminal()) {
            throw new IllegalArgumentException("Game has terminated");
        }

		if (from_UI)
			System.out.println("Human player placed stone on cell " + cell_index);
		else
			System.out.println("Computer player placed stone on cell " + cell_index);

		// if click is outside board, or cell is already occupied --> illegal
		if (cell_index < 0 || state.getCellContent(cell_index) != 0) {
			throw new IllegalArgumentException("Cell index out of bound");
		}
		else if (from_UI && computer_player == state.nextPlayer()) { // UI click when it's computer's turn
            throw new IllegalArgumentException("========It is computer player (player " + state.nextPlayer() + ")'s turn. ========");
        } else { // If legal, update state, return the player index
			pastPlacements.push(cell_index);
			state.placePiece(cell_index);
		}

		// TODO: switch timer

//		System.out.println(state.totalRounds() + ", " + state.totalTurns() + ", " + state.currentRound() + ", " + state.currentTurn() + ", " + state.turnsLeft());


        if (from_UI && state.nextPlayer() == computer_player) { // Turn switches from human to computer
            System.out.println("Entering a-b with " + state.turnsLeft() + " turns left");
            search.alphaBeta(state, state.turnsLeft(), Integer.MAX_VALUE, Integer.MIN_VALUE);
            search.getNextMove(state); // TODO: send a copy to search to mess up
        }
	}

	public byte numPlayers() {
		return state.getNumPlayer();
	}

	public void setView(View v) {
	    this.view = v;
    }

	public void notifyChange() {
        view.repaint();
//		pastPlacements.push(cell_index);
    }

    // TODO: only need to dump past movements and timer
    public void requestCache() {
//		Gson gson = new Gson();
//		String json = gson.toJson(pastPlacements);
//		System.out.println(json);

		ObjectOutputStream oos = null;
		FileOutputStream fout = null;
		try{
			fout = new FileOutputStream(log_dir + "\\" + timestamp, true);
			oos = new ObjectOutputStream(fout);
			oos.writeObject(pastPlacements);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (oos != null) {
					oos.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void applyCache(Queue<Short> log) {
		//Apply cache

	}

    public byte getMinSize() {
		return MIN_SIZE;
	}

	public void setBoardSize(byte size) {
//		this.size = size;
		state.reset(size);
		view.reset();
	}

	public byte getMaxSize() {
		return MAX_SIZE;
	}

	public byte getBoardSize() {
		return state.getSize();
	}

	public String progressInfo() {
		return "Round " + state.currentRound() + ", next player " + state.nextPlayer();
	}

	public byte getComputerPlayer() {
		return computer_player;
	}

	public void setComputer(byte i) {
		this.computer_player = i;
		start();
	}

	public void reverseMove() {
		short cell = pastPlacements.pop();
		state.unplacePiece(cell);
	}

	public void start() {
		timer = new long[state.getNumPlayer()];
		placementOrder = new ArrayList<>(state.getTotalCells());
		pastPlacements = new Stack<>();
		timestamp = LocalDateTime.now().toString().replace( ":" , "-" );

		if (state.nextPlayer() == computer_player) {
			search.getNextMove(state);
		}
		//TODO: disable all kinds of configuration shits
	}

	public String[] getStrategies() {
//		for (SearchStrategy s : strategies) {
//
//		}
		return (String[]) Arrays.stream(strategies).map(s -> s.strategy_name).toArray();
//		return null;
//				.filter(obj -> obj instanceof ScheduleIntervalContainer)
//				.map(obj -> (ScheduleIntervalContainer) obj)
	}

	public void turnFinished() {

	}
}