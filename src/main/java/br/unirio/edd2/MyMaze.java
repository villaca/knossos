package br.unirio.edd2;

import java.util.ArrayList;
import java.util.Random;

/**
 * Creates a random, perfect (without cycles) maze
 *
 * The code of the class is an adaptation, with the original commentary, of the answer given
 * by user DoubleMx2 on August 25 to a question posted by user nazar_art at stackoverflow.com:
 * http://stackoverflow.com/questions/18396364/maze-generation-arrayindexoutofboundsexception
 */
public class MyMaze {
    private int dimensionX, dimensionY; // dimension of maze
    private int gridDimensionX, gridDimensionY; // dimension of output grid
    private char[][] mazeGrid; // output grid
    private Cell[][] cells; // 2d array of Cells
    private Random random = new Random(); // The random object
    private MazePanel mazePanel; // The associated maze panel

    // initialize with x and y the same
    public MyMaze(int aDimension, MazePanel mazePanel) {
        // Initialize
        this(aDimension, aDimension, mazePanel);
    }
    // constructor
    public MyMaze(int xDimension, int yDimension, MazePanel mazePanel) {
        dimensionX = xDimension;
        dimensionY = yDimension;
        gridDimensionX = xDimension * 2 + 1;
        gridDimensionY = yDimension * 2 + 1;
        mazeGrid = new char[gridDimensionX][gridDimensionY];
        this.mazePanel = mazePanel;
        init();
        generateMaze();
    }

    private void init() {
        // create cells
        cells = new Cell[dimensionX][dimensionY];
        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                cells[x][y] = new Cell(x, y, false); // create cell (see Cell constructor)
            }
        }
    }

    // inner class to represent a cell
    private class Cell {
        int x, y; // coordinates
        // cells this cell is connected to
        ArrayList<Cell> neighbors = new ArrayList<>();
        // impassable cell
        boolean wall = true;
        // if true, has yet to be used in generation
        boolean open = true;
        // construct Cell at x, y
        Cell(int x, int y) {
            this(x, y, true);
        }
        // construct Cell at x, y and with whether it isWall
        Cell(int x, int y, boolean isWall) {
            this.x = x;
            this.y = y;
            this.wall = isWall;
        }
        // add a neighbor to this cell, and this cell as a neighbor to the other
        void addNeighbor(Cell other) {
            if (!this.neighbors.contains(other)) { // avoid duplicates
                this.neighbors.add(other);
            }
            if (!other.neighbors.contains(this)) { // avoid duplicates
                other.neighbors.add(this);
            }
        }
        // used in updateGrid()
        boolean isCellBelowNeighbor() {
            return this.neighbors.contains(new Cell(this.x, this.y + 1));
        }
        // used in updateGrid()
        boolean isCellRightNeighbor() {
            return this.neighbors.contains(new Cell(this.x + 1, this.y));
        }
        // useful Cell equivalence
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Cell)) return false;
            Cell otherCell = (Cell) other;
            return (this.x == otherCell.x && this.y == otherCell.y);
        }

        // should be overridden with equals
        @Override
        public int hashCode() {
            // random hash code method designed to be usually unique
            return this.x + this.y * 256;
        }

    }
    // generate from upper left (In computing the y increases down often)
    private void generateMaze() {
        generateMaze(0, 0);
    }
    // generate the maze from coordinates x, y
    private void generateMaze(int x, int y) {
        generateMaze(getCell(x, y)); // generate from Cell
    }
    private void generateMaze(Cell startAt) {
        // don't generate from cell not there
        if (startAt == null) return;
        startAt.open = false; // indicate cell closed for generation
        ArrayList<Cell> cellsList = new ArrayList<>();
        cellsList.add(startAt);

        while (!cellsList.isEmpty()) {
            Cell cell;
            // this is to reduce but not completely eliminate the number
            // of long twisting halls with short easy to detect branches
            // which results in easy mazes
            if (random.nextInt(10)==0)
                cell = cellsList.remove(random.nextInt(cellsList.size()));
            else cell = cellsList.remove(cellsList.size() - 1);
            // for collection
            ArrayList<Cell> neighbors = new ArrayList<>();
            // cells that could potentially be neighbors
            Cell[] potentialNeighbors = new Cell[]{
                    getCell(cell.x + 1, cell.y),
                    getCell(cell.x, cell.y + 1),
                    getCell(cell.x - 1, cell.y),
                    getCell(cell.x, cell.y - 1)
            };
            for (Cell other : potentialNeighbors) {
                // skip if outside, is a wall or is not opened
                if (other==null || other.wall || !other.open) continue;
                neighbors.add(other);
            }
            if (neighbors.isEmpty()) continue;
            // get random cell
            Cell selected = neighbors.get(random.nextInt(neighbors.size()));
            // add as neighbor
            selected.open = false; // indicate cell closed for generation
            cell.addNeighbor(selected);
            cellsList.add(cell);
            cellsList.add(selected);
        }
        updateGrid();
    }
    // used to get a Cell at x, y; returns null out of bounds
    public Cell getCell(int x, int y) {
        try {
            return cells[x][y];
        } catch (ArrayIndexOutOfBoundsException e) { // catch out of bounds
            return null;
        }
    }
    // draw the maze
    public void updateGrid() {
        char backChar = ' ', wallChar = 'X', cellChar = ' ';
        // fill background
        for (int x = 0; x < gridDimensionX; x ++) {
            for (int y = 0; y < gridDimensionY; y ++) {
                mazeGrid[x][y] = backChar;
            }
        }
        // build walls
        for (int x = 0; x < gridDimensionX; x ++) {
            for (int y = 0; y < gridDimensionY; y ++) {
                if (x % 2 == 0 || y % 2 == 0)
                    mazeGrid[x][y] = wallChar;
            }
        }
        // make meaningful representation
        for (int x = 0; x < dimensionX; x++) {
            for (int y = 0; y < dimensionY; y++) {
                Cell current = getCell(x, y);
                int gridX = x * 2 + 1, gridY = y * 2 + 1;
                mazeGrid[gridX][gridY] = cellChar;
                if (current.isCellBelowNeighbor()) {
                    mazeGrid[gridX][gridY + 1] = cellChar;
                }
                if (current.isCellRightNeighbor()) {
                    mazeGrid[gridX + 1][gridY] = cellChar;
                }
            }
        }

        // We create a clean grid ...
        this.mazePanel.searching = false;
        this.mazePanel.endOfSearch = false;
        this.mazePanel.fillGrid();
        // ... and copy into it the positions of obstacles
        // created by the maze construction algorithm
        for (int x = 0; x < gridDimensionX; x++) {
            for (int y = 0; y < gridDimensionY; y++) {
                if (mazeGrid[x][y] == wallChar && this.mazePanel.grid[x][y] != this.mazePanel.ROBOT && this.mazePanel.grid[x][y] != this.mazePanel.TARGET){
                    this.mazePanel.grid[x][y] = this.mazePanel.OBST;
                }
            }
        }
    }
} // end nested class MyMaze
