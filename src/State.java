import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class State implements Serializable {
    public byte[] cells; // Color placed in the cell. Empty is 0
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
    private int value;

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
        adj_list = s.adj_list.clone();
        num_player = s.num_player;

        total_cells = s.total_cells;
        used_cells = s.used_cells;
        cells = s.cells.clone();

        uf_parent = s.uf_parent.clone();
        uf_size = s.uf_size.clone();

        group_size_counter = new HashMap<>(group_size_counter);
        sim = true;

        scores = s.scores.clone();
//        if (value != s.value) {
//            System.out.println(value + "<--" + s.value);
        value = s.value;
//        }

    }

    public void reset(byte size) {
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
    }

    public short getCellContent(short cell) {
        return cells[cell];
    }

    public void placePiece(short cell) {
        cells[cell] = nextColor();
        used_cells++;
        calcConnectedAreaSize(cell);
        calcScores();

        //TODO: if in search step, don't notify change
        if (!sim) {
            c.notifyChange();
        }
    }

    //TODO: also remove points
    public void unplacePiece(short cell) {
        cells[cell] = 0;
        used_cells--;
        c.notifyChange();
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
                  if (group_size_counter.containsKey(key_nbg_root))
                    // TODO: why does it break in simulation rounds?
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

//        if (!sim) {
//            System.out.println("==========================" + uf_size[cell]);
//            System.out.println(group_size_counter.toString());
//        }
        return uf_size[cell];
//        byte cur_player = cells[cell];
//        byte cnt_ngb_color = 0;
//        ArrayList<Short> group = null;
//        for (int neighbor : adj_list[cell]) {
//
//            if (cells[cell] == cells[neighbor]) {
//                cnt_ngb_color++;
//                if (group == null) { // first connected group found.
//                    group = cell_group_map[neighbor];
//                    group_size_counter.computeIfPresent((short) (cur_player * 1000 + cell_group_map[neighbor].size()),
//                            (k, v) -> (byte) (v - 1));
//                    group.add(cell); //modify group list by adding current cell
//                } else if (!group.equals(cell_group_map[neighbor])){// there has been a new group found. merge group
////                    System.out.println("------------------------NEW GROUP------------------------");
//                    group_size_counter.computeIfPresent((short) (cur_player * 1000 + cell_group_map[neighbor].size()),
//                            (k, v) -> (byte) (v - 1));
////                    groups.remove(cell_group_map[neighbor]);
//                    group.addAll(cell_group_map[neighbor]);
//                    cell_group_map[neighbor] = group;
//                }
//                cell_group_map[cell] = group; //update reference to group
//            }
//        }
//        if (cnt_ngb_color == 0) {// no neighbor of same color, area consisting of this piece alone
////            System.out.println("--------------------adding intial list-----------------");
//            group = new ArrayList<Short>(cells.length); // create a new list containing current cell
//            group.add(cell);
//            cell_group_map[cell] = group;
////            groups.add(group);
//        } else {
//            group_size_counter.merge((short) (cur_player * 1000 + group.size()), (byte) 1, (a, b) -> (byte) (a + b));
//        }
//            //        for (ArrayList<Short> g : cell_group_map) {
////            if (g!=null)
////                System.out.println(g);
////        }
//        return cell_group_map[cell].size();
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

    //TODO: implement evaluation function
    //TODO: would this overflow in larger boards?
    //TODO: now this only takes diff with another play (for 2-player game only)
    // getValue depends on player's perspective
    public int getValue() {
        // current player's score - the opponent
//        if (isLeftNode) {// if i'm the last depth
////            System.out.println("------------------------------" );
//            return (int) (scores[currentPlayer] - scores[num_player - 1 - currentPlayer]);
//        }
//        else {// if i'm not the last depth, my value should have been updated by my child nodes
//            return (int) (scores[currentPlayer] - scores[num_player - 1 - currentPlayer]);
//            System.out.println("---" + value);
        return value;
//        }
    }

    public void eval(int currentPlayer) {
        value = (int) (scores[currentPlayer] - scores[num_player - 1 - currentPlayer]);
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

    public int currentTurn() {
        return (currentRound() - 1) * num_player + 1;
    }

    public byte turnsLeft() {
        return (byte) (totalTurns() - currentTurn());
    }

    public byte getSize() {
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
}