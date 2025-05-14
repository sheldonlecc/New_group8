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
}