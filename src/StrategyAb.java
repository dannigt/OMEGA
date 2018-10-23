import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class StrategyAb extends SearchStrategy{
    private short[] curBestMove; // store the most recent chosen move
    private int cnt;
    private long startTime;
    private int timeLimit = 0;

    StrategyAb(Controller c, String name) {
        super(c, name);
    }

    @Override
    short[] getNextMove(State state, int time, int pIndex) {
        cnt = 0;
        startTime = System.currentTimeMillis();
        timeLimit = time;
        // choose something first
        short[][] moves = state.moveGen();

        curBestMove = moves[moves.length-1];

        try {
            alphaBeta(state, state.turnsLeft(), -60000, 60000, curBestMove, pIndex);
//            alphaBeta(state, state.turnsLeft(), Integer.MIN_VALUE, Integer.MAX_VALUE, curBestMove, pIndex);
        } catch (TimeoutException ex) {
            System.out.println("timeout");
        }

        System.out.println(Arrays.toString(curBestMove));
        System.out.println("Evaluated " + cnt + " children.");
//        System.exit(0);
        return curBestMove;
    }

    @Override
    boolean waitsForUI() {
        return false;
    }

    private State alphaBeta(State sIn, int depth, int alpha, int beta, short[] curBestMove, int pIndex) throws TimeoutException{
        // stop recursion if: 1) time is out, 2) is terminal
        if ((System.currentTimeMillis() - startTime) > timeLimit) {
//            return sIn;
            throw new TimeoutException();
        }

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
