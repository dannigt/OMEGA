import java.util.ArrayList;

public class Model {
    //	private int board_size;
    public byte[] cells; // Color placed in the cell. Empty is 0
    private ArrayList<Short>[] adj_list;  // neighbor indices
    private byte num_player;
    // cell index --> group represented by array list
    private ArrayList<Short>[] cell_group_map;

    public Model(short size, byte num_player) {
        this.num_player = num_player;
        init(size);
    }

    private void init(short size) {
//		this.board_size = size;
        short num_cells = (short) ((size*2+size-1)*size - size*2 + 1);
        cells = new byte[num_cells];
//		adj_mat = new boolean[num_cells][num_cells];
        System.out.println(num_cells + " cells, ");

        adj_list = new ArrayList[num_cells];
        cell_group_map = new ArrayList[num_cells];

        for (short i=0; i < adj_list.length; i++) {
            //at most 6 neighbours.
            adj_list[i] = new ArrayList<Short>(6);
        }

        short num_iters = (short) (size - 1);
        short cur_idx = 0;
        // build adjacency matrix
        for (short i=0 ; i < num_iters; i++) {
            int num_cells_per_row = size + i;
            for (byte j=0; j < num_cells_per_row; j++) {
                // cross-row
                adj_list[cur_idx].add((short) (cur_idx + num_cells_per_row));
                adj_list[cur_idx].add((short) (cur_idx + num_cells_per_row + 1));
                adj_list[cur_idx + num_cells_per_row].add(cur_idx);
                adj_list[cur_idx + num_cells_per_row + 1].add(cur_idx);

//				adj_mat[cur_idx][cur_idx + num_cells_per_row] = true;
//				adj_mat[cur_idx + num_cells_per_row][cur_idx] = true;
//				adj_mat[cur_idx][cur_idx + num_cells_per_row + 1] = true;
//				adj_mat[cur_idx + num_cells_per_row + 1][cur_idx] = true;

//				System.out.println(cur_idx + ", " + (cur_idx + num_cells_per_row) + " and " + (cur_idx + num_cells_per_row + 1));
                short mirror_idx = (short) (num_cells - cur_idx - 1);

                adj_list[mirror_idx].add((short) (mirror_idx - num_cells_per_row));
                adj_list[mirror_idx].add((short) (mirror_idx - num_cells_per_row - 1));
                adj_list[mirror_idx - num_cells_per_row].add(mirror_idx);
                adj_list[mirror_idx - num_cells_per_row - 1].add(mirror_idx);

//				adj_mat[mirror_idx][mirror_idx - num_cells_per_row] = true;
//				adj_mat[mirror_idx - num_cells_per_row][mirror_idx] = true;
//				adj_mat[mirror_idx][mirror_idx - num_cells_per_row - 1] = true;
//				adj_mat[mirror_idx - num_cells_per_row - 1][mirror_idx] = true;

                // same row
                if (j < num_cells_per_row - 1) {
                    adj_list[cur_idx].add((short) (cur_idx + 1));
                    adj_list[cur_idx + 1].add(cur_idx);
                    adj_list[mirror_idx].add((short) (mirror_idx - 1));
                    adj_list[mirror_idx - 1].add(mirror_idx);

//					adj_mat[cur_idx][cur_idx + 1] = true;
//					adj_mat[cur_idx + 1][cur_idx] = true;
//					adj_mat[mirror_idx][mirror_idx - 1] = true;
//					adj_mat[mirror_idx - 1][mirror_idx] = true;
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
        calcConnectedAreaSize(cell);
    }

    private int calcConnectedAreaSize(short cell) {
        int cnt_ngb_color = 0;
        ArrayList<Short> group = null;
        for (int neighbor : adj_list[cell]) {
            if (cells[cell] == cells[neighbor]) {
                cnt_ngb_color++;
                if (group == null) { // first connected group found
                    group = cell_group_map[neighbor];
                    group.add(cell); //modify group list by adding current cell
                } else if (!group.equals(cell_group_map[neighbor])){ // there has been a new group found. merge group
                    group.addAll(cell_group_map[neighbor]);
                    cell_group_map[neighbor] = group;
                }
                cell_group_map[cell] = group; //update reference to group
            }
        }
        if (cnt_ngb_color == 0) { // no neighbor of same color, area consisting of this piece alone
            System.out.println("adding intial list");
            group = new ArrayList<Short>(cells.length); // create a new list containing current cell
            group.add(cell);
            cell_group_map[cell] = group;
        }
        for (ArrayList<Short> g : cell_group_map) {
            if (g!=null)
                System.out.println(g);
        }
        return cell_group_map[cell].size();
    }

    public boolean isTermination() {
        //TODO: check for termination status
        return false;
    }

    private byte nextColor() {
        short cnt = 0;
        for (short cell : cells) {
            cnt = (cell == 0) ? cnt : (short)(cnt + 1);
        }
//		System.out.println("filled: " + cnt + ", player: " + (cnt % num_player + 1));
        return (byte) (cnt % num_player + 1);
    }
}