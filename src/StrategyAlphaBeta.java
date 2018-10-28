import java.util.concurrent.TimeoutException;

public class StrategyAlphaBeta extends SearchStrategy{
    private long startTime;
    private int timeLimit;
    private State bestState;

    StrategyAlphaBeta(Controller c, String name) {
        super(c, name);
    }

    @Override
    State getNextState(State state, int millisec, byte pIndex) {
//        return moves[(int) (Math.random() * moves.length)];
        return null;
    }

    @Override
    short[] getNextMove(State state, int time, byte pIndex) {
//        cnt = 0;
        startTime = System.currentTimeMillis();
        timeLimit = time;

        byte currentTurn = (byte) (state.currentTurn()+1);
        if (currentTurn <=2) { // 0th or 1st turn, use opening book
            return openingBook(state, pIndex, state.currentTurn());
        }

        State res = null;
        try {
            res = alphaBeta(state, 3, -60000, 60000, pIndex, currentTurn, true,
                    state.currentTurn(), state.totalTurns());
        } catch (TimeoutException ex) {  // store the most recent best move
            res = bestState;
        }

        short[] curBestMove = new short[]{0,0};
        for (byte i=0; i< res.getNumCells(); i++) {
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

    private State alphaBeta(State sIn, int depth, int alpha, int beta, byte pIndex,
                            byte curTurn, boolean isRoot, byte rootTurn, byte totalTurns) throws TimeoutException{
        if ((System.currentTimeMillis() - startTime) > timeLimit) {
            throw new TimeoutException();
        }

        if (sIn.isTerminal() || depth == 0) {
            sIn.eval(pIndex, (rootTurn > (totalTurns / 4)), pIndex != sIn.nextPlayerIdx()); // leaf node, eval and return myself.
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

            int value = -alphaBeta(sChild, depth - 1, -beta, -alpha, pIndex, (byte)(curTurn+1), false,
                    rootTurn, totalTurns).getValue();

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

        return bestChild;
    }

    @Override
    public short[] requestFallback(State state) {
        return state.moveGen()[(int) (Math.random() * state.numMoves())];
    }
}
