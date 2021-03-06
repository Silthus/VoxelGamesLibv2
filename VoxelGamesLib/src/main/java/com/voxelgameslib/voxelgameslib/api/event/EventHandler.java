package com.voxelgameslib.voxelgameslib.api.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.voxelgameslib.voxelgameslib.VoxelGamesLib;
import com.voxelgameslib.voxelgameslib.api.game.Game;
import com.voxelgameslib.voxelgameslib.components.user.User;
import com.voxelgameslib.voxelgameslib.components.user.UserHandler;
import com.voxelgameslib.voxelgameslib.internal.handler.Handler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.EventExecutor;

@Singleton
public class EventHandler implements Handler, Listener {

    private static final EventFilter filterPlayers = (event, registeredListener, user) ->
            user.filter(user1 -> registeredListener.getGame().isPlaying(user1.getUuid())).isPresent();
    private static final EventFilter filterSpectators = (event, registeredListener, user) ->
            user.filter(user1 -> registeredListener.getGame().isSpectating(user1.getUuid())).isPresent();

    private static final Logger log = Logger.getLogger(EventHandler.class.getName());

    private final EventExecutor eventExecutor = (listener, event) -> callEvent(event);

    private final Map<Class<? extends Event>, List<RegisteredListener>> activeEvents = new HashMap<>();
    private Map<UUID, List<RegisteredListener>> activeListeners = new HashMap<>();

    private Map<Class<? extends Event>, Method> reflectionCachePlayer = new HashMap<>();
    private Map<Class<? extends Event>, Method> reflectionCacheUser = new HashMap<>();
    private Map<Class<? extends Event>, Method> reflectionCacheEntity = new HashMap<>();

    @Inject
    private VoxelGamesLib voxelGamesLib;
    @Inject
    private UserHandler userHandler;

    public void registerEvents(@Nonnull Listener listener, @Nonnull Game game) {
        Set<Class<Event>> newEvents = new HashSet<>();
        Arrays.stream(listener.getClass().getMethods()).filter((method -> method.isAnnotationPresent(GameEvent.class))).forEach(
                method -> {
                    if (method.getParameterCount() != 1 && method.getParameterCount() != 2) {
                        log.warning("Invalid parameters for " + listener.getClass().getName() + " " + method.toString());
                        return;
                    }

                    if (Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        //noinspection unchecked
                        Class<Event> eventClass = (Class<Event>) method.getParameterTypes()[0];
                        GameEvent annotation = method.getAnnotation(GameEvent.class);

                        RegisteredListener registeredListener = new RegisteredListener(listener, game, eventClass, method, new ArrayList<>());

                        if (annotation.filterPlayers()) {
                            registeredListener.addFilter(filterPlayers);
                        }
                        if (annotation.filterSpectators()) {
                            registeredListener.addFilter(filterSpectators);
                        }

                        activeListeners.computeIfAbsent(game.getUuid(), (key) -> new CopyOnWriteArrayList<>()).add(registeredListener);

                        activeEvents.computeIfAbsent(eventClass, (key) -> {
                            newEvents.add(eventClass);
                            return new CopyOnWriteArrayList<>();
                        }).add(registeredListener);
                    } else {
                        log.warning("Invalid parameter for " + listener.getClass().getName() + " " + method.toString());
                        return;
                    }
                }
        );

        // check if we need to register a new event
        newEvents.forEach(eventClass ->
                Bukkit.getServer().getPluginManager().registerEvent(eventClass, this, EventPriority.HIGH, eventExecutor, voxelGamesLib));

        // register normal events
        Bukkit.getServer().getPluginManager().registerEvents(listener, voxelGamesLib);
    }

