import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Experiment {
    private final int REPEAT = 2;
    private SearchStrategy[] strategies;
    private static Controller c;

    private Experiment(SearchStrategy... strategies) {
        this.strategies = strategies;
    }

    private void runExperiments() {
        for (int k=0; k<REPEAT; k++) {
            createExperiments(k, strategies);
        }
    }

    private void createExperiments(int run, SearchStrategy[] setups) {
        String[] names = new String[setups.length];
        for (int i = 0; i < setups.length; i++) {
            names[i] = setups[i].getStrategyName();
        }
        String pair = String.join("_VS_", names);

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
        String[] names = new String[strategies.length];
        for (int i = 0; i < strategies.length; i++) {
            names[i] = strategies[i].getStrategyName();
        }
        String pair = String.join("_VS_", names);

        Path folder = Paths.get(System.getProperty("user.dir") , "exp", pair);

        for (int run=0; run<REPEAT; run++)  {
            try {
                c.applyCache(Paths.get(folder.toString(), Integer.toString(run)).toString());
            } catch (Exception ex) {
//                System.out.println();
                ex.printStackTrace();
                System.err.println("Fail to load file." + (Paths.get(folder.toString(), Integer.toString(run)).toString()));
            }
        }
   }

    public static void main(String[] args) {
        Experiment exp;
        c = new Controller();
        //TODO: here running
        // For two-player version
//        exp = new Experiment(  // Change instantiations for different experiment setup
//                new StrategyMonteCarlo(c, "Monte Carlo"),
//                new StrategyAlphaBetaIdMc(c, "a-b with mc"));
//        exp.runExperiments();

        //For multi-player version
//        exp = new Experiment(  // Change instantiations for different experiment setup
//                new StrategyRandom(c, "Random"), // White: random
//                new StrategyRandom(c, "Random"), // Black: random
//                new StrategyMonteCarlo(c, "Monte Carlo")); // Red: MC
//        exp.runExperiments();

        /**
         * Uncomment the following for running and analyzing experiment.
         * If only analyzing experiments, leave the exp.runExperiments() commented out!
         * board size 5. a-b with ID - MC. 2-player.
         */
        exp = new Experiment(  // Change instantiations for different experiment setup
                new StrategyAlphaBetaId(c, "a-b with id"), // White
                new StrategyMonteCarlo(c, "Monte Carlo")); // Black
        exp.runExperiments();
        exp.analyzeExperients();

        /**
         * Uncomment the following for running and analyzing experiment.
         * If only analyzing experiments, leave the exp.runExperiments() commented out!
         * board size 5. a-b with ID - MC. 2-player.
         */
        exp = new Experiment(  // Change instantiations for different experiment setup
                 new StrategyMonteCarlo(c, "Monte Carlo"),// White
                new StrategyAlphaBetaId(c, "a-b with id")); // Black
        exp.runExperiments();
        exp.analyzeExperients();

        /**
         * Uncomment the following for running and analyzing experiment.
         * If only analyzing experiments, leave the exp.runExperiments() commented out!
         * board size 5. MC - random - random. 3 player.
         */
//        exp = new Experiment(  // Change instantiations for different experiment setup
//                new StrategyMonteCarlo(c, "Monte Carlo"), // Black
//                new StrategyRandom(c, "Random"), // White
//                new StrategyRandom(c, "Random")); // Red
//        exp.runExperiments();
//        exp.analyzeExperients();

        /**
         * Uncomment the following for running and analyzing experiment.
         * If only analyzing experiments, leave the exp.runExperiments() commented out!
         * board size 5. random - MC - random. 3 player.
         */
//        exp = new Experiment(  // Change instantiations for different experiment setup
//                new StrategyRandom(c, "Random"), // White
//                new StrategyMonteCarlo(c, "Monte Carlo"), // Black
//                new StrategyRandom(c, "Random")); // Red
//        exp.runExperiments();
//        exp.analyzeExperients();

        /**
         * Uncomment the following for running and analyzing experiment.
         * If only analyzing experiments, leave the exp.runExperiments() commented out!
         * board size 5. random - random - MC. 3 player.
         */
//        exp = new Experiment(  // Change instantiations for different experiment setup
//                new StrategyRandom(c, "Random"), // White
//                new StrategyRandom(c, "Random"), // Black
//                new StrategyMonteCarlo(c, "Monte Carlo")); // Red
//        exp.runExperiments();
//        exp.analyzeExperients();
    }
}