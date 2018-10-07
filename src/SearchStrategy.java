public abstract class SearchStrategy {
    String strategy_name;
    Controller c;

    SearchStrategy(Controller c, String name) {
        this.c = c;
        strategy_name = name;
    }

    abstract short[] getNextMove(State state);


    String getStrategyName() {
        return strategy_name;
    }
}