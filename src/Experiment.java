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
//        for (int i=0; i<strategies.length; i++) {
//            for (int j=i+1; j<strategies.length; j++) {
                for (int k=0; k<REPEAT; k++) {
                    createExperiments(k, strategies);
//                    createExperiments(k, strategies);
//                }
//            }
        }
    }

    private void createExperiments(int run, SearchStrategy[] setups) {
//        String pair = "";
        String[] names = new String[setups.length];
//        SearchStrategy[] setups = new SearchStrategy[in.length];
        for (int i = 0; i < setups.length; i++) {
            names[i] = setups[i].getStrategyName();
        }
        String pair = String.join("_VS_" + names);

//        String pair = strategies[a].getStrategyName() + " VS " + strategies[b].getStrategyName();

        Path folder = Paths.get(System.getProperty("user.dir") , "exp", pair);

        Controller.createDirIfNotExist(Paths.get(System.getProperty("user.dir") , "exp", pair));

        c = new Controller();

        ArrayList<Short> res = c.start(setups);

        try {
            Controller.saveObject(res, Paths.get(folder.toString(), Integer.toString(run)).toString());
        } catch (Exception ex) {

        }
    }


    private void analyzeExperients() {

    }

    public static void main(String[] args) {
        Experiment exp;
        //TODO: here running
        // For two-player version
//        exp = new Experiment(  // Change instantiations for different experiment setup
//                new StrategyMonteCarlo(c, "Monte Carlo"),
//                new StrategyAlphaBetaIdMc(c, "a-b with mc"));
//        exp.runExperiments();

        //For multi-player version
        exp = new Experiment(  // Change instantiations for different experiment setup
                new StrategyRandom(c, "Random"),
                new StrategyRandom(c, "Random"),
                new StrategyMonteCarlo(c, "Monte Carlo"));
        exp.runExperiments();
    }
}