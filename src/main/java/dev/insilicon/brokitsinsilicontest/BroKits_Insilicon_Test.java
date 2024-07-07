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

        //register commands
        getCommand("coinflip").setExecutor(new dev.insilicon.brokitsinsilicontest.commands.CoinflipCMD());
        getCommand("coinflip").setTabCompleter(new dev.insilicon.brokitsinsilicontest.commands.CoinflipCMD());

        getCommand("fillpage").setExecutor(new dev.insilicon.brokitsinsilicontest.commands.FillPage());


        getLogger().info("Coinflip init'ed");



    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        refundAllCoinFlips();
        getLogger().info("Coinflip disabled");
    }

    public void refundAllCoinFlips() {
        for (CoinflipInstance flip : flips) {
            flip.refund();
        }
    }

    public static List<CoinflipInstance> getFlips() {
        return flips;
    }

    public static void setFlips(List<CoinflipInstance> flips) {
        BroKits_Insilicon_Test.flips = flips;
    }

    public static void addFlip(CoinflipInstance flip) {
        flips.add(flip);
    }

    public static void removeFlip(CoinflipInstance flip) {
        flips.remove(flip);
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
