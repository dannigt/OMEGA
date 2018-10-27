import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class State implements Comparable<State>  {
    protected byte[] cells; // Color placed in the cell. Empty is 0
    private short[] uf_parent;
    private short[] uf_size;
    private ArrayList<Short>[] adj_list;  // neighbor indices
    private byte num_player;
    private short total_cells;
    private short used_cells;
    private Controller c;
    private boolean sim = false; // is simulation
    private byte size;
    private HashMap<Short, Short> group_size_counter = new HashMap<>();
    private long[] scores;
    private int value = Integer.MIN_VALUE;
    private byte flag = Byte.MIN_VALUE;
    private long hashKey = 0; // hash key for transposition table

    //Constructors with default values
    public State(Controller c) {
        this(c, (byte) 5, (byte) 2);
    }

    public State(Controller c, byte size, byte num_player) {
        this.c = c;
        this.num_player = num_player;
        init(size);

    }

    // overloaded constructor for copying states
    public State(State s) {
        size = s.size;
        adj_list = s.adj_list;
        num_player = s.num_player;

        total_cells = s.total_cells;
        used_cells = s.used_cells;
        cells = s.cells.clone();

        uf_parent = s.uf_parent.clone();
        uf_size = s.uf_size.clone();

        group_size_counter = new HashMap<>(s.group_size_counter);

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

    private void init(byte size) {
        this.scores = new long[num_player];
        Arrays.fill(scores, 1);

        this.size = size;
        total_cells = (short) ((size * 2 + size - 1) * size - size * 2 + 1);
        used_cells = 0;
        cells = new byte[total_cells];
        uf_parent = new short[total_cells];
        Arrays.fill(uf_parent, (short) -1);
        uf_size = new short[total_cells];
        group_size_counter = new HashMap<>();
        System.out.println(total_cells + " cells, ");

        // Make adjacency list
        adj_list = new ArrayList[total_cells];
        for (short i = 0; i < adj_list.length; i++) {
            //at most 6 neighbours.
            adj_list[i] = new ArrayList<>(6);
        }

        short num_iters = (short) (size - 1);
        short cur_idx = 0;
        // build adjacency matrix
        for (short i = 0; i <= num_iters; i++) {
            int num_cells_per_row = size + i;
            for (byte j = 0; j < num_cells_per_row; j++) {
                if (i != num_iters) {
                    // cross-row
                    adj_list[cur_idx].add((short) (cur_idx + num_cells_per_row));
                    adj_list[cur_idx].add((short) (cur_idx + num_cells_per_row + 1));
                    adj_list[cur_idx + num_cells_per_row].add(cur_idx);
                    adj_list[cur_idx + num_cells_per_row + 1].add(cur_idx);

                    short mirror_idx = (short) (total_cells - cur_idx - 1);

                    adj_list[mirror_idx].add((short) (mirror_idx - num_cells_per_row));
                    adj_list[mirror_idx].add((short) (mirror_idx - num_cells_per_row - 1));
                    adj_list[mirror_idx - num_cells_per_row].add(mirror_idx);
                    adj_list[mirror_idx - num_cells_per_row - 1].add(mirror_idx);

                    // same row
                    if (j < num_cells_per_row - 1) {
                        adj_list[cur_idx].add((short) (cur_idx + 1));
                        adj_list[cur_idx + 1].add(cur_idx);
                        adj_list[mirror_idx].add((short) (mirror_idx - 1));
                        adj_list[mirror_idx - 1].add(mirror_idx);

                    }
                } else if (j < num_cells_per_row - 1) { // middle row
                    adj_list[cur_idx].add((short) (cur_idx + 1));
                    adj_list[cur_idx + 1].add(cur_idx);
                }
                cur_idx++;
            }
        }
        //TODO: no need to XOR all the empty cells. Start the same anyways
//        for (short i=0; i<cells.length; i++) {
//            hashKey ^= c.requestRands()[i][0];
//        }
    }

    public byte getCellContent(short cell) {
        return cells[cell];
    }

    public void placePiece(short cell) {
        byte nextColor = nextColor();
        cells[cell] = nextColor;
        used_cells++;
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
        while (p != uf_parent[p]) {
            uf_parent[p] = uf_parent[uf_parent[p]];    // path compression by halving
            p = uf_parent[p];
        }
        return p;
    }

    private int calcConnectedAreaSize(short cell) {
        // Set parent of this cell to itself
        uf_parent[cell] = cell;
        uf_size[cell] = 1;

        short key = (short) (cells[cell] * 1000 + 1);
        if (group_size_counter.containsKey(key)) {
            group_size_counter.put(key, (short) (group_size_counter.get(key) + 1));
        } else {
            group_size_counter.put(key, (short) 1);
        }

        // Go through neighbors of same color
        for (int neighbor : adj_list[cell]) {
            if (cells[cell] == cells[neighbor]) {
                int nbg_root = findRoot(neighbor);

                if (nbg_root != cell) {
                    // Decrement count for uf_size[nbg_root]
                    short key_nbg_root = (short) (cells[cell] * 1000 + uf_size[nbg_root]);

                    // Decrement count for
                    group_size_counter.put(key_nbg_root, (short) (group_size_counter.get(key_nbg_root) - 1));

                    // Update parent reference
                    uf_parent[nbg_root] = cell; // The newly added cell becomes parent of the neighbor

                    // Decrement count for uf_size[cell]
                    short key_cell = (short) (cells[cell] * 1000 + uf_size[cell]);
                    group_size_counter.put(key_cell, (short) (group_size_counter.get(key_cell) - 1));

                    // Increment counter
                    uf_size[cell] += uf_size[nbg_root];

                   // Increment count for uf_size[cell]
                    key_cell = (short) (cells[cell] * 1000 + uf_size[cell]);
                    if (group_size_counter.containsKey(key_cell)) {
                        group_size_counter.put(key_cell, (short) (group_size_counter.get(key_cell) + 1));
                    } else {
                        group_size_counter.put(key_cell, (short) 1);
                    }
                }
            }
        }

        return uf_size[cell];
    }

    private void calcScores() {
        Arrays.fill(scores, 1);
        for (Map.Entry<Short, Short> entry : group_size_counter.entrySet()) {
//            System.out.println("Player : " + entry.getKey() / 1000 + ", Group size: " + entry.getKey() % 1000 +
//                    " Count : " + entry.getValue());
            scores[ entry.getKey() / 1000 - 1] *= Math.pow(entry.getKey() % 1000, entry.getValue());
        }
//        System.out.println("Points: " + Arrays.toString(scores));
    }

    // number of possible moves given current state
    public int numMoves() {
        return (total_cells - used_cells) * (total_cells - used_cells - 1);
    }

    // Generate all legal moves
    //TODO: move to search strategy
    public short[][] moveGen()  { // TODO: currently it's a naive enumeration of all possible movements
        short[][] moves = new short[numMoves()][num_player]; // return cell index for each color

        int cnt = 0;
        for (short idx_a = 0; idx_a < total_cells; idx_a++) {
            if (cells[idx_a] == 0) {
                for (short idx_b = 0; idx_b < total_cells; idx_b++) {
                    if (cells[idx_b] == 0 && idx_b != idx_a) {
//                        System.out.println("has " + moves.length + ", but accessing " + cnt);
                        moves[cnt][0] = idx_a;
                        moves[cnt][1] = idx_b;
                        cnt++;
                    }
                }
            }
        }
        if (cnt != numMoves()) {
            System.exit(0);
        }
        return moves;
    }

    // Check termination condition
    public boolean isTerminal() {
        return used_cells == (totalRounds() * num_player * num_player);
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
            for (short entry : group_size_counter.keySet()) {
                if (group_size_counter.get(entry)!= 0) {
                    int pIdx = entry / 1000;
                    int size = entry % 1000;

                    if (size > 3) { // if too large groups are of my color, penalize
                        int val = (int) Math.pow(size-3, 2) * group_size_counter.get(entry);
                        value -= ((pIdx == curPlayerIdx+1) ? 1:-1) * Math.pow(size-3, 2) * group_size_counter.get(entry);
                    }
                    else if (size == 1 || size == 2) { // incentivize small groups
                        value += ((pIdx == curPlayerIdx+1) ? 1:-1) * group_size_counter.get(entry);
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
        return total_cells / (num_player * num_player);
    }

    public int totalTurns() {
        return totalRounds() * num_player;
    }

    // which player's turn it is
    public byte nextPlayer() {
        return (byte) (used_cells % (num_player * num_player) / num_player + 1);
    }

    // next color
    public byte nextColor() {
        return (byte) (used_cells % num_player + 1);
    }

    public int currentRound() {
        return used_cells / (num_player * num_player) + 1;
    }

    public byte currentTurn() {
        return (byte) ((currentRound() - 1) * num_player + nextPlayer());
    }

    public byte turnsLeft() {
        return (byte) (totalTurns() - currentTurn());
    }

    public byte getBoardSize() {
        return size;
    }

    public byte getNumPlayer() {
        return num_player;
    }

    public short getTotalCells() {
        return total_cells;
    }

    public String getScore() {
        return Arrays.toString(scores);
    }

    protected short getRandNeighbor(short cell) {
        short idx;
        do {
            idx = (short) (Math.random() * adj_list[cell].size());
        } while (cells[adj_list[cell].get(idx)]!=0);
        return adj_list[cell].get(idx);
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
        return (byte) (num_player - 1 - currentPlayer);
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public byte getFlag() {
        return flag;
    }

    @Override
    public int compareTo(State in) {
        /* For Ascending order*/
//        return this.value-in.getValue();

        /* For Descending order do like this */
        //return compareage-this.studentage;
        if (value < in.getValue())
            return 1;
        else if (value > in.getValue())
            return -1;
        else
            return 0;
//        return in.getValue()-this.value;
    }
}