public abstract class SearchStrategy {
    String strategy_name;
    Controller c;

    SearchStrategy(Controller c, String name) {
        this.c = c;
        strategy_name = name;
    }

    abstract short[] getNextMove(State state, int millisec, byte pIndex);

    abstract short[] requestFallback(State state);

    abstract State getNextState(State state, int millisec, byte pIndex);

    String getStrategyName() {
        return strategy_name;
    }

    abstract boolean waitsForUI();
}