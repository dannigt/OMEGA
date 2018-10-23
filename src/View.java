import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class View
{
	private static Controller c;
	private static int NUM_ROWS;

	private static final Color[] PALETTE = new Color[]{Color.GRAY, Color.WHITE, Color.BLACK, Color.RED, Color.BLUE};
	private static final int SCR_H = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	private static final int SCR_W = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	private static final int SCRSIZE = Math.min(SCR_H, SCR_W);
	private static int boarder_pxl;
	private static ArrayList<Point> cellCenters = new ArrayList<>();
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

//	public void setEnabled(boolean enabled) {
//		panel.setEnabled(enabled);
//	}

	public static void createAndShowGUI(Controller c) {
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

		menuItem = new JMenuItem("Start");
		menuItem.addActionListener(e -> {
			c.start();
		});
		menu.add(menuItem);
		menu.addSeparator();

		//Set board size
		menuItem = new JMenuItem("Set Board Size");

		menuItem.addActionListener(e -> {
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
		});
		menu.add(menuItem);

		//Choose computer player index

		for (byte p=0; p < c.numPlayers(); p++) {
			submenu = new JMenu("Configure Player " + (p+1));
			ButtonGroup group = new ButtonGroup();

			for (byte s=0; s < c.getStrategies().length; s++) {
				rbMenuItem = new JRadioButtonMenuItem(c.getStrategies()[s]);
				if (s == c.getPlayerStrategy(p)) {
					rbMenuItem.setSelected(s == c.getPlayerStrategy(p));
				}
				byte p_final = p;
				byte s_final = s;
				rbMenuItem.addActionListener(e -> {
					c.setPlayerStrategy(p_final, s_final); // Requires a final/effectively final var
				});
				group.add(rbMenuItem);
				submenu.add(rbMenuItem);
			}
			menu.add(submenu);
		}


		//a group of check box menu items
		menu.addSeparator();
		cbMenuItem = new JCheckBoxMenuItem("Keep time");
		menu.add(cbMenuItem);

//		cbMenuItem = new JCheckBoxMenuItem("Another one");
//		menu.add(cbMenuItem);

		//Build second menu in the menu bar.
		menu = new JMenu("Oops...");
		menuItem = new JMenuItem("Load Game Status From...");
		menuItem.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
			// Demonstrate "Save" dialog:
			int rVal = chooser.showSaveDialog(panel);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				String path = Paths.get(chooser.getCurrentDirectory().toString(), chooser.getSelectedFile().getName()).toString();
				try {
					c.applyCache(path);
				}
				catch (Exception ex){
					JOptionPane.showMessageDialog(null,
							"Cannot load the progress dump. Make sure the correct file is chosen.",
							"Loading failed", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		menu.add(menuItem);

		menuItem = new JMenuItem("Reverse");
		menuItem.addActionListener(e -> {
			c.reverseMove();
		});
		menu.add(menuItem);

		menuBar.add(menu);

		return menuBar;
	}

	static class DrawingPanel extends JPanel
	{
	    private JLabel progress_info;
	    private JLabel warning_info;

        public DrawingPanel()
		{
//			super(new GridBagLayout());
			setBackground(Color.LIGHT_GRAY);
			addMouseListener(new MyMouseListener());

			progress_info = new JLabel(c.getProgressInfo());
			this.add(progress_info);

//			warning_info = new JLabel();
//			this.add(warning_info);

			this.setEnabled(false);
		}

		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D)g;
			drawBoard(c.getBoardSize(), g2, SCRSIZE/8, SCRSIZE/8);
			progress_info.setText(formatHTML(new String[] {c.getProgressInfo(), c.getScore(), c.getWarningInfo()}));
		}
		
		class MyMouseListener extends MouseAdapter	{
			public void mouseClicked(MouseEvent e) { 
				int x = e.getX(); 
				int y = e.getY(); 

				try {
					c.processCellClick(pointToCellIndex(x, y), true);
				}
				catch (IllegalArgumentException e1) {
					System.out.println("SHOW ON UI: " + e1.getMessage());
					c.setWarningInfo(e1.getMessage());
					repaint();
				}
			}
		}

		// Format stuff with line breaks
		private String formatHTML(String... args) {
			String res = "<html>";
			for (String arg : args) {
				res += arg + "<br/>";
			}
//			System.out.println(res);
			return res + "</html>";
		}
	} 

	public void repaint() {
	    panel.repaint();
	}

	private static void drawBoard(int size, Graphics2D g2, int x0, int y0) {
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
				drawHex(x, y, g2, to_paint, cnt);
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
				drawHex(x, y, g2, PALETTE[c.getCellColor(cnt)], cnt);
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

	private static void drawHex(int x, int y, Graphics2D g2, Color cell_color, int index) {

		Polygon poly = hex(x,y);
		g2.setColor(cell_color);
		g2.fillPolygon(poly);
		g2.setColor(Color.BLACK);
		g2.drawPolygon(poly);

		g2.drawString(Integer.toString(index), x, y);
	}
	
	//take canvas point (x, y), convert to cell index.
	private static short pointToCellIndex(int x, int y) {
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

//	public static void main(String[] args)
//{
//    Controller c = new Controller();
//
//    SwingUtilities.invokeLater(new Runnable() {
//        public void run() {
//            createAndShowGUI(c);
//        }
//    });
//}
}