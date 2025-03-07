package net.mehvahdjukaar.advframes.integration.neoforge;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.trains.display.FlapDisplaySection;
import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlockTile;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class CreateCompatImpl {

    public static void init(){
        RegHelper.register(
                AdvFrames.res("stat_display_source"), () -> {
                    var obj = new StatDisplaySource();
                    DisplaySource.BY_BLOCK_ENTITY.register(AdvFrames.STAT_FRAME_TILE.get(), List.of(obj));
                    return obj;
                },
                CreateRegistries.DISPLAY_SOURCE);
    }

    public static void setup() {
    }

    public static void setupClient() {
        //TODO:
        //PonderRegistry.TAGS.forTag(DISPLAY_TARGETS).add(AdvFrames.STAT_FRAME.get());
    }

    public static class StatDisplaySource extends SingleLineDisplaySource {
        public static final MutableComponent EMPTY = Component.literal("0");

        @Override
        protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
            if (context.getSourceBlockEntity() instanceof StatFrameBlockTile tile) {
                var stat = tile.getStat();
                var value = tile.getValue();
                if (stat != null) {
                    return Component.literal(stat.format(value));
                }
            }
            return EMPTY;
        }

        @Override
        protected boolean allowsLabeling(DisplayLinkContext context) {
            return true;
        }

        @Override
        protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
            return "Instant";
        }

        @Override
        protected FlapDisplaySection createSectionForValue(DisplayLinkContext context, int size) {
            return new FlapDisplaySection(size * 7.0F, "instant", false, false);
        }

        @Override
        protected String getTranslationKey() {
            return "stat";
        }

    }
}
