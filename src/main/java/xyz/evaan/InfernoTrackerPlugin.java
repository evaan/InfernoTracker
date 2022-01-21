package xyz.evaan;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ArrayUtils;
import xyz.evaan.ui.InfernoTrackerPanel;

import java.io.*;
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
	private InfernoTrackerPanel panel;
	private NavigationButton navButton;

	private static final Pattern wavePattern = Pattern.compile(".*Wave: (\\d+).*");

	private static int wave = 0;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Client client;

	@Inject
	@Getter
	public InfernoTrackerConfig config;

	File configFile = RUNELITE_DIR.toPath().resolve("InfernoTracker.txt").toFile();

	@Override
	protected void startUp() throws Exception {

		panel = new InfernoTrackerPanel(this);

		if (!configFile.exists()) try {configFile.createNewFile();} catch (Exception ignored) {}
		else {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configFile)));
			String line;
			while ((line = br.readLine()) != null) {
				panel.addAttempt(Integer.valueOf(line));
			}
		}

		panel.update();

		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				try {
					if (configFile.exists()) configFile.delete();
					configFile.createNewFile();
					if (panel.attempts.isEmpty()) return;
					FileWriter writer = new FileWriter(configFile);
					for(int wave: panel.attempts) {
						writer.write(wave + System.lineSeparator());
					}
					writer.close();
				} catch(Exception e) {e.printStackTrace();}
			}
		});

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
	protected void shutDown() throws Exception {
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (event.getType() != ChatMessageType.GAMEMESSAGE) return;
		if (!ArrayUtils.contains(client.getMapRegions(), 9043)) {
			wave = 0;
			return;
		}
		final Matcher waveMatcher = wavePattern.matcher(event.getMessage());
		if (!waveMatcher.matches()) return;
		wave = Integer.parseInt(waveMatcher.group(1));
	}

	@Subscribe
	public void onActorDeath(ActorDeath event) {
		if (ArrayUtils.contains(client.getMapRegions(), 9043) && event.getActor() == client.getLocalPlayer()) {
			panel.addAttempt(wave);
		}
	}

	@Provides
	InfernoTrackerConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(InfernoTrackerConfig.class);
	}
}
