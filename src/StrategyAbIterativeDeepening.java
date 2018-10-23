import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.TimeoutException;

public class StrategyAbIterativeDeepening extends SearchStrategy{
    private short[] curBestMove; // store the most recent chosen move
    private int cnt;
    private long startTime;
    private int timeLimit = 0;
    private Hashtable<Long, Object> tt= new Hashtable<Long, Object>(); // transposition table

    StrategyAbIterativeDeepening(Controller c, String name) {
        super(c, name);
    }
//
    @Override
    short[] getNextMove(State state, int milli, int pIdx) {
        cnt = 0;
        startTime = System.currentTimeMillis();
        timeLimit = milli;

        byte total_depth = state.turnsLeft();
        // for (turns left) to 0
        for (byte ply=1; ply < total_depth; ply++) {
            System.out.println("=====================PLY" + ply);
            // if out of time, break
            if ((System.currentTimeMillis() - startTime) > timeLimit) {
                break;
            }
            alphaBeta(state, state.turnsLeft(), Integer.MIN_VALUE, Integer.MAX_VALUE, curBestMove, pIdx);
            // order based on values
        }
        // do a-b search on current level
        // get values for all children
        // do search to next level

//        alphaBeta(state, state.turnsLeft(), Integer.MIN_VALUE, Integer.MAX_VALUE);

        System.out.println("Evaluated " + cnt + " child.");

        return curBestMove;
    }

    @Override
    boolean waitsForUI() {
        return false;
    }

    // alpha beta with TT
    private State alphaBeta(State sIn, int depth, int alpha, int beta, short[] curBestMove, int pIndex) {
        if (sIn.isTerminal() || depth == 0) {
            // Only really eval here
            sIn.eval(pIndex);
            return sIn;
        }

        int score = Integer.MIN_VALUE; // a-b always start with a max player

        short[][] all_moves = sIn.moveGen();

//        State bestChild = null;
        for (int child = 0; child < all_moves.length; child++) { //all_moves.length
            State sChild = new State(sIn); // copy states

            short[] move = all_moves[child];

            sChild.placePiece(move[0]);
            sChild.placePiece(move[1]);

            int value = -alphaBeta(sChild, depth - 1, -beta, -alpha, curBestMove, pIndex).getValue();

            if (value > score) {
                score = value;
                curBestMove[0] = move[0];
                curBestMove[1] = move[1];
                sIn.setValue(value); //TODO: should i set the getValue of the state that came it?????
//                System.out.println("=============================================" + value);
//                System.out.println(depth + " | child:" + child + " | a:" + alpha + " | b:" + beta +
//                        " | best move:" + Arrays.toString(curBestMove) +
//                        " | time out:" + ((System.currentTimeMillis() - startTime) > timeLimit));
            }
            if (score > alpha) {
                alpha = score;
            }
            if (score >= beta) {
                break;
            }
        }
        cnt++;

        return sIn;
    }
}