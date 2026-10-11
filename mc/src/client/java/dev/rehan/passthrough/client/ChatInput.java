package dev.rehan.passthrough.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
/**
 * The host (GTA) types into a chat screen of ours.
 *
 * <p>A screen of ours can never have the focus - Windows only hands it to the foreground process, and that
 * is the host's - so the host keys for us instead. It turns keystrokes into text with the layout the player
 * is actually typing in and sends them; here they go to the chat screen as the characters and special keys
 * they were.
 */
public final class ChatInput {
	private ChatInput() {
	}

	/** The chat screen is the one screen that wants the keyboard rather than the mouse. */
	public static boolean isOpen(final Minecraft minecraft) {
		return minecraft.gui.screen() instanceof ChatScreen;
	}

	/** Typed characters, one codepoint each. */
	public static void text(final Minecraft minecraft, final String s, final int modifiers) {
		if (!(minecraft.gui.screen() instanceof ChatScreen chat) || s.isEmpty()) {
			return;
		}

		for (int i = 0; i < s.length(); ++i) {
			chat.charTyped(new CharacterEvent(s.codePointAt(i)));
		}
	}

	/**
	 * The keys a chat line cannot be written without: arrows, home and end, ctrl+v, and the rest. The host
	 * cannot swallow keys - its own keyboard callback only reports them - so every one of them has to come
	 * through here, or the line can only ever be typed and never edited.
	 */
	public static void special(final Minecraft minecraft, final int glfw, final int modifiers) {
		if (!(minecraft.gui.screen() instanceof ChatScreen chat)) {
			return;
		}

		if (glfw == 256) { // escape: close it here, since the host keeps the key for itself
			chat.onClose();
			return;
		}

		chat.keyPressed(new KeyEvent(glfw, 0, modifiers));
	}
}
