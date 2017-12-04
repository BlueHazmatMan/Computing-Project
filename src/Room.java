
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

public class Room extends JPanel {

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setBackground(Color.BLACK);

        g.setColor(new Color(186, 185, 182));
        g.fillRect(5, 5, 100, 100);
        
        g.setColor(Color.WHITE);
        g.drawString("Room 1", 5, 5);
    }

}
