import java.nio.file.Path;
import java.nio.file.Paths;
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
                String pair = strategies[i].getStrategyName() + "_" + strategies[j].getStrategyName();
                Path folder = Paths.get(System.getProperty("user.dir") , "exp", pair);
                Controller.createDirIfNotExist(Paths.get(System.getProperty("user.dir") , "exp", pair));

                for (int k=0; k<REPEAT; k++) {
                    c = new Controller();
                    System.out.println("Running experiment " + pair);
                    ArrayList<Short> res = c.start(strategies[i], strategies[j]);
//                    c.clear();
                    try {
                        Controller.saveObject(res, Paths.get(folder.toString(), Integer.toString(k)).toString());
                    } catch (Exception ex) {

                    }
                }
            }
        }
    }

    private void analyzeExperients() {

    }

    public static void main(String[] args) {

        Experiment experiment = new Experiment(
                new StrategyRandom(c, "random"),
//                new StrategyAlphaBeta(c, "a-b"),
                new StrategyAlphaBetaId(c, "a-b with id"));

        experiment.runExperiments();
    }
}
