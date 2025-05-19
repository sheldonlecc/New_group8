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
        availableRoles.add(new Engineer());
        availableRoles.add(new Explorer());
        availableRoles.add(new Pilot());
        availableRoles.add(new Messenger());
        availableRoles.add(new Navigator());
        availableRoles.add(new Diver());
    }

    public static List<Role> getRandomRoles(int playerCount) {
        if (playerCount < 2 || playerCount > 4) {
            throw new IllegalArgumentException("玩家数量必须在2-4之间");
        }

        List<Role> shuffledRoles = new ArrayList<>(availableRoles);
        Collections.shuffle(shuffledRoles);
        return shuffledRoles.subList(0, playerCount);
    }

    /**
     * 随机分配角色给玩家，并设置双向绑定
     * 
     * @param players 玩家列表
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