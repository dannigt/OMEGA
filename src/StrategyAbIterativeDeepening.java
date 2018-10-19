public class StrategyAbIterativeDeepening extends SearchStrategy{
    private short[] current_best_move; // store the most recent chosen move
    private int cnt;
    private long startTime;
    private int time_limit = 0;

    StrategyAbIterativeDeepening(Controller c, String name) {
        super(c, name);
    }

    @Override
    short[] getNextMove(State state, int milli) {
        cnt = 0;
        startTime = System.currentTimeMillis();
        time_limit = milli;
        // choose something first
//        current_best_move = state.moveGen()[0];
        byte total_depth = state.turnsLeft();
        // for (turns left) to 0
        for (byte ply=1; ply < total_depth; ply++) {
            System.out.println("=====================PLY" + ply);
            // if out of time, break
            if ((System.currentTimeMillis() - startTime) > time_limit) {
                break;
            }
            alphaBeta(state, ply, Integer.MIN_VALUE, Integer.MAX_VALUE);
            // order based on values
        }
        // do a-b search on current level
        // get values for all children
        // do search to next level

        alphaBeta(state, state.turnsLeft(), Integer.MIN_VALUE, Integer.MAX_VALUE);

        System.out.println("Evaluated " + cnt + " child.");

        return current_best_move;
    }

    @Override
    boolean waitsForUI() {
        return false;
    }

    private State alphaBeta(State s_in, int depth, int alpha, int beta) {
        // stop recursion once time is out
        if ((System.currentTimeMillis() - startTime) > time_limit) {
            return s_in;
        }

        if (s_in.isTerminal() || depth == 0) {
            return new State(s_in);
        }

        int score = Integer.MIN_VALUE;

        short[][] all_moves = s_in.moveGen();
//        System.out.println("================CHILDREN COUNT:" + all_moves.length + "================");

        State best_child_state = null;
        for (int child = 0; child < all_moves.length; child++) { //all_moves.length
            State s_child = new State(s_in); // copy states

            // get current move
            short[] move = all_moves[child];
            s_child.placePiece(move[0]);
            s_child.placePiece(move[1]);

            int value = -alphaBeta(s_child, depth - 1, -beta, -alpha).value();

            if (value > score) {
                score = value;
                current_best_move = move;
                best_child_state = s_child;
                s_in.setValue(score);
            }
            if (score > alpha) {
                alpha = score;
            }
            if (score >= beta) {
                break;
            }
        }
        cnt++;
        // order all children
//        System.out.println(cnt);

        return best_child_state;
    }
}
