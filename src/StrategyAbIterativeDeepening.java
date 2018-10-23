import javax.management.StandardEmitterMBean;
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

        byte totalDepth = state.turnsLeft();
        Hashtable<Byte, Hashtable<Long, State>> tt = new Hashtable<>(totalDepth);

//        Hashtable<Long, Object> tt= new Hashtable<Long, Object>(); // transposition table
        // for (turns left) to 0
        for (byte ply=2; ply < totalDepth; ply++) {
            System.out.println("=====================PLY" + ply);
            // if out of time, break
            if ((System.currentTimeMillis() - startTime) > timeLimit) {
                break;
            }
            // do a-b search with current # of ply
            alphaBetaTT(state, ply, Integer.MIN_VALUE, Integer.MAX_VALUE, curBestMove, pIdx, tt, totalDepth);
            // get values for all children
            // order based on values
            // do search to next level
        }

        System.out.println("Evaluated " + cnt + " child.");

        return curBestMove;
    }

    @Override
    boolean waitsForUI() {
        return false;
    }

    // alpha beta with TT
    private State alphaBetaTT(State sIn, byte depth, int alpha, int beta, short[] curBestMove, int pIndex,
                              Hashtable<Byte, Hashtable<Long, State>> tt, byte totalDepth) {
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

//        State stateTT = null;
        byte turn = (byte) (totalDepth-depth);
        if (tt.containsKey(turn)) {
            if (tt.get(turn).containsKey(sIn.getHashKey())) {
//                System.out.println("found!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                return tt.get(turn).get(sIn.getHashKey());
            }
        }
        // if flag == exact
        // return state
        // if flag == lower bound
//      // alpha = max(alpha, value)
        // else if flag = upper bound
        // beta = min(beta, value)
        // if (alpha >= beta)
        // state.setvalue(value)
        // return state

        if (sIn.isTerminal() || depth == 0) {
            sIn.eval(pIndex);
            return sIn;
        }

        int score = Integer.MIN_VALUE; // a-b always start with a max player

        //TODO: at root, the moves should be generated based on values from last ply
        short[][] all_moves = sIn.moveGen();

//        State bestChild = null;
        for (int child = 0; child < all_moves.length; child++) { //all_moves.length
            State sChild = new State(sIn); // copy states

            short[] move = all_moves[child];

            sChild.placePiece(move[0]);
            sChild.placePiece(move[1]);

            int value = -alphaBetaTT(sChild, (byte) (depth - 1), -beta, -alpha, curBestMove, pIndex, tt, totalDepth).getValue();

            if (value > score) {
                score = value;
                curBestMove[0] = move[0];
                curBestMove[1] = move[1];
                sIn.setValue(value);
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
        //	/* Traditional transposition table storing of bounds */ 
        //    	/* Fail-low result implies an upper bound */
        //
        // if (bestValue <= olda)
            //  flag = UpperBound;

        //	/* Fail-high result implies a lower bound */
        // elseif bestValue >= beta then flag = LowerBound;
        //	elseif  flag = Exact;
        //
        //	store(s, bestMove, bestValue, flag, depth);

        //        store(s, bestMove, bestValue, flag, depth);
        if (!tt.containsKey(turn)) {
            tt.put(turn, new Hashtable<>());
        }
        tt.get(turn).put(sIn.getHashKey(), sIn);

        return sIn;
    }
}