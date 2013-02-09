import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.geom.*;
import java.util.*;
import javax.swing.Timer;

public class DisplayImage{
    private static final int WIDTH = 650;
    private static final int HEIGHT = 670; 
    public static void main(String[] args){
        SwingUtilities.invokeLater( new Runnable() {
                public void run(){
                    createAndShowGUI();
                }
            } );
    }

    public static void createAndShowGUI(){
        JFrame frame = new ImageFrame(WIDTH,HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible( true );
    }
}

class ImageFrame extends JFrame{
    private int width = 600, height = 600;
    private BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
    private Graphics2D g2d = (Graphics2D)image.createGraphics();
    private ImageIcon icon = new ImageIcon(image);
    private JLabel label = new JLabel(icon);
    private JScrollPane pane = new JScrollPane(label);
    private final JButton button = new JButton("Start");
    private double prob = 0.0;
    private int red = 0xFFFF0000, black = 0xFF000000, green = 0xFF00FF00, blue = 0xFF0000FF; 
    private Particle[][] p = new Particle[300][300];
    private Timer timer;
    private final int BETWEEN_FRAME = 500;
    private int state = 1; //paused = -1 running = 1
    
    public ImageFrame(int width, int height){
        this.setTitle("CAP 3027 2012 - HW09 - Erika Guillen");
        this.setSize(width,height);
        addMenu();
        timer = new Timer(BETWEEN_FRAME,  new ActionListener() {
                public void actionPerformed(ActionEvent event){ 
                    timer.stop();
                    updateWorld();
                    timer.restart();
                }
            });
    }

