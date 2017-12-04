package br.unirio.edd2;

import br.unirio.edd2.Comparators.CellComparatorByDist;
import br.unirio.edd2.Comparators.CellComparatorByF;

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
 * This class defines the contents of the main form
 * and contains all the functionality of the program.
 */
public class MazePanel extends JPanel {

    /*
     **********************************************************
     *          Nested classes in MazePanel
     **********************************************************
     */

    /**
     * Class that handles mouse movements as we "paint"
     * obstacles or move the robot and/or target.
     */
    private class MouseHandler implements MouseListener, MouseMotionListener {
        private int cur_row, cur_col, cur_val;
        @Override
        public void mousePressed(MouseEvent evt) {
            int row = (evt.getY() - 10) / squareSize;
            int col = (evt.getX() - 10) / squareSize;
            if (row >= 0 && row < rows && col >= 0 && col < columns) {
                if (realTime ? true : !found && !searching){

                    if (realTime) {
                        searching = true;
                        fillGrid();
                    }
                    cur_row = row;
                    cur_col = col;
                    cur_val = grid[row][col];
                    if (cur_val == EMPTY){
                        grid[row][col] = OBST;
                    }
                    if (cur_val == OBST){
                        grid[row][col] = EMPTY;
                    }
                    if (realTime) {
                        if (dijkstra.isSelected()) {
                            initializeDijkstra();
                        }
                    }
                }
            }
            if (realTime) {
                timer.setDelay(0);
                timer.start();
                checkTermination();
            } else {
                repaint();
            }
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            int row = (evt.getY() - 10) / squareSize;
            int col = (evt.getX() - 10) / squareSize;
            if (row >= 0 && row < rows && col >= 0 && col < columns){
                if (realTime ? true : !found && !searching){
                    if (realTime) {
                        searching = true;
                        fillGrid();
                    }
                    if ((row*columns+col != cur_row*columns+cur_col) && (cur_val == ROBOT || cur_val == TARGET)){
                        int new_val = grid[row][col];
                        if (new_val == EMPTY){
                            grid[row][col] = cur_val;
                            if (cur_val == ROBOT) {
                                robotStart.row = row;
                                robotStart.col = col;
                            } else {
                                targetPos.row = row;
                                targetPos.col = col;
                            }
                            grid[cur_row][cur_col] = new_val;
                            cur_row = row;
                            cur_col = col;
                            if (cur_val == ROBOT) {
                                robotStart.row = cur_row;
                                robotStart.col = cur_col;
                            } else {
                                targetPos.row = cur_row;
                                targetPos.col = cur_col;
                            }
                            cur_val = grid[row][col];
                        }
                    } else if (grid[row][col] != ROBOT && grid[row][col] != TARGET){
                        grid[row][col] = OBST;
                    }
                    if (realTime) {
                        if (dijkstra.isSelected()) {
                            initializeDijkstra();
                        }
                    }
                }
            }
            if (realTime) {
                timer.setDelay(0);
                timer.start();
                checkTermination();
            } else {
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent evt) { }
        @Override
        public void mouseEntered(MouseEvent evt) { }
        @Override
        public void mouseExited(MouseEvent evt) { }
        @Override
        public void mouseMoved(MouseEvent evt) { }
        @Override
        public void mouseClicked(MouseEvent evt) { }

    } // end nested class MouseHandler

    /**
     * When the user presses a button performs the corresponding functionality
     */
    private class ActionHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            String cmd = evt.getActionCommand();

