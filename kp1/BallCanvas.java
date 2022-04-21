import javax.swing.*;
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class BallCanvas extends JPanel{
    private ArrayList<Ball> balls = new ArrayList<>();
    private ArrayList<Pocket> pockets = new ArrayList<>();

    public void addBall(Ball b){
	this.balls.add(b);
    }

    public void removeBall(Ball b) {
	balls.remove(b);
    }

    public void addPocket(Pocket p) {
	this.pockets.add(p);
    }

    public boolean hasDropped(Ball b) {
	for (int i = 0; i < pockets.size(); i++) {
	    if (pockets.get(i).collidesWith(b)) {
		return true;
	    }
	}

	return false;
    }

    @Override
    public void paintComponent(Graphics g){
	super.paintComponent(g);
	Graphics2D g2 = (Graphics2D)g;

	for(int i=0; i<balls.size();i++){
	    Ball b = balls.get(i);
	    b.draw(g2);
	}

	for(int i=0; i<pockets.size();i++){
	    Pocket p = pockets.get(i);
	    p.draw(g2);
	}
    }
}
