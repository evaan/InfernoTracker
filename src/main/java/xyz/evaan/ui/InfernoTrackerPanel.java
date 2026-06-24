package xyz.evaan.ui;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import xyz.evaan.InfernoTrackerPlugin;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

public class InfernoTrackerPanel extends PluginPanel {
	private static final int INFERNO_MAX_WAVE = 69;
	private static final int COLOSSEUM_MAX_WAVE = 12;

	private final InfernoTrackerPlugin plugin;
	private final JPanel infernoAttemptPanel;
	private final JPanel colosseumAttemptPanel;

	public final ArrayList<Attempt> infernoAttempts = new ArrayList<>();
	public final ArrayList<Attempt> colosseumAttempts = new ArrayList<>();

	public static final Map<Integer, String> infernoWaves;
	public static final Map<Integer, String> colosseumWaves;

	public InfernoTrackerPanel(InfernoTrackerPlugin plugin) {
		this.plugin = plugin;

		infernoAttemptPanel = new JPanel();
		infernoAttemptPanel.setLayout(new GridBagLayout());
		infernoAttemptPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		infernoAttemptPanel.setBorder(new EmptyBorder(3, 3, 3, 3));

		colosseumAttemptPanel = new JPanel();
		colosseumAttemptPanel.setLayout(new GridBagLayout());
		colosseumAttemptPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		colosseumAttemptPanel.setBorder(new EmptyBorder(3, 3, 3, 3));

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Inferno", activityTab("Reset Inferno", Activity.INFERNO, infernoAttemptPanel));
		tabs.addTab("Colosseum", activityTab("Reset Colo", Activity.COLOSSEUM, colosseumAttemptPanel));

		add(tabs);
	}

	public void update() {
		SwingUtilities.invokeLater(() -> {
			updateAttemptPanel(infernoAttemptPanel, infernoAttempts);
			updateAttemptPanel(colosseumAttemptPanel, colosseumAttempts);
		});
	}

	public void addAttempt(Activity activity, Result result, int wave) {
		if (shouldLog(activity, result, wave)) {
			getAttempts(activity).add(new Attempt(activity, result, wave));
		}
		update();
	}

	public void addSavedAttempt(Attempt attempt) {
		if (attempt != null) {
			getAttempts(attempt.activity).add(attempt);
		}
	}

	public void resetList(Activity activity) {
		if (JOptionPane.showConfirmDialog(null, "All " + activity.label + " attempts will be cleared, are you sure?",
				"Inferno Tracker", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			getAttempts(activity).clear();
			update();
		}
	}

	private boolean shouldLog(Activity activity, Result result, int wave) {
		if (activity == Activity.INFERNO) {
			return result == Result.COMPLETION || (wave >= plugin.config.minInfernoWave() && wave <= INFERNO_MAX_WAVE);
		}

		return result == Result.COMPLETION || (wave >= plugin.config.minColosseumWave() && wave <= COLOSSEUM_MAX_WAVE);
	}

	private JLabel centeredLabel(String text) {
		JLabel label = new JLabel(text, JLabel.CENTER);
		label.setHorizontalAlignment(JLabel.CENTER);
		return label;
	}

	private JPanel activityTab(String resetText, Activity activity, JPanel attemptPanel) {
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel resetPanel = new JPanel();
		resetPanel.setLayout(new GridLayout(1, 1));
		resetPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		resetPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
		resetPanel.add(resetButton(resetText, activity));
		fixHeight(resetPanel);

		content.add(resetPanel);
		content.add(header());
		content.add(attemptPanel);
		return content;
	}

	private GridBagConstraints columnConstraints(int column) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 0;
		gbc.gridx = column;
		switch (column) {
			case 0:
				gbc.weightx = 0.25;
				break; // Attempt
			case 1:
				gbc.weightx = 0.50;
				break; // Result
			case 2:
				gbc.weightx = 0.25;
				break; // Wave
		}
		return gbc;
	}

	private JPanel header() {
		JPanel title = new JPanel();
		title.setLayout(new GridBagLayout());
		title.setBorder(new EmptyBorder(3, 3, 3, 3));
		title.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		title.add(new JLabel("Attempt", JLabel.CENTER), columnConstraints(0));
		title.add(new JLabel("Result", JLabel.CENTER), columnConstraints(1));
		title.add(new JLabel("Wave", JLabel.CENTER), columnConstraints(2));
		fixHeight(title);
		return title;
	}

	private JButton resetButton(String label, Activity activity) {
		JButton resetButton = new JButton(label);
		resetButton.addActionListener(e -> resetList(activity));
		return resetButton;
	}

	private ArrayList<Attempt> getAttempts(Activity activity) {
		return activity == Activity.INFERNO ? infernoAttempts : colosseumAttempts;
	}

