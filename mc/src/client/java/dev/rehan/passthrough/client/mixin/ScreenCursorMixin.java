package dev.rehan.passthrough.client.mixin;

import dev.rehan.passthrough.client.ScreenInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecraft's own cursor is drawn by SDL, in its own window - and that window never has the focus here,
 * so the cursor is either invisible or wherever the OS left it behind GTA's. The screen is composited over
 * the host's picture, so draw ours here instead: it is part of the overlay, which the host always shows.
 */
@Mixin(Screen.class)
public abstract class ScreenCursorMixin {
	@Inject(method = "render", at = @At("TAIL"))
	private void passthrough$drawCursor(final GuiGraphics graphics, final int mouseX, final int mouseY,
		final float partialTick, final CallbackInfo ci) {
		final Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.gui.screen() != (Object) this || !ScreenInput.wantsCursor()) {
			return;
		}

		final int x = (int) ScreenInput.cursorX(), y = (int) ScreenInput.cursorY();
		// a white arrow's worth, with a dark outline so it reads over both bright and dark slots
		graphics.fill(x, y, x + 1, y + 9, 0xFF000000);
		graphics.fill(x + 1, y, x + 7, y + 1, 0xFF000000);
		graphics.fill(x + 1, y + 1, x + 2, y + 8, 0xFFFFFFFF);
		graphics.fill(x + 2, y + 1, x + 6, y + 2, 0xFFFFFFFF);
		graphics.fill(x + 2, y + 2, x + 3, y + 3, 0xFFFFFFFF);
		graphics.fill(x + 2, y + 4, x + 5, y + 5, 0xFFFFFFFF);
	}
}
