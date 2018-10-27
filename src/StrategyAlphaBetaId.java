import java.util.*;
import java.util.concurrent.*;

public class StrategyAlphaBetaId extends SearchStrategy {
    private long startTime;
    private int timeLimit;
    private Random random = new Random();
    private State bestState;

    StrategyAlphaBetaId(Controller c, String name) {
        super(c, name);
    }

    @Override
    short[] getNextMove(State state, int milli, byte pIndex) {
        startTime = System.currentTimeMillis();
        timeLimit = milli;

        byte currentTurn = state.currentTurn();
        if (currentTurn <=2) { // 0th or 1st turn, use opening book
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

            // do a-b search with current # of ply
            try {
                res = alphaBetaTT(state, ply, -60000, 60000, pIndex, tt, currentTurn, directChildren,
                        true, state.currentTurn());
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
    State getNextState(State state, int millisec, byte pIndex) {
//        return moves[(int) (Math.random() * moves.length)];
        return null;
    }

    private short[] openingBook(State s, byte pIx, byte currentTurn) {
        // pIndex + 1 is color
        short[] res = new short[]{0, 0};

        short opponentIdx = s.getOpponentIdx(pIx);

        if (currentTurn == 1) { // empty board
            res[pIx] = (short) (s.getTotalCells()/2); //0;
            res[opponentIdx] = 0; //(short) (s.getTotalCells()/2);
        }
        else { // 2nd turn onwards
            for (short i=0; i<s.getNumCells(); i++) {
                if (s.getCellContent(i) == pIx+1) { // choose place for my color
                    res[pIx] = s.getRandFarawayCell(i);
//                    res[pIx] = s.getRandNeighbor(i);
                } else if (s.getCellContent(i) == opponentIdx+1) { // for opponent's color
                    short ngb = s.getRandNeighbor(i);
                    while (res[pIx]== ngb) { // avoid putting on the same cell
                        ngb = s.getRandNeighbor(i);
                    }
                    res[opponentIdx] = ngb;
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
                              Hashtable<Byte, Hashtable<Long, State>> tt, byte curTurn,
                              ArrayList<State> directChildren, boolean isRoot, byte rootTurn) throws TimeoutException {
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

        if (sIn.isTerminal() || depth == 0) {
            sIn.eval(pIndex, rootTurn); // leaf node, eval and return myself.
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
                    directChildren, false, rootTurn).getValue();

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

    public short[] requestFallback(State state) {
        System.err.println("----------------------------------------------");
        return state.moveGen()[(int) (Math.random() * state.numMoves())];
    }

    private void shuffle(short[] array) {
        int n = array.length;
        // Loop over array.
        for (int i = 0; i < array.length; i++) {
            // Get a random index of the array past the current index.
            // ... The argument is an exclusive bound.
            //     It will not go past the array's end.
            int randomValue = i + random.nextInt(n - i);

            // Swap the random element with the present element.
            short randomElement = array[randomValue];
            array[randomValue] = array[i];
            array[i] = randomElement;
        }
    }
}