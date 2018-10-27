import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.concurrent.TimeoutException;

public class StrategyMonteCarlo extends SearchStrategy {
    private long startTime;
    private int timeLimit;
    private State bestState;
    private int simCnt = 10000;


    public StrategyMonteCarlo(Controller c, String name) {
        super(c, name);
    }
    @Override
    public short[] getNextMove(State state , int milli, byte pIdx) {
        startTime = System.currentTimeMillis();
        timeLimit = milli;

        int numMoves = state.numMoves();
        ArrayList<State> children = new ArrayList<State>(numMoves);

        short[][] moves = state.moveGen();

        for (int i=0; i < numMoves; i++) {
            State sChild = new State(state);
            for (int j=0; j < state.getNumPlayer(); j++) {
                sChild.placePiece(moves[i][j]);
            }
            children.add(sChild);
        }
        int[] sum = new int[numMoves];
        int[] cnt = new int[numMoves];

        byte currentTurn = state.currentTurn();
        if (currentTurn <=2) { // 0th or 1st turn, use opening book
            return openingBook(state, pIdx, state.currentTurn());
        }

        mc(pIdx, simCnt, children, numMoves, sum, cnt);

        System.out.println(Arrays.toString(sum));
        for (int i=0; i < numMoves; i++) {
//            System.out.println("sum " + sum + " cnt " + cnt);
            children.get(i).setValue(sum[i]);
        }

        Collections.sort(children);

//        for (State s : children) {
//            System.out.print(s.getValue() +",");
//        }
//        System.out.println("");

        State res = children.get(0);
        short[] curBestMove = new short[]{0,0};
        for (byte i=0; i< res.getNumCells(); i++) {
            if (state.getCellContent(i)==0 && res.getCellContent(i)==1) {
                curBestMove[0] = i;
            } else if (state.getCellContent(i)==0 && res.getCellContent(i)==2) {
                curBestMove[1] = i;
            }
        }

        return curBestMove;
    }

    private int playoutAndEval(State sIn, byte pIdx) {
        State s = new State(sIn);
        while(!s.isTerminal()) {
            short[] move = StrategyRandom.randMove(s);
            for (short piece : move) {
                s.placePiece(piece);
            }
        }
        s.eval(pIdx, true, false);
        return s.getValue();
    }

    private void mc(byte pIndex, int simCnt, ArrayList<State> children, int numMoves, int[] sum, int[] cnt) {
        System.out.println("MC for player " + pIndex);
        for (int i = 0; i < simCnt; i++) {
            if ((System.currentTimeMillis() - startTime) > timeLimit) {
                break;
            }
            for (int j = 0; j < numMoves; j++) {
                // TODO: no need to play out further if sum is too low (<-simCnt/2)
                sum[j] += playoutAndEval(children.get(j), pIndex)>0 ? 1: -1;
                cnt[j]++;
            }
//            System.out.println(i + ": " + sChild.getScore());
        }
    }

    @Override
    public State getNextState(State state, int millisec, byte pIndex) {
        return null;
    }
    @Override
    public short[] requestFallback(State state) {
        return state.moveGen()[(int) (Math.random() * state.numMoves())];
    }

    @Override
    boolean waitsForUI() {
        return false;
    }
}
