package dev.insilicon.brokitsinsilicontest.listeners;

import dev.insilicon.brokitsinsilicontest.BroKits_Insilicon_Test;
import dev.insilicon.brokitsinsilicontest.CustomClasses.CoinflipInstance;
import dev.insilicon.brokitsinsilicontest.CustomClasses.PDTKeys;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class GUIListener implements Listener {

    private List<Player> awaitingResp = new ArrayList<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        String name = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        //default menu checks
        if (name.startsWith("Coinflip Menu")) {
            handleCoinflipMenu(event);
        } else {
            return;
        }

        event.setCancelled(true);
    }

    private void handleCoinflipMenu(InventoryClickEvent event) {
        MiniMessage miniMessage = MiniMessage.miniMessage();

        //make sure item is not ghost
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) {
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) {
            return;
        }

        if (!meta.hasItemName()) {
            if (!meta.hasDisplayName()) {

                return;
            } else {
                meta.itemName(meta.displayName());
            }

        }

        //check if it has the verification tile (last item in inv has a veri tile has PDT key)
        //Get inv
        if (event.getClickedInventory().getItem(35).getItemMeta().getPersistentDataContainer().has(PDTKeys.COINFLIP_GUI, PersistentDataType.STRING)) {
            //Has verification tile
        } else {
            Logger.getLogger("CF").warning("Verification tile not found.");
            return;
        }




        // start setting up buttons
        if (meta.itemName().equals(miniMessage.deserialize("<gold>Create Coinflip"))) {

            // chat request for bet amount
            Player player = (Player) event.getWhoClicked();
            System.out.println("Player: " + player.getName());

            player.sendMessage(miniMessage.deserialize("<green>Enter the bet amount:"));
            awaitingResp.add(player);
            event.getClickedInventory().close();
        }

        if (meta.itemName().equals(miniMessage.deserialize("<green>Next Page"))) {

            //logic to find what bets to be displayed
            String inventoryName = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
            int currentPage = Integer.parseInt(inventoryName.split("  ")[1]);

            List<CoinflipInstance> tobedisplayed = BroKits_Insilicon_Test.getFlips();
            int maxPages = (int) Math.ceil(tobedisplayed.size() / 27.0);

            //reconstructing inventory
            Inventory inv = event.getWhoClicked().getServer().createInventory(null, 36, "Coinflip Menu | Page  " + (currentPage + 1));

            for (int i = 0; i < 35; i++) {
                ItemStack grayGlassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta graymeta = grayGlassPane.getItemMeta();
                if (graymeta != null) {
                    graymeta.itemName(miniMessage.deserialize(" "));
                }
                grayGlassPane.setItemMeta(graymeta);
                inv.setItem(i, grayGlassPane);
            }

            // Verification tile for Anti Dupe (remember vulcan ac vulnerability)
            ItemStack verificationTile = createVerificationTile();
            inv.setItem(35, verificationTile);

            ItemStack clock = new ItemStack(Material.CLOCK);
            ItemMeta clockMeta = clock.getItemMeta();
            clockMeta.itemName(miniMessage.deserialize("<green>Refresh"));
            clock.setItemMeta(clockMeta);

            inv.setItem(31, clock);

            ItemStack notAvail = new ItemStack(Material.BARRIER);
            ItemMeta notAvailMeta = notAvail.getItemMeta();
            notAvailMeta.itemName(miniMessage.deserialize("<red>Not Available"));
            notAvail.setItemMeta(notAvailMeta);

            // Arrow Item Point ->
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta arrowMeta = nextPage.getItemMeta();
            arrowMeta.itemName(miniMessage.deserialize("<green>Next Page"));
            nextPage.setItemMeta(arrowMeta);

            // Arrow Item Point <-
            ItemStack backPage = new ItemStack(Material.ARROW);
            ItemMeta arrowMeta2 = backPage.getItemMeta();
            arrowMeta2.itemName(miniMessage.deserialize("<green>Go back"));
            backPage.setItemMeta(arrowMeta2);

            ItemStack createCF = new ItemStack(Material.GOLD_BLOCK);
            ItemMeta createCFMeta = createCF.getItemMeta();
            createCFMeta.itemName(miniMessage.deserialize("<gold>Create Coinflip"));
            createCF.setItemMeta(createCFMeta);

            inv.setItem(27, createCF);

            int start = currentPage * 27;
            int end = Math.min((currentPage + 1) * 27, tobedisplayed.size());

            for (int i = start; i < end; i++) {
                CoinflipInstance cf = tobedisplayed.get(i);
                ItemStack cfItem = cf.createCoinflipItem();
                if (cf.getPlayer1().equals(event.getWhoClicked())) {
                    ItemMeta cfMeta = cfItem.getItemMeta();
                    List<Component> lore = new ArrayList<>(cfMeta.lore());
                    lore.add(miniMessage.deserialize("<red>Click to remove & refund"));
                    cfMeta.lore(lore);
                    cfItem.setItemMeta(cfMeta);
                }
                inv.setItem(i - start, cfItem);
            }

            if (currentPage < maxPages - 1) {
                inv.setItem(32, nextPage);
            } else {
                inv.setItem(32, notAvail);
            }

            if (currentPage > 0) {
                inv.setItem(30, backPage);
            } else {
                inv.setItem(30, notAvail);
            }

            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(inv);
        }

        if (meta.itemName().equals(miniMessage.deserialize("<green>Go back"))) {

            String inventoryName = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
            int currentPage = Integer.parseInt(inventoryName.split("  ")[1]);

            if (currentPage <= 1) {
                return;
            }

            List<CoinflipInstance> tobedisplayed = BroKits_Insilicon_Test.getFlips();
            int maxPages = (int) Math.ceil(tobedisplayed.size() / 27.0);

            Inventory inv = event.getWhoClicked().getServer().createInventory(null, 36, "Coinflip Menu | Page  " + (currentPage - 1));

            for (int i = 0; i < 35; i++) {
                ItemStack grayGlassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta graymeta = grayGlassPane.getItemMeta();
                if (graymeta != null) {
                    graymeta.itemName(miniMessage.deserialize(" "));
                }
                grayGlassPane.setItemMeta(graymeta);
                inv.setItem(i, grayGlassPane);
            }

            // Verification tile for Anti Dupe (remember vulcan ac vulnerability)
            ItemStack verificationTile = createVerificationTile();
            inv.setItem(35, verificationTile);

            ItemStack clock = new ItemStack(Material.CLOCK);
            ItemMeta clockMeta = clock.getItemMeta();
            clockMeta.itemName(miniMessage.deserialize("<green>Refresh"));
            clock.setItemMeta(clockMeta);

            inv.setItem(31, clock);

            ItemStack notAvail = new ItemStack(Material.BARRIER);
            ItemMeta notAvailMeta = notAvail.getItemMeta();
            notAvailMeta.itemName(miniMessage.deserialize("<red>Not Available"));
            notAvail.setItemMeta(notAvailMeta);

            // Arrow Item Point ->
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta arrowMeta = nextPage.getItemMeta();
            arrowMeta.itemName(miniMessage.deserialize("<green>Next Page"));
            nextPage.setItemMeta(arrowMeta);

            // Arrow Item Point <-
            ItemStack backPage = new ItemStack(Material.ARROW);
            ItemMeta arrowMeta2 = backPage.getItemMeta();
            arrowMeta2.itemName(miniMessage.deserialize("<green>Go back"));
            backPage.setItemMeta(arrowMeta2);

            ItemStack createCF = new ItemStack(Material.GOLD_BLOCK);
            ItemMeta createCFMeta = createCF.getItemMeta();
            createCFMeta.itemName(miniMessage.deserialize("<gold>Create Coinflip"));
            createCF.setItemMeta(createCFMeta);

            inv.setItem(27, createCF);

            int start = (currentPage - 2) * 27;
            int end = Math.min((currentPage - 1) * 27, tobedisplayed.size());

            for (int i = start; i < end; i++) {
                CoinflipInstance cf = tobedisplayed.get(i);
                ItemStack cfItem = cf.createCoinflipItem();
                if (cf.getPlayer1().equals(event.getWhoClicked())) {
                    ItemMeta cfMeta = cfItem.getItemMeta();
                    List<Component> lore = new ArrayList<>(cfMeta.lore());
                    lore.add(miniMessage.deserialize("<red>Click to remove & refund"));
                    cfMeta.lore(lore);
                    cfItem.setItemMeta(cfMeta);
                }
                inv.setItem(i - start, cfItem);
            }

            if (currentPage - 2 < maxPages - 1) {
                inv.setItem(32, nextPage);
            } else {
                inv.setItem(32, notAvail);
            }

            if (currentPage - 2 > 0) {
                inv.setItem(30, backPage);
            } else {
                inv.setItem(30, notAvail);
            }

            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(inv);
        }

        if (clickedItem.getType() == Material.GOLD_INGOT) {
            String uid = meta.getPersistentDataContainer().get(PDTKeys.COINFLIP_UID, PersistentDataType.STRING);

            if (uid != null) {

                CoinflipInstance cf = BroKits_Insilicon_Test.getFlips().stream().filter(c -> c.getUid().equals(uid)).findFirst().orElse(null);

                if (cf == null) {
                    Logger.getLogger("CF").warning("Could not find coinflip instance with UID: " + uid);
                    return;
                }

                Player player = (Player) event.getWhoClicked();

                Economy economy = BroKits_Insilicon_Test.getEconomy();

                if (cf.getPlayer1().equals(player)) {

                    if (economy != null) {
                        economy.depositPlayer(player, cf.getBetAmount());
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Refunded: $" + cf.getBetAmount()));
                    } else {
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<red>An error occurred. We will be fixing this shortly!"));
                        Logger.getLogger("CF").warning("Economy plugin not found.");
                        return;
                    }

                    BroKits_Insilicon_Test.getFlips().remove(cf);
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Coinflip removed. Your money of $" + cf.getBetAmount() + " was refunded."));

                } else {

                    if (economy != null) {
                        double playerBalance = economy.getBalance(player);
                        if (playerBalance < cf.getBetAmount()) {
                            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You do not have enough money to join this coinflip."));
                            return;
                        } else {
                            economy.withdrawPlayer(player, cf.getBetAmount());
                            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Bet amount of $" + cf.getBetAmount() + " deducted from your balance."));
                        }
                    } else {
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<red>An error occurred. We will be fixing this shortly!"));
                        Logger.getLogger("CF").warning("Economy plugin not found.");
                        return;
                    }

                    cf.setPlayer2(player);
                    cf.flipCoin();
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have joined the coinflip."));
                }

                // Update the page
                int currentPage = Integer.parseInt(PlainTextComponentSerializer.plainText().serialize(event.getView().title()).split("  ")[1]);
                List<CoinflipInstance> tobedisplayed = BroKits_Insilicon_Test.getFlips();
                int maxPages = (int) Math.ceil(tobedisplayed.size() / 27.0);

                Inventory inv = player.getServer().createInventory(null, 36, "Coinflip Menu | Page  " + currentPage);

                for (int i = 0; i < 35; i++) {
                    ItemStack grayGlassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                    ItemMeta graymeta = grayGlassPane.getItemMeta();
                    if (graymeta != null) {
                        graymeta.itemName(MiniMessage.miniMessage().deserialize(" "));
                    }
                    grayGlassPane.setItemMeta(graymeta);
                    inv.setItem(i, grayGlassPane);
                }

                // Verification tile for Anti Dupe (remember vulcan ac vulnerability)
                ItemStack verificationTile = createVerificationTile();
                inv.setItem(35, verificationTile);

                ItemStack clock = new ItemStack(Material.CLOCK);
                ItemMeta clockMeta = clock.getItemMeta();
                clockMeta.itemName(MiniMessage.miniMessage().deserialize("<green>Refresh"));
                clock.setItemMeta(clockMeta);

                inv.setItem(31, clock);

                ItemStack notAvail = new ItemStack(Material.BARRIER);
                ItemMeta notAvailMeta = notAvail.getItemMeta();
                notAvailMeta.itemName(MiniMessage.miniMessage().deserialize("<red>Not Available"));
                notAvail.setItemMeta(notAvailMeta);

                // Arrow Item Point ->
                ItemStack nextPage = new ItemStack(Material.ARROW);
                ItemMeta arrowMeta = nextPage.getItemMeta();
                arrowMeta.itemName(MiniMessage.miniMessage().deserialize("<green>Next Page"));
                nextPage.setItemMeta(arrowMeta);

                // Arrow Item Point <-
                ItemStack backPage = new ItemStack(Material.ARROW);
                ItemMeta arrowMeta2 = backPage.getItemMeta();
                arrowMeta2.itemName(MiniMessage.miniMessage().deserialize("<green>Go back"));
                backPage.setItemMeta(arrowMeta2);

                ItemStack createCF = new ItemStack(Material.GOLD_BLOCK);
                ItemMeta createCFMeta = createCF.getItemMeta();
                createCFMeta.itemName(MiniMessage.miniMessage().deserialize("<gold>Create Coinflip"));
                createCF.setItemMeta(createCFMeta);

                inv.setItem(27, createCF);

                int start = (currentPage - 1) * 27;
                int end = Math.min(currentPage * 27, tobedisplayed.size());

                for (int i = start; i < end; i++) {
                    CoinflipInstance cfItemInstance = tobedisplayed.get(i);
                    ItemStack cfItem = cfItemInstance.createCoinflipItem();
                    ItemMeta cfMeta = cfItem.getItemMeta();
                    if (cfItemInstance.getPlayer1().equals(player)) {
                        List<Component> lore = cfMeta.lore() != null ? new ArrayList<>(cfMeta.lore()) : new ArrayList<>();
                        lore.add(MiniMessage.miniMessage().deserialize("<red>Click to remove & refund"));
                        cfMeta.lore(lore);
                        cfItem.setItemMeta(cfMeta);
                    }
                    inv.setItem(i - start, cfItem);
                }

                if (currentPage < maxPages) {
                    inv.setItem(32, nextPage);
                } else {
                    inv.setItem(32, notAvail);
                }

                if (currentPage > 1) {
                    inv.setItem(30, backPage);
                } else {
                    inv.setItem(30, notAvail);
                }

                // Replace the clicked item with a gray stained glass pane
                ItemStack grayGlassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta graymeta = grayGlassPane.getItemMeta();
                if (graymeta != null) {
                    graymeta.itemName(MiniMessage.miniMessage().deserialize(" "));
                }
                grayGlassPane.setItemMeta(graymeta);
                event.getInventory().setItem(event.getSlot(), grayGlassPane);

                player.closeInventory();
                player.openInventory(inv);
            } else {
                Logger.getLogger("CF").warning("Could not find coinflip instance with UID: " + uid);
            }
        }

        if (meta.itemName().equals(miniMessage.deserialize("<green>Refresh"))) {
            // Get current page
            String inventoryName = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
            int currentPage = Integer.parseInt(inventoryName.split("  ")[1]);

            List<CoinflipInstance> tobedisplayed = BroKits_Insilicon_Test.getFlips();
            int maxPages = (int) Math.ceil(tobedisplayed.size() / 27.0);

            Inventory inv = event.getWhoClicked().getServer().createInventory(null, 36, "Coinflip Menu | Page  " + currentPage);

            for (int i = 0; i < 35; i++) {
                ItemStack grayGlassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta graymeta = grayGlassPane.getItemMeta();
                if (graymeta != null) {
                    graymeta.itemName(miniMessage.deserialize(" "));
                }
                grayGlassPane.setItemMeta(graymeta);
                inv.setItem(i, grayGlassPane);
            }

            // Verification tile for Anti Dupe (remember vulcan ac vulnerability)
            ItemStack verificationTile = createVerificationTile();
            inv.setItem(35, verificationTile);

            ItemStack clock = new ItemStack(Material.CLOCK);
            ItemMeta clockMeta = clock.getItemMeta();
            clockMeta.itemName(miniMessage.deserialize("<green>Refresh"));
            clock.setItemMeta(clockMeta);

            inv.setItem(31, clock);

            ItemStack notAvail = new ItemStack(Material.BARRIER);
            ItemMeta notAvailMeta = notAvail.getItemMeta();
            notAvailMeta.itemName(miniMessage.deserialize("<red>Not Available"));
            notAvail.setItemMeta(notAvailMeta);

            // Arrow Item Point ->
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta arrowMeta = nextPage.getItemMeta();
            arrowMeta.itemName(miniMessage.deserialize("<green>Next Page"));
            nextPage.setItemMeta(arrowMeta);

            // Arrow Item Point <-
            ItemStack backPage = new ItemStack(Material.ARROW);
            ItemMeta arrowMeta2 = backPage.getItemMeta();
            arrowMeta2.itemName(miniMessage.deserialize("<green>Go back"));
            backPage.setItemMeta(arrowMeta2);

            ItemStack createCF = new ItemStack(Material.GOLD_BLOCK);
            ItemMeta createCFMeta = createCF.getItemMeta();
            createCFMeta.itemName(miniMessage.deserialize("<gold>Create Coinflip"));
            createCF.setItemMeta(createCFMeta);

            inv.setItem(27, createCF);

            int start = (currentPage - 1) * 27;
            int end = Math.min(currentPage * 27, tobedisplayed.size());

            for (int i = start; i < end; i++) {
                CoinflipInstance cfItemInstance = tobedisplayed.get(i);
                ItemStack cfItem = cfItemInstance.createCoinflipItem();
                if (cfItemInstance.getPlayer1().equals(event.getWhoClicked())) {
                    ItemMeta cfMeta = cfItem.getItemMeta();
                    List<Component> lore = new ArrayList<>(cfMeta.lore());
                    lore.add(miniMessage.deserialize("<red>Click to remove & refund"));
                    cfMeta.lore(lore);
                    cfItem.setItemMeta(cfMeta);
                }
                inv.setItem(i - start, cfItem);
            }

            if (currentPage < maxPages) {
                inv.setItem(32, nextPage);
            } else {
                inv.setItem(32, notAvail);
            }

            if (currentPage > 1) {
                inv.setItem(30, backPage);
            } else {
                inv.setItem(30, notAvail);
            }

            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(inv);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {


        if (!awaitingResp.contains(event.getPlayer())) {
            return;
        }
        event.setCancelled(true);
        MiniMessage miniMessage = MiniMessage.miniMessage();


        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        int amount = 0;
        try {
            amount = Integer.parseInt(message);
        } catch (NumberFormatException e) {
            event.getPlayer().sendMessage(miniMessage.deserialize("<red>Invalid number."));
            awaitingResp.remove(event.getPlayer());
            return;
        }

        amount = (int) Math.round(amount);

        if (amount < 1) {
            event.getPlayer().sendMessage(miniMessage.deserialize("<red>Invalid number. Can't be under 1"));
            awaitingResp.remove(event.getPlayer());
            return;
        }

        Economy economy = BroKits_Insilicon_Test.getEconomy();
        if (economy == null) {
            event.getPlayer().sendMessage(miniMessage.deserialize("<red>Could not find economy plugin."));
            awaitingResp.remove(event.getPlayer());
            return;
        }

        if (economy.getBalance(event.getPlayer()) < amount) {
            event.getPlayer().sendMessage(miniMessage.deserialize("<red>You do not have enough money."));
            awaitingResp.remove(event.getPlayer());
            return;
        }

        //Remove money from player
        economy.withdrawPlayer(event.getPlayer(), amount);

        event.getPlayer().sendMessage(miniMessage.deserialize("<green>Creating coinflip with bet amount: $" + amount));


        CoinflipInstance cf = new CoinflipInstance(event.getPlayer(), amount);
        BroKits_Insilicon_Test.getFlips().add(cf);

        awaitingResp.remove(event.getPlayer());


    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
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
