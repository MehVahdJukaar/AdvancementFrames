package net.mehvahdjukaar.advframes.mixins;

import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ServerStatsCounter.class)
public interface ServerStatCounterAccessor {

    @Accessor
    Set<Stat<?>> getDirty();
}
