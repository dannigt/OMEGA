import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.MenuBar;
import java.util.ArrayList;

import javax.swing.*;

public class View
{
	static Controller c;
	//constants and global variables
	final static Color[] PALETTE = new Color[]{Color.GRAY, Color.WHITE, Color.BLACK, Color.RED, Color.BLUE};

	static int BSIZE; //board size.
	static int NUM_ROWS;

	final static int SCRSIZE = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
//	final static int SCRSIZE = height; //HEXSIZE * (BSIZE + 1) + BORDERS*3; //screen size (vertical dimension).
	static int BORDERS;
	static int HEXSIZE;	//hex size in pixels
	
	//canvas x and y coordinates of all cells
	static ArrayList<Integer> xs = new ArrayList<Integer>();
	static ArrayList<Integer> ys = new ArrayList<Integer>();
	static ArrayList<Point> cellCenters = new ArrayList<Point>();
	
//	private static int BORDERS;	//default number of pixels for the border.
	
	private static int h;	// height. Distance between centres of two adjacent hexes. Distance between two opposite sides in a hex.
	private static int s;	// length of one side
	private static int t;	// short side of 30o triangle outside of each hex
	private static int r;	// radius of inscribed circle (centre to middle of each side). r= h/2
	private static int a;
	private DrawingPanel panel;
	
	public View(Controller c, int board_size) {
		this.c = c;
		c.setView(this);
		this.BSIZE = board_size;
//		this.player_colors = Arrays.copyOfRange(PALETTE, 0, num_player + 1);
		
		NUM_ROWS = 2 * BSIZE - 1;
//		final static int SCRSIZE = height; //HEXSIZE * (BSIZE + 1) + BORDERS*3; //screen size (vertical dimension).
		BORDERS = SCRSIZE / NUM_ROWS / 2;
		HEXSIZE = BORDERS;	//hex size in pixels
		
		h=HEXSIZE;	// height. Distance between centres of two adjacent hexes. Distance between two opposite sides in a hex.
		s=HEXSIZE;	// length of one side
		t=0;	// short side of 30o triangle outside of each hex
		r=h/2;	// radius of inscribed circle (centre to middle of each side). r= h/2
		a=(int) (Math.sqrt(3)*(h/2.0));

		panel = new DrawingPanel();

		//JFrame.setDefaultLookAndFeelDecorated(true);

	}

	public void initShowUI() {
		JFrame frame = new JFrame("Omega");
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		Container content = frame.getContentPane();
		content.add(panel);
		//this.add(panel);  -- cannot be done in a static context
		frame.setSize( SCRSIZE, SCRSIZE);
		frame.setResizable(false);
		frame.setLocationRelativeTo( null );
		frame.setVisible(true);
		frame.setJMenuBar(makeMenu());
	}

	private JMenuBar makeMenu() {
		//Create the menu bar.
		JMenu menu, submenu;
		JMenuItem menuItem;
		JRadioButtonMenuItem rbMenuItem;
		JCheckBoxMenuItem cbMenuItem;

		JMenuBar menuBar = new JMenuBar();

		//Build the first menu.
		menu = new JMenu("Start");

		menuBar.add(menu);

		//Set board size
		menuItem = new JMenuItem("Set Board Size");
		menu.add(menuItem);

		//Choose computer player index
		submenu = new JMenu("Computer Player... ");

		ButtonGroup group = new ButtonGroup();

		for (byte i=1; i <= c.numPlayers(); i++) {
			System.out.println(i);
			rbMenuItem = new JRadioButtonMenuItem("Player " + i);
			rbMenuItem.setSelected(false);
			group.add(rbMenuItem);
			submenu.add(rbMenuItem);
		}

		menuItem = new JMenuItem("An item in the submenu");
		submenu.add(menuItem);

		menuItem = new JMenuItem("Another item");
		submenu.add(menuItem);
		menu.add(submenu);

//		menuItem = new JMenuItem("Both text and icon",
//				new ImageIcon("images/middle.gif"));
//		menu.add(menuItem);
//		menuItem = new JMenuItem(new ImageIcon("images/middle.gif"));
//		menu.add(menuItem);

		//a group of check box menu items
		menu.addSeparator();
		cbMenuItem = new JCheckBoxMenuItem("Keep time");
		menu.add(cbMenuItem);

		cbMenuItem = new JCheckBoxMenuItem("Another one");
		menu.add(cbMenuItem);


//Build second menu in the menu bar.
		menu = new JMenu("Another Menu");
		menu.getAccessibleContext().setAccessibleDescription(
				"This menu does nothing");
		menuBar.add(menu);
		return menuBar;
	}

	class DrawingPanel extends JPanel
	{		
		public DrawingPanel()
		{	
			setBackground(Color.WHITE);
			addMouseListener(new MyMouseListener());
		}

		public void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D)g;
			drawBoard(BSIZE, g2, SCRSIZE/8, SCRSIZE/8);
		}
		
		class MyMouseListener extends MouseAdapter	{
			public void mouseClicked(MouseEvent e) { 
				int x = e.getX(); 
				int y = e.getY(); 

				try {
					c.processCellClick(pointToCellIndex(x, y));
				}
				catch (IllegalArgumentException e1) {
					System.out.println("SHOW ON UI: " + e1.getMessage());
				}
				repaint();
			}		 
		}
	} 

	public void repaint() {
	    panel.repaint();
	}

//	public static void setXYasVertex(boolean b) {
//		XYVertex=b;
//	}
//	public static void setBorders(int b){
//		BORDERS=b;
//	}
//	public static void setHeight(int height) {
//		h = height;			// h = basic dimension: height (distance between two adj centresr aka size)
//		s = height;
//		a = (int) (Math.sqrt(3)*(height/2.0));
//	}

	public static void drawBoard(int size, Graphics2D g2, int x0, int y0) {
		//draw grid
		short cnt = 0;
		int num_rows = 2 * size - 1;
//		drawHex(x0, y0, g2, COLOURCELL); // reference point
		
		for (int i=0; i <= num_rows/2; i++) {
			int startX = x0 + a * (num_rows/2 - i); // towards right. X axis.
			int y = (y0 + (int)(Math.sqrt(3) * a * i)); // towards down. Y axis.
//			int lowerY = (y0 + (int)(Math.sqrt(3) * a * (num_rows - i - 1)));
			int tilesPerRow = size + i;
			
			for (int j=0; j < tilesPerRow; j++) {
				int x = startX + 2 * a * j;
				
				Color to_paint = PALETTE[c.getCellColor(cnt)];
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
				drawHex(x, y, g2, PALETTE[c.getCellColor(cnt)]);  
				cnt++;
				cellCenters.add(new Point(x, y));
			}
		}
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
		g2.setColor(Color.BLACK);
		g2.drawPolygon(poly);
	}
	
	//take canvas point (x, y), convert to cell index.
	public static short pointToCellIndex(int x, int y) {
		Point given = new Point(x, y);
		Point nearest = cellCenters.get(0);
		for (Point p : cellCenters) {
			if (p.distance(given) < nearest.distance(given)) {
				nearest = p;
			}
		}
		//Ensure that the point is within the board
		return (short)((nearest.distance(given) < a) ? cellCenters.indexOf(nearest) : -1);
	}
}