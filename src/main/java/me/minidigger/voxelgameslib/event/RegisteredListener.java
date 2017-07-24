package me.minidigger.voxelgameslib.event;

import java.lang.reflect.Method;
import java.util.List;

import me.minidigger.voxelgameslib.game.Game;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisteredListener {

    private Listener listener;
    private Game game;
    private Class<Event> eventClass;
    private Method method;
    private List<EventFilter> filters;

    public void addFilter(EventFilter filter) {
        filters.add(filter);
    }
}