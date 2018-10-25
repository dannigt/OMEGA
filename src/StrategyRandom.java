public class StrategyRandom extends SearchStrategy {

    StrategyRandom(Controller c, String name) {
        super(c, name);
    }

    @Override
    public short[] getNextMove(State state , int millisec, byte pIdx) {
        short[][] moves = state.moveGen();

        try {
            Thread.sleep(10000);
        } catch (Exception ex) {

        }
        return moves[(int) (Math.random() * moves.length)];
    }

    public short[] requestFallback(State state) {
        return state.moveGen()[(int) (Math.random() * state.numMoves())];
    }

    State getNextState(State state, int millisec, byte pIndex) {
//        return moves[(int) (Math.random() * moves.length)];
        return null;
    }

    @Override
    boolean waitsForUI() {
        return false;
    }
}