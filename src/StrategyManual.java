public class StrategyManual extends SearchStrategy {
    StrategyManual(Controller c, String name) {
        super(c, name);
    }

    @Override
    short[] getNextMove(State state , int millisec, byte pIndex) {
        // Wait for two moves from UI. The get move action does not happen here.
        return null;
    }

    @Override
    boolean waitsForUI() {
        return true;
    }
}
