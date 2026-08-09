package io.adriabama06.safetravel.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;

public class EBounce extends Module {
    private enum BounceState {
        // JUMP is held. If the player just touched the floor, schedule a release.
        HOLDING,
        // The tick after touching the floor: JUMP is released.
        RELEASING,
        // The tick after releasing: JUMP is held again.
        RESUMING
    }

    private BounceState state = BounceState.HOLDING;
    private boolean prevOnGround = false;

    public EBounce(Category category) {
        super(category, "e-bounce", "Sprints and walks forward while keeping JUMP pressed. When the player touches the floor, JUMP is released the tick after and pressed again the tick after that.");
    }

    @Override
    public void onActivate() {
        state = BounceState.HOLDING;
        prevOnGround = false;
    }

    @Override
    public void onDeactivate() {
        setKey(mc.options.jumpKey, false);
        setKey(mc.options.sprintKey, false);
        setKey(mc.options.forwardKey, false);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        boolean onGround = mc.player.isOnGround();
        boolean touchedFloor = onGround && !prevOnGround;
        prevOnGround = onGround;

        switch (state) {
            case HOLDING -> {
                if (touchedFloor) state = BounceState.RELEASING;
                setKey(mc.options.jumpKey, true);
            }
            case RELEASING -> {
                setKey(mc.options.jumpKey, false);
                state = BounceState.RESUMING;
            }
            case RESUMING -> {
                setKey(mc.options.jumpKey, true);
                state = BounceState.HOLDING;
            }
        }

        setKey(mc.options.sprintKey, true);
        setKey(mc.options.forwardKey, true);
    }

    private void setKey(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }
}