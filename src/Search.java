import java.io.Serializable;
import java.util.Arrays;

public class Search implements Serializable {
    private byte computer_player;
    private Controller c;
    private short[] chosen_move;

    //If computer is Max or Min player.
    public Search(Controller c) {
        this.c = c;
    }

    public short[] getNextMove(State state, short depth) {
        alphaBeta(state, depth, Integer.MAX_VALUE, Integer.MIN_VALUE);

        for (short i : chosen_move) {
            c.processCellClick(i, false);
        }
        return chosen_move;
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

//        s_in.placePiece(chosen_move[0]);
//        s_in.placePiece(chosen_move[1]);
        this.chosen_move = chosen_move;

        return s;
    }
}