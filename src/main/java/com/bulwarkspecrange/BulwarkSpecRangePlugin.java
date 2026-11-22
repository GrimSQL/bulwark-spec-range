package com.bulwarkspecrange;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
    name = "Bulwark Spec Range",
    description = "Shows the Shield Bash area when you have Dinh's bulwark equipped, with an optional special energy requirement.",
    tags = {"dinh", "shield bash", "special attack", "range", "tile"}
)
public class BulwarkSpecRangePlugin extends Plugin
{
    @Inject
    private OverlayManager overlayManager;

    @Inject
    private BulwarkSpecRangeOverlay overlay;

    @Provides
    BulwarkSpecRangeConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BulwarkSpecRangeConfig.class);
    }

    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
    }
}
