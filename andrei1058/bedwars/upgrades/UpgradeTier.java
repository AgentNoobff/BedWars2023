package com.andrei1058.bedwars.upgrades;

import com.andrei1058.bedwars.arena.BedWarsTeam;
import com.andrei1058.bedwars.configuration.Language;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static com.andrei1058.bedwars.Main.*;
import static com.andrei1058.bedwars.configuration.Language.*;

public class UpgradeTier {

    private String name, currency;
    private List<UpgradeAction> actions;
    private int cost;
    private ItemStack itemStack;

    public UpgradeTier(String name, List<UpgradeAction> actions, int cost, String currency, ItemStack itemStack) {
        this.name = name;
        this.actions = new ArrayList<>(actions);
        this.cost = cost;
        this.currency = currency;
        ItemMeta im = itemStack.getItemMeta();
        im.addEnchant(Enchantment.LURE, 1, true);
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(im);
        this.itemStack = itemStack;
        plugin.debug("loading new UpgradeTier: "+getName());
    }

    public String getName() {
        return name;
    }

    public ItemStack getItemStack(Player p, String path, TeamUpgrade tu, BedWarsTeam bwt) {
        ItemMeta im = itemStack.getItemMeta();
        im.setDisplayName(getMsg(p, path+"."+getName()+".name"));
        List<String> lore = new ArrayList<>();
        for (String s : getList(p, path+"."+getName()+".lore")){
            if (s.contains("{loreFooter}")){
                if (isHighest(bwt, tu)){
                    s=s.replace("{loreFooter}", getMsg(p, loreFooterUnlocked));
                } else if (hasEnoughMoney(p)){
                    s=s.replace("{loreFooter}", getMsg(p, loreFooterClick));
                } else {
                    s=s.replace("{loreFooter}", getMsg(p, loreFooterInsuff));
                }
            }
            if (getCost() == 1){
                s=s.replace("{cost}", String.valueOf(cost)).replace("{currency}", getMsg(p, "meaning."+getCurrency()));
            } else {
                s=s.replace("{cost}", String.valueOf(cost)).replace("{currency}", getMsg(p, lang.pluralPath+"."+getCurrency()));
            }
            lore.add(s);
        }
        im.setLore(lore);
        ItemStack i = itemStack.clone();
        i.setItemMeta(im);
        return i;
    }

    public boolean buy(Player p, BedWarsTeam bwt){
        int money = 0;
        Material currency = null;
        if (getCurrency().equalsIgnoreCase("iron")) {
            currency = Material.IRON_INGOT;
        }
        if (getCurrency().equalsIgnoreCase("gold")) {
            currency = Material.GOLD_INGOT;
        }
        if (getCurrency().equalsIgnoreCase("emerald")) {
            currency = Material.EMERALD;
        }
        if (getCurrency().equalsIgnoreCase("diamond")) {
            currency = Material.DIAMOND;
        }
        if (getCurrency().equalsIgnoreCase("vault")) {
            if (!getEconomy().isEconomy()) {
                p.sendMessage("§cThis item requires vault support!");
                return false;
            } else {
                money = (int) getEconomy().getMoney(p);
            }
        } else if (currency != null) {
            for (ItemStack i : p.getInventory().getContents()) {
                if (i == null) continue;
                if (i.getType() == null) continue;
                if (i.getType() == Material.AIR) continue;
                if (i.getType() == currency) {
                    money += i.getAmount();
                }
            }
        }
        if (money < getCost()) {
            p.playSound(p.getLocation(), nms.insufficientMoney(), 1f, 1f);
            p.sendMessage(getMsg(p, Language.insufficientMoney).replace("{currency}", getMsg(p, "meaning." + getCurrency().toLowerCase())).replace("{amount}", String.valueOf(getCost() - money)));
            p.closeInventory();
            return false;
        }
        boolean done = false;
        if (currency == null) {
            getEconomy().buyAction(p, getCost());
        } else {
            int costt = cost;
            for (ItemStack i : p.getInventory().getContents()) {
                if (done) continue;
                if (i == null) continue;
                if (i.getType() == null) continue;
                if (i.getType() == Material.AIR) continue;
                if (i.getType() == currency) {
                    if (i.getAmount() <= costt) {
                        costt -= i.getAmount();
                        p.getInventory().remove(i);
                        p.updateInventory();
                        done = true;
                    } else {
                        i.setAmount(i.getAmount() - costt);
                        p.updateInventory();
                        done = true;
                    }
                }
            }
        }
        p.playSound(p.getLocation(), nms.bought(), 1f, 1f);
        for (UpgradeAction a : actions){
            a.execute(bwt);
        }
        p.closeInventory();
        //todo mesaj cu bought
        return true;
    }

    public boolean hasEnoughMoney(Player p){
        switch (currency){
            case "vault":
                return getCost()-plugin.getEconomy().getMoney(p)>=0;
            case "iron":
                return getCost()-countItemStackAmount(p, Material.IRON_INGOT)>=0;
            case "gold":
                return getCost()-countItemStackAmount(p, Material.GOLD_INGOT)>=0;
            case "emerald":
                return getCost()-countItemStackAmount(p, Material.EMERALD)>=0;
            case "diamond":
                return getCost()-countItemStackAmount(p, Material.DIAMOND)>=0;
            default:
                return false;
        }
    }

    public boolean isHighest(BedWarsTeam tm, TeamUpgrade tu){
        return tu.getTiers().size()-1 == (tm.getUpgradeTier().containsKey(tu.getSlot()) ? tm.getUpgradeTier().get(tu.getSlot()) : 0);
    }

    public boolean isOne(BedWarsTeam tm, TeamUpgrade tu){
        return 1 == (tm.getUpgradeTier().containsKey(tu.getSlot()) ? tm.getUpgradeTier().get(tu.getSlot()) : 0);
    }

    private static int countItemStackAmount(Player p, Material m){
        int i = 0;
        for (ItemStack is : p.getInventory().getContents()){
            if (is == null) continue;
            if (is.getType() == Material.AIR) continue;
            if (is.getType() == m){
                i += is.getAmount();
            }
        }
        return i;
    }

    public String getCurrency() {
        return currency.toLowerCase();
    }

    public int getCost() {
        return cost;
    }
}
