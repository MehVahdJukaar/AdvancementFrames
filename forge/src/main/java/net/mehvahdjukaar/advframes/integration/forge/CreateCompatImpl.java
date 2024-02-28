package net.mehvahdjukaar.advframes.integration.forge;

import com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours;
import com.simibubi.create.content.redstone.displayLink.DisplayBehaviour;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.trains.display.FlapDisplaySection;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.utility.Components;
import net.mehvahdjukaar.advframes.AdvFrames;
import net.mehvahdjukaar.advframes.blocks.StatFrameBlockTile;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.simibubi.create.infrastructure.ponder.AllPonderTags.DISPLAY_TARGETS;

public class CreateCompatImpl {

    public static void setup() {
        DisplayBehaviour itemDisplaySource = AllDisplayBehaviours.register(
                AdvFrames.res("stat_display_source"), new StatDisplaySource());
        AllDisplayBehaviours.assignBlockEntity(itemDisplaySource, AdvFrames.STAT_FRAME_TILE.get());
    }

    public static void setupClient() {
        PonderRegistry.TAGS.forTag(DISPLAY_TARGETS).add(AdvFrames.STAT_FRAME.get());
    }

    public static class StatDisplaySource extends SingleLineDisplaySource {
        public static final MutableComponent EMPTY = Components.literal("0");

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
