package br.unirio.edd2;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Stack;
import javax.swing.*;
import javax.swing.event.*;

/**
*
* @author Nikos Kanargias, Hellenic Open University student, PLI31 2012-13
* E-mail: nkana@tee.gr
* @version 5.0
*
* The software solves and visualizes the robot motion planning problem,
* by implementing variants of DFS, BFS and A* algorithms, as described
* by E. Keravnou in her book: "Artificial Intelligence and Expert Systems",
* Hellenic Open University,  Patra 2000 (in Greek)
* as well as the Greedy search algorithm, as a special case of A*.
*
* The software also implements Dijkstra's algorithm,
* as just described in the relevant article in Wikipedia.
* http://en.wikipedia.org/wiki/Dijkstra%27s_algorithm
*
* The superiority of  A* and Dijkstra's algorithms against the other three becomes obvious.
*
* The user can change the number of the grid cells, indicating
* the desired number of rows and columns.
*
* The user can add as many obstacles he/she wants, as he/she
* would "paint" free curves with a drawing program.
*
* Individual obstacles can be removed by clicking them.
*
* The position of the robot and/or the target can be changed by dragging with the mouse.
*
* Jump from search in "Step-by-Step" way to "Animation" way and vice versa is done
* by pressing the corresponding button, even when the search is in progress.
*
* The speed of a search can be changed, even if the search is in progress.
* It is sufficient to place the slider "Speed" in the new desired position
* and then press the "Animation" button.
*
* The application considers that the robot itself has some volume.
* Therefore it can’t move diagonally to a free cell passing between two obstacles
* adjacent to one apex.
*
* When 'Step-by-Step' or 'Animation' search is underway it is not possible to change
* the position of obstacles, robot and target, as well as the search algorithm.
*
* When 'Real-Time' search is underway the position of obstacles, robot and target
* can be changed.
*
* Advisable not to draw arrows to predecessors in large grids.
*/

public class App {

    public static JFrame mazeFrame;  // The main form of the program
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int width  = 750; //693
        int height = 590; //545
        mazeFrame = new JFrame("Knossos");
        mazeFrame.setContentPane(new MazePanel(width,height));
        mazeFrame.pack();
        mazeFrame.setResizable(false);

        // the form is located in the center of the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screenWidth = screenSize.getWidth();
        double ScreenHeight = screenSize.getHeight();
        int x = ((int)screenWidth-width)/2;
        int y = ((int)ScreenHeight-height)/2;

        mazeFrame.setLocation(x,y);
        mazeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mazeFrame.setVisible(true);
    } // end main()



} // end class App

