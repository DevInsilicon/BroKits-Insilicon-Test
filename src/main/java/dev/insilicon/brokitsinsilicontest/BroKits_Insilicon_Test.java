package dev.insilicon.brokitsinsilicontest;

import dev.insilicon.brokitsinsilicontest.CustomClasses.CoinflipInstance;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;


public final class BroKits_Insilicon_Test extends JavaPlugin {

    private static Economy economy = null;
    public static List<CoinflipInstance> flips = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic

        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }


        getServer().getPluginManager().registerEvents(new dev.insilicon.brokitsinsilicontest.listeners.GUIListener(), this);

        getLogger().info("Coinflip init'ed");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        getLogger().info("Coinflip disabled");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static Economy getEconomy() {
        return economy;
    }
}
