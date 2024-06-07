package com.examplegg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;


import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * The class <b>Main</b> is a Sudoku game that can be played using a GUI
 * provided by JavaFX.
 * 
 * @author Benoît
 *
 */
public class App extends Application {

	private int value = 0;
	private long countUp = 0;

	private BorderPane root;
	private Scene scene;
	private GridPane table;
	private Sudoku sudoku;

	private ArrayList<Integer> board, untouched;
	private Map<Integer, Button> boardText, numButtons;
	private Map<Integer, GridPane> grid;

	private HBox hbox;
	private Button clear, newGame;

	private Timeline timeline;

	private Stage stage;
	private SudokuClient sudokuClient;

	/**
	 * Changes the CSS ids of the horizontal line
	 * 
	 * @param array
	 *            an Array of CSS ids
	 * @param start
	 *            the horizontal line number
	 */
	private void changeHorizontalIds(String[] array, int start) {
		for (int i = start * 9; i < start * 9 + 9; i++) {
			changeIdsHelper(array, i);
		}
	}

	/**
	 * Changes the CSS ids of the vertical line
	 * 
	 * @param array
	 *            an Array of CSS ids
	 * @param start
	 *            the vertical line number
	 */
	private void changeVerticalIds(String[] array, int start) {
		for (int i = start; i < start + 9 * 9; i += 9) {
			changeIdsHelper(array, i);
		}
	}

	/**
	 * Changes the CSS ids of a specific Sudoku board element according to its
	 * original state
	 * 
	 * @param array
	 *            an Array of CSS ids
	 * @param i
	 *            the location of the button in the Sudoku board
	 */

	private void changeIdsHelper(String[] array, int i) {
		if (!(boardText.get(i).getText()).equals(String.valueOf(value)) || value == 0) {
			if (untouched.get(i) != 0) {
				boardText.get(i).setId(array[0]);
			} else if (board.get(i) != 0) {
				boardText.get(i).setId(array[1]);
			} else {
				boardText.get(i).setId(array[2]);
			}
		} else {
			boardText.get(i).setId(array[3]);
		}
	}

	/**
	 * Resets the game
	 */
	private void reset() {
		// Removes every buttons (GridPane) inside the main GridPane
		for (int i = 0; i < 9; i++) {
			table.getChildren().remove(grid.get(i));
		}

		// Creates a new Sudoku board for the player
		sudoku.clear();
		sudoku.generateBoard();
		sudoku.generatePlayer();

		// Print out the solution
		System.out.println(sudoku.toString());

		// Get player's board
		board = sudoku.getPlayer();

		// List and maps of Buttons, GridPanes and value of the board
		untouched = new ArrayList<Integer>(board);
		boardText = new HashMap<Integer, Button>();
		grid = new HashMap<Integer, GridPane>();
	}

	/**
	 * Returns the number of elements of the specified number
	 * 
	 * @param num
	 *            the number researched
	 * @return a number of elements equal to the parameter
	 */
	private int getNum(int num) {
		int count = 0;
		for (int p = 0; p < 81; p++) {
			if (Integer.valueOf(boardText.get(p).getText()) == num) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Generates the board, in terms of GUI
	 */
	private void generateBoard() {
		// Each block
		for (int i = 0; i < 9; i++) {

			grid.put(i, new GridPane());

			int t = i % 3 * 3 + (i / 3) * 27;
			int temp = 0;

			// Each element in that block
			for (int j = t; j < t + 20; j += 9, temp++) {

				// Each row of the block
				for (int k = 0; k < 3; k++) {

					// Index of current element
					final int pos = j + k;
					System.out.println("GG" + pos + "GG");
					// New Button
					boardText.put(pos, new Button());

					boardText.get(pos).setText(String.valueOf(board.get(pos)));
					grid.get(i).add(boardText.get(pos), k, temp);
				}
			}
			table.add(grid.get(i), i % 3, i / 3);
		}
	}

	private ArrayList<Integer> arrayOfZeroes(int n) {
		ArrayList<Integer> res = new ArrayList<>();
		for(int i=0; i<n; i++) res.add(0);
		return res;
	}

	private void makeBoard() {
		try {
			board = sudokuClient.getBoard();
		} catch (Exception e) {
			e.printStackTrace();
			board = arrayOfZeroes(81);
		}
		
	}

	void endProgram() {
		sudokuClient.endProgram();
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		// Creates a reference to the primaryStage to be
		// able to manipulate it in other methods
		stage = primaryStage;

		// Clear button
		clear = new Button("Send Solution");
		clear.setOnAction(e -> {
			String response  = sudokuClient.sendSolution(board);
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        	alert.setTitle("Message");
        	alert.setHeaderText(response);
			alert.showAndWait();
		});

		// New game button
		newGame = new Button("New Game");
		newGame.setOnAction(e -> {
			makeBoard();
			// Generates the GUI for the board
			generateBoard();
		});

		Button newGame1 = new Button("Solve Puzzle");
		newGame1.setOnAction(e -> {
			sudokuClient.solve(board);
			generateBoard();
		});
		

		// Layout of the board
		table = new GridPane();
		table.setVgap(8);
		table.setHgap(8);
		table.setPadding(new Insets(16, 0, 20, 0));
		table.setAlignment(Pos.CENTER);

		// Layout of the top two buttons
		hbox = new HBox();
		hbox.setSpacing(10);
		hbox.setPadding(new Insets(16, 0, 0, 0));
		hbox.setAlignment(Pos.CENTER);
		hbox.getChildren().addAll(newGame, clear, newGame1);

		// Main layout of the Game
		root = new BorderPane();
		root.setTop(hbox);
		root.setCenter(table);
	
		// List and maps of buttons, GridPanes and value of the board
		boardText = new HashMap<Integer, Button>();
		grid = new HashMap<Integer, GridPane>();
		numButtons = new HashMap<Integer, Button>();

		sudokuClient = new SudokuClient();
		makeBoard();
		// Generates the GUI for the board
		generateBoard();

		// Sets the scene to the BorderPane layout and links the CSS file
		scene = new Scene(root, 350, 450);
		//scene.getStylesheets().add("application.css");

		// Sets the stage, sets its title, displays it, and restricts its minimal size
		primaryStage.setScene(scene);
		primaryStage.setTitle("Sudoku");
		primaryStage.show();
		primaryStage.setMinHeight(primaryStage.getHeight());
		primaryStage.setMinWidth(primaryStage.getWidth());
		//endProgram();
	}

	/**
	 * Main method for the Sudoku game
	 */
	public static void main(String[] args) {
		launch(args);
		
	}
}
