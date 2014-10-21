/*
 * Copyright (C) 2014 Jacob Dodd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.unlucky4ever.customjoinmessage.config;

import com.unlucky4ever.customjoinmessage.CustomJoinMessage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Manages the configuration for the plugin
 *
 * @since 2.0.0
 * @author unlucky4ever
 * @version 2.0.0
 */
public class ConfigurationLoader {

	private final CustomJoinMessage plugin;
	private final File file;
	private YamlConfiguration yaml = null;

	public ConfigurationLoader(CustomJoinMessage plugin) {
		this.plugin = plugin;
		this.file = new File(this.plugin.getDataFolder(), "config.yml");
		verifyConfig();
	}

	/**
	 * Verifies the values within the configuration, and the file itself
	 *
	 * @since 2.0.0
	 * @version 2.0.0
	 */
	public void verifyConfig() {
		if (this.plugin.getDataFolder().exists()) {
			this.plugin.getDataFolder().mkdir();
		}
		if (!this.file.exists()) {
			this.plugin.saveDefaultConfig();
			this.yaml = YamlConfiguration.loadConfiguration(this.file);
		} else {
			this.yaml = YamlConfiguration.loadConfiguration(this.file);

			if (!this.yaml.isSet("update.check")) this.yaml.set("update.check", true);
			if (!this.yaml.isSet("disable.join-message")) this.yaml.set("disable.join-message", false);
			if (!this.yaml.isSet("disable.leave-message")) this.yaml.set("disable.leave-message", false);
			if (!this.yaml.isSet("disable.kick-message")) this.yaml.set("disable.kick-message", false);
			if (!this.yaml.isSet("use-groups")) this.yaml.set("use-groups", false);
			if (!this.yaml.isSet("custom.default.join-message")) this.yaml.set("custom.default.join-message", "%p was has joined.");
			if (!this.yaml.isSet("custom.default.leave-message")) this.yaml.set("custom.default.leave-message", "%p was left.");
			if (!this.yaml.isSet("custom.default.kick-message")) this.yaml.set("custom.default.kick-message", "%p was kicked.");

			this.saveConfig();
		}
	}

	/**
	 * Saves the current configuration from memory
	 *
	 * @since 2.0.0
	 * @version 2.0.0
	 */
	public void saveConfig() {
		try {
			this.yaml.save(this.file);
		} catch (IOException ex) {
			Logger.getLogger(ConfigurationLoader.class.getName()).log(
					Level.SEVERE, "Error saving configuration file!", ex);
		}
	}

	/**
	 * Gets the configuration file for CustomJoinMessage
	 *
	 * @since 2.0.0
	 * @version 2.0.0
	 *
	 * @return YamlConfiguration file, null if verifyConfig() has not been run
	 */
	public YamlConfiguration getConfig() {
		return this.yaml;
	}

	/**
	 * Gets a string value from the config
	 *
	 * @since 1.3.0
	 * @version 1.3.0
	 *
	 * @param path
	 *            Path to string value
	 * @return String value
	 */
	public synchronized String getString(String path) {
		return this.yaml.getString(path);
	}

	/**
	 * Gets an int value from the config
	 *
	 * @since 2.0.0
	 * @version 2.0.0
	 *
	 * @param path
	 *            Path to int value
	 * @return int value
	 */
	public synchronized int getInt(String path) {
		return this.yaml.getInt(path);
	}

	/**
	 * Gets a boolean value from the config
	 *
	 * @since 2.0.0
	 * @version 2.0.0
	 *
	 * @param path
	 *            Path to boolean value
	 * @return boolean value
	 */
	public synchronized boolean getBoolean(String path) {
		return this.yaml.getBoolean(path);
	}
}
