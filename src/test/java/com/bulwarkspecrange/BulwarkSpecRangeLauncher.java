package com.bulwarkspecrange;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public final class BulwarkSpecRangeLauncher
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(BulwarkSpecRangePlugin.class);
        RuneLite.main(args);
    }
}
