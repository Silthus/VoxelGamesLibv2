package me.minidigger.voxelgameslib.feature.features;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Singleton;

import me.minidigger.voxelgameslib.components.team.Team;
import me.minidigger.voxelgameslib.event.GameEvent;
import me.minidigger.voxelgameslib.event.events.game.GameLeaveEvent;
import me.minidigger.voxelgameslib.feature.AbstractFeature;
import me.minidigger.voxelgameslib.feature.AbstractFeatureCommand;
import me.minidigger.voxelgameslib.feature.FeatureCommandImplementor;
import me.minidigger.voxelgameslib.game.DefaultGameData;
import me.minidigger.voxelgameslib.lang.Lang;
import me.minidigger.voxelgameslib.lang.LangKey;
import me.minidigger.voxelgameslib.user.User;

import org.bukkit.ChatColor;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import lombok.extern.java.Log;

@Log
public class TeamSelectFeature extends AbstractFeature implements FeatureCommandImplementor {

    protected List<Team> teams = new ArrayList<>();
    @Expose
    private int teamSize = 4;
    @Expose
    private int teamCount = 4;

    @Override
    public void init() {

    }

    @Override
    public Class[] getDependencies() {
        return new Class[0];
    }

    @Override
    public void start() {
        // create default teams
        List<String> teamNames = new ArrayList<>();
        teamNames.add("BLUE");
        teamNames.add("RED");
        teamNames.add("GREEN");
        teamNames.add("YELLOW");
        teamNames.add("LIGHT_PURPLE");
        teamNames.add("BLACK");

        List<ChatColor> teamColors = new ArrayList<>();
        teamColors.add(ChatColor.BLUE);
        teamColors.add(ChatColor.RED);
        teamColors.add(ChatColor.GREEN);
        teamColors.add(ChatColor.YELLOW);
        teamColors.add(ChatColor.LIGHT_PURPLE);
        teamColors.add(ChatColor.BLACK);

        for (int i = 0; i < teamCount; i++) {
            teams.add(new Team(teamSize, teamNames.get(i), teamColors.get(i), getPhase().getGame()));
        }
    }

    @Override
    public void stop() {
        Map<String, Integer> sizes = calcSizes();

        // add everyone who isn't in a team yet to the smallest team
        for (User user : getPhase().getGame().getPlayers()) {
            if (!getTeam(user).isPresent()) {
                Team t = getTeam(findSmallest(sizes)).orElseThrow(() -> new RuntimeException("Null team encountered"));
                t.join(user, user.getRating(getPhase().getGame().getGameMode()));
                Lang.msg(user, LangKey.TEAM_AUTO_ASSIGNED, t.getName());
                sizes.put(t.getName(), sizes.get(t.getName()) + 1);
            }
        }

        // make teams balanced
        balance();

        // save teams for next phase
        DefaultGameData defaultGameData = getPhase().getGame().getGameData(DefaultGameData.class).orElse(new DefaultGameData());
        defaultGameData.teams = teams;
        getPhase().getGame().putGameData(defaultGameData);
    }

    private Map<String, Integer> calcSizes() {
        Map<String, Integer> sizes = new HashMap<>();

        for (final Team t : teams) {
            sizes.put(t.getName(), t.getPlayers().size());
        }
        return sizes;
    }

    private void balance() {
        // TODO Balancing by rating
        Map<String, Integer> sizes = calcSizes();
        String large = findLargest(sizes);
        if (large == null || large.equals("")) {
            log.finer("no balancing");
            teams.stream().map(t -> t.getName() + ": " + t.getPlayers().size()).forEach(log::finer);
            return;
        }
        int largeCount = sizes.get(large);

        String small = findSmallest(sizes);
        int smallCount = sizes.get(small);

        log.finer("LARGE: " + large + " : " + largeCount);
        log.finer("SMALL: " + small + " : " + smallCount);

        if (!(largeCount == smallCount || largeCount == smallCount + 1)) {
            Team largeT = getTeam(large).orElseThrow(() -> new RuntimeException("Null team encountered"));
            Team smallT = getTeam(small).orElseThrow(() -> new RuntimeException("Null team encountered"));

            boolean switched = false;
            for (int i = largeT.getPlayers().size() - 1; i > 0; i--) {
                User player = largeT.getPlayers().get(i);
                //if(playerIsInParty) {TODO party handling for team select
                log.finer("SWITCH: " + player + " from " + large + " to " + small);
                largeT.leave(player);
                smallT.join(player, player.getRating(getPhase().getGame().getGameMode()));
                Lang.msg(player, LangKey.TEAM_AUTO_BALANCED, largeT.getName(), smallT.getName());
                switched = true;
                break;
                //}
            }

            if (!switched) {
                log.finer(largeT.getName());
                log.finer(largeT.getPlayers().size() + "");
                User player = largeT.getPlayers().get(largeT.getPlayers().size() - 1);
                largeT.leave(player);
                smallT.join(player, player.getRating(getPhase().getGame().getGameMode()));
                Lang.msg(player, LangKey.TEAM_AUTO_BALANCED, largeT.getName(), smallT.getName());
            }

            balance();
        }
    }

    private String findLargest(Map<String, Integer> sizes) {
        int largest = 0;
        String name = "";

        for (String s : sizes.keySet()) {
            int x = sizes.get(s);
            if (x >= largest) {
                largest = x;
                name = s;
            }
        }
        log.finer("largest = " + name);
        return name;
    }

    private String findSmallest(Map<String, Integer> sizes) {
        int largest = 100000;
        String name = "";

        for (final String s : sizes.keySet()) {
            final int x = sizes.get(s);
            if (x <= largest) {
                largest = x;
                name = s;
            }
        }
        log.finer("smallest = " + name);
        return name;
    }

    @Override
    public void tick() {

    }

    /**
     * gets the team for a users
     *
     * @param user the user to check for
     * @return the team of the user, if present
     */
    public Optional<Team> getTeam(User user) {
        return teams.stream().filter(team -> team.contains(user)).findFirst();
    }

    /**
     * gets a team by name
     *
     * @param name the name of the team
     * @return the team of the user, if present
     */
    public Optional<Team> getTeam(String name) {
        return teams.stream().filter(team -> team.getName().equalsIgnoreCase(name)).findFirst();
    }

    public List<Team> getTeams() {
        return teams;
    }

    @GameEvent
    public void onQuit(GameLeaveEvent event) {
        getTeam(event.getUser()).ifPresent(t -> t.leave(event.getUser()));
    }

    @Override
    public AbstractFeatureCommand getCommandClass() {
        return new TeamSelectFeatureCommand();
    }

    public void setTeamCount(int teamCount) {
        this.teamCount = teamCount;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    @Singleton
    @CommandAlias("team")
    class TeamSelectFeatureCommand extends AbstractFeatureCommand {

        @CommandAlias("team")
        @CommandPermission("%user")
        public void team(User sender) {
            //TODO message: you are in team x
        }

        @CommandAlias("join")
        public void join(User sender, String team) {
            // TODO join team
        }
    }
}
