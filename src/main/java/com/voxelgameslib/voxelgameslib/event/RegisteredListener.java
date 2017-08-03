package com.voxelgameslib.voxelgameslib.event;

import com.voxelgameslib.voxelgameslib.game.Game;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;
import java.util.List;

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
