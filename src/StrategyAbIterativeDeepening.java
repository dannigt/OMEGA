import java.util.Hashtable;

public class StrategyAbIterativeDeepening extends SearchStrategy{
    private int cnt;
    private long startTime;
    private int timeLimit = 0;

    StrategyAbIterativeDeepening(Controller c, String name) {
        super(c, name);
    }
//
    @Override
    short[] getNextMove(State state, int milli, int pIdx) {
        cnt = 0;
        startTime = System.currentTimeMillis();
        timeLimit = milli;
        short[] curBestMove = new short[]{0, 0}; // store the most recent chosen move

        byte total_depth = state.turnsLeft();
        Hashtable<Byte, Hashtable<Long, Object>> tt = new Hashtable<>(total_depth);

//        Hashtable<Long, Object> tt= new Hashtable<Long, Object>(); // transposition table
        // for (turns left) to 0
        for (byte ply=1; ply < total_depth; ply++) {
            System.out.println("=====================PLY" + ply);
            // if out of time, break
            if ((System.currentTimeMillis() - startTime) > timeLimit) {
                break;
            }
            alphaBetaTT(state, ply, Integer.MIN_VALUE, Integer.MAX_VALUE, curBestMove, pIdx, tt);
            // order based on values
        }
        // do a-b search on current level
        // get values for all children
        // do search to next level

//        alphaBetaTT(state, state.turnsLeft(), Integer.MIN_VALUE, Integer.MAX_VALUE);

        System.out.println("Evaluated " + cnt + " child.");

        return curBestMove;
    }

    @Override
    boolean waitsForUI() {
        return false;
    }

    // alpha beta with TT
    private State alphaBetaTT(State sIn, byte depth, int alpha, int beta, short[] curBestMove, int pIndex,
                              Hashtable<Byte, Hashtable<Long, Object>> tt) {
//        olda = alpha; /* save original alpha value */
//        n =  retrieve(s) /* Transposition-table lookup */
//        if n.depth >= d then
//        if n.flag = Exact
//          return n.value;
//        elseif n.flag = LowerBound
//          alpha = max(alpha, n.value);
//        elseif n.flag = UpperBound
//          beta = min(beta, n.value);
//        if (alpha>=beta)
//          return n.value;
        if (tt.containsKey(depth)) {
            System.out.println("Blah");
            if (tt.get(depth).containsKey(sIn.getHashKey())) {
                System.out.println("found!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }

        if (sIn.isTerminal() || depth == 0) {
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

            int value = -alphaBetaTT(sChild, (byte) (depth - 1), -beta, -alpha, curBestMove, pIndex, tt).getValue();

            if (value > score) {
                score = value;
                curBestMove[0] = move[0];
                curBestMove[1] = move[1];
                sIn.setValue(value);
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

        // TODO: if reaching here, there was no TT entry. Need to store!
        //        store(s, bestMove, bestValue, flag, depth);


        return sIn;
    }
}