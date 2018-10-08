public class StrategyRandom extends SearchStrategy {

    StrategyRandom(Controller c, String name) {
        super(c, name);
    }

    @Override
    public short[] getNextMove(State state) {
        short[][] moves = state.moveGen();
        return moves[(int) (Math.random() * moves.length)];
    }

    @Override
    boolean waitsForUI() {
        return false;
    }
}