package dev.insilicon.brokitsinsilicontest.CustomClasses;

import dev.insilicon.brokitsinsilicontest.BroKits_Insilicon_Test;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CoinflipInstance {

    private Player player1;
    private Player player2;

    private double betAmount;

    private Economy economy;

    public CoinflipInstance(Player player1, Player player2, double betAmount) {
        this.player1 = player1;
        this.player2 = player2;
        this.betAmount = betAmount;

         economy = BroKits_Insilicon_Test.getEconomy();




    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public double getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(double betAmount) {
        this.betAmount = betAmount;
    }

    public void flipCoin() {

        //If player 2 is null return and throw exception
        if (player2 == null) {
            throw new IllegalStateException("Player 2 is null | Class: CoinflipInstance");
        }

        int winner = (int) (Math.random() * 2) + 1;

        broadcastWinner(winner);

        if (winner == 1) {

            economy.depositPlayer(player1, betAmount * 2);
            player1.sendMessage(MiniMessage.miniMessage().deserialize("<green>You won <yellow>$" + betAmount * 2 + " <green>from the coinflip!"));

        } else if (winner == 2) {

            economy.depositPlayer(player2, betAmount * 2);
            player2.sendMessage(MiniMessage.miniMessage().deserialize("<green>You won <yellow>$" + betAmount * 2 + " <green>from the coinflip!"));

        } else {
            throw new IllegalStateException("Winner is not 1 or 2 | Val: " + winner + " Class: CoinflipInstance");
        }



    }

    public void broadcastWinner(int winner) {
        MiniMessage miniMessage = MiniMessage.miniMessage();

        for (Player player : new Player[] {player1, player2}) {

            if (player == null) {
                continue;
            }

            player.sendMessage(miniMessage.deserialize("<green>---------------------------------------"));
            player.sendMessage(miniMessage.deserialize("<green><bold>                COINFLIP"));
            player.sendMessage(miniMessage.deserialize("<green>                <bold>WINNER      <red><bold>LOSER"));

            Player loser = winner == 1 ? player2 : player1;
            Player winnerplr = winner == 1 ? player1 : player2;

            String spaces = "                    ";
            String centeredUsernames = String.format("<green><bold>%-16s<red><bold>%s", winnerplr.getName(), loser.getName());
            player.sendMessage(miniMessage.deserialize("<green>" + spaces + centeredUsernames));

            player.sendMessage(miniMessage.deserialize("<green>---------------------------------------"));


        }
    }

    public ItemStack createCoinflipItem() {

        MiniMessage miniMessage = MiniMessage.miniMessage();

        //init
        ItemStack cfItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = cfItem.getItemMeta();

        //Lore

        Component loreLine1Comp = miniMessage.deserialize("<gold>Click to join coinflip!");

        Component loreLine2Comp = miniMessage.deserialize("<green>Bet: <yellow>$" + betAmount);


        meta.lore().add(loreLine1Comp);
        meta.lore().add(loreLine2Comp);

        //Extras
        meta.itemName(miniMessage.deserialize("<gold>"+player1.getName()+"'s Coinflip"));


        cfItem.setItemMeta(meta);
        return cfItem;

    }
}
