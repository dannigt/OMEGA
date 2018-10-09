public class StrategyAlphaBeta extends SearchStrategy{
    private short[] chosen_move; // store the most recent chosen move
    private int cnt;

    StrategyAlphaBeta(Controller c, String name) {
        super(c, name);
    }

    @Override
    short[] getNextMove(State state) {
        alphaBeta(state, state.turnsLeft(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        return chosen_move;
    }

    @Override
    boolean waitsForUI() {
        return false;
    }

    public State alphaBeta(State s_in, int depth, int alpha, int beta) {
//        System.out.println("==================================DEPTH" + depth + "==============================");
//        State s = new State(s_in);

        if (s_in.isTerminal() || depth == 0) {
            return s_in;
        }

        int score = Integer.MIN_VALUE;

        short[][] all_moves = s_in.moveGen();
        System.out.println("================CHILDREN COUNT:" + all_moves.length + "================");

        for (int child = 0; child < all_moves.length; child++) { //all_moves.length
            State s = new State(s_in);

            short[] move = all_moves[child];

            //TODO: should search bypass controller and directly put piece in state???
            s.placePiece(move[0]);
            s.placePiece(move[1]);

            int value = -alphaBeta(s, depth - 1, -beta, -alpha).value();

            if (value > score) {
                score = value;
                chosen_move = move;
                s_in.setValue(score);
                System.out.println("==============New value " + score);
            }
            if (score > alpha) {
                alpha = score;
            }
            if (score >= beta) {
                System.out.println("======================beta pruning=================");
                break;
            }
        }
//        this.chosen_move = chosen_move;
        cnt++;
        System.out.println(cnt);

        return s_in;
    }
}
