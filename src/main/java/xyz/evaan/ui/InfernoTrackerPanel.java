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
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

public class InfernoTrackerPanel extends PluginPanel
{
	private static final int INFERNO_MAX_WAVE = 69;
	private static final int COLOSSEUM_MAX_WAVE = 12;

	private final InfernoTrackerPlugin plugin;
	private final JPanel infernoAttemptPanel;
	private final JPanel colosseumAttemptPanel;

	public final ArrayList<Attempt> infernoAttempts = new ArrayList<>();
	public final ArrayList<Attempt> colosseumAttempts = new ArrayList<>();

	public static final Map<Integer, String> infernoWaves;
	public static final Map<Integer, String> colosseumWaves;

	public InfernoTrackerPanel(InfernoTrackerPlugin plugin)
	{
		this.plugin = plugin;

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		infernoAttemptPanel = new JPanel();
		infernoAttemptPanel.setLayout(new GridLayout(0, 3));
		infernoAttemptPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		infernoAttemptPanel.setBorder(new EmptyBorder(3, 3, 3, 3));

		colosseumAttemptPanel = new JPanel();
		colosseumAttemptPanel.setLayout(new GridLayout(0, 3));
		colosseumAttemptPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		colosseumAttemptPanel.setBorder(new EmptyBorder(3, 3, 3, 3));

		panel.add(sectionHeader("Inferno", "Reset Inferno", Activity.INFERNO));
		panel.add(header());
		panel.add(infernoAttemptPanel);
		panel.add(sectionHeader("Colosseum", "Reset Colo", Activity.COLOSSEUM));
		panel.add(header());
		panel.add(colosseumAttemptPanel);

		add(panel);
	}

	public void update()
	{
		SwingUtilities.invokeLater(() ->
		{
			updateAttemptPanel(infernoAttemptPanel, infernoAttempts);
			updateAttemptPanel(colosseumAttemptPanel, colosseumAttempts);
		});
	}

	public void addAttempt(Activity activity, Result result, int wave)
	{
		if (shouldLog(activity, result, wave))
		{
			getAttempts(activity).add(new Attempt(activity, result, wave));
		}
		update();
	}

	public void addSavedAttempt(Attempt attempt)
	{
		if (attempt != null)
		{
			getAttempts(attempt.activity).add(attempt);
		}
	}

