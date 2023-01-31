package xyz.geik.farmer.listeners.backend;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.geik.farmer.Main;
import xyz.geik.farmer.helpers.Settings;
import xyz.geik.farmer.model.Farmer;
import xyz.geik.farmer.model.inventory.FarmerInv;

/**
 * Main event of farmer which collect items to storage
 */
public class ItemEvent implements Listener {

    @EventHandler(priority= EventPriority.HIGHEST)
    public void itemSpawnEvent(@NotNull ItemSpawnEvent e) {
        /**
         *
         * Has Item in farmer
         * Item don't have meta
         * Checked player drop
         * Checked World
         * Checked if has farmer on location
         * Checked if farmer closed
         */
        // Checks world suitable for farmer
        if (!Settings.allowedWorlds.contains(e.getLocation().getWorld().getName()))
            return;
        // Checks if player dropped or naturally dropped
        // if settings contain Cancel player drop then it cancel collecting it.
        if (Settings.ignorePlayerDrop && e.getEntity().getPickupDelay() >= 39)
            return;

        // Cancel if item has meta because there can be unique items
        // which used for something else and it would turn to basic item if farmer collects.
        ItemStack item = new ItemStack(e.getEntity().getItemStack());
        if (item.hasItemMeta())
            return;

        // Checks farmer contain that item also supports old version and newer versions.
        if (!(FarmerInv.defaultItems.stream().anyMatch(itm -> (itm.getName().equalsIgnoreCase(item.getType().name())))
                || Main.isOldVersion() && FarmerInv.defaultItems.stream().anyMatch(itm -> (
                        itm.getName().contains("-")
                                && itm.getName().split("-")[0].equalsIgnoreCase(item.getType().name())
                                    && itm.getName().split("-")[1].equalsIgnoreCase(String.valueOf(item.getDurability()))))))
            return;

        // Checks item dropped in region of a player
        // And checks region owner has a farmer
        String regionID = Main.getIntegration().getRegionID(e.getLocation());
        if (regionID == null || !Main.getFarmers().containsKey(regionID))
            return;

        // Checks farmer in collection state
        Farmer farmer = Main.getFarmers().get(regionID);
        if (farmer.getState() == 0)
            return;

        long left = -1;

        // Filling stock and making amount of item x if stock fills
        // then drop it back again
        int data = 0;
        if (Main.isOldVersion()) {
            data = item.getDurability();
            if (data != 0)
                left = farmer.getInv().sumItemAmount(item.getType().name() + "-" + data, item.getAmount());
        }

        if (left == -1)
            left = farmer.getInv().sumItemAmount(item.getType().name(), item.getAmount());

        if (left != 0)
            item.setAmount((int) left);

        else
            e.setCancelled(true);
    }
}