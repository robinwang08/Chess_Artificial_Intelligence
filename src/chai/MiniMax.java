package chai;

import java.util.Random;

import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.position.Position;

public class MiniMax {
	public Position posit;
	public int player;
	public double bestVal;
	public int nodes;
	public int maxDepthReached;
	public int maximumDepth;

	public MiniMax(int play) {
		// Position object for MiniMax class for doing and undoing moves
		posit = new Position();
		// Whether the AI is black or white
		player = play;
		// Best value
		bestVal = 0;
		// Number of nodes explored
		nodes = 0;
		// Maximum depth reached
		maxDepthReached = 0;
	}

	public short iterativeDeepMiniMax(Position position, int maxDepth) {
		// Set the class variable maximumDepth
		maximumDepth = maxDepth;
		// Array of best moves
		short[] bMoves = new short[maxDepth];
		// Array of values corresponding to the best moves
		double[] bValues = new double[maxDepth];
		// Value for a checkmate
		double checkmate = Double.MAX_VALUE;
		// Best possible move to return
		short btMove = 0;
		// Value for the best possible move to return
		double btValue = 0.0;
		// Get the best move and value at each depth, up to the max depth
		for (int i = 1; i <= maxDepth; i++) {
			bMoves[i - 1] = miniMax(position, i);
			bValues[i - 1] = bestVal;
		}
		// Set best move/value to the best move/value at the deepest value
		btMove = bMoves[maxDepth - 1];
		btValue = bValues[maxDepth - 1];
		// If there was a checkmate at an earlier value, go for the checkmate
		for (int j = 0; j < maxDepth; j++) {
			if (bValues[j] == checkmate) {
				btMove = bMoves[j];
				btValue = bValues[j];
				break;
			}
		}
		// Print Node count for the AI
		System.out.println("Player " + player + " searched through " + nodes);
		System.out.println("Maximmum depth reached: " + maxDepthReached);
		System.out.println("Evaluation function value: " + btValue);
		// Return best possible move
		return btMove;
	}

	public short miniMax(Position position, int mxDepth) {
		// Best possible move to be returned
		short bestMove = 0;
		int depth;
		// Set posit to the current position; we want to manipulate this object
		posit.set(position);
		// Set value to be as small as possible
		double value = Double.NEGATIVE_INFINITY;
		// Temporary variable to compare values
		double temp;

		// Get all possible moves
		short[] allMoves = posit.getAllMoves();

		for (short m : allMoves) {
			// Reset the depth variable
			depth = mxDepth;
			try {
				// Make the move
				posit.doMove(m);
				// Increase node count
				nodes++;
				// Calculate the minimax value of this move
				temp = mini(posit, depth);
				// Check to see if it is the best move
				if (temp > value) {
					value = temp;
					bestMove = m;
				}
				// Undo the move so we can try another
				posit.undoMove();
			} catch (IllegalMoveException e) {
			}
		}
		// Return the best move
		bestVal = value;
		return bestMove;
	}

	public double maxi(Position position, int depth) {
		// Increment the node count
		nodes++;
		// Decrement the depth
		depth--;
		
		//Update the maximum depth reached
		if((maximumDepth-depth) > maxDepthReached){
			maxDepthReached = maximumDepth-depth;
		}
		
		// Passed cutoff test?
		if (cutOff(position, depth))
			// Then return the value
			return util(position);

		// Value is as small as possible
		double v = Double.NEGATIVE_INFINITY;
		// Try all possible moves
		for (short m : position.getAllMoves()) {
			try {
				// Maximize value for AI
				position.doMove(m);
				v = Math.max(v, mini(position, depth));
				position.undoMove();
			} catch (IllegalMoveException e) {
			}
		}
		// Return the value back up the tree
		return v;
	}

	public double mini(Position position, int depth) {
		// Increment the node count
		nodes++;
		// Decrement the depth
		depth--;
		
	//Update the maximum depth reached
			if((maximumDepth-depth) > maxDepthReached){
				maxDepthReached = maximumDepth-depth;
			}
			
			
		// Passed cutoff test?
		if (cutOff(position, depth))
			// Then return the value
			return util(position);
		// Value is as large as possible
		double v = Double.POSITIVE_INFINITY;
		// Try all possible moves
		for (short m : position.getAllMoves()) {
			try {
				// Minimize value for opposing player
				position.doMove(m);
				v = Math.min(v, maxi(position, depth));
				position.undoMove();
			} catch (IllegalMoveException e) {
			}
		}
		// Return the value back up the tree
		return v;
	}

	// When to cutoff
	public boolean cutOff(Position positi, int depth) {
		// Reached a maximum depth
		if (depth <= 0)
			return true;
		// Reached the end of the game: Checkmate or Stalemate
		if (positi.isTerminal() || positi.isMate() || positi.isStaleMate())
			return true;
		// No possible moves left
		if (positi.getAllMoves().length == 0)
			return true;
		// Search is not done
		return false;
	}

	// Utility function
	public double util(Position positi) {
		double value = 0.0;
		// AI was checkmated; loss
		if (positi.getToPlay() == player && positi.isMate()) {
			value = -(Double.MAX_VALUE);
		}
		// Opposing player is checkmated; win
		else if (positi.getToPlay() != player && positi.isMate()) {
			value = (Double.MAX_VALUE);
		}
		// Stalemate tie, no points
		else if (positi.isStaleMate())
			value = 0.0;
		else {
			// Otherwise use the evaluation function to calculate the position's value
			value = eval(positi);
		}
		// Return the minimax value or score
		return value;
	}

	public double eval(Position position) {
		double value = 0.0;
		// Get the correct score for the AI player
		if (player == position.getToPlay()) {
			// Get material score for number of pieces
			value = position.getMaterial();
			// Get domination score
			value += position.getDomination();
			//Being in check is bad, lower score
			if(position.isCheck()){
				value += -95;
			}	
/*			//Check all possible moves for AI player
			for(short m:position.getAllMoves()){
				// Pawn promotions are valuable
				if(Move.isPromotion(m))
					value += 100;
				// The more available capturing moves, the stronger the position
				if(Move.isCapturing(m))
					value += 100;
			}*/
		} else {
			value = -(position.getMaterial());
			value += -(position.getDomination());
			if(position.isCheck()){
				value += 95;
			}
/*		//Check all possible moves for opposing player
			for(short m:position.getAllMoves()){
				// Pawn promotions are bad
				if(Move.isPromotion(m))
					value += -100;
				// The more available capturing moves, the weaker the position
				if(Move.isCapturing(m))
					value += -100;
			}*/
		}
		// Add a random number between 0 and 1 and return the value
		return value + (Math.random());
	}

	static void shuffleArray(short[] ar) {
		Random rnd = new Random();
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			// Simple swap
			short a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

}