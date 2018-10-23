import java.util.ArrayList;

public class Experiment {
    int repeat = 10;
    SearchStrategy[] strategies;

    public Experiment(SearchStrategy... strategies) {
        this.strategies = strategies;
    }

    public void runExperiments() {
        for (int i=0; i<strategies.length; i++) {
            for (int j=i; j<strategies.length; j++) {
                System.out.println(strategies[i] + " v.s." + strategies[j]);
            }
        }
    }
}
