public class ManualStrategy extends SearchStrategy {
    ManualStrategy(Controller c, String name) {
        super(c, name);
    }

    @Override
    short[] getNextMove(State state) {
        // Get two moves from UI.

        return null;
    }
}
