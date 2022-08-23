package net.mehvahdjukaar.advframes.forge;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ForgeHooksClient;

public class AdvFramesClientImpl {
    public static void clearForgeGuiLayers(Minecraft minecraft) {
        ForgeHooksClient.clearGuiLayers(minecraft);
    }
}
