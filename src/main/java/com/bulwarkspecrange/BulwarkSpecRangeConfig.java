package com.bulwarkspecrange;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(BulwarkSpecRangeConfig.GROUP)
public interface BulwarkSpecRangeConfig extends Config
{
    String GROUP = "bulwarkspecrange";

    @ConfigItem(
        keyName = "requireSpecialEnergy",
        name = "Require 50% special energy",
        description = "Show the area only when you have at least 50% special attack energy.",
        position = 0
    )
    default boolean requireSpecialEnergy()
    {
        return true;
    }

    @ConfigItem(
        keyName = "showOutline",
        name = "Show outline",
        description = "Draw a line around the area. Turn off to show the fill on its own.",
        position = 1
    )
    default boolean showOutline()
    {
        return true;
    }

    @Alpha
    @ConfigItem(
        keyName = "outlineColor",
        name = "Outline color",
        description = "Color and opacity of the area outline.",
        position = 2
    )
    default Color outlineColor()
    {
        return new Color(0, 39, 207, 205);
    }

    @Alpha
    @ConfigItem(
        keyName = "fillColor",
        name = "Fill color",
        description = "Color and opacity inside the outline. Set opacity to zero for an outline only.",
        position = 3
    )
    default Color fillColor()
    {
        return new Color(0, 10, 255, 50);
    }

    @Range(min = 1, max = 6)
    @ConfigItem(
        keyName = "outlineWidth",
        name = "Outline width",
        description = "Thickness of the outline in pixels.",
        position = 4
    )
    default double outlineWidth()
    {
        return 1.25;
    }

    @ConfigItem(
        keyName = "highlightCorners",
        name = "Highlight corners",
        description = "Draw the four corners with a brighter, thicker outline.",
        position = 5
    )
    default boolean highlightCorners()
    {
        return true;
    }

    @ConfigItem(
        keyName = "showCenterTile",
        name = "Show center tile",
        description = "Outline your true tile in the center of the marked area.",
        position = 6
    )
    default boolean showCenterTile()
    {
        return false;
    }
}
