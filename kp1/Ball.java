import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.awt.geom.Ellipse2D;

class Ball {
    private BallCanvas canvas;
    private BounceFrame frame;
    private Color color;
    private Thread blockingThread;
    private static final int XSIZE = 20;
    private static final int YSIZE = 20;
    private int x = 0;
    private int y = 0;
    private int dx = 2;
    private int dy = 2;

    public Ball(BallCanvas c, BounceFrame frame, Color color) {
	this(c, frame, color, null);
    }

    public Ball(BallCanvas c, BounceFrame frame, Color color, Thread blockingThread) {
	this.canvas = c;
	this.frame = frame;
	this.color = color;
	this.blockingThread = blockingThread;

	if(Math.random()<0.5){
	    x = new Random().nextInt(this.canvas.getWidth());
	    y = 0;
	}else{
	    x = 0;
	    y = new Random().nextInt(this.canvas.getHeight());
	}
    }

    public static void f(){
	int a = 0;
    }

    public void draw (Graphics2D g2){
	g2.setColor(color);
	g2.fill(new Ellipse2D.Double(x,y,XSIZE,YSIZE));
    }

    public boolean move(){
	if (this.blockingThread != null) {
	    try {
		blockingThread.join();
	    } catch (InterruptedException ex) {

	    }
	    blockingThread = null;
	}
  
	if (canvas.hasDropped(this)) {
	    canvas.removeBall(this);
	    frame.incDropCount();
	    return true;
	}
	
	x+=dx;
	y+=dy;

	if(x<0){
	    x = 0;
	    dx = -dx;
	}
	if(x+XSIZE>=this.canvas.getWidth()){
	    x = this.canvas.getWidth()-XSIZE;
	    dx = -dx;
	}
	if(y<0){
	    y=0;
	    dy = -dy;
	}
	if(y+YSIZE>=this.canvas.getHeight()){
	    y = this.canvas.getHeight()-YSIZE;
	    dy = -dy;
	}
	this.canvas.repaint();

	return false;
    }

    public int getX() {
	return x;
    }

    public int getY() {
	return y;
    }
}
