package dev.insilicon.brokitsinsilicontest.commands;

import dev.insilicon.brokitsinsilicontest.BroKits_Insilicon_Test;
import dev.insilicon.brokitsinsilicontest.CustomClasses.CoinflipInstance;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

// NOTE TO REVIEWER:
// This command is for development purposes only. It is used to fill the page with random coinflips.
public class FillPage implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        // fills pages with coinflips
        for (int i = 0; i < 35; i++) {

            int randomValue = (int) (Math.random() * 100 + 100);

            CoinflipInstance coinflipInstance = new CoinflipInstance((Player) commandSender, randomValue);

            BroKits_Insilicon_Test.addFlip(coinflipInstance);

            //due to coinflip UIDs needs to be a 1ms delay
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        return true;
    }
}
