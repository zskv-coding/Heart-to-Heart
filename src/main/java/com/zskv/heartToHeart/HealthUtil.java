package com.zskv.heartToHeart;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public class HealthUtil {

    private HealthUtil() {
    }

    public static void applyHearts(Player player, int hearts) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        maxHealth.setBaseValue(hearts * 2.0);

        // don't let current health exceed the new max
        if (player.getHealth() > maxHealth.getValue()) {
            player.setHealth(maxHealth.getValue());
        }
    }
}