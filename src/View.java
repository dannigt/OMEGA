import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;

//import Controller.DrawingPanel;
//import Controller.DrawingPanel.MyMouseListener;

public class View
{
	static Controller c;
	//constants and global variables
	final static Color COLOURBACK =  Color.WHITE;
	final static Color COLOURCELL =  Color.WHITE;	 
	final static Color COLOURGRID =  Color.BLACK;	 
	
	final static Color[] PALETTE = new Color[]{Color.GRAY, Color.WHITE, Color.BLACK, Color.RED, Color.BLUE};
	static Color[] player_colors;
	
	final static Color COLOURONE = new Color(255,255,255,200);
	final static Color COLOURONETXT = Color.BLUE;
	final static Color COLOURTWO = new Color(0,0,0,200);
	final static Color COLOURTWOTXT = new Color(255,100,255);
	
	final static int EMPTY = 0;
	final static int BSIZE = 3; //board size.
	final static int NUM_ROWS = 2 * BSIZE - 1;

	final static int SCRSIZE = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
//	final static int SCRSIZE = height; //HEXSIZE * (BSIZE + 1) + BORDERS*3; //screen size (vertical dimension).
	static int BORDERS = SCRSIZE / NUM_ROWS / 2;
	final static int HEXSIZE = BORDERS;	//hex size in pixels
	
	//canvas x and y coordinates of all cells
	private static ArrayList<Integer> xs = new ArrayList<Integer>();
	private static ArrayList<Integer> ys = new ArrayList<Integer>();
	private static ArrayList<Point> cellCenters = new ArrayList<Point>();
	
//	private static int BORDERS;	//default number of pixels for the border.
	
	private static int h=HEXSIZE;	// height. Distance between centres of two adjacent hexes. Distance between two opposite sides in a hex.
	private static int s=HEXSIZE;	// length of one side
	private static int t=0;	// short side of 30o triangle outside of each hex
	private static int r=h/2;	// radius of inscribed circle (centre to middle of each side). r= h/2
	private static int a=(int) (Math.sqrt(3)*(h/2.0));
	
	private static int num_colors;

	public View(Controller c, int num_player) {
		this.c = c;
		this.num_colors = num_player;
		this.player_colors = Arrays.copyOfRange(PALETTE, 0, num_player + 1);
	}
	
	public void createAndShowGUI()
	{
		DrawingPanel panel = new DrawingPanel();

		//JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new JFrame("Hex Testing 4");
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		Container content = frame.getContentPane();
		content.add(panel);
		//this.add(panel);  -- cannot be done in a static context
		//for hexes in the FLAT orientation, the height of a 10x10 grid is 1.1764 * the width. (from h / (s+t))
		frame.setSize( SCRSIZE, SCRSIZE);
		frame.setResizable(false);
		frame.setLocationRelativeTo( null );
		frame.setVisible(true);
	}

	class DrawingPanel extends JPanel
	{		
		public DrawingPanel()
		{	
			setBackground(COLOURBACK);          
			addMouseListener(new MyMouseListener());
		}

		public void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D)g;
//			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//			g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
//			super.paintComponent(g2);
			drawEmptyBoard(BSIZE, g2, SCRSIZE/8, SCRSIZE/8);
		}
		
		class MyMouseListener extends MouseAdapter	{
			public void mouseClicked(MouseEvent e) { 
				int x = e.getX(); 
				int y = e.getY(); 
			
				System.out.println("cell " + pointToCellIndex(x, y) + " clicked");
				
				//TODO: pass cell index to controller.
//				c.processCellClick(pointToCellIndex(x, y));
				int flag = c.processCellClick(pointToCellIndex(x, y));
				if (flag == -1){
					System.out.println("----------------Illegal!----------------");
				}
				
				repaint();
			}		 
		}
	} 
	
