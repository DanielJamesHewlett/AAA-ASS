package Graph;


import java.util.Arrays;
import java.util.ArrayList;

import java.awt.Point;

import utils.PointUtils;

import algos.Sort;
import algos.Search;

public class PRM {

    /*
     - k => number of neighbours
     - n_obstacles => number of obstacles in the map
     - n_nodes => number of nodes in the graph
     - dim => dimensions of the map
    */
    private int k, n_obstacles, n_nodes, dim;
    
    /*
     - nodes is a collection of the nodes in the graph
     - top_left is a collection of the coordinates of the top-left corners of every rectangle
     - bottom_right is a collection of the coordinates of the bottom-right corners of every rectangle
    */
    private ArrayList<Point> nodes, top_left, bottom_right;

    private double[][] adj;

    public PRM(ArrayList<Point> nodes, ArrayList<Point> top_left, ArrayList<Point> bottom_right, int k, int n_obstacles,
            int n_nodes, int dim) {
        
        // initialise vars
        this.k = k;
        this.n_obstacles = n_obstacles;
        this.n_nodes = n_nodes;
        this.dim = dim;

        this.nodes = nodes;
        this.top_left = top_left;
        this.bottom_right = bottom_right;

        // remove samples that overlap with obstacles and update n_nodes accordingly
        removeSamples();
        this.n_nodes = this.nodes.size();

        this.adj = new double[this.n_nodes][this.n_nodes];
        // initialise adjacency matrix adj; fill with zeros
        for (double[] row: this.adj) {
            Arrays.fill(row, 0);
        }

        /*
         - Create adjacency between every node
         - For each adjacency in adj, add the distance between the two nodes
        */
        for ( int i = 0; i < this.n_nodes - 1; ++i ) {
            for ( int j = i + 1; j < this.n_nodes; ++j ) {
                if ( i == j ) {
                    this.adj[i][j] = 0;
                } else {
                    this.adj[i][j] = PointUtils.distance( this.nodes.get(i), this.nodes.get(j) );
                    this.adj[j][i] = adj[i][j];
                }
            }
        }

        /*
         - Find k nearest neighbours by sorting each row so that the first k values (after 
           the first which is zero; distance between a node and itself) are the distances 
           to the k nearest neighbours.
         - Set the value in the adjacency matrix of each neighbour to negative one.
        */
        for ( double[] row: this.adj ) {
            int n_neighbours = this.n_nodes - 1;
            while ( n_neighbours > k ) {
                double max = Arrays.stream( row ).max().getAsDouble();
                row[ Search.linearSearch(row, max) ] = 0;
                --n_neighbours;
            }
            
            for ( int i = 0; i < this.n_nodes; ++i ) {
                if ( row[i] != 0 ) {
                    row[i] = 1;
                }
            }
        }

        // remove edges that overlap obstacles
        removeEdges();
    }

    public double[][] get_adjacency() {
        return this.adj;
    }

    public int[][] manhattanAdj() {
        int[][] manhattan = new int[this.n_nodes][this.n_nodes];
        for ( int i = 0; i < this.n_nodes; ++i ) {
            for ( int j = 0; j < this.n_nodes; ++j ) {
                if ( adj[i][j] == 1 ) {
                    manhattan[i][j] = manhattanDist(i, j);
                }
            }
        }
        return manhattan;
    }

