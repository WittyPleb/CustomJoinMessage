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
		
		if (player.hasPermission("cjm.update")) {
			try {
				if (plugin.newVersion > plugin.currentVersion) {
					player.sendMessage("CustomJoinMessage " +  plugin.newVersion + " is out! You are running " + plugin.currentVersion);
					player.sendMessage("Update Vault at: http://dev.bukkit.org/server-mods/customjoinmessage");
				}
			} catch (Exception e) {
				// ignore exception
			}
		}
		
		if (plugin.getConfig().getBoolean("disable-join-message", true)) {
			event.setJoinMessage(null);
		} else if (plugin.getConfig().getString("custom.users." + event.getPlayer().getName() + ".join-message") != null) {
			event.setJoinMessage(TextUtil.colorizeText(plugin.getConfig().getString("custom.users." + event.getPlayer().getName() + ".join-message").replace("%p", event.getPlayer().getName())));
		} else if (plugin.getConfig().getBoolean("use-groups", true)) {
			if (plugin.getConfig().getString("custom.groups." + CustomJoinMessage.perms.getPrimaryGroup(event.getPlayer()) + ".join-message") != null) {
				event.setJoinMessage(TextUtil.colorizeText(plugin.getConfig().getString("custom.groups." + CustomJoinMessage.perms.getPrimaryGroup(event.getPlayer()) + ".join-message")));
			}
		} else {
			event.setJoinMessage(TextUtil.colorizeText(plugin.getConfig().getString("custom.default.join-message").replace("%p", event.getPlayer().getName())));
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (plugin.getConfig().getBoolean("disable-leave-message", true)) {
			event.setQuitMessage(null);
		} else if (plugin.getConfig().getString("custom.users." + event.getPlayer().getName() + ".leave-message") != null) {
			event.setQuitMessage(TextUtil.colorizeText(plugin.getConfig().getString("custom.users." + event.getPlayer().getName() + ".leave-message").replace("%p", event.getPlayer().getName())));
		} else if (plugin.getConfig().getBoolean("use-groups", true)) {
			if (plugin.getConfig().getString("custom.groups." + CustomJoinMessage.perms.getPrimaryGroup(event.getPlayer()) + ".leave-message") != null) {
				event.setQuitMessage(TextUtil.colorizeText(plugin.getConfig().getString("custom.groups." + CustomJoinMessage.perms.getPrimaryGroup(event.getPlayer()) + ".leave-message")));
			}
		} else {
			event.setQuitMessage(TextUtil.colorizeText(plugin.getConfig().getString("custom.default.leave-message").replace("%p", event.getPlayer().getName())));
		}
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		if (plugin.getConfig().getBoolean("disable-kick-message", true)) {
			event.setLeaveMessage(null);
		} else if (plugin.getConfig().getString("custom.users." + event.getPlayer().getName() + ".kick-message") != null) {
			event.setLeaveMessage(TextUtil.colorizeText(plugin.getConfig().getString("custom.users." + event.getPlayer().getName() + ".kick-message").replace("%p", event.getPlayer().getName())));
		} else if (plugin.getConfig().getBoolean("use-groups", true)) {
			if (plugin.getConfig().getString("custom.groups." + CustomJoinMessage.perms.getPrimaryGroup(event.getPlayer()) + ".kick-message") != null) {
				event.setLeaveMessage(TextUtil.colorizeText(plugin.getConfig().getString("custom.groups." + CustomJoinMessage.perms.getPrimaryGroup(event.getPlayer()) + ".kick-message")));
			}
		} else {
			event.setLeaveMessage(TextUtil.colorizeText(plugin.getConfig().getString("custom.default.kick-message").replace("%p", event.getPlayer().getName())));
		}
	}
}