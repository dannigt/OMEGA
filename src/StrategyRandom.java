import java.util.Arrays;
import java.util.Random;

public class StrategyRandom extends SearchStrategy {
    private static Random random = new Random();

    StrategyRandom(Controller c, String name) {
        super(c, name);
    }

    @Override
    public short[] getNextMove(State state , int millisec, byte pIdx) {
        return randMove(state);
    }

    @Override
    boolean waitsForUI() {
        return false;
    }


    static short[] randMove(State state) {
        short[] indices = state.getEmptyCellsIdx();
        shuffle(indices);
        return Arrays.copyOfRange(indices, 0, state.getNumPlayer());
    }

    static void shuffle(short[] array) {
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