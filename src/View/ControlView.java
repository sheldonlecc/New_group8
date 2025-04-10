package View;

import Model.Player;
import Model.Cards.Card;
import Model.Tile;

import java.util.List;
import java.util.Scanner;

public class ControlView {
    private Scanner scanner = new Scanner(System.in);

    // 显示游戏控制菜单
    public void displayControlMenu() {
        // 显示各种操作选项，如移动、加固、使用卡牌等
    }

    // 获取玩家的操作选择
    public int getPlayerChoice() {
        // 读取玩家输入的选择
        return scanner.nextInt();
    }

    // 显示玩家可以移动到的地块
    public void displayAvailableMoves(List<Tile> availableTiles) {
        // 显示所有可移动的地块信息
    }

    // 显示玩家可以使用的卡牌
    public void displayAvailableCards(List<Card> availableCards) {
        // 显示所有可使用的卡牌信息
    }

    // 提示玩家选择要移动到的地块
    public Tile promptForMove(List<Tile> availableTiles) {
        // 提示玩家输入要移动到的地块编号，并返回对应的地块
        return null;
    }

    // 提示玩家选择要使用的卡牌
    public Card promptForCard(List<Card> availableCards) {
        // 提示玩家输入要使用的卡牌编号，并返回对应的卡牌
        return null;
    }
}