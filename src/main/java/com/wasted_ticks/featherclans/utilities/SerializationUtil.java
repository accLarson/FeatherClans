package com.wasted_ticks.featherclans.utilities;

import org.bukkit.Location;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SerializationUtil {

    public static String stackToString(ItemStack stack) {

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        try {
            BukkitObjectOutputStream bukkitOutputStream = new BukkitObjectOutputStream(byteOutputStream);
            bukkitOutputStream.writeObject(stack);
            bukkitOutputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return Base64Coder.encodeLines(byteOutputStream.toByteArray());
    }

    public static ItemStack stringToStack(String string) {

        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(Base64Coder.decodeLines(string));
        ItemStack stack = null;

        try {
            BukkitObjectInputStream bukkitInputStream = new BukkitObjectInputStream(byteInputStream);
            stack = (ItemStack) bukkitInputStream.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
        return stack;
    }

    public static String locationToString(Location location) {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        try {
            BukkitObjectOutputStream bukkitOutputStream = new BukkitObjectOutputStream(byteOutputStream);
            bukkitOutputStream.writeObject(location);
            bukkitOutputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return Base64Coder.encodeLines(byteOutputStream.toByteArray());

    }

    public static Location stringToLocation(String string) {
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(Base64Coder.decodeLines(string));
        Location location = null;

        try {
            BukkitObjectInputStream bukkitInputStream = new BukkitObjectInputStream(byteInputStream);
            location = (Location) bukkitInputStream.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
        return location;
    }

    public static String bannerBlockToString(Banner banner) {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        try {
            BukkitObjectOutputStream bukkitOutputStream = new BukkitObjectOutputStream(byteOutputStream);
            bukkitOutputStream.writeObject(banner);
            bukkitOutputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return Base64Coder.encodeLines(byteOutputStream.toByteArray());

    }

    public static Banner stringToBannerBlock(String string) {
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(Base64Coder.decodeLines(string));
        Banner banner = null;

        try {
            BukkitObjectInputStream bukkitInputStream = new BukkitObjectInputStream(byteInputStream);
            banner = (Banner) bukkitInputStream.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
        return banner;
    }

    public static String armorStandToString(ArmorStand armorStand) {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        try {
            BukkitObjectOutputStream bukkitOutputStream = new BukkitObjectOutputStream(byteOutputStream);
            bukkitOutputStream.writeObject(armorStand);
            bukkitOutputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return Base64Coder.encodeLines(byteOutputStream.toByteArray());

    }

    public static ArmorStand stringToArmorStand(String string) {
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(Base64Coder.decodeLines(string));
        ArmorStand armorStand = null;

        try {
            BukkitObjectInputStream bukkitInputStream = new BukkitObjectInputStream(byteInputStream);
            armorStand = (ArmorStand) bukkitInputStream.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
        return armorStand;
    }


    public static String signBlockToString(Sign sign) {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        try {
            BukkitObjectOutputStream bukkitOutputStream = new BukkitObjectOutputStream(byteOutputStream);
            bukkitOutputStream.writeObject(sign);
            bukkitOutputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return Base64Coder.encodeLines(byteOutputStream.toByteArray());

    }

    public static Sign stringToSignBlock(String string) {
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(Base64Coder.decodeLines(string));
        Sign sign = null;

        try {
            BukkitObjectInputStream bukkitInputStream = new BukkitObjectInputStream(byteInputStream);
            sign = (Sign) bukkitInputStream.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
        return sign;
    }


}
