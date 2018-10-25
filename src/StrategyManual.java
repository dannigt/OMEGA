public class StrategyManual extends SearchStrategy {
    StrategyManual(Controller c, String name) {
        super(c, name);
    }

    @Override
    short[] getNextMove(State state , int millisec, byte pIndex) {
        // Get two moves from UI. This is an ugly workaround
        return null;
    }

    State getNextState(State state, int millisec, byte pIndex) {
        return null;
    }

    @Override
    boolean waitsForUI() {
        return true;
    }

    public short[] requestFallback(State state) {
        return state.moveGen()[(int) (Math.random() * state.numMoves())];
    }
}
