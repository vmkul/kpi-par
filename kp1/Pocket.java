import java.awt.*;
import java.awt.geom.Ellipse2D;

class Pocket {
    private Component canvas;
    private static final int XSIZE = 40;
    private static final int YSIZE = 40;
    private int x = 0;
    private int y = 0;

    public Pocket(Component c, int x, int y){
	this.canvas = c;
	this.x = x;
	this.y = y;
    }

    public void draw(Graphics2D g2){
	g2.setColor(Color.orange);
	g2.fill(new Ellipse2D.Double(x,y,XSIZE,YSIZE));
    }

    public boolean collidesWith(Ball b) {
	int ballx = b.getX();
	int bally = b.getY();

	return (ballx >= x && ballx <= x + XSIZE && bally >= y && bally <= y + YSIZE);
    }
}
