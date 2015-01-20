package chai;

import java.util.HashMap;

import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.position.Position;

public class ExtraFunctionality {
	public Position posit;
	public int player;
	public double bestVal;
	public int nodes;
	public int finalDepth;
	public double alpha, beta;
	
	public int maxDepthReached;
	public int maximumDepth;

	public HashMap<Long, double[]> transpo;
	public HashMap<Long, short[]> transBest;

	public ExtraFunctionality(int play) {
		posit = new Position();
		// maximizing player

		maxDepthReached = 0;
		player = play;
		bestVal = 0;
		nodes = 0;
		finalDepth = 0;
		transpo = new HashMap<Long, double[]>();
		transBest = new HashMap<Long, short[]>();
		alpha = Double.NEGATIVE_INFINITY;
		beta = Double.POSITIVE_INFINITY;
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

		short firstMove = 0;

		int depth = mxDepth;
		// Set a position object to be the current position, we want to
		// manipulate this position
		posit.set(position);
		// Set value to be as small as possible
		double value = Double.NEGATIVE_INFINITY;
		// Temporary variable

		double temp;
		alpha = Double.NEGATIVE_INFINITY;
		beta = Double.POSITIVE_INFINITY;

		// Move reordering
		// Find if best move is in table
		if (transBest.containsKey(posit.getHashCode())) {
			// Best move determined by the table
			short[] getPrevBestMove;
			getPrevBestMove = transBest.get(posit.getHashCode());
			// Was that move found at a sufficient height?
			if (getPrevBestMove[1] >= depth) {
				// Use this as the best move
				firstMove = getPrevBestMove[0];
				// Reset the depth variable
				depth = mxDepth;
				try {
					// Make the move
					posit.doMove(firstMove);
					nodes++;
					// Figure out value of this move

					// Is position in table
					if (transpo.containsKey(posit.getHashCode())) {
						double blah[] = transpo.get(posit.getHashCode());
						if (depth <= blah[1]) {
							// Better value in transposition table
							temp = blah[0];
						} else {
							// Not in the table
							temp = mini(posit, depth, alpha, beta);
						}
					} else {
						temp = mini(posit, depth, alpha, beta);
					}

					// Not in table - add it
					if (!transpo.containsKey(posit.getHashCode())) {
						transpo.put(posit.getHashCode(), new double[] { temp, (depth) });
					} else {
						// Discovered a deeper value, update the table
						double blah2[] = transpo.get(posit.getHashCode());
						if ((depth) <= blah2[1]) {
							transpo.put(posit.getHashCode(), new double[] { temp, depth });
						}
					}

					// Check if it is the best move
					if (temp > value) {
						value = temp;
						bestMove = firstMove;
					}
					// Undo the move so we can try another one
					posit.undoMove();
				} catch (IllegalMoveException e) {
				}
			}
		}

		// Try all capturing moves after best move provided by the table
		for (short m : posit.getAllCapturingMoves()) {
			// Move was checked already
			if (m == firstMove)
				continue;
			// Reset the depth variable
			depth = mxDepth;
			try {
				// Make the move
				nodes++;
				posit.doMove(m);
				// Figure out value of this move

				// Is it in table
				if (transpo.containsKey(position.getHashCode())) {
					double blah[] = transpo.get(posit.getHashCode());
					if (depth <= blah[1]) {
						// Better value in transposition table
						temp = blah[0];
					} else {
						// Not in the table
						temp = mini(posit, depth, alpha, beta);
					}
				} else {
					temp = mini(posit, depth, alpha, beta);
				}
				// Not in table - Update
				if (!transpo.containsKey(posit.getHashCode())) {
					transpo.put(posit.getHashCode(), new double[] { temp, (depth) });
				} else {
					// Got a deeper value
					double blah2[] = transpo.get(posit.getHashCode());
					if ((depth) <= blah2[1]) {
						transpo.put(posit.getHashCode(), new double[] { temp, depth });
					}
				}

				// Check if it is the best move
				if (temp > value) {
					value = temp;
					bestMove = m;
				}
				// Undo the move so we can try another one
				posit.undoMove();
			} catch (IllegalMoveException e) {
			}
		}

		// Now we try the moves that do not capture pieces
		for (short m : posit.getAllNonCapturingMoves()) {
			// Checked already
			if (m == firstMove)
				continue;
			// Reset the depth variable
			depth = mxDepth;
			try {
				// Make the move
				nodes++;
				posit.doMove(m);
				if (transpo.containsKey(position.getHashCode())) {
					double blah[] = transpo.get(posit.getHashCode());
					if (depth <= blah[1]) {
						temp = blah[0];
					} else {
						temp = mini(posit, depth, alpha, beta);
					}
				} else {
					temp = mini(posit, depth, alpha, beta);
				}
				if (!transpo.containsKey(posit.getHashCode())) {
					transpo.put(posit.getHashCode(), new double[] { temp, (depth) });
				} else {
					double blah2[] = transpo.get(posit.getHashCode());
					if ((depth) <= blah2[1]) {
						transpo.put(posit.getHashCode(), new double[] { temp, depth });
					}
				}
				if (temp > value) {
					value = temp;
					bestMove = m;
				}
				posit.undoMove();
			} catch (IllegalMoveException e) {
			}
		}
		// Return our best move
		bestVal = value;
		short sdepth = (short) depth;
		// Put best move into our move transposition table
		transBest.put(posit.getHashCode(), new short[] { bestMove, sdepth });
		return bestMove;
	}

