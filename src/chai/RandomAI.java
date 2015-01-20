package chai;

import java.util.Random;

import chesspresso.position.Position;

public class RandomAI implements ChessAI {
	
	int depth = 6;
	int player;
	
	public RandomAI(int play){
		player  = play;
	}
	

	public short getMove(Position position) {
		//short [] moves = position.getAllMoves();
		//short move = moves[new Random().nextInt(moves.length)];
	
	

		
		ABTT ai = new ABTT(player);
		return ai.iterativeMiniMax(position, depth);
		
		//return move;
		
	}
	
	
	
	
}
