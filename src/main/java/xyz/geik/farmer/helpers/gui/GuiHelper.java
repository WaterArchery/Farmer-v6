package xyz.geik.farmer.helpers.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.geik.farmer.Main;
import xyz.geik.farmer.helpers.Settings;
import xyz.geik.farmer.model.Farmer;
import xyz.geik.farmer.model.FarmerLevel;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *  All guis using this method for
 *  items and all items located here.
 */
public class GuiHelper {

    /**
     * Filler item of guis.
     * Filler item basically fills empty slots of gui
     *
     * @return
     */
    public static ItemStack getFiller() {
        ItemStack item;
        // If enabled
        if (Main.getLangFile().getBoolean("guiFiller.use")) {
            // Loads item
            try {
                item = new ItemStack(Material.getMaterial(Main.getLangFile().getString("guiFiller.material")), 1);
            }
            // If there is any issue of creating item
            catch (Exception e) {
                // It can because of version material differences
                // And old version template item
                if (Main.isOldVersion())
                    item = new ItemStack(Material.getMaterial("STAINED_GLASS_PANE"), 1, (short) 7);
                // Otherwise material can be wrong
                // So this is template item of filler
                else
                    item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
            }
        }
        else
            item = new ItemStack(Material.AIR);
        return item;
    }

    /**
     * If item has skull it get item as head with
     * custom head data. Otherwise check for material and
     * get item with a material.
     */
    public static @NotNull ItemStack getItem(String path) {
        ItemStack result;
        // If item is skull instead of material based item
        if (Main.getLangFile().contains(path + ".skull")) {
            try {
                // Old version skull material
                if (Main.isOldVersion())
                    result = new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
                else
                    result = new ItemStack(Material.getMaterial("PLAYER_HEAD"), 1);
                SkullMeta meta = (SkullMeta) result.getItemMeta();
                assert meta != null;
                // GameProfile, Filed etc. used mojang lib for catch player skull
                GameProfile profile = new GameProfile(UUID.randomUUID(), null);
                profile.getProperties().put("textures", new Property("textures", Main.getLangFile().getString(path + ".skull")));
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
                result.setItemMeta(meta);
            } catch (Exception e) {
                result = new ItemStack(Material.STONE, 1);
            }
        }
        // If item is material based something
        else {
            try {
                result = new ItemStack(Material.getMaterial(Main.getLangFile().getString(path + ".material")));
            }
            // If material or any other thing wrong
            catch (Exception e) {
                result = new ItemStack(Material.STONE, 1);
            }
        }
        ItemMeta meta = result.getItemMeta();
        if (Main.getLangFile().contains(path + ".lore"))
            meta.setLore(Main.getLangFile().getTextList(path + ".lore"));
        meta.setDisplayName(Main.getLangFile().getText(path + ".name"));
        result.setItemMeta(meta);
        return result;
    }

    /**
     * GuiElement creator simple way
     * for templates
     *
     * @param path
     * @param key
     * @return
     */
    @Contract("_, _ -> new")
    public static @NotNull GuiElement createGuiElement(String path, char key) {
        return new StaticGuiElement(key,
                GuiHelper.getItem(path),
                1,
                click -> {return true;});
    }

    /**
     * Previous page item creator
     *
     * @return
     */
    @Contract(" -> new")
    public static @NotNull GuiElement createPreviousPage() {
        return new GuiPageElement('p',
                new ItemStack(Material.ARROW),
                GuiPageElement.PageAction.PREVIOUS,
                Main.getLangFile().getString("previousPage.name")
        );
    }

    /**
     * Next page item creator
     *
     * @return
     */
    @Contract(" -> new")
    public static @NotNull GuiElement createNextPage() {
        return new GuiPageElement('n',
                new ItemStack(Material.ARROW),
                GuiPageElement.PageAction.NEXT,
                Main.getLangFile().getString("nextPage.name")
        );
    }

