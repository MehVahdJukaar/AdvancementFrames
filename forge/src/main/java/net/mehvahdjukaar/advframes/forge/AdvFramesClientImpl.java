package net.mehvahdjukaar.advframes.forge;

import net.mehvahdjukaar.advframes.AdvFramesClient;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AdvFramesClientImpl {
    public static void clearForgeGuiLayers(Minecraft minecraft) {
        ForgeHooksClient.clearGuiLayers(minecraft);
    }


}
