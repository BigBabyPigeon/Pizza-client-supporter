package net.taunahi.ezskyblockscripts.mixin.client;

import net.taunahi.ezskyblockscripts.feature.impl.Freelook;
import net.taunahi.ezskyblockscripts.handler.MacroHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Mouse.class, priority = Integer.MAX_VALUE, remap = false)
public class MixinMouse {
    @Inject(method = "getEventDWheel()I", at = @At("RETURN"), remap = false, cancellable = true)
    private static void getEventDWheel(CallbackInfoReturnable<Integer> cir) {
        if (Freelook.getInstance().isRunning()) {
            Freelook.getInstance().setDistance(Math.min(20, Math.max(1, Freelook.getInstance().getDistance() + (cir.getReturnValue() / 120f))));
            cir.setReturnValue(0);
            return;
        }
        if (MacroHandler.getInstance().getCurrentMacro().isPresent() && MacroHandler.getInstance().isMacroToggled() && !(Minecraft.getMinecraft().currentScreen instanceof GuiChat)) {
            cir.setReturnValue(0);
        }
    }
}
