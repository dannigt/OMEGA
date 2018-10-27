import java.util.Arrays;
import java.util.Random;

public class StrategyRandom extends SearchStrategy {
    private Random random = new Random();

    StrategyRandom(Controller c, String name) {
        super(c, name);
    }

    @Override
    public short[] getNextMove(State state , int millisec, byte pIdx) {
//        short[][] moves = ;
        short[] indices = state.getEmptyCellsIdx();
        shuffle(indices);
        return Arrays.copyOfRange(indices, 0, state.getNumPlayer());
    }
    @Override
    public short[] requestFallback(State state) {
        return null;
//        state.moveGen()[(int) (Math.random() * state.numMoves())];
    }

    @Override
    State getNextState(State state, int millisec, byte pIndex) {
//        return moves[(int) (Math.random() * moves.length)];
        return null;
    }

    @Override
    boolean waitsForUI() {
        return false;
    }

    void shuffle(short[] array) {
        int n = array.length;
        // Loop over array.
        for (int i = 0; i < array.length; i++) {
            // Get a random index of the array past the current index.
            int randomValue = i + random.nextInt(n - i);

            // Swap the random element with the present element.
            short randomElement = array[randomValue];
            array[randomValue] = array[i];
            array[i] = randomElement;
        }
    }
}