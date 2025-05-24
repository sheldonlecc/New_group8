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
        availableRoles.add(new Diver()); // 可在缺损或淹没板块间移动，游泳时能到最近板块
        availableRoles.add(new Engineer()); // 用1个动作修复2个板块或自身所在板块
        availableRoles.add(new Pilot()); // 用1个动作飞到任一板块
        availableRoles.add(new Messenger()); // 给卡时无需同板块
        availableRoles.add(new Explorer()); // 可斜向移动和修复板块
        availableRoles.add(new Navigator()); // 用1个动作移动其他玩家到2个相邻板块
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