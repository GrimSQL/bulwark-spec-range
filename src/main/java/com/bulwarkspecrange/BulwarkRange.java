package com.bulwarkspecrange;

import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;

final class BulwarkRange
{
    static final int RADIUS = 5;
    private static final int SPECIAL_ENERGY_REQUIRED = 500; // RuneLite stores 100% as 1000.

    private BulwarkRange()
    {
    }

    static boolean isVisible(int weaponId, int specialEnergy, boolean requireSpecialEnergy)
    {
        return (weaponId == ItemID.DINHS_BULWARK || weaponId == ItemID.DINHS_BULWARK_ORNAMENT)
            && (!requireSpecialEnergy || specialEnergy >= SPECIAL_ENERGY_REQUIRED);
    }

    static void forEachTile(WorldPoint center, TileConsumer consumer)
    {
        for (int dx = -RADIUS; dx <= RADIUS; dx++)
        {
            for (int dy = -RADIUS; dy <= RADIUS; dy++)
            {
                consumer.accept(center.dx(dx).dy(dy), dx, dy);
            }
        }
    }

    @FunctionalInterface
    interface TileConsumer
    {
        void accept(WorldPoint tile, int dx, int dy);
    }
}
