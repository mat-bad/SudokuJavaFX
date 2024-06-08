package com.examplegg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class App extends Application {


	private BorderPane root;
	private Scene scene;
	private GridPane table;

	private ArrayList<Integer> board;
	private Map<Integer, Button> boardText;
	private Map<Integer, GridPane> grid;

	private HBox hbox;

	//private Stage stage;
	private SudokuClient sudokuClient;

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

		// Send button
		Button send = new Button("Send Solution");
		send.setOnAction(e -> {
			String response  = sudokuClient.sendSolution(board);
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        	alert.setTitle("Message");
        	alert.setHeaderText(response);
			alert.showAndWait();
		});

		// New game button
		Button newGame = new Button("New Game");
		newGame.setOnAction(e -> {
			makeBoard();
			// Generates the GUI for the board
			generateBoard();
		});

		// Solve button
		Button solve = new Button("Solve Puzzle");
		solve.setOnAction(e -> {
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
		hbox.getChildren().addAll(newGame, send, solve);

		// Main layout of the Game
		root = new BorderPane();
		root.setTop(hbox);
		root.setCenter(table);
	
		// List and maps of buttons, GridPanes and value of the board
		boardText = new HashMap<Integer, Button>();
		grid = new HashMap<Integer, GridPane>();
		//numButtons = new HashMap<Integer, Button>();

		sudokuClient = new SudokuClient();
		makeBoard();
		// Generates the GUI for the board
		generateBoard();

		// Sets the scene to the BorderPane layout
		scene = new Scene(root, 350, 450);

		// Sets the stage, sets its title, displays it, and restricts its minimal size
		primaryStage.setScene(scene);
		primaryStage.setTitle("Sudoku");
		primaryStage.show();
		primaryStage.setMinHeight(primaryStage.getHeight());
		primaryStage.setMinWidth(primaryStage.getWidth());
	}

	/**
	 * Main method for the Sudoku game
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
