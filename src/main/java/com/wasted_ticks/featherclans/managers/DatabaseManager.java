package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.*;

@SuppressWarnings("SqlNoDataSourceInspection")
public class DatabaseManager {

    private final HikariDataSource source;
    private final FeatherClans plugin;
    private final boolean isUseMySQL;

    public DatabaseManager(FeatherClans plugin) {
        this.plugin = plugin;
        source = new HikariDataSource();
        this.isUseMySQL = this.plugin.getFeatherClansConfig().isMysqlEnabled();
        this.initConnection();
        this.initTables();
    }

    public void close() {
        if(!source.isClosed()) {
            source.close();
        }
    }

    private void initConnection() {
        if (this.isUseMySQL) {
            this.initMySQLConnection();
        } else {
            this.initSQLiteConnection();
        }
    }

    private void initSQLiteConnection() {
        File folder = this.plugin.getDataFolder();
        if (!folder.exists()) {
            boolean created = folder.mkdir();
            if (!created) {
                plugin.getLogger().severe("Unable to create plugin data folder.");
            }
        }
        File file = new File(folder.getAbsolutePath() + File.separator + "FeatherClans.db");
        String url = "jdbc:sqlite:" + file.getAbsolutePath();
        source.setJdbcUrl(url);

        try(Connection connection = this.getConnection()) {
            plugin.getLogger().info("Initialized connection to local SQLite database.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Unable to initialize SQLite connection.");
        }
    }

    private void initMySQLConnection() {
        FeatherClansConfig config = this.plugin.getFeatherClansConfig();

        String host = config.getMysqlHost();
        String port = String.valueOf(config.getMysqlPort());
        String database = config.getMysqlDatabase();
        String username = config.getMysqlUsername();
        String password = config.getMysqlPassword();

        source.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s", host, port, database));
        source.setUsername(username);
        source.setPassword(password);

        try(Connection connection = this.getConnection()) {
            plugin.getLogger().info("Initialized connection to MySQL database.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Unable to initialize MySQL connection.");
            plugin.getLogger().severe("Ensure connection can be made with provided mysql strings.");
        }
    }

    private boolean existsTable(String table) {
        try(Connection connection = this.getConnection()) {
            ResultSet set = connection.getMetaData().getTables(null, null, table, null);
            return set.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Unable to query table metadata.");
            return false;
        }
    }

    private void initTables() {
        if (this.isUseMySQL) {
            this.initMySQLTables();
        } else {
            this.initSQLiteTables();
        }
    }

    private void initSQLiteTables() {
        if (!this.existsTable("clans")) {
            plugin.getLogger().info("Creating `clans` table.");
            String query = "CREATE TABLE IF NOT EXISTS `clans` ("
                    + " `id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + " `banner` TEXT NOT NULL, "
                    + " `tag` VARCHAR(255) NOT NULL, "
                    + " `home` TEXT NULL, "
                    + " `camp` TEXT NULL, "
                    + " `leader_uuid` VARCHAR(255) NOT NULL, "
                    + " `officer_count` INTEGER NOT NULL DEFAULT 0,"
                    + " `ally_id` INTEGER DEFAULT NULL,"
                    + " `last_activity_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + " `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);";
            try(Connection connection = this.getConnection()) {
                connection.createStatement().execute(query);
            } catch(SQLException e) {
                plugin.getLogger().severe("Unable to create `clans` table.");
            }
        }
        if (!this.existsTable("clan_members")) {
            plugin.getLogger().info("Creating `clan_members` table.");
            String query = "CREATE TABLE IF NOT EXISTS `clan_members` ("
                    + " `id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + " `mojang_uuid` VARCHAR(255) NOT NULL, "
                    + " `clan_id` INTEGER NOT NULL, "
                    + " `is_officer` BIT NOT NULL DEFAULT 0"
                    + " `last_seen_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + " `join_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);";
            try(Connection connection = this.getConnection()) {
                connection.createStatement().execute(query);
            } catch(SQLException e) {
                plugin.getLogger().severe("Unable to create `clan_members` table.");
            }
        }
        if (!this.existsTable("clan_kills")) {
            plugin.getLogger().info("Creating `clan_kills` table.");
            String query = "CREATE TABLE IF NOT EXISTS `clan_kills` ("
                    + " `id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + " `killer_id` INTEGER, "
                    + " `victim_id` INTEGER, "
                    + " `date` DATE NOT NULL DEFAULT CURRENT_DATE, "
                    + " FOREIGN KEY (`killer_id`) REFERENCES `clan_members`(`id`), "
                    + " FOREIGN KEY (`victim_id`) REFERENCES `clan_members`(`id`));";
            try(Connection connection = this.getConnection()) {
                connection.createStatement().execute(query);
            } catch(SQLException e) {
                plugin.getLogger().severe("Unable to create `clan_kills` table.");
            }
        }
        if (!this.existsTable("clan_memberships")) {
            plugin.getLogger().info("Creating `clan_memberships` table.");
            String query = "CREATE TABLE IF NOT EXISTS `clan_memberships` ("
                    + " `id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + " `mojang_uuid` VARCHAR(255) NOT NULL, "
                    + " `date` DATE NOT NULL DEFAULT CURRENT_DATE, "
                    + " `tag` VARCHAR(255) NOT NULL);";
            try(Connection connection = this.getConnection()) {
                connection.createStatement().execute(query);
            } catch(SQLException e) {
                plugin.getLogger().severe("Unable to create `clan_memberships` table.");
            }
        }
    }

