import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class State {
    //	private int board_size;
    private byte[] cells; // Color placed in the cell. Empty is 0
    private ArrayList<Short>[] adj_list;  // neighbor indices
    private byte num_player;
    // cell index --> group represented by array list
//    private ArrayList<Short>[] cell_group_map; // index: cell index, value: groups
//    private HashMap<Short, Byte> group_size_counter = new HashMap<Short, Byte>(); // key: group size, value: number of occurrences
//    private ArrayList<ArrayList<Short>> groups = new ArrayList<ArrayList<Short>>();
    private short total_cells;
    public short used_cells = 0;
    private Controller c;
    private boolean sim = false;

    public State(Controller c, byte size, byte num_player) {
        this.c = c;
        this.num_player = num_player;
        init(size);
    }

    public void reset(byte size) {
        init(size);
        c.notifyChange();
    }

    // overloaded constructor for copying
    public State(State s) {
        cells = s.cells.clone();
        adj_list = s.adj_list;
        num_player = s.num_player;
//        cell_group_map = s.cell_group_map.clone();
//        group_size_counter = new HashMap<>(s.group_size_counter);
        total_cells = s.total_cells;
        used_cells = s.used_cells;
//        empty_cells = s.empty_cells;
        sim = true;
//        c = s.c;
    }

    private void init(byte size) {
//		this.board_size = size;
        total_cells = (short) ((size*2+size-1)*size - size*2 + 1);
        used_cells = 0;
//        empty_cells = total_cells;
//        usable_cells = ;

        cells = new byte[total_cells];
//		adj_mat = new boolean[total_cells][total_cells];
        System.out.println(total_cells + " cells, ");

        adj_list = new ArrayList[total_cells];
//        cell_group_map = new ArrayList[total_cells];

        for (short i=0; i < adj_list.length; i++) {
            //at most 6 neighbours.
            adj_list[i] = new ArrayList<Short>(6);
        }

        short num_iters = (short) (size - 1);
        short cur_idx = 0;
        // build adjacency matrix
        for (short i=0 ; i <= num_iters; i++) {
            int num_cells_per_row = size + i;
            for (byte j=0; j < num_cells_per_row; j++) {
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
//        empty_cells--;
        used_cells++;
        calcConnectedAreaSize(cell);
//        calcScores();
        //TODO: in search step, don't notify change
        if (!sim)
            c.notifyChange();
    }

    private int calcConnectedAreaSize(short cell) {
        return 0;
//        byte cur_player = cells[cell];
//        byte cnt_ngb_color = 0;
//        ArrayList<Short> group = null;
//        for (int neighbor : adj_list[cell]) {
//
//            if (cells[cell] == cells[neighbor]) {
//                cnt_ngb_color++;
//                if (group == null) { // first connected group found. //TODO: enforce size ordering here
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


    private long[] calcScores() {
//        long[] points = new long[num_player];
//        Arrays.fill(points, 1);
//        for (Map.Entry<Short, Byte> entry : group_size_counter.entrySet()) {
////            System.out.println("Player : " + entry.getKey() / 1000 + ", Group size: " + entry.getKey() % 1000 +
////                    " Count : " + entry.getValue());
//            points[ entry.getKey() / 1000 - 1] *= Math.pow(entry.getKey() % 1000, entry.getValue());
//        }
//        System.out.println("Points: " + Arrays.toString(points));
        return new long[]{1, 1};
    }

    // number of possible moves given current state
    public int numMoves() {
        return (total_cells - used_cells) * (total_cells - used_cells - 1);
    }

    // Generate all legal moves
    public short[][] moveGen() { // TODO: currently it's a naive enumeration of all possible movements
        short[][] moves = new short[numMoves()][num_player]; // return cell index for each color

        int cnt = 0;
        for (short idx_a=0; idx_a < total_cells; idx_a++) {
            if (cells[idx_a] == 0) {
                for (short idx_b=0; idx_b < total_cells; idx_b++) {
                    if (cells[idx_b] == 0 && idx_b != idx_a) {
                        moves[cnt][0] = idx_a;
                        moves[cnt][1] = idx_b;
                        cnt++;
//                        return moves;
                    }
                }
            }
        }
        return moves;
    }

    // Check termination condition
    public boolean isTerminal() {
        return used_cells == (totalRounds() * num_player * num_player);
    }

    //TODO: implement evaluation function
    public int value() {
        return (int) Math.random() * 100;
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

    public int turnsLeft() {
        return totalTurns() - currentTurn();
    }
}