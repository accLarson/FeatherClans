package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.*;

public class DatabaseManager {

    private HikariDataSource source;
    private final FeatherClans plugin;
    private final boolean isUseMySQL;

    public DatabaseManager(FeatherClans plugin) {
        this.plugin = plugin;
        source = new HikariDataSource();
        this.isUseMySQL = this.plugin.getFeatherClansConfig().isMysqlEnabled();
        this.initConnection();
        this.initMySQLTables();
    }

    public void close() {
        if(!source.isClosed()) source.close();
    }

    private void initConnection() {
        if (this.isUseMySQL) {
            this.initMySQLConnection();
        } else {
            plugin.getLogger().warning("MySQL database is not configured - plugin disabled. Configure values and restart.");
            plugin.disable();
        }
    }

    public Connection getConnection() throws SQLException {
        return source.getConnection();
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

    private boolean columnExists(String table, String column) {
        try (Connection connection = this.getConnection()) {
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getColumns(null, null, table, column);
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Unable to check if column exists: " + column);
            return false;
        }
    }

    private void createTableIfNotExists(String table, String primaryColumn, String primaryColumnDefinition) {
        if (!existsTable(table)) {
            String query = String.format("CREATE TABLE IF NOT EXISTS `%s` (`%s` %s)", table, primaryColumn, primaryColumnDefinition);
            try(Connection connection = this.getConnection()) {
                connection.createStatement().execute(query);
                plugin.getLog().info(String.format("Created table '%s' with primary key '%s'", table, primaryColumn));
            } catch(SQLException e) {
                plugin.getLog().severe(String.format("Failed to create base `%s` table.", table));
            }
        }
    }

    private void addColumnIfNotExists(String table, String column, String columnDefinition) {
        if (!columnExists(table, column)) {
            try (Connection connection = this.getConnection()) {
                String query = String.format("ALTER TABLE `%s` ADD COLUMN `%s` %s", table, column, columnDefinition);
                connection.createStatement().execute(query);
                plugin.getLog().info(String.format("Added column '%s' to table '%s'", column, table));
            } catch (SQLException e) {
                plugin.getLog().severe(String.format("Failed to add column '%s' to table '%s'", column, table));
            }
        }
    }

    private void initMySQLTables() {

        // Create clans Table
        createTableIfNotExists("clans", "id", "INTEGER PRIMARY KEY AUTO_INCREMENT");
        addColumnIfNotExists("clans", "banner", "TEXT NOT NULL");
        addColumnIfNotExists("clans", "tag", "VARCHAR(255) NOT NULL");
        addColumnIfNotExists("clans", "colored_tag", "VARCHAR(255) NULL");
        addColumnIfNotExists("clans", "home", "TEXT NULL");
        addColumnIfNotExists("clans", "chestplate", "TEXT NULL");
        addColumnIfNotExists("clans", "leggings", "TEXT NULL");
        addColumnIfNotExists("clans", "boots", "TEXT NULL");
        addColumnIfNotExists("clans", "leader_uuid", "VARCHAR(255) NOT NULL");
        addColumnIfNotExists("clans", "last_activity_date", "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
        addColumnIfNotExists("clans", "created_date", "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");

        // Create clan_members Table
        createTableIfNotExists("clan_members", "id", "INTEGER PRIMARY KEY AUTO_INCREMENT");
        addColumnIfNotExists("clan_members", "mojang_uuid", "VARCHAR(255) NOT NULL");
        addColumnIfNotExists("clan_members", "clan_id", "INTEGER NOT NULL");
        addColumnIfNotExists("clan_members", "is_officer", "BOOLEAN NOT NULL DEFAULT FALSE");
        addColumnIfNotExists("clan_members", "last_seen_date", "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
        addColumnIfNotExists("clan_members", "join_date", "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");


        // Create clan_alliances Table
        createTableIfNotExists("clan_alliances", "id", "INTEGER PRIMARY KEY AUTO_INCREMENT");
        addColumnIfNotExists("clan_alliances", "clan_1", "INTEGER NOT NULL");
        addColumnIfNotExists("clan_alliances", "clan_2", "INTEGER NOT NULL");
        addColumnIfNotExists("clan_alliances", "ally_home", "TEXT NULL");
        addColumnIfNotExists("clan_alliances", "created_date", "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
    }
}