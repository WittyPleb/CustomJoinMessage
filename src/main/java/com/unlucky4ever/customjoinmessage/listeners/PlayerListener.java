package com.unlucky4ever.customjoinmessage.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.unlucky4ever.customjoinmessage.CustomJoinMessage;
import com.unlucky4ever.customjoinmessage.extras.TextUtil;

public class PlayerListener implements Listener {
	
	private CustomJoinMessage plugin;
	public static String message;
	
	public PlayerListener(CustomJoinMessage plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		if (player.hasPermission("cjm.update") && plugin.getConfigurationLoader().getBoolean("update.check")) {
			try {
				if (plugin.newVersion > plugin.currentVersion) {
					player.sendMessage("CustomJoinMessage " +  plugin.newVersion + " is out! You are running " + plugin.currentVersion);
					player.sendMessage("Update Vault at: http://dev.bukkit.org/server-mods/customjoinmessage");
				}
			} catch (Exception e) {
				// ignore exception
			}
		}
		
		if (plugin.getConfigurationLoader().getBoolean("disable.join-message")) {
			event.setJoinMessage(null);
		} else if (plugin.getConfigurationLoader().getString("custom.users." + event.getPlayer().getUniqueId() + ".join-message") != null) {
			event.setJoinMessage(TextUtil.colorizeText(plugin.getConfigurationLoader().getString("custom.users." + event.getPlayer().getUniqueId() + ".join-message").replace("%p", event.getPlayer().getName())));
		} else if (plugin.getConfigurationLoader().getBoolean("use-groups")) {
			if (plugin.getConfigurationLoader().getString("custom.groups." + CustomJoinMessage.perms.getPrimaryGroup(event.getPlayer()) + ".join-message") != null) {
				event.setJoinMessage(TextUtil.colorizeText(plugin.getConfigurationLoader().getString("custom.groups." + CustomJoinMessage.perms.getPrimaryGroup(event.getPlayer()) + ".join-message").replace("%p", event.getPlayer().getName())));
			}
		} else {
			event.setJoinMessage(TextUtil.colorizeText(plugin.getConfigurationLoader().getString("custom.default.join-message").replace("%p", event.getPlayer().getName())));
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (plugin.getConfigurationLoader().getBoolean("disable.leave-message")) {
			event.setQuitMessage(null);
		} else if (plugin.getConfigurationLoader().getString("custom.users." + event.getPlayer().getUniqueId() + ".leave-message") != null) {
			event.setQuitMessage(TextUtil.colorizeText(plugin.getConfigurationLoader().getString("custom.users." + event.getPlayer().getUniqueId() + ".leave-message").replace("%p", event.getPlayer().getName())));
		} else if (plugin.getConfigurationLoader().getBoolean("use-groups")) {
			if (plugin.getConfigurationLoader().getString("custom.groups." + CustomJoinMessage.perms.getPrimaryGroup(event.getPlayer()) + ".leave-message") != null) {
				event.setQuitMessage(TextUtil.colorizeText(plugin.getConfigurationLoader().getString("custom.groups." + CustomJoinMessage.perms.getPrimaryGroup(event.getPlayer()) + ".leave-message").replace("%p", event.getPlayer().getName())));
			}
		} else {
			event.setQuitMessage(TextUtil.colorizeText(plugin.getConfigurationLoader().getString("custom.default.leave-message").replace("%p", event.getPlayer().getName())));
		}
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		if (plugin.getConfigurationLoader().getBoolean("disable.kick-message")) {
			event.setLeaveMessage(null);
		} else if (plugin.getConfigurationLoader().getString("custom.users." + event.getPlayer().getUniqueId() + ".kick-message") != null) {
			event.setLeaveMessage(TextUtil.colorizeText(plugin.getConfigurationLoader().getString("custom.users." + event.getPlayer().getUniqueId() + ".kick-message").replace("%p", event.getPlayer().getName())));
		} else if (plugin.getConfigurationLoader().getBoolean("use-groups")) {
			if (plugin.getConfigurationLoader().getString("custom.groups." + CustomJoinMessage.perms.getPrimaryGroup(event.getPlayer()) + ".kick-message") != null) {
				event.setLeaveMessage(TextUtil.colorizeText(plugin.getConfigurationLoader().getString("custom.groups." + CustomJoinMessage.perms.getPrimaryGroup(event.getPlayer()) + ".kick-message").replace("%p", event.getPlayer().getName())));
			}
		} else {
			event.setLeaveMessage(TextUtil.colorizeText(plugin.getConfigurationLoader().getString("custom.default.kick-message").replace("%p", event.getPlayer().getName())));
		}
	}
}