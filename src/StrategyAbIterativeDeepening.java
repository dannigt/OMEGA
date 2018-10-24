import javax.management.StandardEmitterMBean;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.concurrent.TimeoutException;

public class StrategyAbIterativeDeepening extends SearchStrategy{
    private int cnt;
    private long startTime;
    private int timeLimit = 0;

    StrategyAbIterativeDeepening(Controller c, String name) {
        super(c, name);
    }
//
    @Override
    short[] getNextMove(State state, int milli, int pIndex) {
        cnt = 0;
        startTime = System.currentTimeMillis();
        timeLimit = milli;

//        short[][] moves = state.moveGen();
        short[] curBestMove = new short[]{0,0}; // store the most recent chosen move

        byte totalDepth = state.turnsLeft();
        Hashtable<Byte, Hashtable<Long, State>> tt = new Hashtable<>(totalDepth);
        State res = null;

//        Hashtable<Long, Object> tt= new Hashtable<Long, Object>(); // transposition table
        // for (turns left) to 0
        for (int ply=3; ply < 4; ply++) {
            // if out of time, break
            if ((System.currentTimeMillis() - startTime) > timeLimit) {
                break;
            }
            System.out.println("=====================PLY" + ply);
            // do a-b search with current # of ply
            res = alphaBetaTT(state, ply, Integer.MIN_VALUE, Integer.MAX_VALUE, pIndex, tt, totalDepth);
            // get values for all children
            // order based on values
            // do search to next level
            System.out.println("Evaluated " + cnt + " nodes in PLY " + ply);

            for (byte i=0; i<res.cells.length; i++) {
                if (state.getCellContent(i)==0 && res.getCellContent(i)==1) {
                    curBestMove[0] = i;
                } else if (state.getCellContent(i)==0 && res.getCellContent(i)==2) {
                    curBestMove[1] = i;
                }
            }

        }
        System.out.println(Arrays.toString(state.cells));
        if (res==null) {
            System.err.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }
        System.out.println(Arrays.toString(res.cells));
        System.out.println(Arrays.toString(curBestMove));

//        System.exit(0);

        return curBestMove;
    }

    @Override
    boolean waitsForUI() {
        return false;
    }

    // alpha beta with TT
    private State alphaBetaTT(State sIn, int depth, int alpha, int beta, int pIndex,
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
//        byte turn = (byte) (totalDepth-depth);
//        if (tt.containsKey(turn)) {
//            if (tt.get(turn).containsKey(sIn.getHashKey())) {
////                System.out.println("found!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//                return tt.get(turn).get(sIn.getHashKey());
//            }
//        }
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
            sIn.eval(pIndex); // left node, eval and return myself.
//            System.out.println("terminal: " + sIn.getValue());
            return sIn;
        }

        int score = Integer.MIN_VALUE; // a-b always start with a max player

        short[][] all_moves = sIn.moveGen();

        State bestChild = null;
        for (int child = 0; child < all_moves.length; child++) { //all_moves.length
            State sChild = new State(sIn); // copy states
            if (child==0) {
                bestChild = sChild;
            }

//            short[] move = all_moves[child];
            sChild.placePiece(all_moves[child][0]);
            sChild.placePiece(all_moves[child][1]);

            int value = -alphaBetaTT(sChild, depth - 1, -beta, -alpha, pIndex, tt, totalDepth).getValue();

            if (value > score) {
                score = value;
                sChild.setValue(value); // set value of this child state
                bestChild = sChild;
//                System.out.println("New best: depth " + depth + " | value:" + value + " | score:" + score +
//                        " | child nr.:" + child +
//                        " | a:" + alpha + " | b:" + beta);
            }
            if (score > alpha) {
                alpha = score;
            }
            if (score >= beta) {
                break;
            }
            cnt++;

            //root
//            if (depth==4) {
//                System.out.println("child: " + child + " value: " + sChild.getValue());
//            }
        }
//        sIn = bestChild;

        if (bestChild==null) {
            System.err.println("===================================================null===============================");
        }
        return bestChild;


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
//        if (!tt.containsKey(turn)) {
//            tt.put(turn, new Hashtable<>());
//        }
//        tt.get(turn).put(sIn.getHashKey(), sIn);

//        System.out.println("size: "+ tt.get(turn).size());
    }
}