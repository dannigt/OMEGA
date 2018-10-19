import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class StrategyAb extends SearchStrategy{
    private short[] cur_best_move; // store the most recent chosen move
    private int cnt;
    private long startTime;
    private int time_limit = 0;

    StrategyAb(Controller c, String name) {
        super(c, name);
    }

    @Override
    short[] getNextMove(State state, int milli) {
        cnt = 0;
        startTime = System.currentTimeMillis();
        time_limit = milli;
        // choose something first
        cur_best_move = state.moveGen()[0];

        alphaBeta(state, state.turnsLeft(), Integer.MIN_VALUE, Integer.MAX_VALUE, cur_best_move);

        System.out.println("Evaluated " + cnt + " child.");
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        Future<short[]> future = executor.submit(() -> {
//            // Do you long running calculation here
//            alphaBeta(state, state.turnsLeft(), Integer.MIN_VALUE, Integer.MAX_VALUE);
//
//            return current_best_move;
//        });
//
//        try{
//            current_best_move = future.get(1, TimeUnit.SECONDS);
//        }  catch (final TimeoutException e) {
//            System.err.println("Calculation took to long");
//        } catch (final Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            executor.shutdownNow();
//            System.out.println(Arrays.toString(current_best_move));
//        }
////        finally {
//////            executor.shutdown();
////            if (state.getCellContent(current_best_move[0])!=0) {
////                System.out.println("!!!!!!!!!!!!!!!!!!!!!" + current_best_move[0]);
////            }
////            if (state.getCellContent(current_best_move[1])!=0) {
////                System.out.println("!!!!!!!!!!!!!!!!!!!!!" + current_best_move[1]);
////            }
////            System.out.println("~~~~~~~~~~~~~~~~~~~~" + Arrays.toString(current_best_move) + "~~~~~~~~~~~~~~~~~~~~~");
////
////        }
//        System.out.println("~~~~~~~~~~~~~~~~~~~~" + Arrays.toString(current_best_move) + "~~~~~~~~~~~~~~~~~~~~~");

        return cur_best_move;
    }

    @Override
    boolean waitsForUI() {
        return false;
    }

    private State alphaBeta(State s_in, int depth, int alpha, int beta, short[] current_best_move) {
        // stop recursion once time is out
        if ((System.currentTimeMillis() - startTime) > time_limit) {
            return s_in;
        }

        if (s_in.isTerminal() || depth == 0) {
            return new State(s_in);
        }

        int score = Integer.MIN_VALUE;

        short[][] all_moves = s_in.moveGen();
//        System.out.println("================CHILDREN COUNT:" + all_moves.length + "================");

        State best_child_state = null;
        for (int child = 0; child < all_moves.length; child++) { //all_moves.length
            State s_child = new State(s_in); // copy states

            short[] move = all_moves[child];

            //TODO: should search bypass controller and directly put piece in state???
            s_child.placePiece(move[0]);
            s_child.placePiece(move[1]);

            int value = -alphaBeta(s_child, depth - 1, -beta, -alpha, current_best_move).value();

            if (value > score) {
                score = value;
//                current_best_move = move;
                current_best_move[0] = move[0];
                current_best_move[1] = move[1];
                best_child_state = s_child;
                s_in.setValue(score);
                System.out.println("=============================================");
            }
            if (score > alpha) {
                alpha = score;
            }
            if (score >= beta) {
                break;
            }
        }
        cnt++;
//        System.out.println(cnt);

        return best_child_state;
    }
}
