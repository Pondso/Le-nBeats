package com.leonbeats.leonbeats;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class PlaylistRenderer extends JLabel implements ListCellRenderer<PlaylistVisual> {
    public PlaylistRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends PlaylistVisual> list,
        PlaylistVisual value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {
        setText(value.getNombre());

        if (value.getCaratulaPath() != null && !value.getCaratulaPath().isEmpty()) {
            ImageIcon icon = new ImageIcon(value.getCaratulaPath());
            Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(img));
        } else {
            setIcon(null);
        }

        setFont(new Font("SansSerif", Font.BOLD, 16));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        if (isSelected) {
            setBackground(new Color(30, 30, 30));
            setForeground(Color.WHITE);
        } else {
            setBackground(new Color(50, 50, 50));
            setForeground(Color.LIGHT_GRAY);
        }

        return this;
    }
}