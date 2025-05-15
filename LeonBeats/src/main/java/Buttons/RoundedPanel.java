package Buttons;

import java.awt.*;
import javax.swing.*;

public class RoundedPanel extends JPanel {

    public Color backgroundColor;
    public Color borderColor;
    private int radius;

    // Constructores
    public RoundedPanel() {
        this(new Color(53, 53, 53), new Color(0, 0, 0), 30);
    }

    public RoundedPanel(Color backgroundColor) {
        this(backgroundColor, new Color(0, 0, 0), 30);
    }

    public RoundedPanel(Color backgroundColor, Color borderColor, int radius) {
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.radius = radius;
        setOpaque(false); // Importante para ver bien el fondo redondeado
    }

    // Setters
    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }
    
    public void getRadius(int radius) {
        this.radius = radius;
        repaint();
    }

    public void setPanelColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }
    
    public void getPanelColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }
    
    public void getBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dibujar fondo
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        // Dibujar borde
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

        g2.dispose();

        super.paintComponent(g); // Dibuja componentes hijos
    }
}
