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
                for (int k=0; k<REPEAT; k++) {
                    createExperiments(j, i, k);
                    createExperiments(i, j, k);
                }
            }
        }
    }

    private void createExperiments(int a, int b, int run) {
        String pair = strategies[a].getStrategyName() + " VS " + strategies[b].getStrategyName();

        Path folder = Paths.get(System.getProperty("user.dir") , "exp", pair);

        Controller.createDirIfNotExist(Paths.get(System.getProperty("user.dir") , "exp", pair));

        c = new Controller();

        ArrayList<Short> res = c.start(strategies[a], strategies[b]);

        try {
            Controller.saveObject(res, Paths.get(folder.toString(), Integer.toString(run)).toString());
        } catch (Exception ex) {

        }
    }


    private void analyzeExperients() {

    }

    public static void main(String[] args) {
        //TODO: here running
        Experiment experiment = new Experiment(  // Change instantiations for different experiment setup
                new StrategyMonteCarlo(c, "Monte Carlo"),
                new StrategyAlphaBetaIdMc(c, "a-b with mc"));
        experiment.runExperiments();
    }
}