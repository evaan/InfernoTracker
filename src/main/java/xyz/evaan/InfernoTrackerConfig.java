package xyz.evaan;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("infernotracker")
public interface InfernoTrackerConfig extends Config
{
	@ConfigItem(
		keyName = "minInfernoWave",
		name = "Minimum Inferno Wave",
		description = "Choose the minimum Inferno wave to log deaths"
	)
	default int minInfernoWave()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "minColosseumWave",
		name = "Minimum Colosseum Wave",
		description = "Choose the minimum Colosseum wave to log deaths"
	)
	default int minColosseumWave()
	{
		return 1;
	}
}
