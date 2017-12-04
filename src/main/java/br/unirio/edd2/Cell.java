package br.unirio.edd2;

/**
 * Helper class that represents the cell of the grid
 */
public class Cell {
    public int row;   // the row number of the cell(row 0 is the top)
    public int col;   // the column number of the cell (Column 0 is the left)
    public int g;     // the value of the function g of A* and Greedy algorithms
    public int h;     // the value of the function h of A* and Greedy algorithms
    public int f;     // the value of the function h of A* and Greedy algorithms
    public int dist;  // the distance of the cell from the initial position of the robot
    // Ie the label that updates the Dijkstra's algorithm
    Cell prev; // Each state corresponds to a cell
    // and each state has a predecessor which
    // is stored in this variable

    public Cell(int row, int col){
        this.row = row;
        this.col = col;
    }
} // end nested class Cell