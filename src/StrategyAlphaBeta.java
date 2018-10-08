public class StrategyAlphaBeta extends SearchStrategy{
    private short[] chosen_move; // store the most recent chosen move

    StrategyAlphaBeta(Controller c, String name) {
        super(c, name);
    }

    @Override
    short[] getNextMove(State state) {
        alphaBeta(state, state.turnsLeft(), Integer.MAX_VALUE, Integer.MIN_VALUE);
        return chosen_move;
    }

    @Override
    boolean waitsForUI() {
        return false;
    }

    public State alphaBeta(State s_in, int depth, int alpha, int beta) {
        State s = new State(s_in);

        if (s.isTerminal() || depth == 0) {
            return s;
        }

        int score = Integer.MIN_VALUE;

        short[][] all_moves = s.moveGen();

        short[] chosen_move = all_moves[0];

        //TODO: currently choosing random child
        int child_chosen = (int) (Math.random() * all_moves.length);
//        System.out.println(child_chosen);
        for (int child = child_chosen; child < child_chosen+1; child++) { //all_moves.length
            short[] move = all_moves[child];

            //TODO: should search bypass controller and directly put piece in state???
            s.placePiece(move[0]);
            s.placePiece(move[1]);

            int value = alphaBeta(s, depth - 1, -beta, -alpha).value();

            if (value > score) {
                score = value;
                chosen_move = move;
            }
            if (score > alpha) {
                alpha = score;
            }
            if (score >= beta) {
                break;
            }
        }
        this.chosen_move = chosen_move;

        return s;
    }
}