	public void resetList(Activity activity)
	{
		if (JOptionPane.showConfirmDialog(null, "All " + activity.label + " attempts will be cleared, are you sure?", "Inferno Tracker", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
		{
			getAttempts(activity).clear();
			update();
		}
	}

	private boolean shouldLog(Activity activity, Result result, int wave)
	{
		if (activity == Activity.INFERNO)
		{
			return result == Result.COMPLETION || (wave >= plugin.config.minInfernoWave() && wave <= INFERNO_MAX_WAVE);
		}

		return result == Result.COMPLETION || (wave >= plugin.config.minColosseumWave() && wave <= COLOSSEUM_MAX_WAVE);
	}

	private JLabel centeredLabel(String text)
	{
		JLabel label = new JLabel(text, JLabel.CENTER);
		label.setHorizontalAlignment(JLabel.CENTER);
		return label;
	}

	private JPanel sectionHeader(String titleText, String resetText, Activity activity)
	{
		JPanel header = new JPanel();
		header.setLayout(new GridLayout(0, 2, 4, 0));
		header.setBorder(new EmptyBorder(8, 3, 3, 3));
		header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		header.add(centeredLabel(titleText));
		header.add(resetButton(resetText, activity));
		return header;
	}

	private JPanel header()
	{
		JPanel title = new JPanel();
		title.setLayout(new GridLayout(0, 3));
		title.setBorder(new EmptyBorder(3, 3, 3, 3));
		title.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		title.add(new JLabel("Attempt", JLabel.CENTER));
		title.add(new JLabel("Result", JLabel.CENTER));
		title.add(new JLabel("Wave", JLabel.CENTER));
		return title;
	}

	private JButton resetButton(String label, Activity activity)
	{
		JButton resetButton = new JButton(label);
		resetButton.addActionListener(e -> resetList(activity));
		return resetButton;
	}

	private ArrayList<Attempt> getAttempts(Activity activity)
	{
		return activity == Activity.INFERNO ? infernoAttempts : colosseumAttempts;
	}

	private void updateAttemptPanel(JPanel panel, ArrayList<Attempt> attempts)
	{
		panel.removeAll();
		int i = 1;
		for (Attempt attempt : attempts)
		{
			JLabel attemptLabel = centeredLabel(String.valueOf(i));
			JLabel resultLabel = centeredLabel(attempt.result.label);
			JLabel waveLabel = centeredLabel(String.valueOf(attempt.wave));

			String tooltip = attempt.getTooltip();
			attemptLabel.setToolTipText(tooltip);
			resultLabel.setToolTipText(tooltip);
			waveLabel.setToolTipText(tooltip);

			panel.add(attemptLabel);
			panel.add(resultLabel);
			panel.add(waveLabel);
			i++;
		}
		panel.repaint();
		panel.revalidate();
	}

	public enum Activity
	{
		INFERNO("Inferno"),
		COLOSSEUM("Colosseum");

		private final String label;

		Activity(String label)
		{
			this.label = label;
		}
	}

	public enum Result
	{
		DEATH("Death"),
		COMPLETION("Complete");

		private final String label;

		Result(String label)
		{
			this.label = label;
		}
	}

	public static class Attempt
	{
		private final Activity activity;
		private final Result result;
		private final int wave;

		public Attempt(Activity activity, Result result, int wave)
		{
			this.activity = activity;
			this.result = result;
			this.wave = wave;
		}

		public String toSaveLine()
		{
			if (result == Result.COMPLETION)
			{
				return "COMPLETE";
			}

			return String.valueOf(wave);
		}

		public String getTooltip()
		{
			if (result == Result.COMPLETION)
			{
				return activity.label + " completion";
			}

			if (activity == Activity.INFERNO)
			{
				return infernoWaves.get(wave);
			}

			return colosseumWaves.get(wave);
		}

		public static Attempt fromSaveLine(String line, Activity expectedActivity)
		{
			try
			{
				if ("COMPLETE".equals(line))
				{
					return new Attempt(expectedActivity, Result.COMPLETION, expectedActivity == Activity.INFERNO ? INFERNO_MAX_WAVE : COLOSSEUM_MAX_WAVE);
				}

				return new Attempt(expectedActivity, Result.DEATH, Integer.parseInt(line));
			}
			catch (Exception ignored)
			{
				return null;
			}
		}
	}

	static
	{
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
		colosseumWavesTmp.put(1, "<html>Fremennik Warband, Serpent Shaman<br>Reinforcement: Jaguar Warrior</html>");
		colosseumWavesTmp.put(2, "<html>Fremennik Warband, Serpent Shaman, Javelin Colossus<br>Reinforcement: Jaguar Warrior</html>");
		colosseumWavesTmp.put(3, "<html>Fremennik Warband, Serpent Shaman, 2x Javelin Colossus<br>Reinforcement: Jaguar Warrior</html>");
		colosseumWavesTmp.put(4, "<html>Fremennik Warband, Serpent Shaman, Manticore<br>Reinforcements: Jaguar Warrior, Serpent Shaman</html>");
		colosseumWavesTmp.put(5, "<html>Fremennik Warband, Serpent Shaman, Javelin Colossus, Manticore<br>Reinforcements: Jaguar Warrior, Serpent Shaman</html>");
		colosseumWavesTmp.put(6, "<html>Fremennik Warband, Serpent Shaman, 2x Javelin Colossus, Manticore<br>Reinforcements: Jaguar Warrior, Serpent Shaman</html>");
		colosseumWavesTmp.put(7, "<html>Fremennik Warband, Javelin Colossus, Manticore, Shockwave Colossus<br>Reinforcement: Minotaur</html>");
		colosseumWavesTmp.put(8, "<html>Fremennik Warband, 2x Javelin Colossus, Manticore, Shockwave Colossus<br>Reinforcement: Minotaur</html>");
		colosseumWavesTmp.put(9, "<html>Fremennik Warband, Javelin Colossus, 2x Manticore<br>Reinforcement: Minotaur</html>");
		colosseumWavesTmp.put(10, "<html>Fremennik Warband, 2x Javelin Colossus, 2x Manticore<br>Reinforcements: Minotaur, Serpent Shaman</html>");
		colosseumWavesTmp.put(11, "<html>Fremennik Warband, Javelin Colossus, 2x Manticore, Shockwave Colossus<br>Reinforcements: Minotaur, Serpent Shaman</html>");
		colosseumWavesTmp.put(12, "Sol Heredit");

		infernoWaves = Collections.unmodifiableMap(infernoWavesTmp);
		colosseumWaves = Collections.unmodifiableMap(colosseumWavesTmp);
	}
}
