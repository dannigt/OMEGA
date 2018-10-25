import javax.management.StandardEmitterMBean;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.concurrent.TimeoutException;

public class StrategyAbIterativeDeepening extends SearchStrategy{
    private int cnt;
    private long startTime;
    private int timeLimit = 0;
    State bestState;
//    private Hashtable<Byte, Hashtable<Long, State>> tt = new Hashtable<>(); // current turn

    StrategyAbIterativeDeepening(Controller c, String name) {
        super(c, name);
    }

    private void cleanupTT() {
        // TODO: clear entries
        System.out.println("");
    }

    @Override
    short[] getNextMove(State state, int milli, byte pIndex) {
        Hashtable<Byte, Hashtable<Long, State>> tt = new Hashtable<>(); // current turn
//        System.out.println("=====================cells: " + state.cells.length+
//                " total rounds: " + state.totalRounds() +
//                " total turns: " + state.totalTurns() +
//                " turns left: " + state.turnsLeft() +
//                " next player: " + state.nextPlayer());

        if (state.totalTurns() - state.turnsLeft() < 2) { // 0th or 1st turn
            return openingBook(state, pIndex);
        }

        startTime = System.currentTimeMillis();
        timeLimit = milli;

        short[] curBestMove = new short[]{0,0}; // store the most recent chosen move

//        byte totalDepth = (byte) state.totalTurns();

        State res = null;
        // for (turns left) to 0
        for (byte ply=3; ply < 4; ply++) {
            cnt=0;
            // if out of time, break
            if ((System.currentTimeMillis() - startTime) > timeLimit) {
                break;
            }

            byte currentTurn = (byte) (state.totalTurns() - state.turnsLeft());
            for (byte i=currentTurn; i <= currentTurn+ply; i++) {
                if (!tt.containsKey(i)) {
                    tt.put(i, new Hashtable<Long, State>());
                }
            }

            System.out.println("=====================Search PLY" + ply + " player " + pIndex + " curTurn " + currentTurn);
            // do a-b search with current # of ply
            try {
                res = alphaBetaTT(state, ply, Integer.MIN_VALUE, Integer.MAX_VALUE, pIndex, tt, currentTurn);
            } catch (TimeoutException ex) {
                res = bestState;
            }
            // get values for all children
            // order based on values
            // do search to next level
            System.out.println("Reused " + cnt + " nodes in PLY " + ply);
        }

        for (byte i=0; i<res.cells.length; i++) {
            if (state.getCellContent(i)==0 && res.getCellContent(i)==1) {
                curBestMove[0] = i;
            } else if (state.getCellContent(i)==0 && res.getCellContent(i)==2) {
                curBestMove[1] = i;
            }
        }

        System.out.println(Arrays.toString(state.cells));
        System.out.println(Arrays.toString(res.cells));
        System.out.println(Arrays.toString(curBestMove));

        tt = null;
        return curBestMove;
    }

    State getNextState(State state, int millisec, byte pIndex) {
//        return moves[(int) (Math.random() * moves.length)];
        return null;
    }

    private short[] openingBook(State s, byte pIx) {
        // pIndex + 1 is color
        short[] res = new short[]{0, 0};

        short opponentIdx = s.getOpponentIdx(pIx);

        if (s.totalTurns() == s.turnsLeft()+1) { // empty board
            if (Math.random()<0.5) {
                res[pIx] = (short) (s.getTotalCells()-1);
                res[opponentIdx] = 0;
            } else {
                res[pIx] = 0;
                res[opponentIdx] = (short) (s.getTotalCells()-1);
            }
        }
        else { // 2nd turn
            for (short i=0; i<s.cells.length; i++) {
                if (s.cells[i] == pIx+1) { // choose place for my color
                    res[pIx] = s.getRandNeighbor(i);
                } else if (s.cells[i] == opponentIdx+1) {//for opponent's color
                    res[opponentIdx] = s.getRandNeighbor(i);
                }
            }
        }
        return res;
    }


    @Override
    boolean waitsForUI() {
        return false;
    }

    // alpha beta with TT
    private State alphaBetaTT(State sIn, int depth, int alpha, int beta, byte pIndex,
                              Hashtable<Byte, Hashtable<Long, State>>  tt, byte curTurn) throws TimeoutException {
        if ((System.currentTimeMillis() - startTime) > timeLimit) {
//            return sIn;
            throw new TimeoutException();
        }

        if (tt.get(curTurn).containsKey(sIn.getHashKey())) {
            cnt++;
            return tt.get(curTurn).get(sIn.getHashKey());
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
            sIn.eval(pIndex); // left node, eval and return myself.
//            System.out.println("terminal: " + sIn.isTerminal() + "|| depth 0: " + (depth == 0) + " || " + sIn.getValue());
            //TODO: this thing should know whether it's max or in node!
//            System.out.println("terminal: " + sIn.getValue());
            return sIn;
        }

        int bestValue = Integer.MIN_VALUE; // a-b always start with a max player

        short[][] all_moves = sIn.moveGen();

        State bestChild = null;
        for (int child = 0; child < all_moves.length; child++) { //all_moves.length

            State sChild = new State(sIn); // copy states
            sChild.placePiece(all_moves[child][0]);
            sChild.placePiece(all_moves[child][1]);
            if (child==0)
                bestChild=sChild;

            int value = -alphaBetaTT(sChild, depth - 1, -beta, -alpha, pIndex, tt, (byte)(curTurn+1)).getValue();

            if (value > bestValue) {
                bestValue = value;
                sChild.setValue(value); // set value of this child state
                bestChild = sChild;
                bestState = sChild;
            }
            if (bestValue > alpha) {
                alpha = bestValue;
            }
            if (bestValue >= beta) {
                break;
            }
        }

        tt.get(curTurn).put(sIn.getHashKey(), sIn);
//        System.out.println("size: "+ tt.get(turn).size());
        return bestChild;
    }
}