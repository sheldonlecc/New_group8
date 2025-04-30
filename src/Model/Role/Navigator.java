package Model.Role;

public class Navigator extends Role {
    public Navigator() {
        super("领航员", "每回合可以将其他玩家移动1-2格，不消耗该玩家的移动点");
    }

    @Override
    public void useSpecialAbility() {
        // 实现领航员的特殊能力：移动其他玩家
    }
}