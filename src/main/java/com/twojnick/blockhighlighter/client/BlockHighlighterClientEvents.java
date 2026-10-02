package com.twojnick.blockhighlighter.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.twojnick.blockhighlighter.BlockHighlighter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

public final class BlockHighlighterClientEvents {
    private BlockHighlighterClientEvents() {}

    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(BlockHighlighter.MODID, "category"));

    private static final KeyMapping OPEN_MENU = new KeyMapping(
            "key.blockhighlighter.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
    );

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(BlockHighlighterClientEvents::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(BlockHighlighterClientEvents::onClientTick);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(OPEN_MENU);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (OPEN_MENU.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new HighlighterScreen());
            }
        }
    }
}