    /**
     * Manage gui item which located on
     * farmer main gui.
     *
     * @param farmer
     * @return
     */
    public static @NotNull ItemStack getManageItemOnMain(Farmer farmer) {
        ItemStack manage = GuiHelper.getItem("Gui.manage");
        ItemMeta manageMeta = manage.getItemMeta();
        manageMeta.setLore(manageMeta.getLore().stream().map(key -> {
            return key.replace("{level}", String.valueOf(FarmerLevel.getAllLevels().indexOf(farmer.getLevel()) +1))
                    .replace("{capacity}", String.valueOf(farmer.getLevel().getCapacity()))
                    .replace("{tax}", String.valueOf(farmer.getLevel().getTax()));
        }).collect(Collectors.toList()));
        manage.setItemMeta(manageMeta);
        return manage;
    }

    /**
     * Lore replacer for manager menu status item.
     * Changing status to toggleON or toggleOFF value.
     *
     * @param status
     * @return
     */
    public static @NotNull ItemStack getStatusItem(int status) {
        ItemStack statusItem = GuiHelper.getItem("manageGui.closeFarmer");
        ItemMeta meta = statusItem.getItemMeta();
        meta.setLore(meta.getLore().stream().map(key -> {
            return key.replace("{status}",
                    (status == 1) ? Main.getLangFile().getText("toggleON")
                            : Main.getLangFile().getText("toggleOFF"));
        }).collect(Collectors.toList()));
        statusItem.setItemMeta(meta);
        return statusItem;
    }

    /**
     * Creates level item which is can be in max level.
     * So crates it with checking if farmer in max level,
     * or can be upgradeable and also replacing placeholder keys
     *
     * @param farmer
     * @return
     */
    public static @NotNull ItemStack getLevelItem(@NotNull Farmer farmer){
        int level = FarmerLevel.getAllLevels().indexOf(farmer.getLevel())+1;
        long capacity = farmer.getLevel().getCapacity();
        boolean isMax = FarmerLevel.getAllLevels().indexOf(farmer.getLevel()) == FarmerLevel.getAllLevels().size()-1;
        ItemStack result;
        FarmerLevel nextLevel;
        // In max level
        if (isMax) {
            result = getItem("manageGui.inMaxLevel");
            nextLevel = FarmerLevel.getAllLevels().get(level-1);
        }
        // Can upgradeable
        else {
            result = getItem("manageGui.upgradeNext");
            nextLevel = FarmerLevel.getAllLevels().get(level);
        }

        ItemMeta meta = result.getItemMeta();
        // Max level meta lore update
        if (isMax) {
            meta.setLore(meta.getLore().stream().map(key -> {
                return key.replace("{level}", String.valueOf(level))
                        .replace("{capacity}", String.valueOf(capacity));
            }).collect(Collectors.toList()));
        }
        // Upgradeable lore update
        else {
            meta.setLore(meta.getLore().stream().map(key -> {
                return key.replace("{level}", String.valueOf(level))
                        .replace("{capacity}", String.valueOf(capacity))
                        .replace("{max_level}", String.valueOf(FarmerLevel.getAllLevels().size()))
                        .replace("{next_level}", String.valueOf(level+1))
                        .replace("{next_capacity}", String.valueOf(nextLevel.getCapacity()))
                        .replace("{req_money}", String.valueOf(nextLevel.getReqMoney()));
            }).collect(Collectors.toList()));
        }
        meta.setDisplayName(meta.getDisplayName().replace("{level}", String.valueOf(level)));
        result.setItemMeta(meta);
        return result;
    }

    /**
     * Buy item in BuyGui
     *
     * @return
     */
    public static @NotNull ItemStack getBuyItem() {
        ItemStack result = getItem("buyGui.item");
        ItemMeta meta = result.getItemMeta();
        meta.setLore(meta.getLore().stream().map(key -> {
            return key.replace("{price}", String.valueOf(Settings.farmerPrice));
        }).collect(Collectors.toList()));
        result.setItemMeta(meta);
        return result;
    }
}