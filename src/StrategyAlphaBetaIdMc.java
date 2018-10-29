import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class StrategyAlphaBetaIdMc extends SearchStrategy {
    private long startTime;
    private int timeLimit;
    private Random random = new Random();
    private State bestState;

    StrategyAlphaBetaIdMc(Controller c, String name) {
        super(c, name);
    }

    @Override
    short[] getNextMove(State state, int milli, byte pIndex) {
        startTime = System.currentTimeMillis();
        timeLimit = milli;

        byte currentTurn = state.currentTurn();
        if (currentTurn == 1) { // 0th or 1st turn, use opening book
            return openingBook(state, pIndex, state.currentTurn());
        }

        ArrayList<State> directChildren = new ArrayList<State>(state.numMoves());
        for (short[] move : state.moveGen()) {
            State newChild = new State(state);
            newChild.placePiece(move[0]);
            newChild.placePiece(move[1]);
            directChildren.add(newChild);
        }

        State res = null;
        // for (turns left) to 0
        for (byte ply=1; ply <= state.turnsLeft()+1; ply++) {
            Hashtable<Byte, Hashtable<Long, State>> tt = new Hashtable<>(); // TT for current turn

            // prepare TT
            for (byte i=currentTurn; i <= currentTurn + ply; i++) {
                if (!tt.containsKey(i)) {
                    tt.put(i, new Hashtable<Long, State>());
                }
            }

            System.out.println("-------------------------PLY" + ply);
            // do a-b search with current # of ply
            try {
                res = alphaBetaTT(state, ply, Integer.MIN_VALUE, Integer.MAX_VALUE, pIndex, tt, currentTurn, directChildren,
                        true, 100);
            } catch (TimeoutException ex) {
                res = bestState;
                break;
            }
            // sort nodes at the root based on value
            Collections.sort(directChildren);
//            for (State s : directChildren) {
//                System.out.print("ply " +  ply + s.getValue() + ",");
//            }
        }

        short[] curBestMove = new short[]{0,0}; // store the most recent chosen move
        for (byte i=0; i<res.getNumCells(); i++) {
            if (state.getCellContent(i)==0 && res.getCellContent(i)==1) {
                curBestMove[0] = i;
            } else if (state.getCellContent(i)==0 && res.getCellContent(i)==2) {
                curBestMove[1] = i;
            }
        }
        return curBestMove;
    }

    @Override
    boolean waitsForUI() {
        return false;
    }

    // alpha beta with TT
    private State alphaBetaTT(State sIn, int depth, int alpha, int beta, byte pIndex,
                              Hashtable<Byte, Hashtable<Long, State>> tt, byte curTurn,
                              ArrayList<State> directChildren, boolean isRoot, int simCnt) throws TimeoutException {
        if ((System.currentTimeMillis() - startTime) > timeLimit) {
            throw new TimeoutException();
        }

        int alphaOld = alpha;
        if (tt.get(curTurn).containsKey(sIn.getHashKey())) {
//            cnt++;
            State entry = tt.get(curTurn).get(sIn.getHashKey());
            if (entry.getFlag()==0) {
                return entry;
            } else if (entry.getFlag()==-1) {
                alpha = Math.max(alpha, entry.getValue());
            } else if (entry.getFlag()==1) {
                beta = Math.min(beta, entry.getValue());
            }
            if (alpha>=beta) {
//                entry.setFlag((byte) 0);
                return entry;
            }
        }

        if (sIn.isTerminal()) { // eval terminal node
            sIn.eval(pIndex, true, pIndex!= sIn.nextPlayerIdx());
            return sIn;
        }
        if (depth==0) { // if not terminal node, but a leaf node, use MC eval
            long sum = 0;
            for (int i=0; i<simCnt; i++) {
                sum += StrategyMonteCarlo.playoutAndEval(sIn, pIndex).getValue();
            }
//            if (pIndex != sIn.nextPlayerIdx()) { // flip value if it's a MIN node
//                sum = -sum;
//            }
            sIn.setValue((int) (sum / simCnt));
            return sIn;
        }

        int bestValue = Integer.MIN_VALUE; // a-b always start with a max player

        short[][] all_moves = sIn.moveGen();
        State bestChild = null;

        for (int child = 0; child < all_moves.length; child++) { //all_moves.length
            State sChild;
            if (isRoot) { // get children based on value ordered
                sChild = directChildren.get(child);
            } else {
                sChild = new State(sIn); // copy states
                sChild.placePiece(all_moves[child][0]);
                sChild.placePiece(all_moves[child][1]);
//                while (allChildren.empty()) {
////                    System.out.print("wait");
//                }
//                synchronized (allChildren) {
//                    sChild = allChildren.pop();
//                }
//                System.out.print("-------------------------------------------");
            }

            if (child==0)
                bestChild=sChild;

            int value = -alphaBetaTT(sChild, depth - 1, -beta, -alpha, pIndex, tt, (byte)(curTurn+1),
                    directChildren, false, simCnt).getValue();

            if (value > bestValue) {
                bestValue = value;
                sChild.setValue(value); // set value of this child state
                bestChild = sChild;
                if (isRoot)
                    bestState = sChild;
            }
            if (bestValue > alpha) {
                alpha = bestValue;
            }
            if (bestValue >= beta) {
                break;
            }
        }

        if (bestValue <= alphaOld) { // if bestValue <= olda, flag = UpperBound;
            sIn.setFlag((byte)1);
        } else if (bestValue>=beta) { // if bestValue >= beta, flag = LowerBound;
            sIn.setFlag((byte)-1);
        } else {
            sIn.setFlag((byte)0); //  elseif  flag is exact;
        }

        tt.get(curTurn).put(sIn.getHashKey(), sIn);

        return bestChild;
    }
}