            if (cmd.equals("Limpar")) {
                fillGrid();
                realTime = false;
                realTimeButton.setEnabled(true);
                realTimeButton.setForeground(Color.black);
                stepButton.setEnabled(true);
                animationButton.setEnabled(true);
                slider.setEnabled(true);
                dfs.setEnabled(true);
                bfs.setEnabled(true);
                aStar.setEnabled(true);
                greedy.setEnabled(true);
                dijkstra.setEnabled(true);
                diagonal.setEnabled(true);
                drawArrows.setEnabled(true);
            } else if (cmd.equals("Tempo real") && !realTime) {
                realTime = true;
                searching = true;
                realTimeButton.setForeground(Color.red);
                stepButton.setEnabled(false);
                animationButton.setEnabled(false);
                slider.setEnabled(false);
                dfs.setEnabled(false);
                bfs.setEnabled(false);
                aStar.setEnabled(false);
                greedy.setEnabled(false);
                dijkstra.setEnabled(false);
                diagonal.setEnabled(false);
                drawArrows.setEnabled(false);
                timer.setDelay(0);
                timer.start();
                if (dijkstra.isSelected()) {
                    initializeDijkstra();
                }
                checkTermination();
            } else if (cmd.equals("Passo a Passo") && !found && !endOfSearch) {
                realTime = false;
                // The Dijkstra's initialization should be done just before the
                // start of search, because obstacles must be in place.
                if (!searching && dijkstra.isSelected()) {
                    initializeDijkstra();
                }
                searching = true;
                message.setText(msgSelectStepByStepEtc);
                realTimeButton.setEnabled(false);
                dfs.setEnabled(false);
                bfs.setEnabled(false);
                aStar.setEnabled(false);
                greedy.setEnabled(false);
                dijkstra.setEnabled(false);
                diagonal.setEnabled(false);
                drawArrows.setEnabled(false);
                timer.stop();
                // Here we decide whether we can continue the
                // 'Step-by-Step' search or not.
                // In the case of DFS, BFS, A* and Greedy algorithms
                // here we have the second step:
                // 2. If OPEN SET = [], then terminate. There is no solution.
                checkTermination();
                repaint();
            } else if (cmd.equals("Animação") && !endOfSearch) {
                realTime = false;
                if (!searching && dijkstra.isSelected()) {
                    initializeDijkstra();
                }
                searching = true;
                message.setText(msgSelectStepByStepEtc);
                realTimeButton.setEnabled(false);
                dfs.setEnabled(false);
                bfs.setEnabled(false);
                aStar.setEnabled(false);
                greedy.setEnabled(false);
                dijkstra.setEnabled(false);
                diagonal.setEnabled(false);
                drawArrows.setEnabled(false);
                timer.setDelay(delay);
                timer.start();
            } else if (cmd.equals("About Maze")) {
                //AboutBox aboutBox = new AboutBox(mazeFrame,true);
                //aboutBox.setVisible(true);
            }
        }
    } // end nested class ActionHandler

    /**
     * The class that is responsible for the animation
     */
    private class RepaintAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            // Here we decide whether we can continue or not
            // the search with 'Animation'.
            // In the case of DFS, BFS, A* and Greedy algorithms
            // here we have the second step:
            // 2. If OPEN SET = [], then terminate. There is no solution.
            checkTermination();
            if (found) {
                timer.stop();
            }
            if (!realTime) {
                repaint();
            }
        }
    } // end nested class RepaintAction

    public void checkTermination() {
        if ((dijkstra.isSelected() && graph.isEmpty()) ||
                (!dijkstra.isSelected() && openSet.isEmpty()) ) {
            endOfSearch = true;
            grid[robotStart.row][robotStart.col]=ROBOT;
            message.setText(msgNoSolution);
            stepButton.setEnabled(false);
            animationButton.setEnabled(false);
            slider.setEnabled(false);
            repaint();
        } else {
            expandNode();
            if (found) {
                endOfSearch = true;
                plotRoute();
                stepButton.setEnabled(false);
                animationButton.setEnabled(false);
                slider.setEnabled(false);
                repaint();
            }
        }
    }

    /**
     * The class that creates the AboutBox
     */
        /*private class AboutBox extends JDialog{

            public AboutBox(Frame parent, boolean modal){
                super(parent, modal);
                // the aboutBox is located in the center of the screen
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                double screenWidth = screenSize.getWidth();
                double ScreenHeight = screenSize.getHeight();
                int width = 350;
                int height = 190;
                int x = ((int)screenWidth-width)/2;
                int y = ((int)ScreenHeight-height)/2;
                setSize(width,height);
                setLocation(x, y);

                setResizable(false);
                setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

                JLabel title = new JLabel("Maze", JLabel.CENTER);
                title.setFont(new Font("Helvetica",Font.PLAIN,24));
                title.setForeground(new java.awt.Color(255, 153, 102));

                JLabel version = new JLabel("Version: 5.0", JLabel.CENTER);
                version.setFont(new Font("Helvetica",Font.BOLD,14));

                JLabel programmer = new JLabel("Designer: Nikos Kanargias", JLabel.CENTER);
                programmer.setFont(new Font("Helvetica",Font.PLAIN,16));

                JLabel email = new JLabel("E-mail: nkana@tee.gr", JLabel.CENTER);
                email.setFont(new Font("Helvetica",Font.PLAIN,14));

                JLabel sourceCode = new JLabel("Code and documentation:", JLabel.CENTER);
                sourceCode.setFont(new Font("Helvetica",Font.PLAIN,14));

                JLabel link = new JLabel("<html><a href=\\\"\\\">Code and documentation</a></html>", JLabel.CENTER);
                link.setCursor(new Cursor(Cursor.HAND_CURSOR));
                link.setFont(new Font("Helvetica",Font.PLAIN,16));
                link.setToolTipText
                        ("Click this link to retrieve code and documentation from DropBox");

                JLabel video = new JLabel("<html><a href=\\\"\\\">Watch demo video on YouTube</a></html>", JLabel.CENTER);
                video.setCursor(new Cursor(Cursor.HAND_CURSOR));
                video.setFont(new Font("Helvetica",Font.PLAIN,16));
                video.setToolTipText
                        ("Click this link to watch demo video on YouTube");

                JLabel dummy = new JLabel("");

                add(title);
                add(version);
                add(programmer);
                add(email);
                add(link);
                add(video);
                add(dummy);

                goDropBox(link);
                goYouTube(video);

                title.     setBounds(5,  0, 330, 30);
                version.   setBounds(5, 30, 330, 20);
                programmer.setBounds(5, 55, 330, 20);
                email.     setBounds(5, 80, 330, 20);
                link.      setBounds(5,105, 330, 20);
                video.     setBounds(5,130, 330, 20);
                dummy.     setBounds(5,155, 330, 20);
            }

            private void goDropBox(JLabel website) {
                website.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            Desktop.getDesktop().browse(new URI("https://www.dropbox.com/sh/3tl3vmsd8ebxo24/xKmjl8uXX7"));
                        } catch (URISyntaxException | IOException ex) {
                            //It looks like there's a problem
                        }
                    }
                });
            }
            private void goYouTube(JLabel video) {
                video.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            Desktop.getDesktop().browse(new URI("http://youtu.be/0ol_PptA7rM"));
                        } catch (URISyntaxException | IOException ex) {
                            //It looks like there's a problem
                        }
                    }
                });
            }
        }*/ // end nested class AboutBox



    /*
     **********************************************************
     *          Constants of class MazePanel
     **********************************************************
     */

    protected final static int
            INFINITY = Integer.MAX_VALUE, // The representation of the infinite
            EMPTY    = 0,  // empty cell
            OBST     = 1,  // cell with obstacle
            ROBOT    = 2,  // the position of the robot
            TARGET   = 3,  // the position of the target
            FRONTIER = 4,  // cells that form the frontier (OPEN SET)
            CLOSED   = 5,  // cells that form the CLOSED SET
            ROUTE    = 6;  // cells that form the robot-to-target path

    // Messages to the user
    private final static String
            msgDrawAndSelect =
            "\"Pinte\" obstáculos, então clique 'Tempo real' ou\n 'Passo a Passo' or 'Animado'",
            msgSelectStepByStepEtc =
                    "Clique 'Passo a Passo' ou 'Animado' ou 'Limpar'",
            msgNoSolution =
                    "Não existe trajeto até o alvo!!!";

    /*
     **********************************************************
     *          Variables of class MazePanel
     **********************************************************
     */

    JSpinner rowsSpinner, columnsSpinner; // Spinners for entering # of rows and columns

    int rows    = 41,           // the number of rows of the grid
            columns = 41,           // the number of columns of the grid
            squareSize = 500/rows;  // the cell size in pixels


    int arrowSize = squareSize/2; // the size of the tip of the arrow
    // pointing the predecessor cell
    ArrayList<Cell> openSet   = new ArrayList();// the OPEN SET
    ArrayList<Cell> closedSet = new ArrayList();// the CLOSED SET
    ArrayList<Cell> graph     = new ArrayList();// the set of vertices of the graph
    // to be explored by Dijkstra's algorithm

    Cell robotStart; // the initial position of the robot
    Cell targetPos;  // the position of the target

    JLabel message;  // message to the user

    // basic buttons
    JButton resetButton, mazeButton, clearButton, realTimeButton, stepButton, animationButton;

    // buttons for selecting the algorithm
    JRadioButton dfs, bfs, aStar, greedy, dijkstra;

    // the slider for adjusting the speed of the animation
    JSlider slider;

    // Diagonal movements allowed?
    JCheckBox diagonal;
    // Draw arrows to predecessors
    JCheckBox drawArrows;

    int[][] grid;        // the grid
    boolean realTime;    // Solution is displayed instantly
    boolean found;       // flag that the goal was found
    boolean searching;   // flag that the search is in progress
    boolean endOfSearch; // flag that the search came to an end
    int delay;           // time delay of animation (in msec)
    int expanded;        // the number of nodes that have been expanded

    // the object that controls the animation
    RepaintAction action = new RepaintAction();

    // the Timer which governs the execution speed of the animation
    Timer timer;

    /**
     * The creator of the panel
     * @param width  the width of the panel.
     * @param height the height of the panel.
     */
    public MazePanel(int width, int height) {

        setLayout(null);

        MouseHandler listener = new MouseHandler();
        addMouseListener(listener);
        addMouseMotionListener(listener);

        setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.blue));

        setPreferredSize( new Dimension(width,height) );

        grid = new int[rows][columns];

        // We create the contents of the panel

        message = new JLabel(msgDrawAndSelect, JLabel.CENTER);
        message.setForeground(Color.blue);
        message.setFont(new Font("Helvetica",Font.PLAIN,16));

        JLabel rowsLbl = new JLabel("# de linhas (5-83):", JLabel.RIGHT);
        rowsLbl.setFont(new Font("Helvetica",Font.PLAIN,13));

        SpinnerModel rowModel = new SpinnerNumberModel(41, //initial value
                5,  //min
                83, //max
                1); //step
        rowsSpinner = new JSpinner(rowModel);

        JLabel columnsLbl = new JLabel("# de colunas (5-83):", JLabel.RIGHT);
        columnsLbl.setFont(new Font("Helvetica",Font.PLAIN,13));

        SpinnerModel colModel = new SpinnerNumberModel(41, //initial value
                5,  //min
                83, //max
                1); //step
        columnsSpinner = new JSpinner(colModel);

        resetButton = new JButton("Nova malha");
        resetButton.addActionListener(new ActionHandler());
        resetButton.setBackground(Color.lightGray);
        resetButton.setToolTipText
                ("Limpa e redesenha a malha de acordo com as quantidades de linhas e colunas");
        resetButton.addActionListener(this::resetButtonActionPerformed);

        mazeButton = new JButton("Dédalo");
        mazeButton.addActionListener(new ActionHandler());
        mazeButton.setBackground(Color.lightGray);
        mazeButton.setToolTipText
                ("Cria um labirinto aleatório");
        mazeButton.addActionListener(this::mazeButtonActionPerformed);

        clearButton = new JButton("Limpar");
        clearButton.addActionListener(new ActionHandler());
        clearButton.setBackground(Color.lightGray);
        clearButton.setToolTipText
                ("Primeiro clique: limpa a busca, Segundo clique: limpa obstáculos");

        realTimeButton = new JButton("Tempo real");
        realTimeButton.addActionListener(new ActionHandler());
        realTimeButton.setBackground(Color.lightGray);
        realTimeButton.setToolTipText
                ("Posições dos obstáculos, Teseu e saída podem ser alteradas enquanto a busca está ocorrendo.");

        stepButton = new JButton("Passo a Passo");
        stepButton.addActionListener(new ActionHandler());
        stepButton.setBackground(Color.lightGray);
        stepButton.setToolTipText
                ("A busca é feita passo a passo a cada clique.");

        animationButton = new JButton("Animação");
        animationButton.addActionListener(new ActionHandler());
        animationButton.setBackground(Color.lightGray);
        animationButton.setToolTipText
                ("A busca é feita automaticamente.");

        JLabel velocity = new JLabel("Velocidade", JLabel.CENTER);
        velocity.setFont(new Font("Helvetica",Font.PLAIN,10));

        slider = new JSlider(0,1000,500); // initial value of delay 500 msec
        slider.setToolTipText
                ("Ajusta o atraso entre cada passo (0 a 1 segundo)");

        delay = 1000-slider.getValue();
        slider.addChangeListener((ChangeEvent evt) -> {
            JSlider source = (JSlider)evt.getSource();
            if (!source.getValueIsAdjusting()) {
                delay = 1000-source.getValue();
            }
        });

        // ButtonGroup that synchronizes the five RadioButtons
        // choosing the algorithm, so that only one
        // can be selected anytime
        ButtonGroup algoGroup = new ButtonGroup();

        dfs = new JRadioButton("DFS");
        dfs.setToolTipText("Algoritmo de busca em profundidade");
        algoGroup.add(dfs);
        dfs.addActionListener(new ActionHandler());

        bfs = new JRadioButton("BFS");
        bfs.setToolTipText("Algoritmo de busca em largura");
        algoGroup.add(bfs);
        bfs.addActionListener(new ActionHandler());

        aStar = new JRadioButton("A*");
        aStar.setToolTipText("Algoritmo A*");
        algoGroup.add(aStar);
        aStar.addActionListener(new ActionHandler());

        greedy = new JRadioButton("Guloso");
        greedy.setToolTipText("Algoritmo de busca guloso");
        algoGroup.add(greedy);
        greedy.addActionListener(new ActionHandler());

        dijkstra = new JRadioButton("Dijkstra");
        dijkstra.setToolTipText("Algoritmo de Dijkstra");
        algoGroup.add(dijkstra);
        dijkstra.addActionListener(new ActionHandler());

        JPanel algoPanel = new JPanel();
        algoPanel.setBorder(javax.swing.BorderFactory.
                createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
                        "Algoritmos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.TOP, new java.awt.Font("Helvetica", 0, 14)));

        dfs.setSelected(true);  // DFS is initially selected

        diagonal = new
                JCheckBox("Diagonal movements");
        diagonal.setToolTipText("Diagonal movements are also allowed");

        drawArrows = new
                JCheckBox("Arrows to predecessors");
        drawArrows.setToolTipText("Draw arrows to predecessors");

        JLabel robot = new JLabel("Teseu", JLabel.CENTER);
        robot.setForeground(Color.red);
        robot.setFont(new Font("Helvetica",Font.PLAIN,14));

        JLabel target = new JLabel("Saída", JLabel.CENTER);
        target.setForeground(Color.GREEN);
        target.setFont(new Font("Helvetica",Font.PLAIN,14));

        JLabel frontier = new JLabel("Posição\n atual", JLabel.CENTER);
        frontier.setForeground(Color.blue);
        frontier.setFont(new Font("Helvetica",Font.PLAIN,14));

        JLabel closed = new JLabel("Caminhos\n observados", JLabel.CENTER);
        closed.setForeground(Color.CYAN);
        closed.setFont(new Font("Helvetica",Font.PLAIN,14));

        //JButton aboutButton = new JButton("About Maze");
        //aboutButton.addActionListener(new ActionHandler());
        //aboutButton.setBackground(Color.lightGray);

        // we add the contents of the panel
        add(message);
        add(rowsLbl);
        add(rowsSpinner);
        add(columnsLbl);
        add(columnsSpinner);
        add(resetButton);
        add(mazeButton);
        add(clearButton);
        add(realTimeButton);
        add(stepButton);
        add(animationButton);
        add(velocity);
        add(slider);
        add(dfs);
        add(bfs);
        add(aStar);
        add(greedy);
        add(dijkstra);
        add(algoPanel);
        //add(diagonal);
        //add(drawArrows);
        add(robot);
        add(target);
        add(frontier);
        add(closed);
        //add(aboutButton);

        // we regulate the sizes and positions
        message.setBounds(0, 515, 600, 50);
        rowsLbl.setBounds(520, 5, 130, 25);
        rowsSpinner.setBounds(655, 5, 35, 25);
        columnsLbl.setBounds(520, 35, 130, 25);
        columnsSpinner.setBounds(655, 35, 35, 25);
        resetButton.setBounds(520, 65, 170, 25);
        mazeButton.setBounds(520, 95, 170, 25);
        clearButton.setBounds(520, 125, 170, 25);
        realTimeButton.setBounds(520, 155, 170, 25);
        stepButton.setBounds(520, 185, 170, 25);
        animationButton.setBounds(520, 215, 170, 25);
        velocity.setBounds(520, 245, 170, 10);
        slider.setBounds(520, 255, 170, 25);
        dfs.setBounds(530, 300, 70, 25);
        bfs.setBounds(600, 300, 70, 25);
        aStar.setBounds(530, 325, 70, 25);
        greedy.setBounds(600, 325, 85, 25);
        dijkstra.setBounds(530, 350, 85, 25);
        algoPanel.setLocation(520,280);
        algoPanel.setSize(170, 100);
        diagonal.setBounds(520, 385, 170, 25);
        drawArrows.setBounds(520, 410, 170, 25);
        robot.setBounds(500, 465, 100, 25); // w:80, x:520
        target.setBounds(605, 465, 80, 25); // x:605
        frontier.setBounds(500, 485, 100, 25); // w:80, x:520
        closed.setBounds(600, 485, 150, 25); // x:605, w:80
        //aboutButton.setBounds(520, 515, 170, 25);

        // we create the timer
        timer = new Timer(delay, action);

        // We attach to cells in the grid initial values.
        // Here is the first step of the algorithms
        fillGrid();

    } // end constructor

    static protected JSpinner addLabeledSpinner(Container c,
                                                String label,
                                                SpinnerModel model) {
        JLabel l = new JLabel(label);
        c.add(l);

        JSpinner spinner = new JSpinner(model);
        l.setLabelFor(spinner);
        c.add(spinner);

        return spinner;
    }

    /**
     * Function executed if the user presses the button "New Grid"
     */
    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {
        realTime = false;
        realTimeButton.setEnabled(true);
        realTimeButton.setForeground(Color.black);
        stepButton.setEnabled(true);
        animationButton.setEnabled(true);
        slider.setEnabled(true);
        initializeGrid(false);
    } // end resetButtonActionPerformed()

    /**
     * Function executed if the user presses the button "Maze"
     */
    private void mazeButtonActionPerformed(java.awt.event.ActionEvent evt) {
        realTime = false;
        realTimeButton.setEnabled(true);
        realTimeButton.setForeground(Color.black);
        stepButton.setEnabled(true);
        animationButton.setEnabled(true);
        slider.setEnabled(true);
        initializeGrid(true);
    } // end mazeButtonActionPerformed()

    /**
     * Creates a new clean grid or a new maze
     */
    private void initializeGrid(Boolean makeMaze) {
        rows    = (int)(rowsSpinner.getValue());
        columns = (int)(columnsSpinner.getValue());
        squareSize = 500/(rows > columns ? rows : columns);
        arrowSize = squareSize/2;
        // the maze must have an odd number of rows and columns
        if (makeMaze && rows % 2 == 0) {
            rows -= 1;
        }
        if (makeMaze && columns % 2 == 0) {
            columns -= 1;
        }
        grid = new int[rows][columns];
        robotStart = new Cell(rows-2,1);
        targetPos = new Cell(1,columns-2);
        dfs.setEnabled(true);
        dfs.setSelected(true);
        bfs.setEnabled(true);
        aStar.setEnabled(true);
        greedy.setEnabled(true);
        dijkstra.setEnabled(true);
        diagonal.setSelected(false);
        diagonal.setEnabled(true);
        drawArrows.setSelected(false);
        drawArrows.setEnabled(true);
        slider.setValue(500);
        if (makeMaze) {
            MyMaze maze = new MyMaze(rows/2,columns/2, this);
        } else {
            fillGrid();
        }
    } // end initializeGrid()

    /**
     * Expands a node and creates his successors
     */
    private void expandNode(){
        // Dijkstra's algorithm to handle separately
        if (dijkstra.isSelected()){
            Cell u;
            // 11: while Q is not empty:
            if (graph.isEmpty()){
                return;
            }
            // 12:  u := vertex in Q (graph) with smallest distance in dist[] ;
            // 13:  remove u from Q (graph);
            u = graph.remove(0);
            // Add vertex u in closed set
            closedSet.add(u);
            // If target has been found ...
            if (u.row == targetPos.row && u.col == targetPos.col){
                found = true;
                return;
            }
            // Counts nodes that have expanded.
            expanded++;
            // Update the color of the cell
            grid[u.row][u.col] = CLOSED;
            // 14: if dist[u] = infinity:
            if (u.dist == INFINITY){
                // ... then there is no solution.
                // 15: break;
                return;
                // 16: end if
            }
            // Create the neighbors of u
            ArrayList<Cell> neighbors = createSuccesors(u, false);
            // 18: for each neighbor v of u:
            neighbors.stream().forEach((v) -> {
                // 20: alt := dist[u] + dist_between(u, v) ;
                int alt = u.dist + distBetween(u,v);
                // 21: if alt < dist[v]:
                if (alt < v.dist) {
                    // 22: dist[v] := alt ;
                    v.dist = alt;
                    // 23: previous[v] := u ;
                    v.prev = u;
                    // Update the color of the cell
                    grid[v.row][v.col] = FRONTIER;
                    // 24: decrease-key v in Q;
                    // (sort list of nodes with respect to dist)
                    Collections.sort(graph, new CellComparatorByDist());
                }
            }); // The handling of the other four algorithms
        } else {
            Cell current;
            if (dfs.isSelected() || bfs.isSelected()) {
                // Here is the 3rd step of the algorithms DFS and BFS
                // 3. Remove the first state, Si, from OPEN SET ...
                current = openSet.remove(0);
            } else {
                // Here is the 3rd step of the algorithms A* and Greedy
                // 3. Remove the first state, Si, from OPEN SET,
                // for which f(Si) ≤ f(Sj) for all other
                // open states Sj  ...
                // (sort first OPEN SET list with respect to 'f')
                Collections.sort(openSet, new CellComparatorByF());
                current = openSet.remove(0);
            }
            // ... and add it to CLOSED SET.
            closedSet.add(0,current);
            // Update the color of the cell
            grid[current.row][current.col] = CLOSED;
            // If the selected node is the target ...
            if (current.row == targetPos.row && current.col == targetPos.col) {
                // ... then terminate etc
                Cell last = targetPos;
                last.prev = current.prev;
                closedSet.add(last);
                found = true;
                return;
            }
            // Count nodes that have been expanded.
            expanded++;
            // Here is the 4rd step of the algorithms
            // 4. Create the successors of Si, based on actions
            //    that can be implemented on Si.
            //    Each successor has a pointer to the Si, as its predecessor.
            //    In the case of DFS and BFS algorithms, successors should not
            //    belong neither to the OPEN SET nor the CLOSED SET.
            ArrayList<Cell> succesors;
            succesors = createSuccesors(current, false);
            // Here is the 5th step of the algorithms
            // 5. For each successor of Si, ...
            succesors.stream().forEach((cell) -> {
                // ... if we are running DFS ...
                if (dfs.isSelected()) {
                    // ... add the successor at the beginning of the list OPEN SET
                    openSet.add(0, cell);
                    // Update the color of the cell
                    grid[cell.row][cell.col] = FRONTIER;
                    // ... if we are runnig BFS ...
                } else if (bfs.isSelected()){
                    // ... add the successor at the end of the list OPEN SET
                    openSet.add(cell);
                    // Update the color of the cell
                    grid[cell.row][cell.col] = FRONTIER;
                    // ... if we are running A* or Greedy algorithms (step 5 of A* algorithm) ...
                } else if (aStar.isSelected() || greedy.isSelected()){
                    // ... calculate the value f(Sj) ...
                    int dxg = current.col-cell.col;
                    int dyg = current.row-cell.row;
                    int dxh = targetPos.col-cell.col;
                    int dyh = targetPos.row-cell.row;
                    if (diagonal.isSelected()){
                        // with diagonal movements
                        // calculate 1000 times the Euclidean distance
                        if (greedy.isSelected()) {
                            // especially for the Greedy ...
                            cell.g = 0;
                        } else {
                            cell.g = current.g+(int)((double)1000*Math.sqrt(dxg*dxg + dyg*dyg));
                        }
                        cell.h = (int)((double)1000*Math.sqrt(dxh*dxh + dyh*dyh));
                    } else {
                        // without diagonal movements
                        // calculate Manhattan distances
                        if (greedy.isSelected()) {
                            // especially for the Greedy ...
                            cell.g = 0;
                        } else {
                            cell.g = current.g+Math.abs(dxg)+Math.abs(dyg);
                        }
                        cell.h = Math.abs(dxh)+Math.abs(dyh);
                    }
                    cell.f = cell.g+cell.h;
                    // ... If Sj is neither in the OPEN SET nor in the CLOSED SET states ...
                    int openIndex   = isInList(openSet,cell);
                    int closedIndex = isInList(closedSet,cell);
                    if (openIndex == -1 && closedIndex == -1) {
                        // ... then add Sj in the OPEN SET ...
                        // ... evaluated as f(Sj)
                        openSet.add(cell);
                        // Update the color of the cell
                        grid[cell.row][cell.col] = FRONTIER;
                        // Else ...
                    } else {
                        // ... if already belongs to the OPEN SET, then ...
                        if (openIndex > -1){
                            // ... compare the new value assessment with the old one.
                            // If old <= new ...
                            if (openSet.get(openIndex).f <= cell.f) {
                                // ... then eject the new node with state Sj.
                                // (ie do nothing for this node).
                                // Else, ...
                            } else {
                                // ... remove the element (Sj, old) from the list
                                // to which it belongs ...
                                openSet.remove(openIndex);
                                // ... and add the item (Sj, new) to the OPEN SET.
                                openSet.add(cell);
                                // Update the color of the cell
                                grid[cell.row][cell.col] = FRONTIER;
                            }
                            // ... if already belongs to the CLOSED SET, then ...
                        } else {
                            // ... compare the new value assessment with the old one.
                            // If old <= new ...
                            if (closedSet.get(closedIndex).f <= cell.f) {
                                // ... then eject the new node with state Sj.
                                // (ie do nothing for this node).
                                // Else, ...
                            } else {
                                // ... remove the element (Sj, old) from the list
                                // to which it belongs ...
                                closedSet.remove(closedIndex);
                                // ... and add the item (Sj, new) to the OPEN SET.
                                openSet.add(cell);
                                // Update the color of the cell
                                grid[cell.row][cell.col] = FRONTIER;
                            }
                        }
                    }
                }
            });
        }
    } //end expandNode()

    /**
     * Creates the successors of a state/cell
     *
     * @param current       the cell for which we ask successors
     * @param makeConnected flag that indicates that we are interested only on the coordinates
     *                      of cells and not on the label 'dist' (concerns only Dijkstra's)
     * @return              the successors of the cell as a list
     */
    private ArrayList<Cell> createSuccesors(Cell current, boolean makeConnected){
        int r = current.row;
        int c = current.col;
        // We create an empty list for the successors of the current cell.
        ArrayList<Cell> temp = new ArrayList<>();
        // With diagonal movements priority is:
        // 1: Up 2: Up-right 3: Right 4: Down-right
        // 5: Down 6: Down-left 7: Left 8: Up-left

        // Without diagonal movements the priority is:
        // 1: Up 2: Right 3: Down 4: Left

        // If not at the topmost limit of the grid
        // and the up-side cell is not an obstacle ...
        if (r > 0 && grid[r-1][c] != OBST &&
                // ... and (only in the case are not running the A* or Greedy)
                // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                        isInList(openSet,new Cell(r-1,c)) == -1 &&
                                isInList(closedSet,new Cell(r-1,c)) == -1)) {
            Cell cell = new Cell(r-1,c);
            // In the case of Dijkstra's algorithm we can not append to
            // the list of successors the "naked" cell we have just created.
            // The cell must be accompanied by the label 'dist',
            // so we need to track it down through the list 'graph'
            // and then copy it back to the list of successors.
            // The flag makeConnected is necessary to be able
            // the present method createSuccesors() to collaborate
            // with the method findConnectedComponent(), which creates
            // the connected component when Dijkstra's initializes.
            if (dijkstra.isSelected()){
                if (makeConnected) {
                    temp.add(cell);
                } else {
                    int graphIndex = isInList(graph,cell);
                    if (graphIndex > -1) {
                        temp.add(graph.get(graphIndex));
                    }
                }
            } else {
                // ... update the pointer of the up-side cell so it points the current one ...
                cell.prev = current;
                // ... and add the up-side cell to the successors of the current one.
                temp.add(cell);
            }
        }
        if (diagonal.isSelected()){
            // If we are not even at the topmost nor at the rightmost border of the grid
            // and the up-right-side cell is not an obstacle ...
            if (r > 0 && c < columns-1 && grid[r-1][c+1] != OBST &&
                    // ... and one of the upper side or right side cells are not obstacles ...
                    // (because it is not reasonable to allow
                    // the robot to pass through a "slot")
                    (grid[r-1][c] != OBST || grid[r][c+1] != OBST) &&
                    // ... and (only in the case are not running the A* or Greedy)
                    // not already belongs neither to the OPEN SET nor CLOSED SET ...
                    ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                            isInList(openSet,new Cell(r-1,c+1)) == -1 &&
                                    isInList(closedSet,new Cell(r-1,c+1)) == -1)) {
                Cell cell = new Cell(r-1,c+1);
                if (dijkstra.isSelected()){
                    if (makeConnected) {
                        temp.add(cell);
                    } else {
                        int graphIndex = isInList(graph,cell);
                        if (graphIndex > -1) {
                            temp.add(graph.get(graphIndex));
                        }
                    }
                } else {
                    // ... update the pointer of the up-right-side cell so it points the current one ...
                    cell.prev = current;
                    // ... and add the up-right-side cell to the successors of the current one.
                    temp.add(cell);
                }
            }
        }
        // If not at the rightmost limit of the grid
        // and the right-side cell is not an obstacle ...
        if (c < columns-1 && grid[r][c+1] != OBST &&
                // ... and (only in the case are not running the A* or Greedy)
                // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected())? true :
                        isInList(openSet,new Cell(r,c+1)) == -1 &&
                                isInList(closedSet,new Cell(r,c+1)) == -1)) {
            Cell cell = new Cell(r,c+1);
            if (dijkstra.isSelected()){
                if (makeConnected) {
                    temp.add(cell);
                } else {
                    int graphIndex = isInList(graph,cell);
                    if (graphIndex > -1) {
                        temp.add(graph.get(graphIndex));
                    }
                }
            } else {
                // ... update the pointer of the right-side cell so it points the current one ...
                cell.prev = current;
                // ... and add the right-side cell to the successors of the current one.
                temp.add(cell);
            }
        }
        if (diagonal.isSelected()){
            // If we are not even at the lowermost nor at the rightmost border of the grid
            // and the down-right-side cell is not an obstacle ...
            if (r < rows-1 && c < columns-1 && grid[r+1][c+1] != OBST &&
                    // ... and one of the down-side or right-side cells are not obstacles ...
                    (grid[r+1][c] != OBST || grid[r][c+1] != OBST) &&
                    // ... and (only in the case are not running the A* or Greedy)
                    // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                    ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                            isInList(openSet,new Cell(r+1,c+1)) == -1 &&
                                    isInList(closedSet,new Cell(r+1,c+1)) == -1)) {
                Cell cell = new Cell(r+1,c+1);
                if (dijkstra.isSelected()){
                    if (makeConnected) {
                        temp.add(cell);
                    } else {
                        int graphIndex = isInList(graph,cell);
                        if (graphIndex > -1) {
                            temp.add(graph.get(graphIndex));
                        }
                    }
                } else {
                    // ... update the pointer of the downr-right-side cell so it points the current one ...
                    cell.prev = current;
                    // ... and add the down-right-side cell to the successors of the current one.
                    temp.add(cell);
                }
            }
        }
        // If not at the lowermost limit of the grid
        // and the down-side cell is not an obstacle ...
        if (r < rows-1 && grid[r+1][c] != OBST &&
                // ... and (only in the case are not running the A* or Greedy)
                // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                        isInList(openSet,new Cell(r+1,c)) == -1 &&
                                isInList(closedSet,new Cell(r+1,c)) == -1)) {
            Cell cell = new Cell(r+1,c);
            if (dijkstra.isSelected()){
                if (makeConnected) {
                    temp.add(cell);
                } else {
                    int graphIndex = isInList(graph,cell);
                    if (graphIndex > -1) {
                        temp.add(graph.get(graphIndex));
                    }
                }
            } else {
                // ... update the pointer of the down-side cell so it points the current one ...
                cell.prev = current;
                // ... and add the down-side cell to the successors of the current one.
                temp.add(cell);
            }
        }
        if (diagonal.isSelected()){
            // If we are not even at the lowermost nor at the leftmost border of the grid
            // and the down-left-side cell is not an obstacle ...
            if (r < rows-1 && c > 0 && grid[r+1][c-1] != OBST &&
                    // ... and one of the down-side or left-side cells are not obstacles ...
                    (grid[r+1][c] != OBST || grid[r][c-1] != OBST) &&
                    // ... and (only in the case are not running the A* or Greedy)
                    // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                    ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                            isInList(openSet,new Cell(r+1,c-1)) == -1 &&
                                    isInList(closedSet,new Cell(r+1,c-1)) == -1)) {
                Cell cell = new Cell(r+1,c-1);
                if (dijkstra.isSelected()){
                    if (makeConnected) {
                        temp.add(cell);
                    } else {
                        int graphIndex = isInList(graph,cell);
                        if (graphIndex > -1) {
                            temp.add(graph.get(graphIndex));
                        }
                    }
                } else {
                    // ... update the pointer of the down-left-side cell so it points the current one ...
                    cell.prev = current;
                    // ... and add the down-left-side cell to the successors of the current one.
                    temp.add(cell);
                }
            }
        }
        // If not at the leftmost limit of the grid
        // and the left-side cell is not an obstacle ...
        if (c > 0 && grid[r][c-1] != OBST &&
                // ... and (only in the case are not running the A* or Greedy)
                // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                        isInList(openSet,new Cell(r,c-1)) == -1 &&
                                isInList(closedSet,new Cell(r,c-1)) == -1)) {
            Cell cell = new Cell(r,c-1);
            if (dijkstra.isSelected()){
                if (makeConnected) {
                    temp.add(cell);
                } else {
                    int graphIndex = isInList(graph,cell);
                    if (graphIndex > -1) {
                        temp.add(graph.get(graphIndex));
                    }
                }
            } else {
                // ... update the pointer of the left-side cell so it points the current one ...
                cell.prev = current;
                // ... and add the left-side cell to the successors of the current one.
                temp.add(cell);
            }
        }
        if (diagonal.isSelected()){
            // If we are not even at the topmost nor at the leftmost border of the grid
            // and the up-left-side cell is not an obstacle ...
            if (r > 0 && c > 0 && grid[r-1][c-1] != OBST &&
                    // ... and one of the up-side or left-side cells are not obstacles ...
                    (grid[r-1][c] != OBST || grid[r][c-1] != OBST) &&
                    // ... and (only in the case are not running the A* or Greedy)
                    // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                    ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                            isInList(openSet,new Cell(r-1,c-1)) == -1 &&
                                    isInList(closedSet,new Cell(r-1,c-1)) == -1)) {
                Cell cell = new Cell(r-1,c-1);
                if (dijkstra.isSelected()){
                    if (makeConnected) {
                        temp.add(cell);
                    } else {
                        int graphIndex = isInList(graph,cell);
                        if (graphIndex > -1) {
                            temp.add(graph.get(graphIndex));
                        }
                    }
                } else {
                    // ... update the pointer of the up-left-side cell so it points the current one ...
                    cell.prev = current;
                    // ... and add the up-left-side cell to the successors of the current one.
                    temp.add(cell);
                }
            }
        }
        // When DFS algorithm is in use, cells are added one by one at the beginning of the
        // OPEN SET list. Because of this, we must reverse the order of successors formed,
        // so the successor corresponding to the highest priority, to be placed
        // the first in the list.
        // For the Greedy, A* and Dijkstra's no issue, because the list is sorted
        // according to 'f' or 'dist' before extracting the first element of.
        if (dfs.isSelected()){
            Collections.reverse(temp);
        }
        return temp;
    } // end createSuccesors()

    /**
     * Returns the index of the cell 'current' in the list 'list'
     *
     * @param list    the list in which we seek
     * @param current the cell we are looking for
     * @return        the index of the cell in the list
     *                if the cell is not found returns -1
     */
    private int isInList(ArrayList<Cell> list, Cell current){
        int index = -1;
        for (int i = 0 ; i < list.size(); i++) {
            if (current.row == list.get(i).row && current.col == list.get(i).col) {
                index = i;
                break;
            }
        }
        return index;
    } // end isInList()

    /**
     * Returns the predecessor of cell 'current' in list 'list'
     *
     * @param list      the list in which we seek
     * @param current   the cell we are looking for
     * @return          the predecessor of cell 'current'
     */
    private Cell findPrev(ArrayList<Cell> list, Cell current){
        int index = isInList(list, current);
        return list.get(index).prev;
    } // end findPrev()

    /**
     * Returns the distance between two cells
     *
     * @param u the first cell
     * @param v the other cell
     * @return  the distance between the cells u and v
     */
    private int distBetween(Cell u, Cell v){
        int dist;
        int dx = u.col-v.col;
        int dy = u.row-v.row;
        if (diagonal.isSelected()){
            // with diagonal movements
            // calculate 1000 times the Euclidean distance
            dist = (int)((double)1000*Math.sqrt(dx*dx + dy*dy));
        } else {
            // without diagonal movements
            // calculate Manhattan distances
            dist = Math.abs(dx)+Math.abs(dy);
        }
        return dist;
    } // end distBetween()

    /**
     * Calculates the path from the target to the initial position
     * of the robot, counts the corresponding steps
     * and measures the distance traveled.
     */
    private void plotRoute(){
        searching = false;
        endOfSearch = true;
        int steps = 0;
        double distance = 0;
        int index = isInList(closedSet,targetPos);
        Cell cur = closedSet.get(index);
        grid[cur.row][cur.col]= TARGET;
        do {
            steps++;
            if (diagonal.isSelected()) {
                int dx = cur.col-cur.prev.col;
                int dy = cur.row-cur.prev.row;
                distance += Math.sqrt(dx*dx + dy*dy);
            } else {
                distance++;
            }
            cur = cur.prev;
            grid[cur.row][cur.col] = ROUTE;
        } while (!(cur.row == robotStart.row && cur.col == robotStart.col));
        grid[robotStart.row][robotStart.col]=ROBOT;
        String msg;
        msg = String.format("Nós observados: %d, Passos: %d, Distância: %.3f",
                expanded,steps,distance);
        message.setText(msg);

    } // end plotRoute()

    /**
     * Gives initial values ​​for the cells in the grid.
     * With the first click on button 'Clear' clears the data
     * of any search was performed (Frontier, Closed Set, Route)
     * and leaves intact the obstacles and the robot and target positions
     * in order to be able to run another algorithm
     * with the same data.
     * With the second click removes any obstacles also.
     */
    protected void fillGrid() {
        if (searching || endOfSearch){
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    if (grid[r][c] == FRONTIER || grid[r][c] == CLOSED || grid[r][c] == ROUTE) {
                        grid[r][c] = EMPTY;
                    }
                    if (grid[r][c] == ROBOT){
                        robotStart = new Cell(r,c);
                    }
                    if (grid[r][c] == TARGET){
                        targetPos = new Cell(r,c);
                    }
                }
            }
            searching = false;
        } else {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    grid[r][c] = EMPTY;
                }
            }
            robotStart = new Cell(rows-2,1);
            targetPos = new Cell(1,columns-2);
        }
        if (aStar.isSelected() || greedy.isSelected()){
            robotStart.g = 0;
            robotStart.h = 0;
            robotStart.f = 0;
        }
        expanded = 0;
        found = false;
        searching = false;
        endOfSearch = false;

        // The first step of the other four algorithms is here
        // 1. OPEN SET: = [So], CLOSED SET: = []
        openSet.removeAll(openSet);
        openSet.add(robotStart);
        closedSet.removeAll(closedSet);

        grid[targetPos.row][targetPos.col] = TARGET;
        grid[robotStart.row][robotStart.col] = ROBOT;
        message.setText(msgDrawAndSelect);
        timer.stop();
        repaint();

    } // end fillGrid()

    /**
     * Appends to the list containing the nodes of the graph only
     * the cells belonging to the same connected component with node v.
     * This is a Breadth First Search of the graph starting from node v.
     *
     * @param v    the starting node
     */
    private void findConnectedComponent(Cell v){
        Stack<Cell> stack;
        stack = new Stack();
        ArrayList<Cell> succesors;
        stack.push(v);
        graph.add(v);
        while(!stack.isEmpty()){
            v = stack.pop();
            succesors = createSuccesors(v, true);
            for (Cell c: succesors) {
                if (isInList(graph, c) == -1){
                    stack.push(c);
                    graph.add(c);
                }
            }
        }
    } // end findConnectedComponent()

    /**
     * Initialization of Dijkstra's algorithm
     *
     * When one thinks of Wikipedia pseudocode, observe that the
     * algorithm is still looking for his target while there are still
     * nodes in the queue Q.
     * Only when we run out of queue and the target has not been found,
     * can answer that there is no solution .
     * As is known, the algorithm models the problem as a connected graph.
     * It is obvious that no solution exists only when the graph is not
     * connected and the target is in a different connected component
     * of this initial position of the robot.
     * To be thus possible negative response from the algorithm,
     * should search be made ONLY in the coherent component to which the
     * initial position of the robot belongs.
     */
    private void initializeDijkstra() {
        // First create the connected component
        // to which the initial position of the robot belongs.
        graph.removeAll(graph);
        findConnectedComponent(robotStart);
        // Here is the initialization of Dijkstra's algorithm
        // 2: for each vertex v in Graph;
        for (Cell v: graph) {
            // 3: dist[v] := infinity ;
            v.dist = INFINITY;
            // 5: previous[v] := undefined ;
            v.prev = null;
        }
        // 8: dist[source] := 0;
        graph.get(isInList(graph,robotStart)).dist = 0;
        // 9: Q := the set of all nodes in Graph;
        // Instead of the variable Q we will use the list
        // 'graph' itself, which has already been initialised.

        // Sorts the list of nodes with respect to 'dist'.
        Collections.sort(graph, new CellComparatorByDist());
        // Initializes the list of closed nodes
        closedSet.removeAll(closedSet);
    } // end initializeDijkstra()

    /**
     * paints the grid
     */
    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);  // Fills the background color.

        g.setColor(Color.DARK_GRAY);
        g.fillRect(10, 10, columns*squareSize+1, rows*squareSize+1);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                if (grid[r][c] == EMPTY) {
                    g.setColor(Color.WHITE);
                } else if (grid[r][c] == ROBOT) {
                    g.setColor(Color.RED);
                } else if (grid[r][c] == TARGET) {
                    g.setColor(Color.GREEN);
                } else if (grid[r][c] == OBST) {
                    g.setColor(Color.BLACK);
                } else if (grid[r][c] == FRONTIER) {
                    g.setColor(Color.BLUE);
                } else if (grid[r][c] == CLOSED) {
                    g.setColor(Color.CYAN);
                } else if (grid[r][c] == ROUTE) {
                    g.setColor(Color.YELLOW);
                }
                g.fillRect(11 + c*squareSize, 11 + r*squareSize, squareSize - 1, squareSize - 1);
            }
        }


        if (drawArrows.isSelected()) {
            // We draw all arrows from each open or closed state
            // to its predecessor.
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    // If the current cell is the goal and the solution has been found,
                    // or belongs in the route to the target,
                    // or is an open state,
                    // or is a closed state but not the initial position of the robot
                    if ((grid[r][c] == TARGET && found)  || grid[r][c] == ROUTE  ||
                            grid[r][c] == FRONTIER || (grid[r][c] == CLOSED &&
                            !(r == robotStart.row && c == robotStart.col))){
                        // The tail of the arrow is the current cell, while
                        // the arrowhead is the predecessor cell.
                        Cell head;
                        if (grid[r][c] == FRONTIER){
                            if (dijkstra.isSelected()){
                                head = findPrev(graph,new Cell(r,c));
                            } else {
                                head = findPrev(openSet,new Cell(r,c));
                            }
                        } else {
                            head = findPrev(closedSet,new Cell(r,c));
                        }
                        // The coordinates of the center of the current cell
                        int tailX = 11+c*squareSize+squareSize/2;
                        int tailY = 11+r*squareSize+squareSize/2;
                        // The coordinates of the center of the predecessor cell
                        int headX = 11+head.col*squareSize+squareSize/2;
                        int headY = 11+head.row*squareSize+squareSize/2;
                        // If the current cell is the target
                        // or belongs to the path to the target ...
                        if (grid[r][c] == TARGET  || grid[r][c] == ROUTE){
                            // ... draw a red arrow directing to the target.
                            g.setColor(Color.RED);
                            drawArrow(g,tailX,tailY,headX,headY);
                            // Else ...
                        } else {
                            // ... draw a black arrow to the predecessor cell.
                            g.setColor(Color.BLACK);
                            drawArrow(g,headX,headY,tailX,tailY);
                        }
                    }
                }
            }
        }
    } // end paintComponent()

    /**
     * Draws an arrow from point (x2,y2) to point (x1,y1)
     */
    private void drawArrow(Graphics g1, int x1, int y1, int x2, int y2) {
        Graphics2D g = (Graphics2D) g1.create();

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        // We design an horizontal arrow 'len' in length
        // that ends at the point (0,0) with two tips 'arrowSize' in length
        // which form 20 degrees angles with the axis of the arrow ...
        g.drawLine(0, 0, len, 0);
        g.drawLine(0, 0, (int)(arrowSize*Math.sin(70*Math.PI/180)) , (int)(arrowSize*Math.cos(70*Math.PI/180)));
        g.drawLine(0, 0, (int)(arrowSize*Math.sin(70*Math.PI/180)) , -(int)(arrowSize*Math.cos(70*Math.PI/180)));
        // ... and class AffineTransform handles the rest !!!!!!
        // Java is admirable!!! Isn't it ?
    } // end drawArrow()

} // end nested classs MazePanel