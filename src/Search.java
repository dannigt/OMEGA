public class Search {

    public int alphaBeta(State s_in, int depth, int alpha, int beta) {
//        State s = new State(s_in);
        State s = s_in;
        if (s.isTerminal() || depth == 0) {
            System.out.println("-----------------Terminal, eval score");
            return 0;
        }
        int score = Integer.MIN_VALUE;

        short[][] all_moves = s.moveGen();
//        State[] successors = new State[all_moves.length];
        System.out.println("successor count " + all_moves.length);
        //TODO: choose random child
        int child_chosen = (int) (Math.random() * all_moves.length);
        System.out.println(child_chosen);
        for (int child = child_chosen; child < child_chosen+1; child++) { //all_moves.length

            short[] move = all_moves[child];
            s.placePiece(move[0]);
            s.placePiece(move[1]);

            try
            {
                Thread.sleep(2000);
            }
            catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }

            int value = alphaBeta(s, depth - 1, -beta, -alpha);
            if (value > score) {
                score = value;
            }
            if (score > alpha) {
                alpha = score;
            }
            if (score >= beta) {
                break;
            }
        }
        return score;
    }
}