    public void unregister(@Nonnull Listener listener, @Nonnull Game game) {
        //noinspection unchecked
        Arrays.stream(listener.getClass().getMethods())
                .filter((method -> method.isAnnotationPresent(GameEvent.class)))
                .filter(method -> method.getParameterCount() != 1 || method.getParameterCount() != 2)
                .filter(method -> Event.class.isAssignableFrom(method.getParameterTypes()[0]))
                .map(method -> (Class<Event>) method.getParameterTypes()[0]).forEach(
                eventClass -> activeEvents.get(eventClass).removeIf(registeredListener -> registeredListener.getListener().equals(listener)));

        if (activeListeners.containsKey(game.getUuid())) {
            activeListeners.get(game.getUuid()).removeIf(registeredListener -> registeredListener.getListener().equals(listener));
            if (activeListeners.get(game.getUuid()).size() == 0) {
                activeListeners.remove(game.getUuid());
            }
        }

        HandlerList.unregisterAll(listener);
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void callEvent(@Nonnull T event) {
        Class<Event> eventClass = (Class<Event>) event.getClass();
        while (!eventClass.equals(Object.class)) {
            if (activeEvents.containsKey(eventClass)) {
                activeEvents.get(eventClass).forEach(registeredListener -> {
                    Optional<User> user = Optional.empty();
                    boolean tried = false;
                    for (EventFilter filter : registeredListener.getFilters()) {
                        if (!user.isPresent() && !tried) {
                            user = figureOutUser(event);
                            tried = true;
                        }
                        if (!filter.filter(event, registeredListener, user)) {
                            return;
                        }
                    }

                    try {
                        if (registeredListener.getMethod().getParameterCount() == 2) {
                            registeredListener.getMethod().invoke(registeredListener.getListener(), event, user.orElse(null));
                        } else {
                            registeredListener.getMethod().invoke(registeredListener.getListener(), event);
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.log(Level.SEVERE, "Error while calling eventhandler!", e);
                    }
                });
                break;
            } else {
                eventClass = (Class<Event>) eventClass.getSuperclass();
            }
        }
    }

    @Nonnull
    private <T extends Event> Optional<User> figureOutUser(@Nonnull T event) {
        if (event instanceof PlayerEvent) {
            return userHandler.getUser(((PlayerEvent) event).getPlayer().getUniqueId());
        } else if (event instanceof com.voxelgameslib.voxelgameslib.api.event.events.player.PlayerEvent) {
            return Optional.of(((com.voxelgameslib.voxelgameslib.api.event.events.player.PlayerEvent) event).getUser());
        }

        // search for method to get player
        if (!reflectionCachePlayer.containsKey(event.getClass()) && !reflectionCacheUser.containsKey(event.getClass()) && !reflectionCacheEntity.containsKey(event.getClass())) {
            Method entityMethod = null;
            boolean found = false;
            for (Method m : event.getClass().getMethods()) {
                if (m.getReturnType().equals(User.class)) {
                    reflectionCacheUser.put(event.getClass(), m);
                    found = true;
                    break;
                } else if (m.getReturnType().equals(Player.class)) {
                    reflectionCachePlayer.put(event.getClass(), m);
                    found = true;
                    break;
                } else if (Entity.class.isAssignableFrom(m.getReturnType())) {
                    entityMethod = m;
                }
            }

            // entity should be fallback, if there is something better don't use it
            if (!found && entityMethod != null) {
                reflectionCacheEntity.put(event.getClass(), entityMethod);
            }
        }

        // check cache to find user
        if (reflectionCacheUser.containsKey(event.getClass())) {
            Method method = reflectionCacheUser.get(event.getClass());
            try {
                return Optional.of((User) method.invoke(event));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        } else if (reflectionCachePlayer.containsKey(event.getClass())) {
            Method method = reflectionCachePlayer.get(event.getClass());
            try {
                return userHandler.getUser(((Player) method.invoke(event)).getUniqueId());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        } else if (reflectionCacheEntity.containsKey(event.getClass())) {
            Method method = reflectionCacheEntity.get(event.getClass());
            try {
                Entity entity = (Entity) method.invoke(event);
                if (entity instanceof Player) {
                    return userHandler.getUser(entity.getUniqueId());
                } else {
                    return Optional.empty();
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        } else {
            log.warning("Could not even a way to get a user out of " + event.getEventName() + "!");
            return Optional.empty();
        }
    }
}
