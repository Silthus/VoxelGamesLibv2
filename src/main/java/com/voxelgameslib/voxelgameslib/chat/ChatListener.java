package com.voxelgameslib.voxelgameslib.chat;

import com.voxelgameslib.voxelgameslib.user.User;
import com.voxelgameslib.voxelgameslib.user.UserHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
@SuppressWarnings("Javadoc")
public class ChatListener implements Listener {

    @Inject
    private UserHandler userHandler;

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Optional<User> user = userHandler.getUser(event.getPlayer().getUniqueId());

        user.ifPresent(u -> u.getActiveChannel().sendMessage(u, event.getMessage()));

        event.setCancelled(true);
    }
}
