//public class StrategyAbIterativeDeepening extends SearchStrategy{
//    private short[] curBestMove; // store the most recent chosen move
//    private int cnt;
//    private long startTime;
//    private int timeLimit = 0;
//    StrategyAbIterativeDeepening(Controller c, String name) {
//        super(c, name);
//    }
//
//    @Override
//    short[] getNextMove(State state, int milli, int pIdx) {
//        cnt = 0;
//        startTime = System.currentTimeMillis();
//        timeLimit = milli;
//        // choose something first
////        current_best_move = state.moveGen()[0];
//        byte total_depth = state.turnsLeft();
//        // for (turns left) to 0
//        for (byte ply=1; ply < total_depth; ply++) {
//            System.out.println("=====================PLY" + ply);
//            // if out of time, break
//            if ((System.currentTimeMillis() - startTime) > timeLimit) {
//                break;
//            }
//            alphaBeta(state, ply, Integer.MIN_VALUE, Integer.MAX_VALUE, curBestMove, pIdx);
//            // order based on values
//        }
//        // do a-b search on current level
//        // get values for all children
//        // do search to next level
//
////        alphaBeta(state, state.turnsLeft(), Integer.MIN_VALUE, Integer.MAX_VALUE);
//
//        System.out.println("Evaluated " + cnt + " child.");
//
//        return curBestMove;
//    }
//
//    @Override
//    boolean waitsForUI() {
//        return false;
//    }
//
//    private State alphaBeta(State sIn, int depth, int alpha, int beta, short[] curBestMove, int pIndex) {
//        // stop recursion if: 1) time is out, 2) is terminal
//        if ((System.currentTimeMillis() - startTime) > timeLimit || sIn.isTerminal() || depth == 0) {
//            return sIn;
//        }
//
//        int score = Integer.MIN_VALUE;
//
//        short[][] all_moves = sIn.moveGen();
//
////        State bestChild = null;
//        for (int child = 0; child < all_moves.length; child++) { //all_moves.length
//            State sChild = new State(sIn); // copy states
//
//            short[] move = all_moves[child];
//
//            sChild.placePiece(move[0]);
//            sChild.placePiece(move[1]);
//
//            int value = -alphaBeta(sChild, depth - 1, -beta, -alpha, curBestMove, pIndex).getValue(pIndex, depth==1);
//
//            if (value > score) {
//                score = value;
//                curBestMove[0] = move[0];
//                curBestMove[1] = move[1];
////                bestChild = sChild;
//                sIn.setValue(score); //TODO: should i set the getValue of the state that came it?????
////                System.out.println("=============================================");
//            }
//            if (score > alpha) {
//                alpha = score;
//            }
//            if (score >= beta) {
//                break;
//            }
//        }
//        cnt++;
//
//        return sIn;
//    }
//}
