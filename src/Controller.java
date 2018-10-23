//import org.apache.commons.lang3.time.StopWatch;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

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
//	private StopWatch stopwatch = new StopWatch();
	private String timestamp;
//	private SearchStrategy[] strategies;
	private byte[] player_strategy = new byte[] {0, 3};

	private String[] strategyNames = new String[] {"random", "human", "a-b", "a-b with id"};

	private long[][] rands;
	// TODO: timer for players
	private long[] timer;
	private boolean paused;

	private String warning_info = "";

	private int TIME_LIMIT = 10000;

	public Controller() {
		state = new State(this);

		log_dir = Paths.get(System.getProperty("user.dir"), "log").toString();
		hash_dir = Paths.get(System.getProperty("user.dir") , "hash").toString();

		paused = true;

		// TODO: move to elsewhere?
		createDirIfNotExist(Paths.get(hash_dir));
		createDirIfNotExist(Paths.get(log_dir));
  	}

  	private SearchStrategy getStrategy(String name) {
		switch (name.toLowerCase()) {
			case "random":
				return new StrategyRandom(this, name);
			case "human":
				return new StrategyManual(this, name);
			case "a-b":
				return new StrategyAb(this, name);
			case "a-b with id":
				return new StrategyAbIterativeDeepening(this, name);
			default:
				throw new IllegalArgumentException("No strategy with name " + name);
		}
	}


	private Object readObject(String path) throws Exception {
        Object res = null;
        try {
            FileInputStream fin = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fin);
            res = ois.readObject();
        } catch (Exception ex) {
            throw ex;
        }
        return res;
    }
  	// generate random number for hashing
  	private void loadRand() {
		String path = Paths.get(hash_dir + "board_size_" + state.getBoardSize() + ".dat").toString();

        try {
            rands = (long[][]) readObject(path);
        } catch (Exception ex) {
            System.err.println("Cannot cast");
            // TODO: generate here
        }

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
			Path out = Paths.get(log_dir , timestamp);
			fout = new FileOutputStream(out.toString(), false);
			oos = new ObjectOutputStream(fout);
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
		return state.getBoardSize();
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

	// star the game
	public void start() {
		paused = false;
		loadRand();

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
				for (byte pIdx = 1; pIdx <= numPlayers(); pIdx++) {
					SearchStrategy s = players[pIdx-1];//strategies[player_strategy[p_idx-1]];

					if (s.waitsForUI()) {
						// wait for UI input
						do {
//							System.out.println(s.strategy_name + ", waiting for UI input");
							// And From your main() method or any other method
						} while
						(state.nextPlayer() == pIdx);
					} else {
						short[] moves = s.getNextMove(state, TIME_LIMIT, pIdx-1);

						for (short stone_placement : moves) {
							System.out.println("===================strategy " + s.strategy_name + " move " + stone_placement);
							processCellClick(stone_placement, false);
						}
					}
				}
			}
		});

		thread.start();
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

	private void createDirIfNotExist(Path path) {
		if(Files.notExists(path)){
			try {
				Files.createDirectory(path);
			}
			catch (Exception ex) {
				System.err.println("Cannot create dir " + path.toString());
			}
		}
	}

	public long[][] requestRands() {
	    if (rands == null) {
	        loadRand();
        }
	    return rands;
    }
}