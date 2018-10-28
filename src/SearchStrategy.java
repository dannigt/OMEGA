public abstract class SearchStrategy {
    String strategy_name;
    Controller c;

    SearchStrategy(Controller c, String name) {
        this.c = c;
        strategy_name = name;
    }

    abstract short[] getNextMove(State state, int millisec, byte pIndex);

    abstract short[] requestFallback(State state);

    abstract State getNextState(State state, int millisec, byte pIndex);

    String getStrategyName() {
        return strategy_name;
    }

    abstract boolean waitsForUI();

    protected short[] openingBook(State s, byte pIx, byte currentTurn) {
        // pIndex + 1 is color
        short[] res = new short[]{0, 0};

        short opponentIdx = s.getOpponentIdx(pIx);

        if (currentTurn == 1) { // empty board
//            res[pIx] = 0; //(short) (s.getTotalCells()/2); //0;
            for (byte i = 0; i < s.getNumPlayer(); i++) {
                if (i == pIx) {
                    res[i] = 0;
                } else {
                    res[i] = (short)((s.getTotalCells()-i)/2);
                }
            }
//            res[opponentIdx] = (short)(s.getTotalCells()/2);
        }
//        else { // 2nd turn onwards
//            for (short i=0; i<s.getNumCells(); i++) {
//                if (s.getCellContent(i) == pIx+1) { // choose place for my color
//                    res[pIx] = s.getRandFarawayCell(i);
////                    res[pIx] = s.getRandNeighbor(i);
//                } else if (s.getCellContent(i) == opponentIdx+1) { // for opponent's color
//                    short ngb = s.getRandNeighbor(i);
//                    while (res[pIx]== ngb) { // avoid putting on the same cell
//                        ngb = s.getRandNeighbor(i);
//                    }
//                    res[opponentIdx] = ngb;
//                }
//            }
//        }
        return res;
    }
}