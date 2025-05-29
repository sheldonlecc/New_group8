package Controller;

import Model.Role.Role;
import Model.Role.Engineer;
import Model.Role.Explorer;
import Model.Role.Pilot;
import Model.Role.Messenger;
import Model.Role.Navigator;
import Model.Role.Diver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoleManager {
    private static final List<Role> availableRoles = new ArrayList<>();

    static {
        availableRoles.add(new Diver()); // Can move between missing or flooded tiles, can swim to nearest tile
        availableRoles.add(new Engineer()); // Use 1 action to repair 2 tiles or own tile
        availableRoles.add(new Pilot()); // Use 1 action to fly to any tile
        availableRoles.add(new Messenger()); // Give cards without being on same tile
        availableRoles.add(new Explorer()); // Can move and repair tiles diagonally
        availableRoles.add(new Navigator()); // Use 1 action to move other players to 2 adjacent tiles
    }

    public static List<Role> getRandomRoles(int playerCount) {
        if (playerCount < 2 || playerCount > 4) {
            throw new IllegalArgumentException("Player count must be between 2-4");
        }

        List<Role> shuffledRoles = new ArrayList<>(availableRoles);
        Collections.shuffle(shuffledRoles);
        return shuffledRoles.subList(0, playerCount);
    }

    /**
     * Randomly assign roles to players and set bidirectional binding
     * 
     * @param players Player list
     */
    public static void assignRolesToPlayers(List<Model.Player> players) {
        List<Role> randomRoles = getRandomRoles(players.size());
        for (int i = 0; i < players.size(); i++) {
            Model.Player player = players.get(i);
            Role role = randomRoles.get(i);
            player.setRole(role);
            role.setPlayer(player);
        }
    }
}