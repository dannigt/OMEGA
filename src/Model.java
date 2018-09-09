import java.util.ArrayList;

public class Model {
	private int board_size;
	public int[] cells; // empty is 0
//	private boolean[][] adj_mat; // from, to
	private ArrayList<Integer>[] adj_list;
	private int[] player_idx;
	private int cur_player_idx = 0;
	private int max_player_idx;
	
	public Model(int size, int num_player) {
//		player_idx = new int[num_player];
//		for (int i=0; i < num_player; i++) {
//			player_idx[i] = i;
//		}
		max_player_idx = num_player - 1;
		init(size);
	}
	
	private void init(int size) {
		this.board_size = size;
		int num_cells = (size*2+size-1)*size - size*2 + 1;
		cells = new int[num_cells];
//		adj_mat = new boolean[num_cells][num_cells];
		System.out.println(num_cells + " cells, ");
		
		adj_list = new ArrayList[num_cells];
		
		for (int i=0; i < adj_list.length; i++) {
			adj_list[i] = new ArrayList<Integer>();
		}
		
		int num_iters = size - 1;
		int cur_idx = 0;
		// build adjacency matrix
		for (int i=0 ; i < num_iters; i++) {
			int num_cells_per_row = size + i;
			for (int j=0; j < num_cells_per_row; j++) {
				// cross-row
				adj_list[cur_idx].add(cur_idx + num_cells_per_row);
				adj_list[cur_idx].add(cur_idx + num_cells_per_row + 1);
				adj_list[cur_idx + num_cells_per_row].add(cur_idx);
				adj_list[cur_idx + num_cells_per_row + 1].add(cur_idx);
				
//				adj_mat[cur_idx][cur_idx + num_cells_per_row] = true;
//				adj_mat[cur_idx + num_cells_per_row][cur_idx] = true;
//				adj_mat[cur_idx][cur_idx + num_cells_per_row + 1] = true;
//				adj_mat[cur_idx + num_cells_per_row + 1][cur_idx] = true;
			
//				System.out.println(cur_idx + ", " + (cur_idx + num_cells_per_row) + " and " + (cur_idx + num_cells_per_row + 1));
				int mirror_idx = num_cells - cur_idx - 1;
				
				adj_list[mirror_idx].add(mirror_idx - num_cells_per_row);
				adj_list[mirror_idx].add(mirror_idx - num_cells_per_row - 1);
				adj_list[mirror_idx - num_cells_per_row].add(mirror_idx);
				adj_list[mirror_idx - num_cells_per_row - 1].add(mirror_idx);
				
//				adj_mat[mirror_idx][mirror_idx - num_cells_per_row] = true;
//				adj_mat[mirror_idx - num_cells_per_row][mirror_idx] = true;
//				adj_mat[mirror_idx][mirror_idx - num_cells_per_row - 1] = true;
//				adj_mat[mirror_idx - num_cells_per_row - 1][mirror_idx] = true;
				
				// same row
				if (j < num_cells_per_row - 1) {
					adj_list[cur_idx].add(cur_idx + 1);
					adj_list[cur_idx + 1].add(cur_idx);
					adj_list[mirror_idx].add(mirror_idx - 1);
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
	
	public int getCellContent(int cell) {
		return cells[cell];
	}
	
	public void setCellContent(int cell, int player) {
		cells[cell] = player;
	}
	
	public ArrayList<Integer> getNeighbors(int cell) {
//		ArrayList<Integer> res = new ArrayList<Integer>();
//		for (int i=0; i < adj_mat[cell].length; i++) {
//			if (adj_mat[cell][i]) {
//				res.add(i);
//			}
//		}
		return adj_list[cell];
	}
	
	public boolean update(int cell, int value) {
		//TODO: check if it's legal to update cell index 'cell' with value
		return true;
	}
	
	public boolean isTermination() {
		//TODO: check for termination status
		return false;
	}
	
	public int nextPlayer() {
		if (cur_player_idx < max_player_idx) {
			cur_player_idx++;
		} else {
			cur_player_idx = 0;
		}
		return cur_player_idx;
	}
}
