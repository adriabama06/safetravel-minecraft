package io.adriabama06.safetravel;

import io.adriabama06.safetravel.commands.CommandExample;
import io.adriabama06.safetravel.hud.HudExample;
import io.adriabama06.safetravel.modules.ModuleExample;
import io.adriabama06.safetravel.modules.SafeTravel;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
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
        Modules.get().add(new ModuleExample());
        Modules.get().add(new SafeTravel(CATEGORY));

        // Commands
        Commands.add(new CommandExample());

        // HUD
        Hud.get().register(HudExample.INFO);
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
