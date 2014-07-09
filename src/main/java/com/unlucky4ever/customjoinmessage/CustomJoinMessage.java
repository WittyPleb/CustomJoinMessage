package com.unlucky4ever.customjoinmessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
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

import com.unlucky4ever.customjoinmessage.config.ConfigurationLoader;
import com.unlucky4ever.customjoinmessage.extras.Metrics;
import com.unlucky4ever.customjoinmessage.listeners.PlayerListener;

public class CustomJoinMessage extends JavaPlugin {
	
	protected ConfigurationLoader cloader;
	
	private static Logger log;
	private String newVersionTitle = "";
	private String currentVersionTitle = "";
	private CustomJoinMessage plugin;
	private Metrics metrics;
	
	public double newVersion = 0;
	public double currentVersion = 0;
	public static Permission perms = null;
	
	@Override
	public void onDisable() {
		// Remove all service registrations
		getServer().getServicesManager().unregisterAll(this);
		Bukkit.getScheduler().cancelTasks(this);
	}
	
	@Override
	public void onLoad() {
		getLogger().info("Loading Configuration mananger...");
		cloader = new ConfigurationLoader(this);
		
		try {
			Thread.sleep(500L);
		} catch (InterruptedException e) {
			Logger.getLogger(CustomJoinMessage.class.getName()).log(Level.SEVERE, null, e);
		}
	}
	
	@Override
	public void onEnable() {
		plugin = this;
		log = this.getLogger();
		currentVersionTitle = getDescription().getVersion().split("-")[0];
		currentVersion = Double.valueOf(currentVersionTitle.replaceFirst("\\.", ""));
		
		if (cloader.getBoolean("use-groups")) {
			if (getServer().getPluginManager().getPlugin("Vault") != null) {
				if (setupPermissions()) {} else {
					log.info("Permission plugin not found, shutting down.");
					getServer().getPluginManager().disablePlugin(this);
				}
			} else {
				log.info("Please install Vault to use the groups feature. Shutting down.");
				getServer().getPluginManager().disablePlugin(this);
			}
		}
		
		getCommand("cjm").setExecutor(this);
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		
		// Schedule to check the version every 30 minutes for an update. This is to update the most recent
		// version so if an admin reconnects they will be warned about newer versions.
		this.getServer().getScheduler().runTask(this, new Runnable() {
			@Override
			public void run() {
				// Programmatically set the default perission value because Bukkit doesn't handle plugin.yml properly for Load order STARTUP plugins
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
						if (getServer().getConsoleSender().hasPermission("cjm.update") && cloader.getBoolean("update.check")) {
							try {
								newVersion = updateCheck(currentVersion);
								log.info("Checking for updates...");
								if (newVersion > currentVersion) {
									log.warning("Stable Version: " + newVersionTitle + " is out!" + " You are still running version: " + currentVersionTitle);
									log.warning("Update at: " + getDescription().getWebsite());
								} else if (currentVersion > newVersion) {
									log.info("Stable Version: " + newVersionTitle + " | Current Version: " + currentVersionTitle);
								} else {
									log.info("No new version available.");
								}
							} catch (Exception e) {
								// Ignore exceptions
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
			// Ignore exception
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		
		if (!sender.hasPermission("cjm.admin")) {
			sender.sendMessage("You do not have permission to use that command.");
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("cjm")) {
			if (args[0].equalsIgnoreCase("reload")) {
				cloader.verifyConfig();
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
	
	public double updateCheck(double currentVersion) {
		try {
			URL url = new URL("https://api.curseforge.com/servermods/files?projectids=42203");
			URLConnection conn = url.openConnection();
			conn.setReadTimeout(5000);
			conn.addRequestProperty("User-Agent", "CustomJoinMessage update checker");
			conn.setDoOutput(true);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			final String response = reader.readLine();
			final JSONArray array = (JSONArray) JSONValue.parse(response);
			
			if (array.size() == 0) {
				this.getLogger().warning("No files found, or feed URL is bad.");
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
	
	/**
	 * Gets the configuration manager for CustomJoinMessage
	 * 
	 * @since 2.0.0
	 * @version 2.0.0
	 * 
	 * @return The main ConfigurationLoader
	 */
	public ConfigurationLoader getConfigurationLoader() {
		return this.cloader;
	}
	
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		
		return perms != null;
	}
}