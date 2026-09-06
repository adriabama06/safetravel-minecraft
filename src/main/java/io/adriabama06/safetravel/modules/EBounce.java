package io.adriabama06.safetravel.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.KeyMapping;

public class EBounce extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFps = settings.createGroup("Fps");

    private final Setting<Boolean> lockYaw = sgGeneral.add(new BoolSetting.Builder()
            .name("lock-yaw")
            .description("Fix player yaw.")
            .defaultValue(false)
            .build());

    private final Setting<Double> travelYaw = sgGeneral.add(new DoubleSetting.Builder()
            .name("yaw")
            .description("Yaw used to travel.")
            .range(0, 360)
            .sliderRange(0, 360)
            .defaultValue(0)
            .visible(() -> lockYaw.get())
            .build());

    private final Setting<Boolean> lockPitch = sgGeneral.add(new BoolSetting.Builder()
            .name("lock-pitch")
            .description("Fix player pitch.")
            .defaultValue(true)
            .build());

    private final Setting<Double> travelPitch = sgGeneral.add(new DoubleSetting.Builder()
            .name("pitch")
            .description("Pitch used to travel.")
            .range(0, 90)
            .sliderRange(0, 90)
            .defaultValue(73)
            .visible(() -> lockPitch.get())
            .build());

    private final Setting<Boolean> autoEnableFreeLook = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-enable-free-look")
            .description("What it says, make sure to enable mode: Camera.")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> autoFpsLower = sgFps.add(new BoolSetting.Builder()
            .name("auto-fps-lower")
            .description("Enable to reduce fps on travel.")
            .defaultValue(true)
            .build());

    private int originalFps = 60;

    private final Setting<Integer> autoFpsLowerTarget = sgFps.add(new IntSetting.Builder()
            .name("target-fps")
            .description("Set the fps you want (10fps usually allows to reach +35 blocks/s).")
            .defaultValue(10)
            .min(1)
            .sliderRange(1, 60)
            .visible(() -> autoFpsLower.get())
            .build());

    private final Setting<Integer> autoFpsLowerMinSpeed = sgFps.add(new IntSetting.Builder()
            .name("auto-fps-lower-min-speed")
            .description("Minimum speed required to lower fps (in blocks/s)")
            .defaultValue(20)
            .min(1)
            .sliderRange(1, 40)
            .visible(() -> autoFpsLower.get())
            .build());

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

        originalFps = mc.options.framerateLimit().get();

        if(autoEnableFreeLook.get()) {
            FreeLook freelook = Modules.get().get(FreeLook.class);
            if (!freelook.isActive()) {
                freelook.toggle();
            }
        }
    }

    @Override
    public void onDeactivate() {
        setKey(mc.options.keyJump, false);
        setKey(mc.options.keySprint, false);
        setKey(mc.options.keyUp, false);

        mc.options.framerateLimit().set(originalFps);

        if(autoEnableFreeLook.get()) {
            FreeLook freelook = Modules.get().get(FreeLook.class);
            if (freelook.isActive()) {
                freelook.toggle();
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        processYawPitch();
        processElytraBounce();
        processFps();
    }

    private void processElytraBounce() {
        if (mc.player == null || mc.level == null) return;

        boolean onGround = mc.player.onGround();
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

        setKey(mc.options.keyJump, holdJump);
        setKey(mc.options.keySprint, true);
        setKey(mc.options.keyUp, true);
    }

    private void processYawPitch() {
        if(lockYaw.get()) {
            mc.player.setYRot(travelYaw.get().floatValue());
        }
        if(lockPitch.get()) {
            mc.player.setXRot(travelPitch.get().floatValue());
        }
    }

    private void processFps() {
        if(!autoFpsLower.get()) return;

        if(getSpeed() > autoFpsLowerMinSpeed.get()) {
            mc.options.framerateLimit().set(autoFpsLowerTarget.get());
        }
        else
        {
            mc.options.framerateLimit().set(originalFps);
        }
    }

    private double lastX, lastZ;

    public double getSpeed() {
        if (mc.player == null) return 0;

        double dx = mc.player.getX() - lastX;
        double dz = mc.player.getZ() - lastZ;
        double speed = Math.sqrt(dx * dx + dz * dz) * 20; // blocks/s

        lastX = mc.player.getX();
        lastZ = mc.player.getZ();

        return speed;
    }

    private void setKey(KeyMapping key, boolean pressed) {
        key.setDown(pressed);
        Input.setKeyState(key, pressed);
    }
}
