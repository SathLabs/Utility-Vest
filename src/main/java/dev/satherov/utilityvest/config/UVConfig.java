package dev.satherov.utilityvest.config;

import dev.satherov.utilityvest.config.annotation.Config;
import dev.satherov.utilityvest.config.annotation.ConfigVal;

import net.neoforged.fml.config.ModConfig;

@Config(ModConfig.Type.CLIENT)
public class UVConfig {
    
    @ConfigVal(name = "invert_radial_scroll", comment = "Invert the scroll direction in the radial menu")
    @ConfigVal.Boolean
    public static boolean InvertRadialScroll = false;
    
    @ConfigVal(name = "radial_tooltip", comment = "Defines when the tooltip in the radial menu should be displayed")
    @ConfigVal.Enum(ToolTipDisplay.class)
    public static ToolTipDisplay RadialTooltip = ToolTipDisplay.ALWAYS;
    @ConfigVal(name = "radial_stack_count", comment = "Defines when stack size label should be displayed for single items")
    @ConfigVal.Enum(RadialStackCountDisplay.class)
    public static RadialStackCountDisplay RadialStackCount = RadialStackCountDisplay.TOOLS_ONLY;
    @ConfigVal(name = "remember_radial_row", comment = "Reopen radial menu on last closed row")
    @ConfigVal.Boolean
    public static boolean RememberRadialRow = true;
    
    public enum ToolTipDisplay implements ConfigEnum {
        ALWAYS("Always show the tooltip"),
        SHIFT("Only show the tooltip when holding shift"),
        NEVER("Never show the tooltip"),
        ;
        
        public final String comment;
        
        ToolTipDisplay(String comment) {
            this.comment = comment;
        }
        
        @Override
        public String comment() {
            return this.comment;
        }
    }
    
    public enum RadialStackCountDisplay implements ConfigEnum {
        ALWAYS("Always show the stack counts for single items"),
        TOOLS_ONLY("Hide stack counts for tools & armor only"),
        NEVER("Never show the stack count for single items"),
        ;
        
        public final String comment;
        
        RadialStackCountDisplay(String comment) {
            this.comment = comment;
        }
        
        @Override
        public String comment() {
            return this.comment;
        }
    }
}
