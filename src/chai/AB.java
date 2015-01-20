package chai;

import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.position.Position;

public class AB {
	public Position posit;
	public int player;
	public double bestVal;
	public int nodes;
	public int maxDepthReached;
	public int maximumDepth;

	public AB(int play) {
		posit = new Position();
		// maximizing player
		player = play;
		bestVal = 0;
		nodes = 0;
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
			bMoves[i - 1] = alphaBetaMiniMax(position, i);
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
	
	
	
	
public short iterativeMiniMax(Position position, int maxDepth) {
		
		short[] blah = new short[maxDepth];
		double[] blah2 = new double[maxDepth];
		double btValue = 0.0;

		double temp = Double.NEGATIVE_INFINITY;
		short btMove = 0;
		for (int i = 1; i <= maxDepth; i++) {
			blah[i - 1] = alphaBetaMiniMax(position, i);
			blah2[i - 1] = bestVal;
		}

		for (int j = 0; j < maxDepth; j++) {
			if (blah2[j] > temp) {
				temp = blah2[j];
				btMove = blah[j];
			}
		}

		// Print Node count for the AI
		System.out.println("Player " + player + " searched through " + nodes);
		System.out.println("Maximmum depth reached: " + maxDepthReached);
		System.out.println("Evaluation function value: " + temp);
		// Return best possible move
		return btMove;
	}



	public short alphaBetaMiniMax(Position position, int mxDepth) {
		// Best possible move
		short bestMove = 0;
		// Depth
		int depth;
		// Set posit to the current position; we want to manipulate this object
		posit.set(position);
		// Set value to be as small as possible
		double value = Double.NEGATIVE_INFINITY;
		// Temporary variables, alpha and beta
		double temp, alpha, beta;
		alpha = Double.NEGATIVE_INFINITY;
		beta = Double.POSITIVE_INFINITY;

		// Get all possible moves
		for (short m : posit.getAllMoves()) {
			// Reset the depth variable
			depth = mxDepth;
			try {
				// Make the move

				posit.doMove(m);

				// Increase the node count
				nodes++;

				// Calculate the minimax value of this move
				temp = mini(posit, depth, alpha, beta);
				// Check to see if it is the best move
				if (temp > value) {
					value = temp;
					bestMove = m;

				}
				// Undo the move so we can try another one
				posit.undoMove();
			} catch (IllegalMoveException e) {
			}
		}
		// Return our best move
		bestVal = value;
		return bestMove;
	}

	// maximizing player
	public double maxi(Position position, int depth, double alpha, double beta) {
		// Increment the node count
		nodes++;
		// Decrement the depth
		depth--;

		// Update the maximum depth reached
		if ((maximumDepth - depth) > maxDepthReached) {
			maxDepthReached = maximumDepth - depth;
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
				v = Math.max(v, mini(position, depth, alpha, beta));
				position.undoMove();

				// Pruning
				if (v >= beta) {
					return v;
				}
				alpha = Math.max(alpha, v);

			} catch (IllegalMoveException e) {
			}
		}
		return v;
	}

	public double mini(Position position, int depth, double alpha, double beta) {
		// Increment the node count
		nodes++;
		// Decrement the depth
		depth--;

		// Update the maximum depth reached
		if ((maximumDepth - depth) > maxDepthReached) {
			maxDepthReached = maximumDepth - depth;
		}

		// Passed cutoff test?
		if (cutOff(position, depth))
			// Then return the value
			return util(position);

		// Value is as large as possible
		double v = Double.POSITIVE_INFINITY;

		// Try all moves
		for (short m : position.getAllMoves()) {
			try {

				// Minimization
				position.doMove(m);
				v = Math.min(v, maxi(position, depth, alpha, beta));
				position.undoMove();

				// Pruning
				if (v <= alpha) {
					return v;
				}
				beta = Math.min(beta, v);

			} catch (IllegalMoveException e) {
			}
		}
		return v;
	}

	// When to cutoff
	public boolean cutOff(Position positi, int depth) {
		if (depth <= 0)
			return true;
		if (positi.isMate() || positi.isStaleMate() || positi.isTerminal())
			return true;
		if (positi.getAllMoves().length == 0)
			return true;
		return false;
	}

	// Utility function
	public double util(Position positi) {
		double value = 0.0;
		// Player is mated = loss
		if (positi.getToPlay() == player && positi.isMate()) {

			value = -(Double.MAX_VALUE);
		}
		// Mated the other player = win
		else if (positi.getToPlay() != player && positi.isMate()) {

			value = (Double.MAX_VALUE);
		}
		// Stalemate tie
		else if (positi.isStaleMate())
			value = 0.0;
		else {
			value = eval(positi);
		}
		return value;
	}

	public double eval(Position positi) {
		double value = 0.0;

		// you
		if (player == positi.getToPlay()) {
			value = positi.getMaterial();
		}
		// not you
		else {
			value = -positi.getMaterial();
		}

		return value + (Math.random() * 50);
	}

}