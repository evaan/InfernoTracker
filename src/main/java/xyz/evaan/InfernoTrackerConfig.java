package xyz.evaan;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("infernotracker")
public interface InfernoTrackerConfig extends Config
{
	@ConfigItem(
		keyName = "minWave",
		name = "Minimum Wave",
		description = "Choose the minimum wave to start logging inferno deaths"
	)
	default int minWave() {return 1;}
}
