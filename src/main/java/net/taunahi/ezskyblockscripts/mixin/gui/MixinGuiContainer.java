package net.taunahi.ezskyblockscripts.mixin.gui;

import net.taunahi.ezskyblockscripts.event.DrawScreenAfterEvent;
import net.taunahi.ezskyblockscripts.util.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {

    @Inject(method = "drawScreen", at = @At("RETURN"))
    public void drawScreen_after(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        String name = InventoryUtils.getInventoryName();
        MinecraftForge.EVENT_BUS.post(new DrawScreenAfterEvent(Minecraft.getMinecraft().currentScreen));
    }
}
