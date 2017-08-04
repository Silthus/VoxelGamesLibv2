package com.voxelgameslib.voxelgameslib.event.events.player;

import com.voxelgameslib.voxelgameslib.game.Game;
import com.voxelgameslib.voxelgameslib.user.User;
import lombok.Getter;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * This event is called when a player is eliminated from a game.<br>Gametypes should call this
 * themselves.
 */
public class PlayerEliminationEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    @Getter
    private Game game;

    public PlayerEliminationEvent(@Nonnull User user, @Nonnull Game game) {
        super(user);
        this.game = game;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}