	private void updateAttemptPanel(JPanel panel, ArrayList<Attempt> attempts) {
		panel.removeAll();
		int row = 0;
		for (Attempt attempt : attempts) {
			String tooltip = attempt.getTooltip();
			JLabel attemptLabel = centeredLabel(String.valueOf(row + 1));
			JLabel resultLabel = centeredLabel(attempt.result.label);
			JLabel waveLabel = centeredLabel(String.valueOf(attempt.wave));

			attemptLabel.setToolTipText(tooltip);
			resultLabel.setToolTipText(tooltip);
			waveLabel.setToolTipText(tooltip);

			GridBagConstraints gbc0 = columnConstraints(0);
			gbc0.gridy = row;
			GridBagConstraints gbc1 = columnConstraints(1);
			gbc1.gridy = row;
			GridBagConstraints gbc2 = columnConstraints(2);
			gbc2.gridy = row;

			panel.add(attemptLabel, gbc0);
			panel.add(resultLabel, gbc1);
			panel.add(waveLabel, gbc2);
			row++;
		}
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
		panel.repaint();
		panel.revalidate();
	}

	private void fixHeight(JPanel panel) {
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
	}

	public enum Activity {
		INFERNO("Inferno"),
		COLOSSEUM("Colosseum");

		private final String label;

		Activity(String label) {
			this.label = label;
		}
	}

	public enum Result {
		DEATH("Death"),
		COMPLETION("Complete");

		private final String label;

		Result(String label) {
			this.label = label;
		}
	}

	public static class Attempt {
		private final Activity activity;
		private final Result result;
		private final int wave;

		public Attempt(Activity activity, Result result, int wave) {
			this.activity = activity;
			this.result = result;
			this.wave = wave;
		}

		public String toSaveLine() {
			if (result == Result.COMPLETION) {
				return "COMPLETE";
			}

			return String.valueOf(wave);
		}

		public String getTooltip() {
			if (result == Result.COMPLETION) {
				return activity.label + " completion";
			}

			if (activity == Activity.INFERNO) {
				return infernoWaves.get(wave);
			}

			return colosseumWaves.get(wave);
		}

		public static Attempt fromSaveLine(String line, Activity expectedActivity) {
			try {
				if ("COMPLETE".equals(line)) {
					return new Attempt(expectedActivity, Result.COMPLETION,
							expectedActivity == Activity.INFERNO ? INFERNO_MAX_WAVE : COLOSSEUM_MAX_WAVE);
				}

				return new Attempt(expectedActivity, Result.DEATH, Integer.parseInt(line));
			} catch (Exception ignored) {
				return null;
			}
		}
	}

