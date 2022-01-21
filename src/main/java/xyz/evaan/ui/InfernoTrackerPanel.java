package xyz.evaan.ui;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import xyz.evaan.InfernoTrackerPlugin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class InfernoTrackerPanel extends PluginPanel {
    InfernoTrackerPlugin plugin;

    JPanel attemptPanel;
    public ArrayList<Integer> attempts = new ArrayList<>();

    int a = 69;

    public static final Map<Integer, String> waves;

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
        resetButton.addActionListener(e -> {
            resetList();
            a--;
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
                    temp2.setToolTipText(waves.get(wave));

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
        if (wave >= plugin.config.minWave() && wave <= 69) attempts.add(wave);
        update();
    }

    public void resetList() {
        if (JOptionPane.showConfirmDialog(null, "All of the waves will be cleared, are you sure?", "Inferno Tracker", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            attempts.clear();
            update();
        }
    }

    static {
        //could i have done this better? absolutely.
        Hashtable<Integer, String> wavesTmp = new Hashtable<>();
        wavesTmp.put(1, "1x Bat");
        wavesTmp.put(2, "2x Bat");
        wavesTmp.put(3, "6x Nibblers");
        wavesTmp.put(4, "1x Blob");
        wavesTmp.put(5, "1x Blob, 1x Bat");
        wavesTmp.put(6, "1x Blob, 2x Bat");
        wavesTmp.put(7, "2x Blob");
        wavesTmp.put(8, "6x Nibblers");
        wavesTmp.put(9, "1x Melee");
        wavesTmp.put(10, "1x Melee, 1x Bat");
        wavesTmp.put(11, "1x Melee, 2x Bat");
        wavesTmp.put(12, "1x Melee, 1x Blob");
        wavesTmp.put(13, "1x Melee, 1x Blob, 1x Bat");
        wavesTmp.put(14, "1x Melee, 1x Blob, 2x Bat");
        wavesTmp.put(15, "1x Melee, 2x Blob");
        wavesTmp.put(16, "2x Melee");
        wavesTmp.put(17, "6x Nibbler");
        wavesTmp.put(18, "1x Range");
        wavesTmp.put(19, "1x Range, 1x Bat");
        wavesTmp.put(20, "1x Range, 2x Bat");
        wavesTmp.put(21, "1x Range, 1x Blob");
        wavesTmp.put(22, "1x Range, 1x Blob, 1x Bat");
        wavesTmp.put(23, "1x Range, 1x Blob, 2x Bat");
        wavesTmp.put(24, "1x Range, 2x Blob");
        wavesTmp.put(25, "1x Range, 1x Melee");
        wavesTmp.put(26, "1x Range, 1x Melee, 1x Bat");
        wavesTmp.put(27, "1x Range, 1x Melee, 2x Bat");
        wavesTmp.put(28, "1x Range, 1x Melee, 1x Blob");
        wavesTmp.put(29, "1x Range, 1x Melee, 1x Blob, 1x Bat");
        wavesTmp.put(30, "1x Range, 1x Melee, 1x Blob, 2x Bat");
        wavesTmp.put(31, "1x Range, 1x Melee, 2x Blob");
        wavesTmp.put(32, "1x Range, 2x Melee");
        wavesTmp.put(33, "2x Range");
        wavesTmp.put(34, "6x Nibblers");
        wavesTmp.put(35, "1x Mage");
        wavesTmp.put(36, "1x Mage, 1x Bat");
        wavesTmp.put(37, "1x Mage, 2x Bat");
        wavesTmp.put(38, "1x Mage, 1x Blob");
        wavesTmp.put(39, "1x Mage, 1x Blob, 1x Bat");
        wavesTmp.put(40, "1x Mage, 1x Blob, 2x Bat");
        wavesTmp.put(41, "1x Mage, 2x Blob");
        wavesTmp.put(42, "1x Mage, 1x Melee");
        wavesTmp.put(43, "1x Mage, 1x Melee, 1x Bat");
        wavesTmp.put(44, "1x Mage, 1x Melee, 2x Bat");
        wavesTmp.put(45, "1x Mage, 1x Melee, 1x Blob");
        wavesTmp.put(46, "1x Mage, 1x Melee, 1x Blob, 1x Bat");
        wavesTmp.put(47, "1x Mage, 1x Melee, 1x Blob, 2x Bat");
        wavesTmp.put(48, "1x Mage, 1x Melee, 2x Blob");
        wavesTmp.put(49, "1x Mage, 2x Melee");
        wavesTmp.put(50, "1x Mage, 1x Range");
        wavesTmp.put(51, "1x Mage, 1x Range, 1x Bat");
        wavesTmp.put(52, "1x Mage, 1x Range, 2x Bat");
        wavesTmp.put(53, "1x Mage, 1x Range, 1x Blob");
        wavesTmp.put(54, "1x Mage, 1x Range, 1x Blob, 1x Bat");
        wavesTmp.put(55, "1x Mage, 1x Range, 1x Blob, 2x Bat");
        wavesTmp.put(56, "1x Mage, 1x Range, 2x Blob");
        wavesTmp.put(57, "1x Mage, 1x Range, 1x Melee");
        wavesTmp.put(58, "1x Mage, 1x Range, 1x Melee, 1x Bat");
        wavesTmp.put(59, "1x Mage, 1x Range, 1x Melee, 2x Bat");
        wavesTmp.put(60, "1x Mage, 1x Range, 1x Melee, 1x Blob");
        wavesTmp.put(61, "1x Mage, 1x Range, 1x Melee, 1x Blob, 1x Bat");
        wavesTmp.put(62, "1x Mage, 1x Range, 1x Melee, 1x Blob, 2x Bat");
        wavesTmp.put(63, "1x Mage, 1x Range, 1x Melee, 2x Blob");
        wavesTmp.put(64, "1x Mage, 1x Range, 2x Melee");
        wavesTmp.put(65, "1x Mage, 2x Range");
        wavesTmp.put(66, "2x Mage");
        wavesTmp.put(67, "1x Jad");
        wavesTmp.put(68, "3x Jad");
        wavesTmp.put(69, "1x Zuk");

        waves = Collections.unmodifiableMap(wavesTmp);
    }
}
