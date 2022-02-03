import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BounceFrame extends JFrame {
    private BallCanvas canvas;
    private int droppedCount = 0;
    private JLabel droppedCountLabel;
    public static final int WIDTH = 900;
    public static final int HEIGHT = 700;
    
    public BounceFrame() {
	this.setSize(WIDTH, HEIGHT);
	this.setTitle("Bounce program");
	this.canvas = new BallCanvas();
	System.out.println("In Frame Thread name = "
			   + Thread.currentThread().getName());
	Container content = this.getContentPane();
	content.add(this.canvas, BorderLayout.CENTER);
	JPanel buttonPanel = new JPanel();
	buttonPanel.setBackground(Color.lightGray);
	JButton buttonAddBlue = new JButton("Add Blue");
	JButton buttonAddRed = new JButton("Add Red");
	JButton buttonAddRegular = new JButton("Add Regular");
	JButton buttonStop = new JButton("Stop");

	this.droppedCountLabel = new JLabel("Balls dropped: 0");

	canvas.setBackground(java.awt.Color.green.darker());

	canvas.addPocket(new Pocket(canvas, 0, 0));
	canvas.addPocket(new Pocket(canvas, WIDTH - 40, 0));

	canvas.addPocket(new Pocket(canvas, 0, HEIGHT - 115));
	canvas.addPocket(new Pocket(canvas, WIDTH - 40, HEIGHT - 115));

	canvas.addPocket(new Pocket(canvas, (WIDTH - 40) / 2, 0));
	canvas.addPocket(new Pocket(canvas, (WIDTH - 40) / 2, HEIGHT - 115));

	buttonAddRegular.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		    addBall(Color.darkGray, 5);
		}
	    });

	buttonAddBlue.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		    addBall(Color.blue, 1);
		}
	    });

	buttonAddRed.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		    addBall(Color.red, 10);
		}
	    });
	
	buttonStop.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		    System.exit(0);
		}
	    });
 
	buttonPanel.add(buttonAddRegular);
	buttonPanel.add(buttonAddBlue);
	buttonPanel.add(buttonAddRed);
	buttonPanel.add(buttonStop);
	buttonPanel.add(droppedCountLabel);

	content.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addBall(Color c, int priority) {
	Ball b = new Ball(canvas, this, c);
	canvas.addBall(b);

	BallThread thread = new BallThread(b);
	thread.setPriority(priority);
	thread.start();
	
	System.out.println("Thread name = " + thread.getName());
    }

    public void incDropCount() {
	droppedCount++;
	droppedCountLabel.setText("Balls dropped: " + droppedCount);
    }
}
