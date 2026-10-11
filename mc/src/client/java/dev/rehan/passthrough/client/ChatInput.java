package dev.rehan.passthrough.client;

import dev.rehan.passthrough.WorldBridge;
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
 *
 * <p>Enter and Backspace are handled here rather than by the chat's own key path: that path goes through
 * the game's keyboard callbacks, which check the real window for the key being down, and this window never
 * has the focus. So the line is kept here, and submitted from here.
 */
public final class ChatInput {
	private ChatInput() {
	}

	private static final StringBuilder LINE = new StringBuilder();

	/** The chat screen is the one screen that wants the keyboard rather than the mouse. */
	public static boolean isOpen(final Minecraft minecraft) {
		return minecraft.gui.screen() instanceof ChatScreen;
	}

	/** Typed characters, one codepoint each. */
	public static void text(final Minecraft minecraft, final String s, final int modifiers) {
		if (!(minecraft.gui.screen() instanceof ChatScreen chat) || s.isEmpty()) {
			return;
		}

		LINE.append(s);
		for (int i = 0; i < s.length(); ++i) {
			chat.charTyped(new CharacterEvent(s.codePointAt(i)));
		}
	}

	/** The keys a chat line cannot be written without: arrows, home and end, ctrl+v, and the rest. */
	public static void special(final Minecraft minecraft, final int glfw, final int modifiers) {
		if (!(minecraft.gui.screen() instanceof ChatScreen chat)) {
			return;
		}

		switch (glfw) {
			case 256 -> { // escape: the host keeps the key for itself, so close it here
				LINE.setLength(0);
				chat.onClose();
				return;
			}
			case 257 -> { // enter: run the line as a command, exactly as the chat would
				final String line = LINE.toString().trim();
				LINE.setLength(0);
				if (!line.isEmpty()) {
					WorldBridge.command(line);
				}

				chat.onClose();
				return;
			}
			case 259 -> { // backspace
				if (LINE.length() > 0) {
					LINE.setLength(LINE.length() - 1);
				}

				chat.keyPressed(new KeyEvent(259, 0, modifiers));
				return;
			}
			default -> {
			}
		}

		chat.keyPressed(new KeyEvent(glfw, 0, modifiers));
	}
}
