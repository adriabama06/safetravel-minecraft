package io.adriabama06.safetravel;

import io.adriabama06.safetravel.modules.EBounce;
import io.adriabama06.safetravel.modules.SafeTravel;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class SafeTravelAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("SafeTravel");
    public static final HudGroup HUD_GROUP = new HudGroup("SafeTravel");

    @Override
    public void onInitialize() {
        LOG.info("Initializing SafeTravel Addon");

        // Modules
        Modules.get().add(new SafeTravel(CATEGORY));
        Modules.get().add(new EBounce(CATEGORY));
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "io.adriabama06.safetravel";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("adriabama06", "safetravel-minecraft");
    }
}