    private int manhattanDist(int node_1, int node_2) {
        Point a = this.nodes.get(node_1);
        Point b = this.nodes.get(node_2);

        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    // helper function for generate; removes samples that removes samples that overlap obstacles
    private void removeSamples() {
        ArrayList<Point> nodes_clone = (ArrayList<Point>) this.nodes.clone();
        // number of obstacles = number of top-left corners = number of bottom-right corners
        for ( Point sample: nodes_clone ) {
            for ( int i = 0; i < this.n_obstacles; ++i ) {
                /*
                  if a samples x and y coordinates are between the top-left and bottom-right corners' x and y
                  coordinates remove the sample.
                */
                if ( sample.x >= this.top_left.get(i).x && sample.x <= this.bottom_right.get(i).x ) {
                    if ( sample.y >= this.top_left.get(i).y && sample.y <= this.bottom_right.get(i).y ) {
                        this.nodes.remove( sample );
                        break;
                    }
                }

            }
        }
    }

    /*
       - Check if the lines between two nodes crosses any of the 
       boundary lines of each obstacle. If it does. remove the
       edge between them.
       - Represent the line as a function of x [line_0] and evaluate it at
       each boundary x-value. Remove an edge if the output value
       of the line is in between the boundary y-values of
       the obstacle.
       - Represent the line as a function of y [line_1] and evaluate it at
       each boundary y-value. Remove an edge if the output value
       of the line is in between the boundary x-values of
       the obstacle.
    */
    private void removeEdges() {
        for ( int i = 0; i < this.n_nodes; ++i ) {
            for ( int j = i + 1; j < this.n_nodes; ++j ) {
                if ( adj[i][j] == 0 ) {
                    continue;
                }

                // get the nodes that make the adjacency
                Point a = this.nodes.get(i);
                Point b = this.nodes.get(j);

                // top-left and bottom-right corners of the rectangle formed by nodes a and b
                Point edge_tl = new Point( Math.min( a.x, b.x ), Math.min( a.y, b.y ) );
                Point edge_br = new Point( Math.max( a.x, b.x ), Math.max( a.y, b.y ) );


                /*
                 - if y values are equal, check that the value is within the horizontal boundaries of
                   the obstacle and that the x values are outside of the vertical boundaries of the
                   obstacle.
                 - if x values are equal, check that the value is within the vertical boundaries of
                   the obstacle and that the y values are outside of the horizontal boundaries of the
                   obstacle.
                */
                if ( a.y == b.y ) {
                    for ( int k = 0; k < this.n_obstacles; ++k ) {
                        if ((this.top_left.get(k).y <= a.y && a.y <= this.bottom_right.get(k).y)
                                && (a.x <= this.top_left.get(k).x && this.bottom_right.get(k).x <= a.x)) {
                            this.adj[i][j] = 0;
                            this.adj[j][i] = 0;
                            break;
                        }
                    }
                } else if ( a.x == b.x ) {
                    for ( int k = 0; k < this.n_obstacles; ++k ) {
                        if ((top_left.get(k).x <= a.x && a.x <= bottom_right.get(k).x)
                                && (a.y <= top_left.get(k).y && bottom_right.get(k).y <= a.y)) {
                            this.adj[i][j] = 0;
                            this.adj[j][i] = 0;
                            break;
                        }
                    }
                } else {
                    // slope of the line_0
                    double m_y = (double) (b.y - a.y) / (b.x - a.x);
                    // slope of the line_1
                    double m_x = (double) (b.x - a.x) / (b.y - a.y);
    
                    // y intercept of the line_0
                    double c_y = b.y - b.x * m_y;
                    // x intercept of the line_1
                    double c_x = b.x - b.y * m_x;
    
                    /*
                       for every obstacle, check if the output of each line at each boundary value
                       is within the relevant boundary range (i.e. y-range of obstacle for line_0
                       and x-range of obstacle for line_1)
                    */
                    for ( int k = 0; k < this.n_obstacles; ++k ) {
                        double x_left = this.top_left.get(k).x,
                               x_right = this.bottom_right.get(k).x,
                               y_top = this.top_left.get(k).y,
                               y_bottom = this.bottom_right.get(k).y;

                        /*
                           The obstacle must intersect the rectangle created by the nodes. If this isn't the
                           case, then the line through the points may intersect the obstacle elsewhere i.e.
                           another part of the line which isn't the edge between the nodes overlaps the ob-
                           stacle.
                        */
                        if ((edge_tl.y <= y_top && y_top <= edge_br.y)
                                || (edge_tl.y <= y_bottom && y_bottom <= edge_br.y)
                                || (edge_tl.x <= x_left && x_left <= edge_br.x)
                                || (edge_tl.x <= x_right && x_right <= edge_br.x)) {
                                    
                            // evaluate line_0 at boundary x-values
                            double y_0 = m_y * x_left + c_y;
                            double y_1 = m_y * x_right + c_y;
        
                            // evaluate line_1 at boundary y-values
                            double x_0 = m_x * y_top + c_x;
                            double x_1 = m_x * y_bottom + c_x;
        
        
                            if ((y_top <= y_0 && y_0 <= y_bottom)
                                    || (y_top <= y_1 && y_1 <= y_bottom)
                                    || (x_left <= x_0 && x_0 <= x_right)
                                    || (x_left <= x_1 && x_1 <= x_right)) {
    
                                this.adj[i][j] = 0;
                                this.adj[j][i] = 0;
                                break;
                            }

                        }
                    }
                }
            }
        }
    }
}