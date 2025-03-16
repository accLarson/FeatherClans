package com.wasted_ticks.featherclans.utilities;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SerializationUtility {

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
}
