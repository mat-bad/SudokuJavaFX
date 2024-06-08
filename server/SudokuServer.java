import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.lang.Thread;

class SudokuHandler implements Runnable{
    private Socket connectionSocket;
    static int BOARD_START_INDEX = 0;
    static int BOARD_SIZE = 9;
    static int NO_VALUE = 0;
    static int MIN_VALUE = 1;
    static int MAX_VALUE = 9;
    static int SUBSECTION_SIZE = 3;

    int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
    int[][] board2 = new int[BOARD_SIZE][BOARD_SIZE];
    Scanner scanner;
    PrintWriter out;
    Scanner in;

    public SudokuHandler(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
        init();
    }

    void init() {
        try {
            scanner = new Scanner(new FileReader("input.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("no input file");
        }

        try {
            in = new Scanner(connectionSocket.getInputStream());
            out = new PrintWriter(connectionSocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("output file exception");
        }
    }

    void getInput(int[][] board, Scanner scanner) throws FileNotFoundException, InterruptedException {    
        for(int i=0; i<9; i++) {
          for(int j=0; j<9; j++) {
            while(!scanner.hasNextInt()) {
                //System.out.println("Waiting on client response...");
                Thread.sleep(100);
            }
            board[i][j] = scanner.nextInt();
          }
        }
      }

    void endProgram() {
        out.close();
        scanner.close();
        in.close();
    }

    void output(String s) {
        out.print(s);
        out.flush();
        //System.out.print(s);
    }

    void outputln(String s) {
        output(s + "\n");
    }

    void outputln() {
        outputln("");
    }

    private boolean checkConstraint(int[][] board, int row, boolean[] constraint, int column) {
        if (board[row][column] != NO_VALUE) {
            if (!constraint[board[row][column] - 1]) {
                constraint[board[row][column] - 1] = true;
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean subsectionConstraint(int[][] board, int row, int column) {
        boolean[] constraint = new boolean[BOARD_SIZE];
        int subsectionRowStart = (row / SUBSECTION_SIZE) * SUBSECTION_SIZE;
        int subsectionRowEnd = subsectionRowStart + SUBSECTION_SIZE;

        int subsectionColumnStart = (column / SUBSECTION_SIZE) * SUBSECTION_SIZE;
        int subsectionColumnEnd = subsectionColumnStart + SUBSECTION_SIZE;

        for (int r = subsectionRowStart; r < subsectionRowEnd; r++) {
            for (int c = subsectionColumnStart; c < subsectionColumnEnd; c++) {
                if (!checkConstraint(board, r, constraint, c))
                    return false;
            }
        }
        return true;
    }

    private boolean columnConstraint(int[][] board, int column) {
        boolean[] constraint = new boolean[BOARD_SIZE];
        return IntStream.range(BOARD_START_INDEX, BOARD_SIZE)
                .allMatch(row -> checkConstraint(board, row, constraint, column));
    }

    private boolean rowConstraint(int[][] board, int row) {
        boolean[] constraint = new boolean[BOARD_SIZE];
        return IntStream.range(BOARD_START_INDEX, BOARD_SIZE)
                .allMatch(column -> checkConstraint(board, row, constraint, column));
    }

    private boolean isValid(int[][] board, int row, int column) {
        return (rowConstraint(board, row)
                && columnConstraint(board, column)
                && subsectionConstraint(board, row, column));
    }

    private boolean validate(int[][] board1, int[][] board2) {
        for (int row = BOARD_START_INDEX; row < BOARD_SIZE; row++) {
            for (int column = BOARD_START_INDEX; column < BOARD_SIZE; column++) {
                if(board2[row][column] == NO_VALUE) return false;
                if (board1[row][column] != board2[row][column] && board1[row][column] != NO_VALUE) return false;
                if(!isValid(board2, row, column)) return false;
            }
        }
        return true;
    }

    private void printBoard(int[][] board) {
        for (int row = BOARD_START_INDEX; row < BOARD_SIZE; row++) {
            for (int column = BOARD_START_INDEX; column < BOARD_SIZE; column++) {
                output(board[row][column] + " ");
            }
            outputln();
        }
    }

    @Override
    public void run() {
        try {
            getInput(board, scanner);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int operation;
        while(true) {
            if(!in.hasNextInt()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    continue;
                }
            }
            try {
                operation = in.nextInt();
            } catch (NoSuchElementException e) {
                System.out.println("Goodbye!");
                return;
            }
            if(operation == 1) {
                System.out.println("op1");
                printBoard(board);
            } else if(operation == 2) {
                System.out.println("op2");
                try {
                    getInput(board2, in);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(validate(board, board2)) {
                    outputln("Correct!");
                    System.out.println("Correct!");
                } else {
                    outputln("Wrong answer");
                    System.out.println("Wrong answer");
                }
            } else if(operation == 3) {
                endProgram();
                break;
            }
        }
        
    }
}

public class SudokuServer {

    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();
        try (ServerSocket welcomingSocket = new ServerSocket(5757)) {
            System.out.print("Server created.\nShould wait for a client ... ");
            while (true) {
                Socket connectionSocket = welcomingSocket.accept();
                System.out.println("client accepted!");
                pool.execute(new SudokuHandler(connectionSocket));
            }
        } catch (IOException ex) {
            System.err.println(ex);
        } finally {
            pool.shutdown();
            System.out.print("done.\nClosing server ... ");
        }
        System.out.println("done.");
    }
}
/*
 * 8 0 0 0 0 0 0 0 0
 * 0 0 3 6 0 0 0 0 0
 * 0 7 0 0 9 0 2 0 0
 * 0 5 0 0 0 7 0 0 0
 * 0 0 0 0 4 5 7 0 0
 * 0 0 0 1 0 0 0 3 0
 * 0 0 1 0 0 0 0 6 8
 * 0 0 8 5 0 0 0 1 0
 * 0 9 0 0 0 0 4 0 0
 * 
 * 0 2 0 5 0 1 0 9 0
 * 8 0 0 2 0 3 0 0 6
 * 0 3 0 0 6 0 0 7 0
 * 0 0 1 0 0 0 6 0 0
 * 5 4 0 0 0 0 0 1 9
 * 0 0 3 0 0 0 7 0 0
 * 0 9 0 0 3 0 0 8 0
 * 2 0 0 8 0 4 0 0 7
 * 0 1 0 9 0 7 0 6 0
 * 
 */