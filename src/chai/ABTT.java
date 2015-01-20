package chai;

import java.util.HashMap;

import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.position.Position;

public class ABTT {
	public Position posit;
	public int player;
	public double bestVal;
	public int nodes;
	public int finalDepth;
	public int maxDepthReached;
	public int maximumDepth;

	public HashMap<Long, double[]> transpo;

	public ABTT(int play) {
		posit = new Position();
		// maximizing player
		player = play;
		bestVal = 0;
		nodes = 0;
		finalDepth = 0;
		maxDepthReached = 0;
		transpo = new HashMap<Long, double[]>();
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
	// Set the class variable maximumDepth
			maximumDepth = maxDepth;
		
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

		// For each possible move
		for (short m : posit.getAllMoves()) {
			// Reset the depth variable
			depth = mxDepth;
			try {
				// Make the move

				posit.doMove(m);

				// Increment the node count
				nodes++;
				// Now we need to figure out value of this move

				// Is the position in the table
				if (transpo.containsKey(posit.getHashCode())) {
					double blah[] = transpo.get(posit.getHashCode());
					// Is the value in the table "better"
					if (depth < blah[1]) {
						temp = blah[0];
					} else {
						// If not, then we will continue down the branch
						temp = mini(posit, depth, alpha, beta);
					}
				} else {
					// Position was not in the table
					temp = mini(posit, depth, alpha, beta);
				}

				// If the position was not in the table
				if (!transpo.containsKey(posit.getHashCode())) {
					// We want to add the value we found as well as the height
					transpo.put(posit.getHashCode(), new double[] { temp, (depth) });
				} else {
					// Position was in the table, but we found a value at a deeper height
					double blah2[] = transpo.get(posit.getHashCode());
					if ((depth) < blah2[1]) {
						System.out.print("Founded");
						transpo.put(posit.getHashCode(), new double[] { temp, depth });
					}
				}

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

		// Increment node count
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

		// Get all moves
		for (short m : position.getAllMoves()) {
			try {

				// Try a move
				position.doMove(m);

				// Check to see if it is in the table
				if (transpo.containsKey(position.getHashCode())) {
					double blah[] = transpo.get(position.getHashCode());
					// Is the value in the table more "valuable"
					if (depth < blah[1]) {
						v = blah[0];
					} else {
						// It is not
						v = Math.max(v, mini(position, depth, alpha, beta));
					}
				} else {
					// Position is not in the table
					v = Math.max(v, mini(position, depth, alpha, beta));
				}

				// Position was not in the table
				if (!transpo.containsKey(position.getHashCode())) {
					// Add in the value we found for the given position
					transpo.put(position.getHashCode(), new double[] { v, (depth) });
				} else {
					// We found a value at a deeper height; more "valuable"
					double blah2[] = transpo.get(position.getHashCode());
					if ((depth) < blah2[1]) {
					//	System.out.print("Founded");
						transpo.put(position.getHashCode(), new double[] { v, depth });
					}
				}
				// Undo the move to try another move
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

		// Increment node count
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

		// Try all possible moves
		for (short m : position.getAllMoves()) {
			try {
				// Try a move
				position.doMove(m);
				// Is the position in our transposition table
				if (transpo.containsKey(position.getHashCode())) {

					// It is
					double blah[] = transpo.get(position.getHashCode());
					// The value is informative, we use that value instead
					if (depth < blah[1]) {
						v = blah[0];
					} else {
						// It isn't valuable to use, search anyways
						v = Math.min(v, maxi(position, depth, alpha, beta));
					}
				} else {
					// Position was not in the table
					v = Math.min(v, maxi(position, depth, alpha, beta));
				}

				// Position is not in the table
				if (!transpo.containsKey(position.getHashCode())) {
					// We put in the value we found for future reference
					transpo.put(position.getHashCode(), new double[] { v, (depth) });
				} else {
					// We found a value at a deeper height
					double blah2[] = transpo.get(position.getHashCode());
					if ((depth) < blah2[1]) {
				//		System.out.print("Founded");
						transpo.put(position.getHashCode(), new double[] { v, (depth) });
					}
				}
				// Undo the move so we can try another
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