package dev.insilicon.brokitsinsilicontest.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        System.out.println("Inventory clicked: " + event.getCurrentItem());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        System.out.println("Inventory closed by: " + event.getPlayer().getName());
    }
}
