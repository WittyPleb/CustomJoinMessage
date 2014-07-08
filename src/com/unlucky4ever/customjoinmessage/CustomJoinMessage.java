package com.unlucky4ever.customjoinmessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mcstats.Metrics;

import com.unlucky4ever.customjoinmessage.listeners.PlayerListener;

public class CustomJoinMessage extends JavaPlugin {
	private static Logger log;
	private String newVersionTitle = "";
	public double newVersion = 0;
	public double currentVersion = 0;
	private String currentVersionTitle = "";
	private Metrics metrics;
	private CustomJoinMessage plugin;
	public static Permission perms = null;
	
	@Override
	public void onDisable() {
		// Remove all service registrations
		getServer().getServicesManager().unregisterAll(this);
		Bukkit.getScheduler().cancelTasks(this);
	}
	
	@Override
	public void onEnable() {
		plugin = this;
		log = this.getLogger();
		currentVersionTitle = getDescription().getVersion().split("-")[0];
		currentVersion = Double.valueOf(currentVersionTitle.replaceFirst("\\.", ""));
		
		// set defaults
		getConfig().addDefault("update-check", true);
		getConfig().addDefault("disable-join-message", false);
		getConfig().addDefault("disable-leave-message", false);
		getConfig().addDefault("disable-kick-message", false);
		getConfig().addDefault("use-groups", false);
		getConfig().addDefault("custom.default.join-message", "%p has joined.");
		getConfig().addDefault("custom.default.leave-message", "%p has left.");
		getConfig().addDefault("custom.default.kick-message", "%p was kicked");
		getConfig().addDefault("custom.users.8f2d51db-6818-4016-b06a-0d0df5fd4d7e.join-message", "%p has joined.");
		getConfig().addDefault("custom.users.8f2d51db-6818-4016-b06a-0d0df5fd4d7e.leave-message", "%p has left.");
		getConfig().addDefault("custom.users.8f2d51db-6818-4016-b06a-0d0df5fd4d7e.kick-message", "%p was kicked");
		getConfig().addDefault("custom.groups.admin.join-message", "%p has joined.");
		getConfig().addDefault("custom.groups.admin.leave-message", "%p has left.");
		getConfig().addDefault("custom.groups.admin.kick-message", "%p was kicked");
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		if (getConfig().getBoolean("use-groups")) {
			if (getServer().getPluginManager().getPlugin("Vault") != null) {
				if (setupPermissions()) {
					log.info("Using: " + perms.getName() + " for permissions!");
				} else {
					log.info("Permission plugin not found, shutting down.");
				}
			} else {
				log.info("Please install Vault to use the groups feature.");
			}
		}
		
		getCommand("cjm").setExecutor(this);
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		
		// Schedule to check the version every 30 minutes for an update. This is to update the most recent
		// version so if an admin reconnects they will be warned about newer versions.
		this.getServer().getScheduler().runTask(this, new Runnable() {
			
			@Override
			public void run() {
				// Programmatically set the default permission value cause Bukkit doesn't handle plugin.yml properly for Load order STARTUP plugins
				org.bukkit.permissions.Permission perm = getServer().getPluginManager().getPermission("cjm.update");
				
				if (perm == null) {
					perm = new org.bukkit.permissions.Permission("cjm.update");
					perm.setDefault(PermissionDefault.OP);
					plugin.getServer().getPluginManager().addPermission(perm);
				}
				
				perm.setDescription("Allows a user or the console to check for CustomJoinMessage updates.");
				
				getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
					
					@Override
					public void run() {
						if (getServer().getConsoleSender().hasPermission("cjm.update") && getConfig().getBoolean("update-check", true)) {
							try {
								newVersion = updateCheck(currentVersion);
								log.info("Checking for updates...");
								if (newVersion > currentVersion) {
									log.warning("Stable Version: " + newVersionTitle + " is out!" + " You are still running version: " + currentVersionTitle);
									log.warning("Update at: http://dev.bukkit.org/server-mods/customjoinmessage");
								} else if (currentVersion > newVersion) {
									log.info("Stable Version: " + newVersionTitle + " | Current Version: " + currentVersionTitle);
								} else {
									log.info("No new version available.");
								}
							} catch (Exception e) {
								// ignore exceptions
							}
						}
					}
				}, 0, 432000);
			}
		});
		
		// Load up the plugin metrics
		try {
			metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// ignore exception
		}		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (!sender.hasPermission("cjm.admin")) {
			sender.sendMessage("You do not have permission to use that command!");
			return true;
		}
		
		if (command.getName().equalsIgnoreCase("cjm")) {
			if (args[0].equalsIgnoreCase("reload")) {
				reloadCommand(sender);
				return true;
			} else {
				// Show help
				sender.sendMessage("CustomJoinMessage Commands:");
				sender.sendMessage("  /cjm reload - Reloads the configuration file for CustomJoinMessage.");
				return true;
			}
		}
		
		return false;
	}
	
	private void reloadCommand(CommandSender sender) {
		plugin.reloadConfig();
		sender.sendMessage("Configuration has been successfully reloaded.");
	}
	
	public double updateCheck(double currentVersion) {
		try {
			URL url = new URL("https://api.curseforge.com/servermods/files?projectids=42203");
			URLConnection conn = url.openConnection();
			conn.setReadTimeout(5000);
			conn.addRequestProperty("User-Agent", "CustomJoinMessage Update Checker");
			conn.setDoOutput(true);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			final String response = reader.readLine();
			final JSONArray array = (JSONArray) JSONValue.parse(response);
			
			if (array.size() == 0) {
				this.getLogger().warning("No files found, or Feed URL is bad.");;
				return currentVersion;
			}
			
			// Pull the last version from the JSON
			newVersionTitle = ((String) ((JSONObject) array.get(array.size() - 1)).get("name")).replace("CustomJoinMessage", "").trim();
			return Double.valueOf(newVersionTitle.replaceFirst("\\.", "").trim());
		} catch (Exception e) {
			log.info("There was an issue attempting to check for the latest version.");
		}
		
		return currentVersion;
	}
	
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		
		return perms != null;
	}
}