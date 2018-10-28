import java.util.ArrayList;
import java.util.Collections;

public class StrategyMonteCarlo extends SearchStrategy {
    private long startTime;
    private int timeLimit;
    private int simCnt = Integer.MAX_VALUE; // TODO: set it to max


    public StrategyMonteCarlo(Controller c, String name) {
        super(c, name);
    }
    @Override
    public short[] getNextMove(State state , int milli, byte pIdx) {
        startTime = System.currentTimeMillis();
        timeLimit = milli;

        int numMoves = state.numMovesSinglePiece(); //numMoves();//
        ArrayList<State> children = new ArrayList<State>(numMoves);
//
        generateDirectChildren(state, state.getNumPlayer(), children);
//
//        int numMoves = state.numMoves();
//        short[][] moves = state.generateDirectChildren();
//
//        for (int i=0; i < numMoves; i++) {
//            State sChild = new State(state);
//            for (int j=0; j < moves[0].length; j++) { // place pieces for of each color
//                sChild.placePiece(moves[i][j]);
//            }
//            children.add(sChild);
//        }
        int[] sum = new int[numMoves];
        int[] cnt = new int[numMoves];

        mc(pIdx, simCnt, children, numMoves, sum, cnt);

//        System.out.println(Arrays.toString(sum));
        for (int i=0; i < numMoves; i++) {
//            System.out.println("sum " + sum + " cnt " + cnt);
            children.get(i).setValue(sum[i]);
        }

        Collections.sort(children);

//        for (State s : children) {
//            System.out.print(s.getValue() +",");
//        }
//        System.out.println("");

//        System.out.println(Arrays.toString(res.));
        State res = children.get(0);
        short[] curBestMove = new short[state.getNumPlayer()];
        for (byte i=0; i< res.getNumCells(); i++) { // cell index
            for (short j=1; j <= state.getNumPlayer(); j++) {
                if (state.getCellContent(i)==0 && res.getCellContent(i)==j) {
                    curBestMove[j-1] = i;
                }
            }
        }
        return curBestMove;
    }

    private void generateDirectChildren(State state, byte depth, ArrayList<State> children) {
        if (depth==0) {
            children.add(state);
            return;
        }
        short[] moves = state.moveGenSinglePiece();
        for (short move : moves) { // place pieces for of each color
            State sChild = new State(state);
            sChild.placePiece(move);
            generateDirectChildren(sChild, (byte)(depth-1), children);
        }
    }

    public static State playoutAndEval(State sIn, byte pIdx) {
        State s = new State(sIn);
        while(!s.isTerminal()) {
            short[] move = StrategyRandom.randMove(s);
            for (int j=0; j < s.getNumPlayer(); j++) { // place pieces for of each color
                s.placePiece(move[j]);
            }
        }
        s.eval(pIdx, true, false);
        return s;
    }

    private void mc(byte pIndex, int simCnt, ArrayList<State> children, int numMoves, int[] sum, int[] cnt) {
        System.out.println("MC for player " + pIndex);
        for (int i = 0; i < simCnt; i++) {
            if ((System.currentTimeMillis() - startTime) > timeLimit) {
                break;
            }
            for (int j = 0; j < numMoves; j++) {
                // TODO: no need to play out further if sum is too low (<-simCnt/2)
//                playoutAndEval(children.get(j), pIndex);
                sum[j] += playoutAndEval(children.get(j), pIndex).getValue();
                cnt[j]++;
//
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
