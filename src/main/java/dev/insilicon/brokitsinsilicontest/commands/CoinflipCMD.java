package dev.insilicon.brokitsinsilicontest.commands;

import dev.insilicon.brokitsinsilicontest.BroKits_Insilicon_Test;
import dev.insilicon.brokitsinsilicontest.CustomClasses.CoinflipInstance;
import dev.insilicon.brokitsinsilicontest.CustomClasses.PDTKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CoinflipCMD implements CommandExecutor, TabCompleter {


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        Player player = (Player) commandSender;
        MiniMessage miniMessage = MiniMessage.miniMessage();


        Inventory inv = commandSender.getServer().createInventory(null, 36, "Coinflip Menu | Page  1");



        // Fill the chest with gray glass panes
        for (int i = 0; i < 36; i++) {
            ItemStack grayGlassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = grayGlassPane.getItemMeta();
            if (meta != null) {
                meta.setItemName(" ");
            }
            grayGlassPane.setItemMeta(meta);
            inv.setItem(i, grayGlassPane);
        }

        // Verification tile for Anti Dupe (remember vulcan ac vurnerability)
        ItemStack verificationTile = createVerificationTile();
        inv.setItem(35, verificationTile);

        //LAYOUT

        // OXXXXXXXX 9
        // XXXXXXXXX 18
        // XXX<R>XXX 27
        //    234

        // < > arrows - Switch Page
        // R - Clock - Refresh
        // X - Gray Glass Pane
        // O - Verification Tile
        // B - Barrier Block - Not Avail

        //refresh item
        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta clockMeta = clock.getItemMeta();
        clockMeta.itemName(miniMessage.deserialize("<green>Refresh"));
        clock.setItemMeta(clockMeta);

        inv.setItem(31, clock);

        //Not Avail item
        ItemStack notAvail = new ItemStack(Material.BARRIER);
        ItemMeta notAvailMeta = notAvail.getItemMeta();
        notAvailMeta.itemName(miniMessage.deserialize("<red>Not Available"));
        notAvail.setItemMeta(notAvailMeta);

        //Automatically not avail on go back arrow
        inv.setItem(30, notAvail);

        //Arrow Item Point ->
        ItemStack NextPage = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = NextPage.getItemMeta();
        arrowMeta.itemName(miniMessage.deserialize("<green>Next Page"));
        NextPage.setItemMeta(arrowMeta);

        //Create Create Coinflip block
        ItemStack createCF = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta createCFMeta = createCF.getItemMeta();
        createCFMeta.itemName(miniMessage.deserialize("<gold>Create Coinflip"));
        createCF.setItemMeta(createCFMeta);

        inv.setItem(27, createCF);


        //Create cf items and stop at item 26
        for (int i = 0; i < BroKits_Insilicon_Test.getFlips().size(); i++) {
            if (i == 26) {
                break;
            }
            CoinflipInstance cf = BroKits_Insilicon_Test.getFlips().get(i);
            ItemStack cfItem = cf.createCoinflipItem();
            ItemMeta meta = cfItem.getItemMeta();

            if (cf.getPlayer1().equals(player)) {
                List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                lore.add(MiniMessage.miniMessage().deserialize("<red>Click to remove & refund"));
                meta.lore(lore);
            }
            cfItem.setItemMeta(meta);
            inv.setItem(i, cfItem);
        }



        if (BroKits_Insilicon_Test.getFlips().toArray().length > 16) {
            inv.setItem(32, NextPage);
        } else {
            inv.setItem(32, notAvail);
        }


        player.openInventory(inv);



        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }

    public ItemStack createVerificationTile() {
        ItemStack verificationTile = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = verificationTile.getItemMeta();


        if (meta != null) {
            meta.setItemName("V");
            NamespacedKey key = PDTKeys.COINFLIP_GUI;
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, " ");
            verificationTile.setItemMeta(meta);
        }

        return verificationTile;
    }
}
