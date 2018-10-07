public class ManualStrategy extends SearchStrategy {
    ManualStrategy(Controller c, String name) {
        super(c, name);
    }

    @Override
    short[] getNextMove(State state) {
        // Get two moves from UI. This is an ugly workaround
        return null;
    }

    @Override
    boolean waitsForUI() {
        return true;
    }
}
