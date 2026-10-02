package com.twojnick.blockhighlighter;

import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.HashMap;
import java.util.Map;

@Mod(BlockHighlighter.MODID)
public class BlockHighlighter {
    public static final String MODID = "blockhighlighter";

    /**
     * A minimal shared state for the first iteration of the UI.
     * We'll persist this properly (config) once the UI flow is finalized.
     */
    public static final Map<Block, Boolean> SELECTED_BLOCKS = new HashMap<>();

    public BlockHighlighter(IEventBus modEventBus, ModContainer modContainer) {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            try {
                Class.forName("com.twojnick.blockhighlighter.client.BlockHighlighterClientEvents")
                        .getMethod("init", IEventBus.class)
                        .invoke(null, modEventBus);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to init client events", e);
            }
        }
    }
}

