package dev.rehan.passthrough.client.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * The mouse handler keeps its own cursor position, which is what a screen actually renders from. Moving
 * only the screen's own coordinates moves a phantom: the drawn cursor and the slot under it stay put, so
 * the host's mouse has to drive these two as well.
 */
@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
	@Accessor("xpos")
	double passthrough$getX();

	@Accessor("xpos")
	void passthrough$setX(double xpos);

	@Accessor("ypos")
	double passthrough$getY();

	@Accessor("ypos")
	void passthrough$setY(double ypos);
}
