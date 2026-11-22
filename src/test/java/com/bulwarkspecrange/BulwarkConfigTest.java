package com.bulwarkspecrange;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.client.RuneLite;
import net.runelite.client.RuneLiteModule;
import net.runelite.client.RuntimeConfigLoader;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class BulwarkConfigTest
{
    public static void main(String[] args) throws Exception
    {
        OkHttpClient http = new OkHttpClient.Builder().addInterceptor(chain ->
            new Response.Builder()
                .request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .body(ResponseBody.create(MediaType.parse("application/json"), "{}")).build()
        ).build();
        Client client = (Client) Proxy.newProxyInstance(Client.class.getClassLoader(), new Class<?>[]{Client.class},
            (proxy, method, arguments) -> null);
        Injector injector = Guice.createInjector(new RuneLiteModule(
            http, () -> client, new RuntimeConfigLoader(http), true, false, true,
            new File(RuneLite.RUNELITE_DIR, "test-session"), null, null, false, false));
        RuneLite.setInjector(injector);
        try
        {
            verifyConfig(injector);
        }
        finally
        {
            injector.getInstance(ScheduledExecutorService.class).shutdownNow();
            http.dispatcher().executorService().shutdownNow();
        }
    }

    private static void verifyConfig(Injector injector) throws Exception
    {
        ConfigManager configManager = injector.getInstance(ConfigManager.class);
        configManager.load();
        PluginManager pluginManager = injector.getInstance(PluginManager.class);
        Plugin plugin = pluginManager.loadPlugins(List.of(BulwarkSpecRangePlugin.class), null).get(0);

        Config detected = pluginManager.getPluginConfigProxy(plugin);
        check(detected instanceof BulwarkSpecRangeConfig, "RuneLite must discover the configuration");
        BulwarkSpecRangeConfig config = (BulwarkSpecRangeConfig) detected;
        configManager.setDefaultConfiguration(config, false);
        check(configManager.getConfigDescriptor(config).getItems().stream()
            .map(item -> item.getItem().keyName()).collect(Collectors.toList())
            .equals(List.of("requireSpecialEnergy", "showOutline", "outlineColor", "fillColor", "outlineWidth", "highlightCorners", "showCenterTile")), "RuneLite settings order");
        check(config.outlineColor().equals(new Color(0, 39, 207, 205)), "Default outline color and opacity");
        check(config.fillColor().equals(new Color(0, 10, 255, 50)), "Default fill and opacity");
        check(config.outlineWidth() == 1.25 && config.highlightCorners() && config.showOutline(), "Default outline");
        check(config.requireSpecialEnergy(), "Require 50% by default");
        check(!config.showCenterTile(), "Center tile is off by default");
        verifyCog(injector, pluginManager, plugin);

        Color selected = new Color(55, 170, 224, 125);
        configManager.setConfiguration(BulwarkSpecRangeConfig.GROUP, "outlineColor", selected);
        configManager.setConfiguration(BulwarkSpecRangeConfig.GROUP, "highlightCorners", false);
        configManager.setConfiguration(BulwarkSpecRangeConfig.GROUP, "showOutline", false);
        configManager.setConfiguration(BulwarkSpecRangeConfig.GROUP, "requireSpecialEnergy", false);
        configManager.setConfiguration(BulwarkSpecRangeConfig.GROUP, "showCenterTile", true);
        check(config.outlineColor().equals(selected), "Color and alpha update");
        check(!config.highlightCorners(), "Corner preference update");
        check(!config.showOutline() && !config.requireSpecialEnergy(), "New toggles update live");
        check(config.showCenterTile(), "Center tile preference updates live");
        System.out.println("PASS: RuneLite's cog opens all seven settings. Defaults and live updates work.");
    }

    private static void verifyCog(Injector injector, PluginManager pluginManager, Plugin plugin) throws Exception
    {
        Class<?> panelType = Class.forName("net.runelite.client.plugins.config.PluginListPanel");
        Method rebuildPluginList = panelType.getDeclaredMethod("rebuildPluginList");
        rebuildPluginList.setAccessible(true);
        pluginManager.add(plugin);
        SwingUtilities.invokeAndWait(() ->
        {
            Container panel = (Container) injector.getInstance(panelType);
            try
            {
                rebuildPluginList.invoke(panel);
            }
            catch (ReflectiveOperationException e)
            {
                throw new AssertionError(e);
            }
            JButton cog = findCog(panel);
            check(cog != null && cog.isVisible(), "RuneLite settings cog is visible");
            cog.doClick(0);
            Container tabs = panel.getParent();
            check(hasLabel(tabs, "Require 50% special energy") && hasLabel(tabs, "Show outline")
                && hasLabel(tabs, "Outline color") && hasLabel(tabs, "Fill color")
                && hasLabel(tabs, "Outline width") && hasLabel(tabs, "Highlight corners")
                && hasLabel(tabs, "Show center tile"), "Cog opens the settings form");
        });
    }

    private static JButton findCog(Container container)
    {
        for (Component child : container.getComponents())
        {
            if (child instanceof JButton && "Edit plugin configuration".equals(((JButton) child).getToolTipText()))
            {
                return (JButton) child;
            }
            JButton nested = child instanceof Container ? findCog((Container) child) : null;
            if (nested != null)
            {
                return nested;
            }
        }
        return null;
    }

    private static boolean hasLabel(Container container, String text)
    {
        for (Component child : container.getComponents())
        {
            if (child instanceof JLabel && text.equals(((JLabel) child).getText()))
            {
                return true;
            }
            if (child instanceof Container && hasLabel((Container) child, text))
            {
                return true;
            }
        }
        return false;
    }

    private static void check(boolean condition, String reason)
    {
        if (!condition)
        {
            throw new AssertionError(reason);
        }
    }
}
