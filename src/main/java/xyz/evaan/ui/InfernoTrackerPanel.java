package xyz.evaan.ui;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import xyz.evaan.InfernoTrackerPlugin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InfernoTrackerPanel extends PluginPanel {
    InfernoTrackerPlugin plugin;

    JPanel attemptPanel;
    public ArrayList<Integer> attempts = new ArrayList<>();

    int a = 69;

    public InfernoTrackerPanel(InfernoTrackerPlugin plugin) {
        this.plugin = plugin;

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel title = new JPanel();
        title.setLayout(new GridLayout(0, 2));
        title.setBorder(new EmptyBorder(3, 3, 3, 3));
        title.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        title.add(new JLabel("Attempt", JLabel.CENTER));
        title.add(new JLabel("Wave", JLabel.CENTER));

        attemptPanel = new JPanel();
        attemptPanel.setLayout(new GridLayout(0, 2));
        attemptPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        attemptPanel.setBorder(new EmptyBorder(3, 3, 3, 3));

        panel.add(title);
        panel.add(attemptPanel);

        JButton resetButton = new JButton("Reset List");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetList();
                a--;
            }
        });

        add(resetButton);
        add(panel);


    }

    public void update() {
        attemptPanel.removeAll();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                attemptPanel.removeAll();
                int i = 1;
                for (int wave : attempts) {
                    //extremely optimized like everything else this plugin has to offer
                    JLabel temp1 = new JLabel(String.valueOf(i), JLabel.CENTER);
                    temp1.setHorizontalAlignment(JLabel.CENTER);
                    JLabel temp2 = new JLabel(String.valueOf(wave), JLabel.CENTER);
                    temp2.setHorizontalAlignment(JLabel.CENTER);

                    attemptPanel.add(temp1);
                    attemptPanel.add(temp2);
                    i++;
                }
                attemptPanel.repaint();
                attemptPanel.revalidate();
            }
        });
    }

    public void addAttempt(int wave) {
        if (wave >= plugin.config.minWave()) attempts.add(wave);
        update();
    }

    public void resetList() {
        if (JOptionPane.showConfirmDialog(null, "All of the waves will be cleared, are you sure?", "Inferno Tracker", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            attempts.clear();
            update();
        }
    }
}
