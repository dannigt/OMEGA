import javax.management.StandardEmitterMBean;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class StrategyAlphaBetaId extends SearchStrategy{
//    private int cnt;
    private long startTime;
    private int timeLimit = 0;
    private Random random = new Random();
    State bestState;
//    private Hashtable<Byte, Hashtable<Long, State>> tt = new Hashtable<>(); // current turn

    StrategyAlphaBetaId(Controller c, String name) {
        super(c, name);
    }

    @Override
    short[] getNextMove(State state, int milli, byte pIndex) {
        startTime = System.currentTimeMillis();
        timeLimit = milli;

//        System.out.println("=====================cells: " + state.cells.length +
//                ", total rounds: " + state.totalRounds() +
//                ", total turns: " + state.totalTurns() +
//                ", current round: " + state.currentRound() +
//                ", current turn: " + state.currentTurn() +
//                ", next player: " + state.nextPlayer());

        byte currentTurn = state.currentTurn();
        if (currentTurn <=2) { // 0th or 1st turn
            return openingBook(state, pIndex, state.currentTurn());
        }

        ArrayList<State> directChildren = new ArrayList<State>(state.numMoves());
        for (short[] move : state.moveGen()) {
            State newChild = new State(state);
            newChild.placePiece(move[0]);
            newChild.placePiece(move[1]);
            directChildren.add(newChild);
        }

        short[] curBestMove = new short[]{0,0}; // store the most recent chosen move

        State res = null;
        // for (turns left) to 0
        for (byte ply=1; ply <= state.turnsLeft()+1; ply++) {
//            cnt=0;
            Hashtable<Byte, Hashtable<Long, State>> tt = new Hashtable<>(); // current turn
            // if out of time, break
            if ((System.currentTimeMillis() - startTime) > timeLimit) {
                break;
            }

            for (byte i=currentTurn; i <= currentTurn+ply; i++) {
                if (!tt.containsKey(i)) {
                    tt.put(i, new Hashtable<Long, State>());
                }
            }

            System.out.println("=====================Search PLY" + ply + " player " + pIndex + " curTurn " + currentTurn);
            // do a-b search with current # of ply
            try {
                res = alphaBetaTT(state, ply, -60000, 60000, pIndex, tt, currentTurn, directChildren,
                        true, state.currentTurn());
            } catch (TimeoutException ex) {
                res = bestState;
            }
//            System.out.println("Reused " + cnt + " nodes in PLY " + ply);
            tt = null;
            // sort nodes at the root based on value
            Collections.sort(directChildren);
            for (State s : directChildren) {
                System.out.print(s.getValue() + ",");
            }
//            System.out.println("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT" + (System.currentTimeMillis() - startTime));
        }

        for (byte i=0; i<res.cells.length; i++) {
            if (state.getCellContent(i)==0 && res.getCellContent(i)==1) {
                curBestMove[0] = i;
            } else if (state.getCellContent(i)==0 && res.getCellContent(i)==2) {
                curBestMove[1] = i;
            }
        }
        return curBestMove;
    }

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
            for (short i=0; i<s.cells.length; i++) {
                if (s.cells[i] == pIx+1) { // choose place for my color
                    res[pIx] = s.getRandFarawayCell(i);
//                    res[pIx] = s.getRandNeighbor(i);
                } else if (s.cells[i] == opponentIdx+1) { // for opponent's color
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
        //TODO: don't generate "all moves!!!"
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

//        if bestValue <= olda then flag = UpperBound;
//
//        /* Fail-high result implies a lower bound */ elseif bestValue >= beta then flag = LowerBound;
//        elseif  flag = Exact;
        if (bestValue <= alphaOld) {
            sIn.setFlag((byte)1);
        } else if (bestValue>=beta) {
            sIn.setFlag((byte)-1);
        } else {
            sIn.setFlag((byte)0);
        }

        tt.get(curTurn).put(sIn.getHashKey(), sIn);
//        System.out.println("size: "+ tt.get(curTurn).size());

        return bestChild;
    }

    public short[] requestFallback(State state) {
        System.err.println("---------------------");
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

//    private void generateMoves(State s, Stack<State> allChildren) {
//        short[][] all_moves = s.moveGen();
//        for (int child = 0; child < all_moves.length; child++) {
//            State sChild = new State(s); // copy states
//            sChild.placePiece(all_moves[child][0]);
//            sChild.placePiece(all_moves[child][1]);
//            allChildren.add(sChild);
//        }
//    }
}