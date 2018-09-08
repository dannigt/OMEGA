import java.util.ArrayList;

public class Model {
	private int board_size;
	private int[] cells; // empty is 0
	private boolean[][] adj_mat; // from, to
	
	public Model(int size) {
		init(size);
	}
	
	private void init(int size) {
		this.board_size = size;
		int num_cells = (size*2+size-1)*size - size*2 + 1;
		cells = new int[num_cells];
		adj_mat = new boolean[num_cells][num_cells];
		System.out.println(num_cells + " cells, ");
		
		int num_iters = size - 1;
		int cur_idx = 0;
		// build adjacency matrix
		for (int i=0 ; i < num_iters; i++) {
			int num_cells_per_row = size + i;
			for (int j=0; j < num_cells_per_row; j++) {
				// cross-row
				adj_mat[cur_idx][cur_idx + num_cells_per_row] = true;
				adj_mat[cur_idx + num_cells_per_row][cur_idx] = true;
				adj_mat[cur_idx][cur_idx + num_cells_per_row + 1] = true;
				adj_mat[cur_idx + num_cells_per_row + 1][cur_idx] = true;
			
//				System.out.println(cur_idx + ", " + (cur_idx + num_cells_per_row) + " and " + (cur_idx + num_cells_per_row + 1));
				int mirror_idx = num_cells - cur_idx - 1;
				adj_mat[mirror_idx][mirror_idx - num_cells_per_row] = true;
				adj_mat[mirror_idx - num_cells_per_row][mirror_idx] = true;
				adj_mat[mirror_idx][mirror_idx - num_cells_per_row - 1] = true;
				adj_mat[mirror_idx - num_cells_per_row - 1][mirror_idx] = true;
				// same row
				if (j < num_cells_per_row - 1) {
					adj_mat[cur_idx][cur_idx + 1] = true;
					adj_mat[cur_idx + 1][cur_idx] = true;
					adj_mat[mirror_idx][mirror_idx - 1] = true;
					adj_mat[mirror_idx - 1][mirror_idx] = true;
				}
				cur_idx++;
			}
		}
	}
	
	public ArrayList<Integer> getNeighbors(int cell) {
		ArrayList<Integer> res = new ArrayList<Integer>();
		for (int i=0; i < adj_mat[cell].length; i++) {
			if (adj_mat[cell][i]) {
				res.add(i);
			}
		}
		return res;
	}
	
	public boolean update(int cell, int value) {
		//TODO: check if it's legal to update cell index 'cell' with value
		return true;
	}
	
	public boolean isTermination() {
		//TODO: check for termination status
		return false;
	}
}
