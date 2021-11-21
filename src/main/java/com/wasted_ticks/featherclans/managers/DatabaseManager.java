package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import org.javalite.activejdbc.Base;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {

    private static Connection connection;
    private final FeatherClans plugin;
    private File file;

    public DatabaseManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.initConnection();
        this.initTables();
    }

    public boolean isAttached() {
        return Base.hasConnection();
    }

    public void attachBase() {
        Base.attach(connection);
    }

    public void closeBase() {
        Base.close();
    }

    public Connection getConnection() {
        try {
            if(connection.isClosed()) {
                this.initConnection();
            }
        } catch (SQLException e) {
            plugin.getLog().severe("[FeatherClans] Unable to receive connection.");
        }
        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                Base.close();
                connection.close();
            } catch (SQLException e) {
                plugin.getLog().severe("[FeatherClans] Unable to close DatabaseManager connection.");
            }
        }
    }

    private void initConnection() {
        File folder = this.plugin.getDataFolder();
        if(!folder.exists()) {
            boolean created = folder.mkdir();
            if(!created) {
                plugin.getLog().severe("[FeatherClans] Unable to create plugin data folder.");
            }
        }
        this.file = new File(folder.getAbsolutePath() + File.separator +  "FeatherClans.db");

        try {
            DatabaseManager.connection = DriverManager.getConnection("jdbc:sqlite:" + this.file.getAbsolutePath());
            Base.attach(DatabaseManager.connection);
        } catch (SQLException e) {
            plugin.getLog().severe("[FeatherClans] Unable to initialize DatabaseManager connection.");
        }

    }

    private boolean existsTable(String table) {
        try {
            if(!connection.isClosed()) {
                ResultSet rs = connection.getMetaData().getTables(null, null, table, null);
                return rs.next();
            } else {
                return false;
            }
        } catch (SQLException e) {
            plugin.getLog().severe("[FeatherClans] Unable to query table metadata.");
            return false;
        }

    }

    private void initTables() {
        if(!this.existsTable("clans")) {
            plugin.getLog().info("[FeatherClans] Creating clans table.");
            String query = "CREATE TABLE IF NOT EXISTS `clans` ("
                    + " `id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + " `banner` VARCHAR(255) NOT NULL, "
                    + " `tag` VARCHAR(255) NOT NULL, "
                    + " `home` VARCHAR(255) NULL, "
                    + " `leader_uuid` VARCHAR(255) NOT NULL, "
                    + " `last_activity_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + " `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);";
            try {
                if(!connection.isClosed()) {
                    connection.createStatement().execute(query);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLog().severe("[FeatherClans] Unable to create feather_clans table.");
            }
        }

        if(!this.existsTable("clan_members")) {
            plugin.getLog().info("[FeatherClans] Creating clan_members table.");
            String query = "CREATE TABLE IF NOT EXISTS `clan_members` ("
                    + " `id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + " `mojang_uuid` VARCHAR(255) NOT NULL, "
                    + " `clan_id` INTEGER NOT NULL, "
                    + " `last_seen_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + " `join_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);";
            try {
                if(!connection.isClosed()) {
                    connection.createStatement().execute(query);
                }
            } catch (SQLException e) {
                plugin.getLog().severe("[FeatherClans] Unable to create feather_clan_members table.");
            }
        }
    }
}
