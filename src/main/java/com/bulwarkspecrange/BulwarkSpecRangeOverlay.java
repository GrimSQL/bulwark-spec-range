package com.bulwarkspecrange;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

final class BulwarkSpecRangeOverlay extends Overlay
{
    private static final Color CENTER_COLOR = new Color(185, 209, 255, 230);
    private static final Color CENTER_BACKING = new Color(17, 27, 54, 175);
    private static final BasicStroke CENTER_STROKE = stroke(1.25f);
    private static final BasicStroke CENTER_BACKING_STROKE = stroke(3);

    private final Client client;
    private final BulwarkSpecRangeConfig config;

    @Inject
    BulwarkSpecRangeOverlay(Client client, BulwarkSpecRangeConfig config)
    {
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (client.getGameState() != GameState.LOGGED_IN || !isAvailable())
        {
            return null;
        }

        Player player = client.getLocalPlayer();
        if (player == null)
        {
            return null;
        }

        WorldView worldView = player.getWorldView();
        Color fillColor = config.fillColor();
        Color outlineColor = config.outlineColor();
        boolean showOutline = config.showOutline() && outlineColor.getAlpha() > 0;
        boolean showCenterTile = config.showCenterTile();
        boolean highlightCorners = config.highlightCorners();
        Path2D fill = new Path2D.Float();
        Path2D border = new Path2D.Float();
        Path2D corners = new Path2D.Float();
        Path2D centerTile = new Path2D.Float();

        BulwarkRange.forEachTile(player.getWorldLocation(), (tile, dx, dy) ->
        {
            LocalPoint local = LocalPoint.fromWorld(worldView, tile);
            Polygon polygon = local == null ? null : Perspective.getCanvasTilePoly(client, local);
            if (polygon == null)
            {
                return;
            }

            fill.append(polygon, false);
            if (showCenterTile && dx == 0 && dy == 0)
            {
                centerTile.append(polygon, false);
            }
            if (!showOutline)
            {
                return;
            }

            // Polygon vertices are (-x,-y), (+x,-y), (+x,+y), (-x,+y) relative to the tile's center.
            boolean cornerTile = highlightCorners
                && Math.abs(dx) == BulwarkRange.RADIUS && Math.abs(dy) == BulwarkRange.RADIUS;
            Path2D edgePath = cornerTile ? corners : border;
            if (dx == -BulwarkRange.RADIUS)
            {
                addEdge(edgePath, polygon, 3, 0);
            }
            if (dy == BulwarkRange.RADIUS)
            {
                addEdge(edgePath, polygon, 2, 3);
            }
            if (dx == BulwarkRange.RADIUS)
            {
                addEdge(edgePath, polygon, 1, 2);
            }
            if (dy == -BulwarkRange.RADIUS)
            {
                addEdge(edgePath, polygon, 0, 1);
            }
        });

        Graphics2D overlayGraphics = (Graphics2D) graphics.create();
        try
        {
            overlayGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            overlayGraphics.setColor(fillColor);
            overlayGraphics.fill(fill);
            if (showOutline)
            {
                float width = (float) config.outlineWidth();
                overlayGraphics.setColor(new Color(32, 22, 15, outlineColor.getAlpha() * 85 / 100));
                overlayGraphics.setStroke(stroke(width + 2.25f));
                overlayGraphics.draw(border);
                overlayGraphics.draw(corners);
                overlayGraphics.setColor(outlineColor);
                overlayGraphics.setStroke(stroke(width));
                overlayGraphics.draw(border);
                if (highlightCorners)
                {
                    overlayGraphics.setColor(cornerColor(outlineColor));
                    overlayGraphics.setStroke(stroke(width + 1));
                    overlayGraphics.draw(corners);
                }
            }
            if (showCenterTile)
            {
                overlayGraphics.setColor(CENTER_BACKING);
                overlayGraphics.setStroke(CENTER_BACKING_STROKE);
                overlayGraphics.draw(centerTile);
                overlayGraphics.setColor(CENTER_COLOR);
                overlayGraphics.setStroke(CENTER_STROKE);
                overlayGraphics.draw(centerTile);
            }
        }
        finally
        {
            overlayGraphics.dispose();
        }

        return null;
    }

    private boolean isAvailable()
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
        Item weapon = equipment == null ? null : equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
        return weapon != null && BulwarkRange.isVisible(
            weapon.getId(), client.getVarpValue(VarPlayerID.SA_ENERGY), config.requireSpecialEnergy());
    }

    private static void addEdge(Path2D border, Polygon polygon, int from, int to)
    {
        border.moveTo(polygon.xpoints[from], polygon.ypoints[from]);
        border.lineTo(polygon.xpoints[to], polygon.ypoints[to]);
    }

    private static BasicStroke stroke(float width)
    {
        return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    private static Color cornerColor(Color outline)
    {
        return new Color(
            (outline.getRed() * 3 + 510) / 5,
            (outline.getGreen() * 3 + 510) / 5,
            (outline.getBlue() * 3 + 510) / 5,
            Math.min(255, outline.getAlpha() * 6 / 5));
    }
}
