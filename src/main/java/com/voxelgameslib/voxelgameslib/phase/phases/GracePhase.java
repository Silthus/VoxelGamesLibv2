package com.voxelgameslib.voxelgameslib.phase.phases;

import com.voxelgameslib.voxelgameslib.GameConstants;
import com.voxelgameslib.voxelgameslib.feature.features.*;
import com.voxelgameslib.voxelgameslib.phase.TimedPhase;
import org.bukkit.GameMode;

/**
 * The grace phase is the phase before the real action starts. pvp is disabled, players are expected
 * to run away, hide or collect stuff.
 */
public class GracePhase extends TimedPhase {

    @Override
    public void init() {
        setName("GracePhase");
        super.init();
        setAllowJoin(false);
        setAllowSpectate(true);
        setTicks(60 * GameConstants.TPS);

        MapFeature mapFeature = getGame().createFeature(MapFeature.class, this);
        mapFeature.setShouldUnload(false);
        addFeature(mapFeature);

        SpawnFeature spawnFeature = getGame().createFeature(SpawnFeature.class, this);
        addFeature(spawnFeature);

        MapInfoFeature mapInfoFeature = getGame().createFeature(MapInfoFeature.class, this);
        addFeature(mapInfoFeature);

        ScoreboardFeature scoreboardFeature = getGame().createFeature(ScoreboardFeature.class, this);
        addFeature(scoreboardFeature);

        NoBlockBreakFeature noBlockBreakFeature = getGame()
                .createFeature(NoBlockBreakFeature.class, this);
        addFeature(noBlockBreakFeature);

        NoBlockPlaceFeature noBlockPlaceFeature = getGame()
                .createFeature(NoBlockPlaceFeature.class, this);
        addFeature(noBlockPlaceFeature);

        ClearInventoryFeature clearInventoryFeature = getGame()
                .createFeature(ClearInventoryFeature.class, this);
        addFeature(clearInventoryFeature);

        NoDamageFeature noDamageFeature = getGame().createFeature(NoDamageFeature.class, this);
        addFeature(noDamageFeature);

        HealFeature healFeature = getGame().createFeature(HealFeature.class, this);
        addFeature(healFeature);

        GameModeFeature gameModeFeature = getGame().createFeature(GameModeFeature.class, this);
        gameModeFeature.setGameMode(GameMode.SURVIVAL);
        addFeature(gameModeFeature);
    }
}
