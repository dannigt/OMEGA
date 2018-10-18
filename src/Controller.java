import org.apache.commons.lang3.time.StopWatch;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class Controller // implements Serializable
{
	private final byte MIN_SIZE = 2;
	private final byte MAX_SIZE = 10;
	private State state;
	private View view;
//	private byte computer_player;
//	private StrategyRandom search;

	// TODO: also track past movements here
	private ArrayList<Short> placementOrder;
	private Stack<Short> pastPlacements;
	private String log_dir;
	private String hash_dir;
	private StopWatch stopwatch = new StopWatch();
	private String timestamp;
//	private SearchStrategy[] strategies;
	private byte[] player_strategy = new byte[] {0, 2};

	private String[] strategyNames = new String[] {"random", "human", "a-b"};

	// TODO: for hashing
	private long[][] rands;
	// TODO: timer for players
	private long[] timer;
	private boolean paused;

	private String warning_info = "";

	public Controller() {
		state = new State(this);

		log_dir = Paths.get(System.getProperty("user.dir"), "log").toString();
		hash_dir = Paths.get(System.getProperty("user.dir") , "hash").toString();

//		strategies = new SearchStrategy[]{new StrategyRandom(this, "Random"),
//				new StrategyManual(this, "Human Player"), new StrategyAlphaBeta(this, "A-B")};


		paused = true;
  	}

  	private SearchStrategy getStrategy(String name) {
		switch (name.toLowerCase()) {
			case "random":
				return new StrategyRandom(this, name);
			case "human player":
				return new StrategyManual(this, name);
			case "a-b":
				return new StrategyAlphaBeta(this, name);
			default:
				return null;
		}
	}

  	// generate random number for hashing
  	private void randGen() {
		String path = hash_dir + "\\board_size_" + state.getSize() + ".dat";

		File f = new File(path);
		if(f.exists() && !f.isDirectory()) {
			try {
				FileInputStream fis = new FileInputStream(path);
				ObjectInputStream iis = new ObjectInputStream(fis);
				rands = (long[][]) iis.readObject();
			} catch (Exception e) {

			}
		} else {
			// If not exist
			Random randomLong = new Random();
			long[][] rands = new long[state.getTotalCells()][numPlayers()+1];

			for (short cell = 0; cell < state.getTotalCells(); cell++) {
				for (byte value = 0; value <= numPlayers(); value++) {
					rands[cell][value] = randomLong.nextLong();
				}
			}

			try {
				FileOutputStream fos = new FileOutputStream(path);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(rands);

			} catch (Exception e) {

			}
		}
	}

	public short getCellColor(short cell_index) {
		return state.getCellContent(cell_index);
	}
	
	public void processCellClick(short cell_index, boolean from_UI) {
		if (state.isTerminal()) {
			throw new IllegalArgumentException("Game has terminated. Not possible to place stone.");
		} else if (paused) {
			throw new IllegalArgumentException("Game has paused. Not possible to place stone. (Re)start game.");
		}
//		if (from_UI)
//			System.out.println("Human player placed stone on cell " + cell_index);
//		else
//			System.out.println("Computer player placed stone on cell " + cell_index);

		// if click is outside board, or cell is already occupied --> illegal
		if (cell_index < 0 || state.getCellContent(cell_index) != 0) {
			throw new IllegalArgumentException("Cell index out of bound or already occupied");
        } else { // If legal, update state, return the player index
			state.placePiece(cell_index);
			placementOrder.add(cell_index);
			makeCache();
		}
		// TODO: switch timer
		// TODO: send a copy to search to mess up
	}

	public byte numPlayers() {
		return state.getNumPlayer();
	}

	public void setView(View v) {
	    this.view = v;
    }

	public void notifyChange() {
        view.repaint();
    }

    // TODO: only need to dump past movements and timer
	// save past moves and timer to file
    public void makeCache() {
//		Gson gson = new Gson();
//		String json = gson.toJson(pastPlacements);
//		System.out.println(json);
		ObjectOutputStream oos = null;
		FileOutputStream fout = null;
		try{
			fout = new FileOutputStream(Paths.get(log_dir , timestamp).toString(), false);
			oos = new ObjectOutputStream(fout);
			System.out.println("placement order: " + placementOrder);
			oos.writeObject(placementOrder);
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

	public void applyCache(String path) throws Exception {
		//Apply cache
		FileInputStream fin = null;
		ObjectInputStream ois = null;
		try{
			fin = new FileInputStream(path);
			ois = new ObjectInputStream(fin);
			// TODO: downcasting is ugly. Another other way?
			ArrayList<Short> foo = (ArrayList<Short>) ois.readObject();

			System.out.println("==========================read object" + foo);
			for (short move : foo) {
				state.placePiece(move);
			}

		} catch (Exception ex) {
			throw ex;
		}
	}

    public byte getMinSize() {
		return MIN_SIZE;
	}

	public void setBoardSize(byte size) {
//		this.size = size;
		this.paused = true;
		state.reset(size);
		view.reset();
	}

	public byte getMaxSize() {
		return MAX_SIZE;
	}

	public byte getBoardSize() {
		return state.getSize();
	}

	public String getProgressInfo() {
		return "Round " + state.currentRound() + ", next player " + state.nextPlayer();
	}

	public String getWarningInfo(){
		return warning_info;
	}

	public void setWarningInfo(String in) {
		warning_info = in;
	}

	public String getScore() {
		return state.getScore();
	}

	public void reverseMove() {
		short cell = pastPlacements.pop();
		state.unplacePiece(cell);
	}

	class SayHello extends TimerTask {
		public void run() {
			System.out.println("Remaining time: " + timer);
		}
	}


	// star the game
	public void start() {
		paused = false;
		randGen();

		// separate thread for running the game
		Thread thread = new Thread(() -> {
			placementOrder = new ArrayList<>(state.getTotalCells());
//			pastPlacements = new Stack<>();
			timestamp = LocalDateTime.now().toString().replace( ":" , "-" );

			SearchStrategy[] players = new SearchStrategy[numPlayers()];
			for (byte p_idx = 1; p_idx <= numPlayers(); p_idx++) {
				players[p_idx-1] = getStrategy(strategyNames[player_strategy[p_idx-1]]);
			}
			// while game not terminated, alternate between players to get next move
			while (!state.isTerminal()) {
				// every round
				for (byte p_idx = 1; p_idx <= numPlayers(); p_idx++) {
					SearchStrategy s = players[p_idx-1];//strategies[player_strategy[p_idx-1]];

					if (s.waitsForUI()) {
						// wait for UI input
						do {
							System.out.println(s.strategy_name + ", waiting for UI input");
							// And From your main() method or any other method
						} while
						(state.nextPlayer() == p_idx);
//						while (state.nextPlayer() == p_idx) {
//							System.out.println(s.strategy_name + ", waiting for player " + state.nextPlayer());
//						}
					} else {
						short[] moves = s.getNextMove(state);

						System.out.println("generated moves: " + Arrays.toString(moves));
						for (short stone_placement : moves) {
							System.out.println("===================strategy " + s.strategy_name + " move " + stone_placement);
							processCellClick(stone_placement, false);
						}
					}
				}
			}
		});


		thread.start();
//		timer = new long[state.getNumPlayer()];
////		placementOrder = new ArrayList<>(state.getTotalCells());
//		pastPlacements = new Stack<>();
//		timestamp = LocalDateTime.now().toString().replace( ":" , "-" );
//
//		// while game not terminated
//		// Alternate between players to get next move
//		while (!state.isTerminal()) {
//			for (byte p : player_strategy) {
//				System.out.println(strategies[p].getStrategyName());
//				short[] movements = strategies[p].getNextMove(state);
//
//				if (movements != null) {
//					for (short stone_placement : strategies[p].getNextMove(state)) {
//						System.out.println("====================strategy " + p + " move " + stone_placement);
//						processCellClick(stone_placement, false);
//					}
//					//				break;
//				} else {
//					System.out.println("ELSE");
//					// wait for ui stuff
//					while (state.nextPlayer() != computer_player) {
//						System.out.println("waiting for UI....");
//					}
//				}
//			}
//		}
	}

	public String[] getStrategies() {
		String[] names = new String[strategyNames.length];
		for (byte i = 0; i < strategyNames.length; i++) {
			names[i] = strategyNames[i];//.getStrategyName();
		}
		return names;
	}

	public byte getPlayerStrategy(byte p) {
		return player_strategy[p];
	}

	public void setPlayerStrategy(byte p, byte s) {
		player_strategy[p] = s;
		System.out.println(Arrays.toString(player_strategy));
//		start();
	}

	public void storeHumanMovement() {
		//check current player index

		//moves are full --> confirm --> process into state
	}
}