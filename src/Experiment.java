import java.util.ArrayList;

public class Experiment {
    private final int REPEAT = 10;
    private SearchStrategy[] strategies;
    private static Controller c;

    private Experiment(SearchStrategy... strategies) {
        this.strategies = strategies;
    }

    private void runExperiments() {
        for (int i=0; i<strategies.length; i++) {
            for (int j=i+1; j<strategies.length; j++) {
//                for (int k=0; k<REPEAT; k++) {
                    System.out.println("Running experiment "
                            + strategies[i].getStrategyName() + " v.s. " + strategies[j].getStrategyName());
                    c.start(strategies[i], strategies[j]);
//                }
            }
        }
    }

    public static void main(String[] args) {
        c = new Controller();
        Experiment experiment = new Experiment(
                new StrategyRandom(c, "random"),
//                new StrategyAb(c, "a-b"),
                new StrategyAbIterativeDeepening(c, "a-b with id"));

        experiment.runExperiments();
    }
}