    private void addMenu(){
        JMenu fileMenu = new JMenu("File");
        JMenuItem random = new JMenuItem("Randomly populated world");
        random.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    String result = JOptionPane.showInputDialog("Enter the the probability of a random cell being alive [0.0,1.0]");
                    prob = Double.parseDouble(result);
                    createRandomWorld();
                }
            });
        fileMenu.add(random);
        JMenuItem empty = new JMenuItem("Empty World");
        empty.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    createEmptyWorld();
                }
            });
        fileMenu.add(empty);
        //save
        JMenuItem save = new JMenuItem("Save Image");
        save.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    String result = JOptionPane.showInputDialog("Enter the name you want to save it as.");
                    save(result);
                }
            });
        fileMenu.add(save); 
        //exit option
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event){
                    System.exit(0);
                }
            });
        fileMenu.add(exitItem);        
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        this.setJMenuBar(menuBar);
    }

    private void save(String out){
        File outputFile = new File(out + ".png");
        try{
            javax.imageio.ImageIO.write(image, "png", outputFile);
        }catch(IOException e){
            JOptionPane.showMessageDialog(ImageFrame.this, "Error saving file", "oops!", JOptionPane.ERROR_MESSAGE);
        }
    } 

    public void displayBufferedImage(){
        addButton();
        this.validate();
    }

    private void addButton(){
        this.getContentPane().removeAll();
        final NewPanel buttonBar = new NewPanel();        
        button.addActionListener( new ActionListener(){ 
                public void actionPerformed( ActionEvent event ){
                    String buttonName = "";
                    switch(state){
                        case -1:    timer.stop();
                                    buttonName = "Start";   
                                    state = 1;
                                    break;
                        case 1:     timer.start();
                                    buttonName = "Pause"; 
                                    state = -1;
                                    break;
                    }
                    button.setText(buttonName);
                }
            } );
        this.getContentPane().add(buttonBar, BorderLayout.CENTER);
        this.getContentPane().add(button, BorderLayout.SOUTH);
        this.pack();
    }

    private void createRandomWorld(){
        Random rand = new Random();
        int tempColor;
        for(int i = 0; i < width; i+=2){
            for(int j = 0; j < height; j+=2){
                double d  = rand.nextDouble();
                if(d <= prob)
                    tempColor = green;
                else
                    tempColor = red;                    
                p[i/2][j/2] = new Particle(i,j,tempColor);
                p[i/2][j/2].display();
            }
        }
        displayBufferedImage();
    }

    private void updateWorld(){ 
        for(int i = 0; i < width; i+=2){
            for(int j = 0; j < height; j+=2){
                p[i/2][j/2].checkNeighbors();
                p[i/2][j/2].update();
                p[i/2][j/2].display();   
            }
        }
        displayBufferedImage();        
    }

    private void createEmptyWorld(){
        for(int i = 0; i < width; i+=2){
            for(int j = 0; j < height; j+=2){    
                Particle e = new Particle(i,j,red);
                p[i/2][j/2] = e;
                p[i/2][j/2].display();
            }
        }
        displayBufferedImage();
    }

    class NewPanel extends JPanel{
        // image displayed on panel
        private final int WIDTH;
        private final int HEIGHT;
        public NewPanel(){
            WIDTH = image.getWidth();
            HEIGHT = image.getHeight();
            Dimension size = new Dimension(WIDTH, HEIGHT);
            setMinimumSize(size);
            setMaximumSize(size);
            setPreferredSize(size);
            addMouseListener( new MouseAdapter(){
                    public void mousePressed( MouseEvent event ){
                        if(button.getText() == "Start"){
                            Point point = event.getPoint();
                            int x = ((point.x >> 1) << 1);
                            int y = ((point.y >> 1) << 1);
                            p[x/2][y/2].flipStatus();
                            p[x/2][y/2].display();
                            repaint();
                        }
                    }

                    public void mouseClicked( MouseEvent event ){
                    }
                } );
            addMouseMotionListener( new MouseMotionListener(){
                    public void mouseMoved(MouseEvent event){
                        setCursor( Cursor.getPredefinedCursor (Cursor.CROSSHAIR_CURSOR));
                    }

                    public void mouseDragged(MouseEvent event){
                    }
                } );
        }

        public void paintComponent( Graphics g ){
            super.paintComponent( g );
            g.drawImage( image, 0, 0, null );
        }
    }

    class Particle{
        public int x;
        public int y;
        public int dimension = 600;
        public int color;
        public boolean lonely = false, overCrowded = false, jesus = false, noChange = false;
        public int aliveNeighbors = 0;
        public Rectangle r;

        Particle(int x, int y, int color){
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public void checkNeighbors(){
            int xL = x - 2;
            int xR = x + 2;
            int yL = y - 2;
            int yR = y + 2;
            //correct bounds if needed
            if(xL < 0)
                xL = dimension -2;
            else if(xL >= dimension)
                xL = 0;            
            if(xR < 0)
                xR = dimension -2;
            else if(xR >= dimension)
                xR = 0;            
            if(yL < 0)
                yL = dimension -2;
            else if(yL >= dimension)
                yL = 0;            
            if(yR < 0)
                yR = dimension -2;
            else if(yR >= dimension)
                yR = 0;                

            //check how how neighbors are alive    
            if(image.getRGB(xL,yR) == blue || image.getRGB(xL,yR) == green)
                aliveNeighbors+=1;
            if(image.getRGB(xL,y) == blue || image.getRGB(xL,y) == green)
                aliveNeighbors+=1;
            if(image.getRGB(xL,yL) == blue || image.getRGB(xL,yL) == green)
                aliveNeighbors+=1;
            if(image.getRGB(x,yL) == blue || image.getRGB(x,yL) == green)
                aliveNeighbors+=1;
            if(image.getRGB(xR,yL) == blue || image.getRGB(xR,yL) == green)
                aliveNeighbors+=1;
            if(image.getRGB(xR,y) == blue || image.getRGB(xR,y) == green)
                aliveNeighbors+=1;
            if(image.getRGB(xR,yR) == blue || image.getRGB(xR,yR) == green)
                aliveNeighbors+=1;
            if(image.getRGB(x,yR) == blue || image.getRGB(x,yR) == green)
                aliveNeighbors+=1;
        }

        public boolean isAlive(){
            boolean alive = false;
            if(color == blue || color == green)
                alive = true;               
            return alive;   
        }

        public void flipStatus(){
            if(color == red || color == black){
                color = green;
            }else if(color == green || color == blue){
                color = red;
            }
        }

        public void update(){
            if(isAlive() && aliveNeighbors < 2)
                lonely = true;
            if(isAlive() && aliveNeighbors > 3)
                overCrowded = true;
            if(!isAlive() &&  aliveNeighbors == 3)
                jesus = true;
            if(!lonely && !overCrowded && !jesus)
                noChange = true;

            if(lonely){
                color = red;
            }else if(overCrowded){
                color = red;
            }else if(jesus){
                color = green;
            }else if(noChange){
                if(color == red)
                    color = black;
                else if(color == green)
                    color = blue;
            }
            reset();
        }

        public void reset(){
            lonely = false; overCrowded = false; jesus = false; noChange = false;
            aliveNeighbors = 0;
        }
        
        public void display(){
            r = new Rectangle(x,y,2,2);
            g2d.setColor(new Color(color));
            g2d.fill(r);
        }
    }
}
