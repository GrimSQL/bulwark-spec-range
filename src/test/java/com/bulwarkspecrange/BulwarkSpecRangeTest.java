package com.bulwarkspecrange;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.Scene;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;

public final class BulwarkSpecRangeTest
{
    public static void main(String[] args)
    {
        check(!BulwarkRange.isVisible(ItemID.DINHS_BULWARK, 499, true), "Hidden just below 50%");
        check(BulwarkRange.isVisible(ItemID.DINHS_BULWARK, 500, true), "Visible at exactly 50%");
        check(BulwarkRange.isVisible(ItemID.DINHS_BULWARK, 1000, true), "Visible at 100%");
        check(BulwarkRange.isVisible(ItemID.DINHS_BULWARK_ORNAMENT, 500, true), "Blazing variant");
        check(!BulwarkRange.isVisible(ItemID.DINHS_BULWARK_ORNAMENT, 499, true), "Blazing below 50%");
        check(!BulwarkRange.isVisible(-1, 1000, true), "Empty weapon slot");
        check(!BulwarkRange.isVisible(ItemID.DINHS_BULWARK_ORNAMENT_KIT, 1000, true), "Ornament kit is not a weapon");
        check(BulwarkRange.isVisible(ItemID.DINHS_BULWARK, 0, false), "Normal bulwark shows at 0% with the requirement off");
        check(BulwarkRange.isVisible(ItemID.DINHS_BULWARK_ORNAMENT, 0, false), "Blazing bulwark shows at 0% with the requirement off");
        check(!BulwarkRange.isVisible(-1, 0, false), "Bulwark is still required with the energy requirement off");

        WorldPoint center = new WorldPoint(3200, 3200, 2);
        assertSquare(center);
        assertSquare(center.dx(2).dy(-1));
        assertRendering();
        System.out.println("PASS: visibility, 121-tile squares, perimeter, fill-only rendering and optional center tile");
    }

    private static void assertRendering()
    {
        byte[][][] tileSettings = new byte[4][Constants.EXTENDED_SCENE_SIZE][Constants.EXTENDED_SCENE_SIZE];
        Scene scene = stub(Scene.class, (proxy, method, args) -> tileSettings);
        WorldView view = stub(WorldView.class, (proxy, method, args) ->
        {
            switch (method.getName())
            {
                case "getId": return WorldView.TOPLEVEL;
                case "isTopLevel": return true;
                case "getSizeX":
                case "getSizeY": return Constants.SCENE_SIZE;
                case "getBaseX":
                case "getBaseY": return 3200;
                case "getScene": return scene;
                default: return 0;
            }
        });
        Player player = stub(Player.class, (proxy, method, args) ->
            method.getName().equals("getWorldView") ? view : new WorldPoint(3250, 3250, 0));
        ItemContainer equipment = stub(ItemContainer.class, (proxy, method, args) -> new Item(ItemID.DINHS_BULWARK, 1));
        Client client = stub(Client.class, (proxy, method, args) ->
        {
            switch (method.getName())
            {
                case "getGameState": return GameState.LOGGED_IN;
                case "getItemContainer": return equipment;
                case "getLocalPlayer": return player;
                case "getWorldView": return view;
                case "getCameraX":
                case "getCameraY": return 50 * 128 + 64;
                case "getCameraZ": return -640;
                case "getCameraPitch": return 4096;
                case "getScale": return 100;
                case "getViewportWidth":
                case "getViewportHeight": return 320;
                case "isGpu": return false;
                default: return 0;
            }
        });

        BufferedImage defaultImage = draw(client, new BulwarkSpecRangeConfig() {});
        check(alpha(defaultImage, 160, 160) == 0, "The energy requirement hides the overlay at 0%");
        for (boolean outline : new boolean[]{true, false})
        {
            BufferedImage image = draw(client, new BulwarkSpecRangeConfig()
            {
                @Override public boolean requireSpecialEnergy() { return false; }
                @Override public boolean showOutline() { return outline; }
            });
            check(alpha(image, 160, 160) == 50, "The configured fill is visible at 0%");
            check(alpha(image, 150, 160) == 50, "The center tile has no extra outline by default");
            check(alpha(image, 60, 150) == 50, "No perpendicular line inside the left border");
            check(alpha(image, 150, 60) == 50, "No perpendicular line inside the top border");
            check(alpha(image, 50, 160) > 50 == outline && alpha(image, 160, 50) > 50 == outline
                && alpha(image, 269, 160) > 50 == outline && alpha(image, 160, 269) > 50 == outline,
                "Only the enabled outline draws all four edges");
            check(alpha(image, 50, 50) > 50 == outline, "Corner follows the outline toggle");
        }

        BufferedImage centerImage = draw(client, new BulwarkSpecRangeConfig()
        {
            @Override public boolean requireSpecialEnergy() { return false; }
            @Override public boolean showOutline() { return false; }
            @Override public boolean showCenterTile() { return true; }
        });
        check(alpha(centerImage, 150, 160) > 50, "The center tile has a visible outline");
        check(alpha(centerImage, 130, 160) == 50, "The adjacent tile has no extra outline");
        check(alpha(centerImage, 50, 160) == 50, "The center outline works with the area outline disabled");
    }

    private static BufferedImage draw(Client client, BulwarkSpecRangeConfig config)
    {
        BufferedImage image = new BufferedImage(320, 320, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try
        {
            new BulwarkSpecRangeOverlay(client, config).render(graphics);
        }
        finally
        {
            graphics.dispose();
        }
        return image;
    }

    private static int alpha(BufferedImage image, int x, int y)
    {
        return image.getRGB(x, y) >>> 24;
    }

    private static <T> T stub(Class<T> type, InvocationHandler handler)
    {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static void assertSquare(WorldPoint center)
    {
        Set<WorldPoint> marked = new HashSet<>();
        BulwarkRange.forEachTile(center, (tile, dx, dy) ->
        {
            check(tile.equals(center.dx(dx).dy(dy)), "Offset and plane");
            check(Math.abs(dx) <= 5 && Math.abs(dy) <= 5, "Outside of 11x11");
            check(marked.add(tile), "Duplicate tile");
        });
        check(marked.size() == 121, "11x11 square");
        check(marked.contains(center), "Center tile");
    }

    private static void check(boolean condition, String reason)
    {
        if (!condition)
        {
            throw new AssertionError(reason);
        }
    }
}
