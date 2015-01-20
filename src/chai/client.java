package chai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import chesspresso.Chess;
import chesspresso.game.Game;
import chesspresso.pgn.PGNReader;
import chesspresso.pgn.PGNSyntaxError;
import chesspresso.position.Position;

public class client extends Application {

	private static final int PIXELS_PER_SQUARE = 64;
	private static final String welcomeMessage = "Moves can be made using algebraic notation;"
			+ " for example the command c2c3 would move the piece at c2 to c3.  \n";

	TextField commandField;
	TextArea logArea;

	BoardView boardView;
	ChessGame game;
	
	int firstTenMoves = 20;
	ArrayList<Game> openingBook = new ArrayList<Game>();
	boolean isHumanPlaying = false;
	int humanPlayer=0;


	// RandomMoveSource[] playerMoveSources;

	MoveMaker[] moveMaker;

	public static void main(String[] args) {

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("CS 76 Chess");
	
		//Setting up the opening book
		URL url = this.getClass().getResource("book.pgn");
		File f;
		try {
			f = new File(url.toURI());
			FileInputStream fis;
			fis = new FileInputStream(f);
			PGNReader pgnReader = new PGNReader(fis, "/chess/src/chai/book.pgn");
			// Hack: we know there are only 120 games in the opening book
			for (int i = 0; i < 120; i++) {
				//Add each game to the book
				Game games = pgnReader.parseGame();
				openingBook.add(games);
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PGNSyntaxError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		game = new ChessGame();
		// build the board
		boardView = new BoardView(game, PIXELS_PER_SQUARE);
		// build the text area for giving log info to user
		logArea = new TextArea();
		// logArea.setPrefColumnCount(50);
		logArea.setPrefRowCount(5);
		logArea.setEditable(false);
		logArea.setWrapText(true);
		log(welcomeMessage);
		// build the command entry text field
		commandField = new TextField();
		// request focus on the command field after the ui is built,
		// to get a blinking cursor
		Platform.runLater(new Runnable() {
			public void run() {
				commandField.requestFocus();
			}
		});

		// set up the movemakers for black and white players.
		// Movemakers handle getting input from an AI, from the keyboard, or
		// from a server, depending on which type is used.
		moveMaker = new MoveMaker[2];
		//moveMaker[Chess.WHITE] = new AIMoveMaker(new RandomAI(0));
		moveMaker[Chess.BLACK] = new AIMoveMaker(new RandomAI(1));

		 moveMaker[Chess.WHITE] = new TextFieldMoveMaker();
isHumanPlaying = true;
humanPlayer = 0;

		VBox vb = new VBox();
		vb.getChildren().addAll(boardView, logArea, commandField);
		vb.setSpacing(10);
		vb.setPadding(new Insets(10, 10, 10, 10));

		// add everything to a root stackpane, and then to the main window
		StackPane root = new StackPane();
		root.getChildren().add(vb);
		primaryStage.setScene(new Scene(root)); // , boardView.getPreferredWidth(),
																						// 600));
		primaryStage.show();

		// sets the game world's game loop (Timeline)
		Timeline timeline = new Timeline(1.0);
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().add(
				new KeyFrame(Duration.seconds(.05), new GameHandler()));
		timeline.playFromStart();
		timeline.playFromStart();

		// moveMaker = new AIMoveMaker(new RandomAI());

	}

	private void log(String logText) {
		logArea.appendText(logText + "\n");

	}

	// As time passes, handle the state of the game
	private class GameHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent e) {
			// System.out.println("timer fired");

			// System.out.println(boardView.ready());
			// setting activeMoveSource to null will cause a new one to be
			// created:

			MoveMaker mover = moveMaker[game.position.getToPlay()];
			if (mover.getState() == Worker.State.READY) {
				mover.start(game.position);
			} else if (mover.getState() == Worker.State.SUCCEEDED
					&& boardView.ready()) {

				// Move to be executed
				short move = 0;
				// In case there is a human player
				if (isHumanPlaying && game.position.getToPlay() == humanPlayer) {
					move = mover.getMove();
				} else {
					// Whether or not we found a move
					boolean foundMove = false;
					// Only do this in the first ten moves for a player
					firstTenMoves--;
					if (firstTenMoves <= 0) {
						move = mover.getMove();
						foundMove = true;
					} else {
						// Look for a game in the opening book containing such a position
						for (Game g : openingBook) {
							if (g.containsPosition(game.position)) {
								g.gotoPosition(game.position);
								move = g.getNextShortMove();
								foundMove = true;
								break;
							}
						}
					}
					// Didn't find the position in the book
					if (!foundMove)
						move = mover.getMove();
				}
				// Execute move
				boardView.doMove(move);

				// check for game over here
				if (game.position.isTerminal() || game.position.isMate()
						|| game.position.isStaleMate()) {
					log("End");
				}
				mover.reset();
			}

		}

	}

	private class TextFieldMoveMaker implements MoveMaker,
			EventHandler<ActionEvent> {

		private Worker.State state;
		short move;

		public TextFieldMoveMaker() {
			this.state = Worker.State.READY;
			commandField.setOnAction(this);
			move = 0;
		}

		@Override
		public void start(Position position) {
			// String[] players = {"WHITE", "BLACK"};
			// commandField.setPromptText("Your move," + players[position.getToPlay()]
			// + ".");

		}

		@Override
		public void reset() {
			commandField.setText("");
			this.state = Worker.State.READY;

		}

		@Override
		public State getState() {
			return state;
		}

		@Override
		public short getMove() {
			return move;
		}

		@Override
		public void handle(ActionEvent e) {
			String text = commandField.getText();
			if (text != null & text != "") {
				int fromSqi = Chess.strToSqi(text.charAt(0), text.charAt(1));
				int toSqi = Chess.strToSqi(text.charAt(2), text.charAt(3));

				move = game.findMove(fromSqi, toSqi);
				this.state = Worker.State.SUCCEEDED;

			}

		}

	}

	// AI

	private class AIMoveMaker implements MoveMaker {
		ChessAI ai;
		AIMoveTask moveTask;

		public AIMoveMaker(ChessAI ai) {
			super();
			this.ai = ai;
			this.moveTask = null;
		}

		public void start(Position position) {
			// moveTask = new AIMoveTask(ai, position);

			moveTask = new AIMoveTask(ai, new Position(position));
			moveTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					event.getSource().getException().printStackTrace();
				}
			});

			new Thread(moveTask).start();

		}

		public Worker.State getState() {

			if (moveTask == null)
				return Worker.State.READY;
			if (moveTask.getState() == Worker.State.READY)
				return Worker.State.RUNNING;
			return moveTask.getState();


		}

		public short getMove() {

			return moveTask.getValue();
		}

		public void reset() {
			this.moveTask = null;
		}

	}

}