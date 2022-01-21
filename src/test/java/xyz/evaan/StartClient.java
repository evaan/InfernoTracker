package xyz.evaan;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class StartClient
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(InfernoTrackerPlugin.class);
		RuneLite.main(args);
	}
}