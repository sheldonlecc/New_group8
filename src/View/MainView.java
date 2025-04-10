package View;

import Model.Player;
import java.util.List;

public class MainView {
    // 显示游戏开始界面
    public void displayGameStart() {
        // 显示游戏开始的欢迎信息
    }

    // 显示游戏结束界面
    public void displayGameEnd(boolean isWin) {
        if (isWin) {
            // 显示游戏胜利信息
        } else {
            // 显示游戏失败信息
        }
    }

    // 显示当前玩家信息
    public void displayCurrentPlayer(Player player) {
        // 显示当前玩家的名称、手牌数量等信息
    }

    // 显示所有玩家信息
    public void displayAllPlayers(List<Player> players) {
        // 遍历所有玩家，显示每个玩家的信息
    }
}