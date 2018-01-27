package com.andrei1058.bedwars.arena;

import org.bukkit.scheduler.BukkitRunnable;

public class Rotate extends BukkitRunnable {
    @Override
    public void run() {
        for (OreGenerator h : OreGenerator.getRotation()){
            h.rotate();
        }
    }
}