	static {
		Hashtable<Integer, String> infernoWavesTmp = new Hashtable<>();
		infernoWavesTmp.put(1, "1x Bat");
		infernoWavesTmp.put(2, "2x Bat");
		infernoWavesTmp.put(3, "6x Nibblers");
		infernoWavesTmp.put(4, "1x Blob");
		infernoWavesTmp.put(5, "1x Blob, 1x Bat");
		infernoWavesTmp.put(6, "1x Blob, 2x Bat");
		infernoWavesTmp.put(7, "2x Blob");
		infernoWavesTmp.put(8, "6x Nibblers");
		infernoWavesTmp.put(9, "1x Melee");
		infernoWavesTmp.put(10, "1x Melee, 1x Bat");
		infernoWavesTmp.put(11, "1x Melee, 2x Bat");
		infernoWavesTmp.put(12, "1x Melee, 1x Blob");
		infernoWavesTmp.put(13, "1x Melee, 1x Blob, 1x Bat");
		infernoWavesTmp.put(14, "1x Melee, 1x Blob, 2x Bat");
		infernoWavesTmp.put(15, "1x Melee, 2x Blob");
		infernoWavesTmp.put(16, "2x Melee");
		infernoWavesTmp.put(17, "6x Nibbler");
		infernoWavesTmp.put(18, "1x Range");
		infernoWavesTmp.put(19, "1x Range, 1x Bat");
		infernoWavesTmp.put(20, "1x Range, 2x Bat");
		infernoWavesTmp.put(21, "1x Range, 1x Blob");
		infernoWavesTmp.put(22, "1x Range, 1x Blob, 1x Bat");
		infernoWavesTmp.put(23, "1x Range, 1x Blob, 2x Bat");
		infernoWavesTmp.put(24, "1x Range, 2x Blob");
		infernoWavesTmp.put(25, "1x Range, 1x Melee");
		infernoWavesTmp.put(26, "1x Range, 1x Melee, 1x Bat");
		infernoWavesTmp.put(27, "1x Range, 1x Melee, 2x Bat");
		infernoWavesTmp.put(28, "1x Range, 1x Melee, 1x Blob");
		infernoWavesTmp.put(29, "1x Range, 1x Melee, 1x Blob, 1x Bat");
		infernoWavesTmp.put(30, "1x Range, 1x Melee, 1x Blob, 2x Bat");
		infernoWavesTmp.put(31, "1x Range, 1x Melee, 2x Blob");
		infernoWavesTmp.put(32, "1x Range, 2x Melee");
		infernoWavesTmp.put(33, "2x Range");
		infernoWavesTmp.put(34, "6x Nibblers");
		infernoWavesTmp.put(35, "1x Mage");
		infernoWavesTmp.put(36, "1x Mage, 1x Bat");
		infernoWavesTmp.put(37, "1x Mage, 2x Bat");
		infernoWavesTmp.put(38, "1x Mage, 1x Blob");
		infernoWavesTmp.put(39, "1x Mage, 1x Blob, 1x Bat");
		infernoWavesTmp.put(40, "1x Mage, 1x Blob, 2x Bat");
		infernoWavesTmp.put(41, "1x Mage, 2x Blob");
		infernoWavesTmp.put(42, "1x Mage, 1x Melee");
		infernoWavesTmp.put(43, "1x Mage, 1x Melee, 1x Bat");
		infernoWavesTmp.put(44, "1x Mage, 1x Melee, 2x Bat");
		infernoWavesTmp.put(45, "1x Mage, 1x Melee, 1x Blob");
		infernoWavesTmp.put(46, "1x Mage, 1x Melee, 1x Blob, 1x Bat");
		infernoWavesTmp.put(47, "1x Mage, 1x Melee, 1x Blob, 2x Bat");
		infernoWavesTmp.put(48, "1x Mage, 1x Melee, 2x Blob");
		infernoWavesTmp.put(49, "1x Mage, 2x Melee");
		infernoWavesTmp.put(50, "1x Mage, 1x Range");
		infernoWavesTmp.put(51, "1x Mage, 1x Range, 1x Bat");
		infernoWavesTmp.put(52, "1x Mage, 1x Range, 2x Bat");
		infernoWavesTmp.put(53, "1x Mage, 1x Range, 1x Blob");
		infernoWavesTmp.put(54, "1x Mage, 1x Range, 1x Blob, 1x Bat");
		infernoWavesTmp.put(55, "1x Mage, 1x Range, 1x Blob, 2x Bat");
		infernoWavesTmp.put(56, "1x Mage, 1x Range, 2x Blob");
		infernoWavesTmp.put(57, "1x Mage, 1x Range, 1x Melee");
		infernoWavesTmp.put(58, "1x Mage, 1x Range, 1x Melee, 1x Bat");
		infernoWavesTmp.put(59, "1x Mage, 1x Range, 1x Melee, 2x Bat");
		infernoWavesTmp.put(60, "1x Mage, 1x Range, 1x Melee, 1x Blob");
		infernoWavesTmp.put(61, "1x Mage, 1x Range, 1x Melee, 1x Blob, 1x Bat");
		infernoWavesTmp.put(62, "1x Mage, 1x Range, 1x Melee, 1x Blob, 2x Bat");
		infernoWavesTmp.put(63, "1x Mage, 1x Range, 1x Melee, 2x Blob");
		infernoWavesTmp.put(64, "1x Mage, 1x Range, 2x Melee");
		infernoWavesTmp.put(65, "1x Mage, 2x Range");
		infernoWavesTmp.put(66, "2x Mage");
		infernoWavesTmp.put(67, "1x Jad");
		infernoWavesTmp.put(68, "3x Jad");
		infernoWavesTmp.put(69, "1x Zuk");

		Hashtable<Integer, String> colosseumWavesTmp = new Hashtable<>();
		colosseumWavesTmp.put(1, "Fremennik Warband, Serpent Shaman<br>Reinforcement: Jaguar Warrior");
		colosseumWavesTmp.put(2,
				"Fremennik Warband, Serpent Shaman, Javelin Colossus<br>Reinforcement: Jaguar Warrior");
		colosseumWavesTmp.put(3,
				"Fremennik Warband, Serpent Shaman, 2x Javelin Colossus<br>Reinforcement: Jaguar Warrior");
		colosseumWavesTmp.put(4,
				"Fremennik Warband, Serpent Shaman, Manticore<br>Reinforcements: Jaguar Warrior, Serpent Shaman");
		colosseumWavesTmp.put(5,
				"Fremennik Warband, Serpent Shaman, Javelin Colossus, Manticore<br>Reinforcements: Jaguar Warrior, Serpent Shaman");
		colosseumWavesTmp.put(6,
				"Fremennik Warband, Serpent Shaman, 2x Javelin Colossus, Manticore<br>Reinforcements: Jaguar Warrior, Serpent Shaman");
		colosseumWavesTmp.put(7,
				"Fremennik Warband, Javelin Colossus, Manticore, Shockwave Colossus<br>Reinforcement: Minotaur");
		colosseumWavesTmp.put(8,
				"Fremennik Warband, 2x Javelin Colossus, Manticore, Shockwave Colossus<br>Reinforcement: Minotaur");
		colosseumWavesTmp.put(9, "Fremennik Warband, Javelin Colossus, 2x Manticore<br>Reinforcement: Minotaur");
		colosseumWavesTmp.put(10,
				"Fremennik Warband, 2x Javelin Colossus, 2x Manticore<br>Reinforcements: Minotaur, Serpent Shaman");
		colosseumWavesTmp.put(11,
				"Fremennik Warband, Javelin Colossus, 2x Manticore, Shockwave Colossus<br>Reinforcements: Minotaur, Serpent Shaman");
		colosseumWavesTmp.put(12, "Sol Heredit");

		infernoWaves = Collections.unmodifiableMap(infernoWavesTmp);
		colosseumWaves = Collections.unmodifiableMap(colosseumWavesTmp);
	}
}
