import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class State implements Comparable<State>  {
    private Controller c;

    private byte[] cells; // Color placed in the cell. Empty is 0
    private byte size; // board size
    private ArrayList<Short>[] adjList;  // neighbor indices
    private byte numPlayer;
    private short totalCells;
    private short usedCells;

    private short[] ufParent;
    private short[] ufSize;

    private long[] scores;
    private HashMap<Short, Short> groupSizeCounter = new HashMap<>();

    private int value = Integer.MIN_VALUE;
    private byte flag = Byte.MIN_VALUE;

    private long hashKey = 0; // hash key for transposition table
    private boolean sim = false; // is simulation

    //Constructors with default values
    public State(Controller c) {
        this(c, (byte) 5, (byte) 2);
    }

    public State(Controller c, byte size, byte numPlayer) {
        this.c = c;
        this.numPlayer = numPlayer;
        init(size);

    }

    // overloaded constructor for copying states
    public State(State s) {
        size = s.size;
        adjList = s.adjList;
        numPlayer = s.numPlayer;

        totalCells = s.totalCells;
        usedCells = s.usedCells;
        cells = s.cells.clone();

        ufParent = s.ufParent.clone();
        ufSize = s.ufSize.clone();

        groupSizeCounter = new HashMap<>(s.groupSizeCounter);

        sim = true;

        scores = s.scores.clone();

        hashKey = s.hashKey;

        c = s.c;
    }

    public void reset(byte size) {
        init(size);
        c.notifyChange();
    }

    public void reset() {
        init(size);
        c.notifyChange();
    }

    // initialize the state, build adjacency matrix, arrays for Union Find etc.
    private void init(byte size) {
        this.scores = new long[numPlayer];
        Arrays.fill(scores, 1);

        this.size = size;
        totalCells = (short) ((size * 2 + size - 1) * size - size * 2 + 1);
        usedCells = 0;
        cells = new byte[totalCells];
        ufParent = new short[totalCells];
        Arrays.fill(ufParent, (short) -1);
        ufSize = new short[totalCells];
        groupSizeCounter = new HashMap<>();

        // Make adjacency list
        adjList = new ArrayList[totalCells];
        for (short i = 0; i < adjList.length; i++) {
            adjList[i] = new ArrayList<>(6); // 6 as initial capacity (at most 6 neighbours)
        }

        short numIters = (short) (size - 1);
        short curIdx = 0;
        // build adjacency matrix
        for (short i = 0; i <= numIters; i++) {
            int numCellsPerrow = size + i;
            for (byte j = 0; j < numCellsPerrow; j++) {
                if (i != numIters) {
                    // cross-row
                    adjList[curIdx].add((short) (curIdx + numCellsPerrow));
                    adjList[curIdx].add((short) (curIdx + numCellsPerrow + 1));
                    adjList[curIdx + numCellsPerrow].add(curIdx);
                    adjList[curIdx + numCellsPerrow + 1].add(curIdx);

                    short mirrorIdx = (short) (totalCells - curIdx - 1);

                    adjList[mirrorIdx].add((short) (mirrorIdx - numCellsPerrow));
                    adjList[mirrorIdx].add((short) (mirrorIdx - numCellsPerrow - 1));
                    adjList[mirrorIdx - numCellsPerrow].add(mirrorIdx);
                    adjList[mirrorIdx - numCellsPerrow - 1].add(mirrorIdx);

                    // same row
                    if (j < numCellsPerrow - 1) {
                        adjList[curIdx].add((short) (curIdx + 1));
                        adjList[curIdx + 1].add(curIdx);
                        adjList[mirrorIdx].add((short) (mirrorIdx - 1));
                        adjList[mirrorIdx - 1].add(mirrorIdx);

                    }
                } else if (j < numCellsPerrow - 1) { // middle row
                    adjList[curIdx].add((short) (curIdx + 1));
                    adjList[curIdx + 1].add(curIdx);
                }
                curIdx++;
            }
        }
    }

    public byte getCellContent(short cell) {
        return cells[cell];
    }

    public void placePiece(short cell) {
        byte nextColor = nextColor();
        cells[cell] = nextColor;
        usedCells++;
        calcConnectedAreaSize(cell);
        calcScores();

        // Update hashkey
        // hahKey *= rand[cell][color];
        try {
            hashKey ^= c.requestRands()[cell][nextColor];
        } catch (Exception ex) {
            System.err.println("sim " + sim);
        }
        // If in search step, don't notify change
        if (!sim) {
            c.notifyChange();
        }
    }

    public long getHashKey() {
        return hashKey;
    }

    //For union find
    private int findRoot(int p) {
        while (p != ufParent[p]) {
            ufParent[p] = ufParent[ufParent[p]];    // path compression by halving
            p = ufParent[p];
        }
        return p;
    }

    private int calcConnectedAreaSize(short cell) {
        // Set parent of this cell to itself
        ufParent[cell] = cell;
        ufSize[cell] = 1;

        short key = (short) (cells[cell] * 1000 + 1);
        if (groupSizeCounter.containsKey(key)) {
            groupSizeCounter.put(key, (short) (groupSizeCounter.get(key) + 1));
        } else {
            groupSizeCounter.put(key, (short) 1);
        }

        // Go through neighbors of same color
        for (int neighbor : adjList[cell]) {
            if (cells[cell] == cells[neighbor]) {
                int nbgRoot = findRoot(neighbor);

                if (nbgRoot != cell) {
                    // Decrement count for ufSize[nbgRoot]
                    short keyNbgRoot = (short) (cells[cell] * 1000 + ufSize[nbgRoot]);

                    // Decrement count for
                    groupSizeCounter.put(keyNbgRoot, (short) (groupSizeCounter.get(keyNbgRoot) - 1));

                    // Update parent reference
                    ufParent[nbgRoot] = cell; // The newly added cell becomes parent of the neighbor

                    // Decrement count for ufSize[cell]
                    short keyCell = (short) (cells[cell] * 1000 + ufSize[cell]);
                    groupSizeCounter.put(keyCell, (short) (groupSizeCounter.get(keyCell) - 1));

                    // Increment counter
                    ufSize[cell] += ufSize[nbgRoot];

                   // Increment count for ufSize[cell]
                    keyCell = (short) (cells[cell] * 1000 + ufSize[cell]);
                    if (groupSizeCounter.containsKey(keyCell)) {
                        groupSizeCounter.put(keyCell, (short) (groupSizeCounter.get(keyCell) + 1));
                    } else {
                        groupSizeCounter.put(keyCell, (short) 1);
                    }
                }
            }
        }

        return ufSize[cell];
    }

    private void calcScores() {
        Arrays.fill(scores, 1);
        for (Map.Entry<Short, Short> entry : groupSizeCounter.entrySet()) {
//            System.out.println("Player : " + entry.getKey() / 1000 + ", Group size: " + entry.getKey() % 1000 +
//                    " Count : " + entry.getValue());
            scores[ entry.getKey() / 1000 - 1] *= Math.pow(entry.getKey() % 1000, entry.getValue());
        }
//        System.out.println("Points: " + Arrays.toString(scores));
    }

    // number of possible moves given current state
    public int numMoves() {
        int res = totalCells - usedCells;
        for (byte i = 1; i< numPlayer; i++) {
            res *= (totalCells - usedCells - i);
        }
        return res;
    }

    // Generate all legal moves
    public short[][] moveGen()  { // TODO: currently it's a naive enumeration of all possible movements
        short[][] moves = new short[numMoves()][numPlayer]; // return cell index for each color

        int cnt = 0;
        if (numPlayer ==2) { // array operations when numPlayer == 2
            for (short idxA = 0; idxA < totalCells; idxA++) {
                if (cells[idxA] == 0) {
                    for (short idxB = 0; idxB < totalCells; idxB++) {
                        if (cells[idxB] == 0 && idxB != idxA) {
                            moves[cnt][0] = idxA;
                            moves[cnt][1] = idxB;
                            cnt++;
                        }
                    }
                }
            }
        } else { // otherwise use more heavyweight to generate
            short[] perm = new short[numPlayer];

            short[] emptyCellsIdx = new short[totalCells - usedCells];
            short cntEmpty = 0;
            for (short idx = 0; idx < totalCells; idx++) {
                if (cells[idx]==0) {
                    emptyCellsIdx[cntEmpty] = idx;
                    cntEmpty++;
                }
            }
            c.permutation(moves, perm, 0, emptyCellsIdx, new AtomicInteger(0));
        }
        return moves;
    }

    // Check termination condition
    public boolean isTerminal() {
        return usedCells == (totalRounds() * numPlayer * numPlayer);
    }

    // getValue depends on player's perspective
    public int getValue() {
        return value;
    }

    //TODO: implement evaluation function
    //TODO: would this overflow in larger boards?
    public void eval(byte curPlayerIdx, byte fromTurn) {
        // in earlier turns, use
        if (fromTurn <= (totalTurns() / 2)) { // TODO: closed groups!
            value = 0;
            for (short entry : groupSizeCounter.keySet()) {
                if (groupSizeCounter.get(entry)!= 0) {
                    int pIdx = entry / 1000;
                    int size = entry % 1000;

                    if (size > 3) { // if too large groups are of my color, penalize
                        int val = (int) Math.pow(size-3, 2) * groupSizeCounter.get(entry);
                        value -= ((pIdx == curPlayerIdx+1) ? 1:-1) * Math.pow(size-3, 2) * groupSizeCounter.get(entry);
                    }
                    else if (size == 1 || size == 2) { // incentivize small groups
                        value += ((pIdx == curPlayerIdx+1) ? 1:-1) * groupSizeCounter.get(entry);
                    }
                }
            }
        } else { // in later turns, use the score directly
            value = (int) (scores[curPlayerIdx] - scores[getOpponentIdx(curPlayerIdx)]); // score different MAX-MIN
        }
        // flip value if it's a MIN node
        if ((curPlayerIdx+1) != nextPlayer()) { // current player is next player
            value = -value;
        }
    }

    public void setValue(int v) {
        value = v;
    }

    // number of total rounds
    public int totalRounds() {
        return totalCells / (numPlayer * numPlayer);
    }

    public int totalTurns() {
        return totalRounds() * numPlayer;
    }

    // which player's turn it is
    public byte nextPlayer() {
        return (byte) (usedCells % (numPlayer * numPlayer) / numPlayer + 1);
    }

    // next color
    public byte nextColor() {
        return (byte) (usedCells % numPlayer + 1);
    }

    public int currentRound() {
        return usedCells / (numPlayer * numPlayer) + 1;
    }

    public byte currentTurn() {
        return (byte) ((currentRound() - 1) * numPlayer + nextPlayer());
    }

    public byte turnsLeft() {
        return (byte) (totalTurns() - currentTurn());
    }

    public byte getBoardSize() {
        return size;
    }

    public byte getNumPlayer() {
        return numPlayer;
    }

    public short getTotalCells() {
        return totalCells;
    }

    public String getScore() {
        return Arrays.toString(scores);
    }

    protected short getRandNeighbor(short cell) {
        short idx;
        do {
            idx = (short) (Math.random() * adjList[cell].size());
        } while (cells[adjList[cell].get(idx)]!=0);
        return adjList[cell].get(idx);
    }

    public short getRandFarawayCell(short cell) {
        short res;
        if (cell>=30 && cell<=42)
            res = (short) (0);
        else if (18<=cell && cell<30) {
            res = (short) (cells.length-1);
        } else {
            res = (short) (cells.length - cell - 1);
        }
        if (cells[res] != 0) {
            return getRandNeighbor(res);
        }
        return res;
    }

    public byte getOpponentIdx(byte currentPlayer) {
        return (byte) (numPlayer - 1 - currentPlayer);
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public byte getFlag() {
        return flag;
    }


    public int getNumCells() {
        return cells.length;
    }
    @Override
    public int compareTo(State in) {
        if (value < in.getValue())
            return 1;
        else if (value > in.getValue())
            return -1;
        else
            return 0;
    }
}