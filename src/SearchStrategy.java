public abstract class SearchStrategy {
    String strategy_name;
    Controller c;

    SearchStrategy(Controller c, String name) {
        this.c = c;
        strategy_name = name;
    }

    abstract short[] getNextMove(State state, int millisec, byte pIndex);

    // In case of illegal move (e.g. hash collision), return a random move
    protected short[] requestFallback(State state) {
        return StrategyRandom.randMove(state);
    }

    String getStrategyName() {
        return strategy_name;
    }

    abstract boolean waitsForUI();

    protected short[] openingBook(State s, byte pIx, byte currentTurn) {
        // pIndex + 1 is color
        short[] res = new short[]{0, 0};

        if (currentTurn == 1) { // first round
            for (byte i = 0; i < s.getNumPlayer(); i++) {
                if (i == pIx) {
                    res[i] = 0; // put me int eh score
                } else {
                    res[i] = (short)((s.getTotalCells()-i)/2); // put opponent in center
                }
            }
        }
        return res;
    }
}