//	public static void setXYasVertex(boolean b) {
//		XYVertex=b;
//	}
//	public static void setBorders(int b){
//		BORDERS=b;
//	}

	public static void setHeight(int height) {
		h = height;			// h = basic dimension: height (distance between two adj centresr aka size)
//		r = h/2;			// r = radius of inscribed circle
		
		s = height;
		a = (int) (Math.sqrt(3)*(height/2.0));
	}

	public static void drawEmptyBoard(int size, Graphics2D g2, int x0, int y0) {
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
//		super.paintComponent(g2);
		
		//draw grid
		int cnt = 0;
		int num_rows = 2 * size - 1;
//		drawHex(x0, y0, g2, COLOURCELL); // reference point
		
		for (int i=0; i <= num_rows/2; i++) {
			int startX = x0 + a * (num_rows/2 - i); // towards right. X axis.
			int y = (y0 + (int)(Math.sqrt(3) * a * i)); // towards down. Y axis.
//			int lowerY = (y0 + (int)(Math.sqrt(3) * a * (num_rows - i - 1)));
			int tilesPerRow = size + i;
			
			for (int j=0; j < tilesPerRow; j++) {
				int x = startX + 2 * a * j;
				
				int foo = c.getCellColor(cnt);
				Color to_paint = player_colors[foo];
				drawHex(x, y, g2, to_paint);  
				cnt++;
				cellCenters.add(new Point(x, y));
			}
		}
		
		for (int i=1; i <= num_rows/2; i++) {
			int startX = x0 + a * (i); // towards right. X axis.
			int y = (y0 + (int)(Math.sqrt(3) * a * (i + num_rows/2))); // towards down. Y axis.
			int tilesPerRow = num_rows - i;
			
			for (int j=0; j < tilesPerRow; j++) {
				int x = startX + 2 * a * j;
				drawHex(x, y, g2, player_colors[c.getCellColor(cnt)]);  
				cnt++;
				cellCenters.add(new Point(x, y));
				
			}
		}
//		System.out.println(cellCenters.toString());
	}
	
/*********************************************************
Name: hex()
Parameters: (x0,y0) This point is normally the top left corner 
    of the rectangle enclosing the hexagon. 
    However, if XYVertex is true then (x0,y0) is the vertex of the 
    top left corner of the hexagon. 
Returns: a polygon containing the six points.
Called from: drawHex(), fillhex()
Purpose: This function takes two points that describe a hexagon
and calculates all six of the points in the hexagon.
*********************************************************/
	public static Polygon hex (int x0, int y0) {
		int[] cornerXs,cornerYs;
		int y = y0;// + BORDERS;
		int x = x0;// + BORDERS; // + (XYVertex ? t : 0); //Fix added for XYVertex = true. 
//		  *for POINTY ORIENTATION:
		cornerXs = new int[] {x,x+a,x+a,x,x-a,x-a};
		cornerYs = new int[] {y-s, y-s/2, y+s/2, y+s, y+s/2, y-s/2};
		return new Polygon(cornerXs,cornerYs,6); //hexagon 6 sides
	}

/********************************************************************
Name: drawHex()
Parameters: (i,j) : the x,y coordinates of the inital point of the hexagon
	    g2: the Graphics2D object to draw on.
Returns: void
Calls: hex() 
Purpose: This function draws a hexagon based on the initial point (x,y).
The hexagon is drawn in the colour specified in hexgame.COLOURELL.
*********************************************************************/
	public static void drawHex(int x, int y, Graphics2D g2, Color cell_color) {
		Polygon poly = hex(x,y);
		g2.setColor(cell_color);
		g2.fillPolygon(poly);
//		System.out.println(cell_color);
		g2.setColor(COLOURGRID);
		g2.drawPolygon(poly);
	}
	
	//take canvas point (x, y), convert to cell index.
	public static int pointToCellIndex(int x, int y) {
		Point given = new Point(x, y);
		Point nearest = cellCenters.get(0);
		for (Point p : cellCenters) {
			if (p.distance(given) < nearest.distance(given)) {
				nearest = p;
			}
		}
		//Ensure that the point is within the board
		return (nearest.distance(given) < a) ? cellCenters.indexOf(nearest) : -1;
	}
	
/***************************************************************************
* Name: fillHex()
* Parameters: (i,j) : the x,y coordinates of the initial point of the hexagon
		n   : an integer number to indicate a letter to draw in the hex
		g2  : the graphics context to draw on
* Return: void
* Called from:
* Calls: hex()
*Purpose: This draws a filled in polygon based on the coordinates of the hexagon.
	  The colour depends on whether n is negative or positive.
	  The colour is set by hexgame.COLOURONE and hexgame.COLOURTWO.
	  The value of n is converted to letter and drawn in the hexagon.
*****************************************************************************/
	public static void fillHex(int i, int j, int n, Graphics2D g2) {
		char c='o'; 
		int x = i * (s+t);
		int y = j * h + (i%2) * h/2;
		if (n < 0) {
			g2.setColor(COLOURONE);
			g2.fillPolygon(hex(x,y));
			g2.setColor(COLOURONETXT);
			c = (char)(-n);
			g2.drawString(""+c, x+r+BORDERS, y+r+BORDERS+4); //FIXME: handle XYVertex
			//g2.drawString(x+","+y, x+r+BORDERS, y+r+BORDERS+4);
		}
		if (n > 0) {
			g2.setColor(COLOURTWO);
			g2.fillPolygon(hex(x,y));
			g2.setColor(COLOURTWOTXT);
			c = (char)n;
			g2.drawString(""+c, x+r+BORDERS, y+r+BORDERS+4); //FIXME handle XYVertex
			//g2.drawString(i+","+j, x+r+BORDERS, y+r+BORDERS+4);
		}
	}
}