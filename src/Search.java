public class Search {

    public int alphaBeta(State s, int depth, int alpha, int beta) {
        if (s.isTerminal() || depth == 0) {
            System.out.println("-----------------Terminal, eval score");
            return 0;
        }
        int score = Integer.MIN_VALUE;

        short[][] all_moves = s.moveGen();
//        State[] successors = new State[all_moves.length];
        System.out.println("successor count " + all_moves.length);
        for (int child = 0; child < 1; child++) { //all_moves.length
            short[] move = all_moves[child];
            s.placePiece(move[0]);
            s.placePiece(move[1]);

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