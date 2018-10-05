import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.stream.IntStream;
import javax.swing.*;

public class View
{
	private static Controller c;
	private static int NUM_ROWS;

	private static final Color[] PALETTE = new Color[]{Color.GRAY, Color.WHITE, Color.BLACK, Color.RED, Color.BLUE};
	private static final int SCR_H = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	private static final int SCR_W = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	private static final int SCRSIZE = Math.min(SCR_H, SCR_W);
	static int boarder_pxl;
	static ArrayList<Point> cellCenters = new ArrayList<>();
	private static int h;	// height. Distance between centres of two adjacent hexes. Distance between two opposite sides in a hex.
	private static int s;	// length of one side
	private static int r;	// radius of inscribed circle (centre to middle of each side). r= h/2
	private static int a;
	private static DrawingPanel panel;// = new DrawingPanel();
	
	public View(Controller c) {
		this.c = c;
		c.setView(this);
		reset();
	}

	public void reset() {
		NUM_ROWS = 2 * c.getBoardSize() - 1;
		boarder_pxl = SCRSIZE / NUM_ROWS / 2;
		h= boarder_pxl;	// height. Distance between centres of two adjacent hexes. Distance between two opposite sides in a hex.
		s= boarder_pxl;	// length of one side
		r=h/2;	// radius of inscribed circle (centre to middle of each side). r= h/2
		a=(int) (Math.sqrt(3)*(h/2.0));
		cellCenters.clear();
	}

	private static void initShowUI(Controller c) {
		new View(c);
		JFrame frame = new JFrame("Omega");
		panel = new DrawingPanel();
		frame.getContentPane().add(panel);
		frame.setJMenuBar(makeMenu(frame));
		frame.setSize(SCRSIZE, SCRSIZE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

	private static JMenuBar makeMenu(JFrame frame) {
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

		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				final JOptionPane optionPane = new JOptionPane("Choose board size:",
						JOptionPane.PLAIN_MESSAGE,
						JOptionPane.DEFAULT_OPTION);

				Object[] options = IntStream.range(c.getMinSize(), c.getMaxSize() + 1).mapToObj(i -> i).toArray();

				Object res = JOptionPane.showInputDialog(
						frame,
						"Choose board size:",
						"Choose Board Size",
						JOptionPane.PLAIN_MESSAGE,
						null,
						options,
						null);

				if (res != null) {
					c.setBoardSize((byte) (int) res);
					System.out.println("New board size " + res);
				}

//				}
			}
		});
		menu.add(menuItem);

		//Choose computer player index
		submenu = new JMenu("Computer Player... ");

		ButtonGroup group = new ButtonGroup();

//			System.out.println();
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


	static class DrawingPanel extends JPanel
	{
		public DrawingPanel()
		{
			setBackground(Color.LIGHT_GRAY);
			addMouseListener(new MyMouseListener());
		}

		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D)g;
			drawBoard(c.getBoardSize(), g2, SCRSIZE/8, SCRSIZE/8);
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
			}
		}
	} 

	public void repaint() {
	    panel.repaint();
	}

	public static void drawBoard(int size, Graphics2D g2, int x0, int y0) {
		boolean add = cellCenters.isEmpty();
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
				if (add)
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
				if (add)
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
		int y = y0;// + boarder_pxl;
		int x = x0;// + boarder_pxl; // + (XYVertex ? t : 0); //Fix added for XYVertex = true.
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

	public static void main(String[] args)
	{
		Controller c = new Controller();

		SwingUtilities.invokeLater(() -> {
			initShowUI(c);
		});
	}
}