    private void initMySQLTables() {
        if (!this.existsTable("clans")) {
            plugin.getLogger().info("Creating `clans` table.");
            String query = "CREATE TABLE IF NOT EXISTS `clans` ("
                    + " `id` INTEGER PRIMARY KEY AUTO_INCREMENT, "
                    + " `banner` TEXT NOT NULL, "
                    + " `tag` VARCHAR(255) NOT NULL, "
                    + " `home` TEXT NULL, "
                    + " `camp` TEXT NULL, "
                    + " `leader_uuid` VARCHAR(255) NOT NULL, "
                    + " `officer_count` INTEGER NOT NULL DEFAULT 0,"
                    + " `ally_id` INTEGER DEFAULT NULL,"
                    + " `last_activity_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + " `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + " FOREIGN KEY (`ally_id`) REFERENCES `clans`(`id`));";
            try(Connection connection = this.getConnection()) {
                connection.createStatement().execute(query);
            } catch(SQLException e) {
                plugin.getLogger().severe("Unable to create `clans` table.");
            }
        }
        if (!this.existsTable("clan_members")) {
            plugin.getLogger().info("Creating `clan_members` table.");
            String query = "CREATE TABLE IF NOT EXISTS `clan_members` ("
                    + " `id` INTEGER PRIMARY KEY AUTO_INCREMENT, "
                    + " `mojang_uuid` VARCHAR(255) NOT NULL, "
                    + " `clan_id` INTEGER NOT NULL, "
                    + " `is_officer` BIT NOT NULL DEFAULT 0,"
                    + " `last_seen_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + " `join_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);";
            try(Connection connection = this.getConnection()) {
                connection.createStatement().execute(query);
            } catch(SQLException e) {
                plugin.getLogger().severe("Unable to create `clan_members` table.");
            }
        }
        if (!this.existsTable("clan_kills")) {
            plugin.getLogger().info("Creating `clan_kills` table.");
            String query = "CREATE TABLE IF NOT EXISTS `clan_kills` ("
                    + " `id` INTEGER PRIMARY KEY AUTO_INCREMENT, "
                    + " `killer_id` INTEGER, "
                    + " `victim_id` INTEGER, "
                    + " `date` DATE NOT NULL DEFAULT CURRENT_DATE, "
                    + " FOREIGN KEY (`killer_id`) REFERENCES `clan_members`(`id`), "
                    + " FOREIGN KEY (`victim_id`) REFERENCES `clan_members`(`id`));";
            try(Connection connection = this.getConnection()) {
                connection.createStatement().execute(query);
            } catch(SQLException e) {
                plugin.getLogger().severe("Unable to create `clan_kills` table.");
            }
        }
        if (!this.existsTable("clan_memberships")) {
            plugin.getLogger().info("Creating `clan_memberships` table.");
            String query = "CREATE TABLE IF NOT EXISTS `clan_memberships` ("
                    + " `id` INTEGER PRIMARY KEY AUTO_INCREMENT, "
                    + " `mojang_uuid` VARCHAR(255) NOT NULL, "
                    + " `date` DATE NOT NULL DEFAULT CURRENT_DATE, "
                    + " `tag` VARCHAR(255) NOT NULL);";
            try(Connection connection = this.getConnection()) {
                connection.createStatement().execute(query);
            } catch(SQLException e) {
                plugin.getLogger().severe("Unable to create `clan_memberships` table.");
            }
        }
    }

    public Connection getConnection() throws SQLException {
        return source.getConnection();
    }

}