	// maximizing player
	public double maxi(Position position, int depth, double alpha, double beta) {
		nodes++;
		depth--;
		short firstMove = 0;
		// Check to see if we cutoff
		if (cutOff(position, depth))

			return util(position);

		// else we go on
		double v = Double.NEGATIVE_INFINITY;

		if (transBest.containsKey(position.getHashCode())) {
			short[] getPrevBestMove;
			getPrevBestMove = transBest.get(position.getHashCode());
			if (getPrevBestMove[1] >= depth) {
				firstMove = getPrevBestMove[0];
				//System.out.println("Move Reorder //ed!");

				try {

					position.doMove(firstMove);

					// is in table
					if (transpo.containsKey(position.getHashCode())) {
						double blah[] = transpo.get(position.getHashCode());
						if (depth <= blah[1]) {
							// better value in transpo table
							v = blah[0];
						} else {
							v = Math.max(v, mini(position, depth, alpha, beta));
						}
					} else {

						v = Math.max(v, mini(position, depth, alpha, beta));
					}

					// not in table
					if (!transpo.containsKey(position.getHashCode())) {
						transpo.put(position.getHashCode(), new double[] { v, (depth) });
					} else {
						// got a deeper value
						double blah2[] = transpo.get(position.getHashCode());
						if ((depth) <= blah2[1]) {
							transpo.put(position.getHashCode(), new double[] { v, depth });
						}
					}
					position.undoMove();
					if (v >= beta) {
						return v;
					}
					alpha = Math.max(alpha, v);

				} catch (IllegalMoveException e) {
				}

			}

		}

		for (short m : position.getAllCapturingMoves()) {

			if (m == firstMove)
				continue;

			try {

				position.doMove(m);

				// is in table
				if (transpo.containsKey(position.getHashCode())) {
					double blah[] = transpo.get(position.getHashCode());
					if (depth <= blah[1]) {
						// better value in transpo table
						v = blah[0];
					} else {
						v = Math.max(v, mini(position, depth, alpha, beta));
					}
				} else {

					v = Math.max(v, mini(position, depth, alpha, beta));
				}

				// not in table
				if (!transpo.containsKey(position.getHashCode())) {
					transpo.put(position.getHashCode(), new double[] { v, (depth) });
				} else {
					// got a deeper value
					double blah2[] = transpo.get(position.getHashCode());
					if ((depth) <= blah2[1]) {
						transpo.put(position.getHashCode(), new double[] { v, depth });
					}
				}
				position.undoMove();
				if (v >= beta) {
					return v;
				}
				alpha = Math.max(alpha, v);

			} catch (IllegalMoveException e) {
			}
		}

		for (short m : position.getAllNonCapturingMoves()) {

			if (m == firstMove)
				continue;

			try {

				position.doMove(m);

				// is in table
				if (transpo.containsKey(position.getHashCode())) {
					double blah[] = transpo.get(position.getHashCode());
					if (depth <= blah[1]) {
						// better value in transpo table
						v = blah[0];
					} else {
						v = Math.max(v, mini(position, depth, alpha, beta));
					}
				} else {

					v = Math.max(v, mini(position, depth, alpha, beta));
				}

				// not in table
				if (!transpo.containsKey(position.getHashCode())) {
					transpo.put(position.getHashCode(), new double[] { v, (depth) });
				} else {
					// got a deeper value
					double blah2[] = transpo.get(position.getHashCode());
					if ((depth) <= blah2[1]) {
						transpo.put(position.getHashCode(), new double[] { v, depth });
					}
				}
				position.undoMove();
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
		nodes++;
		depth--;

		short firstMove = 0;
		if (cutOff(position, depth))

			return util(position);

		double v = Double.POSITIVE_INFINITY;

		if (transBest.containsKey(position.getHashCode())) {
			short[] getPrevBestMove;
			getPrevBestMove = transBest.get(position.getHashCode());
			if (getPrevBestMove[1] >= depth) {
				firstMove = getPrevBestMove[0];
				//System.out.println("Move Reorder //ed!");

				try {
					position.doMove(firstMove);
					// is in table
					if (transpo.containsKey(position.getHashCode())) {

						double blah[] = transpo.get(position.getHashCode());
						if (depth <= blah[1]) {
							// better value in transpo table
							v = blah[0];
						} else {
							v = Math.min(v, maxi(position, depth, alpha, beta));
						}
					} else {

						v = Math.min(v, maxi(position, depth, alpha, beta));
					}

					// not in table
					if (!transpo.containsKey(position.getHashCode())) {
						transpo.put(position.getHashCode(), new double[] { v, (depth) });
					} else {
						// got a deeper value
						double blah2[] = transpo.get(position.getHashCode());
						if ((depth) <= blah2[1]) {
							transpo.put(position.getHashCode(), new double[] { v, (depth) });
						}
					}

					position.undoMove();
					if (v <= alpha) {
						return v;
					}
					beta = Math.min(beta, v);

				} catch (IllegalMoveException e) {
				}
			}
		}

		for (short m : position.getAllCapturingMoves()) {

			if (m == firstMove)
				continue;

			try {
				position.doMove(m);
				// is in table
				if (transpo.containsKey(position.getHashCode())) {

					double blah[] = transpo.get(position.getHashCode());
					if (depth <= blah[1]) {
						// better value in transpo table
						v = blah[0];
					} else {
						v = Math.min(v, maxi(position, depth, alpha, beta));
					}
				} else {

					v = Math.min(v, maxi(position, depth, alpha, beta));
				}

				// not in table
				if (!transpo.containsKey(position.getHashCode())) {
					transpo.put(position.getHashCode(), new double[] { v, (depth) });
				} else {
					// got a deeper value
					double blah2[] = transpo.get(position.getHashCode());
					if ((depth) <= blah2[1]) {
						transpo.put(position.getHashCode(), new double[] { v, (depth) });
					}
				}

				position.undoMove();
				if (v <= alpha) {
					return v;
				}
				beta = Math.min(beta, v);

			} catch (IllegalMoveException e) {
			}
		}

		for (short m : position.getAllNonCapturingMoves()) {

			if (m == firstMove)
				continue;

			try {
				position.doMove(m);
				// is in table
				if (transpo.containsKey(position.getHashCode())) {

					double blah[] = transpo.get(position.getHashCode());
					if (depth <= blah[1]) {
						// better value in transpo table
						v = blah[0];
					} else {
						v = Math.min(v, maxi(position, depth, alpha, beta));
					}
				} else {

					v = Math.min(v, maxi(position, depth, alpha, beta));
				}

				// not in table
				if (!transpo.containsKey(position.getHashCode())) {
					transpo.put(position.getHashCode(), new double[] { v, (depth) });
				} else {
					// got a deeper value
					double blah2[] = transpo.get(position.getHashCode());
					if ((depth) <= blah2[1]) {
						transpo.put(position.getHashCode(), new double[] { v, (depth) });
					}
				}

				position.undoMove();
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