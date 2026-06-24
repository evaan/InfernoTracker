package xyz.evaan;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ArrayUtils;
import xyz.evaan.ui.InfernoTrackerPanel;
import xyz.evaan.ui.InfernoTrackerPanel.Activity;
import xyz.evaan.ui.InfernoTrackerPanel.Result;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
@PluginDescriptor(
	name = "Inferno Tracker",
	description = "A plugin to help track your inferno attempts."
)
public class InfernoTrackerPlugin extends Plugin
{
	private static final int INFERNO_REGION = 9043;
	private static final int COLOSSEUM_REGION = 7216;
	private static final int INFERNO_COMPLETION_WAVE = 69;
	private static final int COLOSSEUM_COMPLETION_WAVE = 12;

	private static final Pattern WAVE_PATTERN = Pattern.compile(".*Wave:?\\s*(\\d+).*");
	private static final Pattern INFERNO_COMPLETION_PATTERN = Pattern.compile(".*Your TzKal-Zuk kill count is: \\d+\\..*");
	private static final String COLOSSEUM_COMPLETION_MESSAGE = "Wave 12 completed!";

	private InfernoTrackerPanel panel;
	private NavigationButton navButton;

	private Activity currentActivity;
	private int infernoWave = 0;
	private int colosseumWave = 0;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Client client;

	@Inject
	@Getter
	public InfernoTrackerConfig config;

	File infernoConfigFile = RUNELITE_DIR.toPath().resolve("InfernoTracker.txt").toFile();
	File colosseumConfigFile = RUNELITE_DIR.toPath().resolve("ColosseumTracker.txt").toFile();

	@Override
	protected void startUp() throws Exception
	{
		panel = new InfernoTrackerPanel(this);
		loadAttempts();
		panel.update();

		Runtime.getRuntime().addShutdownHook(new Thread(this::saveAttempts));

		navButton = NavigationButton.builder()
				.tooltip("Inferno Tracker")
				.icon(ImageUtil.loadImageResource(getClass(), "/icon.png"))
				.priority(6)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);

		log.info("Inferno Tracker started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		saveAttempts();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String message = event.getMessage();
		boolean inInferno = isInInferno();
		boolean inColosseum = isInColosseum();

		if (inInferno && INFERNO_COMPLETION_PATTERN.matcher(message).matches())
		{
			panel.addAttempt(Activity.INFERNO, Result.COMPLETION, INFERNO_COMPLETION_WAVE);
			infernoWave = 0;
			return;
		}

		if (inColosseum && message.contains(COLOSSEUM_COMPLETION_MESSAGE))
		{
			panel.addAttempt(Activity.COLOSSEUM, Result.COMPLETION, COLOSSEUM_COMPLETION_WAVE);
			colosseumWave = 0;
			return;
		}

		if (!inInferno && !inColosseum)
		{
			currentActivity = null;
			infernoWave = 0;
			colosseumWave = 0;
			return;
		}

		final Matcher waveMatcher = WAVE_PATTERN.matcher(message);
		if (!waveMatcher.matches())
		{
			return;
		}

		int wave = Integer.parseInt(waveMatcher.group(1));
		if (inInferno)
		{
			currentActivity = Activity.INFERNO;
			infernoWave = wave;
			return;
		}

		if (inColosseum && wave >= 1 && wave <= COLOSSEUM_COMPLETION_WAVE)
		{
			currentActivity = Activity.COLOSSEUM;
			colosseumWave = wave;
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (event.getActor() != client.getLocalPlayer())
		{
			return;
		}

		if (currentActivity == Activity.INFERNO)
		{
			panel.addAttempt(Activity.INFERNO, Result.DEATH, infernoWave);
			currentActivity = null;
			infernoWave = 0;
			return;
		}

		if (currentActivity == Activity.COLOSSEUM)
		{
			panel.addAttempt(Activity.COLOSSEUM, Result.DEATH, colosseumWave);
			currentActivity = null;
			colosseumWave = 0;
		}
	}

	private boolean isInInferno()
	{
		return ArrayUtils.contains(client.getMapRegions(), INFERNO_REGION);
	}

	private boolean isInColosseum()
	{
		if (ArrayUtils.contains(client.getMapRegions(), COLOSSEUM_REGION))
		{
			return true;
		}

		return false;
	}

	private void loadAttempts()
	{
		loadAttempts(infernoConfigFile, Activity.INFERNO);
		loadAttempts(colosseumConfigFile, Activity.COLOSSEUM);
	}

	private void saveAttempts()
	{
		saveAttempts(infernoConfigFile, panel.infernoAttempts);
		saveAttempts(colosseumConfigFile, panel.colosseumAttempts);
	}

	private void loadAttempts(File file, Activity defaultActivity)
	{
		if (!file.exists())
		{
			return;
		}

		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
		{
			String line;
			while ((line = br.readLine()) != null)
			{
				panel.addSavedAttempt(InfernoTrackerPanel.Attempt.fromSaveLine(line, defaultActivity));
			}
		}
		catch (Exception e)
		{
			log.warn("Unable to load Inferno Tracker attempts", e);
		}
	}

	private void saveAttempts(File file, Iterable<InfernoTrackerPanel.Attempt> attempts)
	{
		try
		{
			if (!file.exists())
			{
				file.createNewFile();
			}

			try (FileWriter writer = new FileWriter(file))
			{
				for (InfernoTrackerPanel.Attempt attempt : attempts)
				{
					writer.write(attempt.toSaveLine() + System.lineSeparator());
				}
			}
		}
		catch (Exception e)
		{
			log.warn("Unable to save Inferno Tracker attempts", e);
		}
	}

	@Provides
	InfernoTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InfernoTrackerConfig.class);
	}
}
