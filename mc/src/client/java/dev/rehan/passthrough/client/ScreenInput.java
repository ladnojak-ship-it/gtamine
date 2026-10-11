package dev.rehan.passthrough.client;

import dev.rehan.passthrough.client.mixin.MinecraftAccessor;
import dev.rehan.passthrough.client.mixin.MouseHandlerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

/**
 * The cursor inside a screen of minecraft's own, while the host (GTA) has the focus and the mouse.
 *
 * <p>The host cannot give its window away - Windows only hands the focus to the foreground process - so
 * instead the host steers this cursor: it turns off its own look controls while a screen is open and sends
 * the mouse it would have used as {@code {"t":"mouse","dx":..,"dy":..}}. Clicks arrive as the usual
 * {@code attack}/{@code use} keys and are routed to the screen at the cursor.
 *
 * <p>The screen's own coordinates are only half of it: what is drawn, and what a click lands on, comes
 * from the mouse handler's position, so that is driven too.
 */
public final class ScreenInput {
	private ScreenInput() {
	}

	private static double x, y;
	private static boolean inScreen;
	static int mouseLog;

	/** A cursor is only steered while a screen is open and it still wants pointer input. */
	public static boolean wantsCursor() {
		final Minecraft minecraft = Minecraft.getInstance();
		return minecraft.gui.screen() != null
			&& !(minecraft.gui.screen() instanceof ChatScreen);
	}

	/**
	 * The host's mouse, applied to whatever screen is open. The axis is the host's: it counts down the
	 * screen while the screen counts up, so it is not negated - the overlay the host composites is already
	 * the right way up, and negating it made the cursor run the other way from the hand.
	 */
	public static void move(final Minecraft minecraft, final double dx, final double dy) {
		final Screen screen = minecraft.gui.screen();
		if (screen == null) {
			return;
		}
		if (!inScreen) {
			// the screen just opened: start at its centre rather than wherever the last one left off
			x = screen.width / 2.0;
			y = screen.height / 2.0;
			inScreen = true;
		}
		x = Math.max(0.0, Math.min(screen.width, x + dx));
		y = Math.max(0.0, Math.min(screen.height, y + dy));
		screen.mouseMoved(x, y);
		// what the screen renders the cursor from, and what a click resolves against, is the handler's
		// position in window pixels - scale it up from the GUI-scaled coordinates the screen uses
		final MouseHandler handler = ((MinecraftAccessor) minecraft).passthrough$getMouseHandler();
		if (handler != null) {
			final double scale = minecraft.getWindow().getWidth() / (double) minecraft.getWindow().getGuiScaledWidth();
			final MouseHandlerAccessor access = (MouseHandlerAccessor) handler;
			access.passthrough$setX(x * scale);
			access.passthrough$setY(y * scale);
		}
	}

	/** Called when a screen closes, so the cursor restarts centred next time. */
	public static void resetScreen() {
		inScreen = false;
	}

	/** Where the cursor is now, in GUI-scaled pixels: for the log, so a stuck cursor is visible. */
	public static double cursorX() {
		return x;
	}

	public static double cursorY() {
		return y;
	}

	/** The host's mouse button at the cursor: {@code 0} attack (left), {@code 1} use (right). */
	public static void mouse(final boolean down, final int button) {
		final Minecraft minecraft = Minecraft.getInstance();
		final Screen screen = minecraft.gui.screen();
		if (screen == null) {
			return;
		}
		move(minecraft, 0.0, 0.0);
		final MouseButtonEvent event = new MouseButtonEvent(x, y, new MouseButtonInfo(button, 0));
		if (down) {
			screen.mouseClicked(event, false);
		} else {
			screen.mouseReleased(event);
		}
	}
}
