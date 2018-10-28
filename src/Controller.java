//import org.apache.commons.lang3.time.StopWatch;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller // implements Serializable
{
	private final byte MIN_SIZE = 3;
	private final byte MAX_SIZE = 10;
	private State state;
	private View view;
	private ArrayList<Short> placementOrder;
	private final String LOG_DIR;
	private final String HASH_DIR;
    private final String[] STRATEGY_NAMES;
	private String timestamp;
	private byte[] player_strategy;
	private long[][] RAND;
	private int[] timer;
	private boolean paused;
	private String warning_info = "";

	public void clear() {
	    state.reset();
        player_strategy = new byte[state.getNumPlayer()];
        timer = new int[state.getNumPlayer()];
        Arrays.fill(player_strategy, (byte) 0);
        Arrays.fill(timer, 900000);
        paused = true;
    }

	public Controller() {
		state = new State(this);

		LOG_DIR = Paths.get(System.getProperty("user.dir"), "log").toString();
		HASH_DIR = Paths.get(System.getProperty("user.dir") , "hash").toString();
        STRATEGY_NAMES = new String[] {"Random", "Manual", "A-B", "A-B with ID", "Monte Carlo", "A-B with MC"};

		player_strategy = new byte[state.getNumPlayer()];
		timer = new int[state.getNumPlayer()];
		Arrays.fill(player_strategy, (byte) 0);
		Arrays.fill(timer, 900000);
		paused = true;

		createDirIfNotExist(Paths.get(HASH_DIR));
		createDirIfNotExist(Paths.get(LOG_DIR));
  	}

  	private SearchStrategy getStrategy(String name) {
		switch (name.toLowerCase()) {
			case "random":
				return new StrategyRandom(this, name);
			case "human":
				return new StrategyManual(this, name);
			case "a-b":
				return new StrategyAlphaBeta(this, name);
			case "a-b with id":
				return new StrategyAlphaBetaId(this, name);
            case "monte carlo":
                return new StrategyMonteCarlo(this, name);
            case "a-b with mc":
                return new StrategyAlphaBetaIdMc(this, name);
			default:
				throw new IllegalArgumentException("No strategy with name " + name);
		}
	}

  	// generate random number for hashing
  	private void loadRand() {
		String path = Paths.get(HASH_DIR , "board_size_" + state.getBoardSize() +
                "_p_" + state.getNumPlayer() + ".dat").toString();

        try {
            RAND = (long[][]) readObject(path);
        } catch (Exception ex) {
            Random randomLong = new Random();
            long[][] rands = new long[state.getTotalCells()][numPlayers()+1];

            for (short cell = 0; cell < state.getTotalCells(); cell++) {
                for (byte value = 0; value <= numPlayers(); value++) {
                    rands[cell][value] = randomLong.nextLong();
                }
            }
            try {
                saveObject(rands, path);
//				FileOutputStream fos = new FileOutputStream(path);
//				ObjectOutputStream oos = new ObjectOutputStream(fos);
//				oos.writeObject(rands);
            } catch (Exception e) {

            }
            System.err.println("Cannot cast from the object");
        }
	}

    public byte getCellColor(short cIndex) {
        return state.getCellContent(cIndex);
    }

    public boolean isFocusedCell(short cIndex) { // the piece placed in the most recent round
	    if (placementOrder != null && placementOrder.size() > numPlayers())
	        //
           return (cIndex==placementOrder.get(placementOrder.size()-1)) ||
                (cIndex == placementOrder.get(placementOrder.size()-2));
        else
            return false;
	}
	
	public void processCellClick(short cell_index, boolean from_UI) {
		if (state.isTerminal()) {
			throw new IllegalArgumentException("Game has terminated. Not possible to place stone.");
		} else if (paused) {
			throw new IllegalArgumentException("Game has paused. Not possible to place stone. (Re)start game.");
		}

		// if click is outside board, or cell is already occupied --> illegal
		if (cell_index < 0 || state.getCellContent(cell_index) != 0) {
			throw new IllegalArgumentException("Cell " + cell_index + " out of bound or already occupied");
        } else { // If legal, update state, return the player index
			state.placePiece(cell_index);
			placementOrder.add(cell_index);
			makeCache();
		}
	}

	public byte numPlayers() {
		return state.getNumPlayer();
	}

	public void setView(View v) {
	    this.view = v;
    }

	public void notifyChange() {
	    if (view != null)
            view.repaint();
    }

	// save past moves and timer to file
    public void makeCache() {
//		Gson gson = new Gson();
//		String json = gson.toJson(pastPlacements);
//		System.out.println(json);
		ObjectOutputStream oos = null;
		FileOutputStream fout = null;
		try{
			Path out = Paths.get(LOG_DIR, timestamp);
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

            clear();
			ArrayList<Short> foo = (ArrayList<Short>) ois.readObject();

			for (short move : foo) { // one second per piece, for better visibility
				state.placePiece(move);
                TimeUnit.MILLISECONDS.sleep(500);
			}

            System.out.println(getScore());

		} catch (Exception ex) {
			throw ex;
		}
	}

    /**
     * Getters and setters
     */
    public byte getMinSize() {
		return MIN_SIZE;
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
    public void setBoardSize(byte size) {
//		this.size = size;
		this.paused = true;
//		state = new State(this, size, numPlayers());
		state.reset(size);
		view.reset();
	}

	public void reverseMove() {
	    placementOrder.remove(placementOrder.size()-1);
	    state.reset();
        for (short move : placementOrder) {
            state.placePiece(move);
        }
	}

	// return the move sequences
	public ArrayList<Short> start(SearchStrategy... players) {
        state = new State(this, (byte) players.length);
        clear();

        paused = false;
        loadRand();
        placementOrder = new ArrayList<>(state.getTotalCells());
        timestamp = LocalDateTime.now().toString().replace( ":" , "-" );

        // while game not terminated, alternate between players to get next move
        while (!state.isTerminal()) {
            // every round
            for (byte pIdx = 1; pIdx <= numPlayers(); pIdx++) {
                SearchStrategy s = players[pIdx-1];//strategies[player_strategy[p_idx-1]];
                if (s.waitsForUI()) {// wait for UI input
                    do {
                        System.out.print("");
                    } while
                    (state.nextPlayer() == pIdx);
                } else {
                    long start = System.currentTimeMillis();//timer[pIdx-1]/(Math.max(1, state.turnsLeft()/2-4)
                    int limit = timer[pIdx - 1]/(Math.max(2, state.turnsLeft()/2));
//                    if (state.currentTurn() <= 14) {
//                        limit = TIME_LIMIT;
//                    }
                    short[] moves = s.getNextMove(state, limit, (byte) (pIdx-1));
                    System.out.println("==================\t\t" + s.getStrategyName() + ": " + Arrays.toString(moves));
//                    try {
                        for (short stone_placement : moves) {
                            processCellClick(stone_placement, false);
                        }
//                    } catch (Exception ex) {
//                        s.requestFallback(state);
//                    }
                    timer[pIdx-1] -= (System.currentTimeMillis() - start);
                    System.out.println("Timer: " + Arrays.toString(timer));
                }
            }
        }
        return placementOrder;
    }

	// star the game
	public void start() {
        System.out.println("--------------" + numPlayers());
        SearchStrategy[] players = new SearchStrategy[numPlayers()];
        for (byte p_idx = 1; p_idx <= numPlayers(); p_idx++) {
			players[p_idx-1] = getStrategy(STRATEGY_NAMES[player_strategy[p_idx-1]]);
		}
		start(players);
	}

	public String[] getStrategies() {
		String[] names = new String[STRATEGY_NAMES.length];
		for (byte i = 0; i < STRATEGY_NAMES.length; i++) {
			names[i] = STRATEGY_NAMES[i];//.getStrategyName();
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

	public long[][] requestRands() {
	    if (RAND == null) {
	        loadRand();
        }
	    return RAND;
    }

    //
    //
    // Static helper methods
    public static void createDirIfNotExist(Path path) {
        if(Files.notExists(path)){
            try {
                Files.createDirectory(path);
            }
            catch (Exception ex) {
                System.err.println("Cannot create dir " + path.toString());
            }
        }
    }

    public static void saveObject(Object o, String path) throws Exception {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(o);
        } catch (Exception ex) {
            throw ex;
        }
    }

    public static Object readObject(String path) throws Exception {
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

    public static void permutation(short[][] res, short[] perm, int pos, short[] source, AtomicInteger cnt) {
        if (pos == perm.length) {
            for (int i=0; i<perm.length;i++) {
                res[cnt.intValue()][i] = perm[i];
            }
            cnt.getAndIncrement();
        } else {
            for (int i = 0 ; i < source.length ; i++) {
                if (source[i] != -1) {
                    perm[pos] = source[i];
                    short[] newSource = source.clone();
                    newSource[i] = -1;
                    permutation(res, perm, pos + 1, newSource, cnt);
                }
            }
        }
    }
}