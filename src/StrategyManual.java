public class StrategyManual extends SearchStrategy {
    StrategyManual(Controller c, String name) {
        super(c, name);
    }

    @Override
    short[] getNextMove(State state , int millisec, int pIndex) {
        // Get two moves from UI. This is an ugly workaround
        return null;
    }

    @Override
    boolean waitsForUI() {
        return true;
    }
}
