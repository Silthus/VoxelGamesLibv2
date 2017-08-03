package com.voxelgameslib.voxelgameslib.matchmaking;

import com.voxelgameslib.voxelgameslib.game.GameMode;
import com.voxelgameslib.voxelgameslib.handler.Handler;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class MatchmakingHandler implements Handler {

    private Map<GameMode, Queue> queues;

    @Override
    public void start() {
        queues = new HashMap<>();
        //TODO match making
    }

    @Override
    public void stop() {
        queues.clear();
        queues = null;
    }
}
