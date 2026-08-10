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
    private int jumpSpamTicks = 0;
    private boolean spamPressed = false;

    public EBounce(Category category) {
        super(category, "e-bounce", "Elytra Bounce, but legit mode simulating player keyboard.");
    }

    @Override
    public void onActivate() {
        state = BounceState.HOLDING;
        prevOnGround = false;
        jumpSpamTicks = 0;
        spamPressed = false;
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
        boolean leftGround = !onGround && prevOnGround;
        prevOnGround = onGround;

        if (leftGround) {
            jumpSpamTicks = 5;
            spamPressed = false;
        }

        boolean holdJump = false;
        switch (state) {
            case HOLDING -> {
                if (touchedFloor) state = BounceState.RELEASING;
                holdJump = true;
            }
            case RELEASING -> {
                holdJump = false;
                state = BounceState.RESUMING;
            }
            case RESUMING -> {
                holdJump = true;
                state = BounceState.HOLDING;
            }
        }

        if (jumpSpamTicks > 0) {
            jumpSpamTicks--;
            holdJump = spamPressed;
            spamPressed = !spamPressed;
        }

        setKey(mc.options.jumpKey, holdJump);
        setKey(mc.options.sprintKey, true);
        setKey(mc.options.forwardKey, true);
    }

    private void setKey(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }
}