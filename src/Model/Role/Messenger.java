package Model.Role;

public class Messenger extends Role {
    public Messenger() {
        super("信使", "可以将手牌给予任意玩家，不受距离限制");
    }

    @Override
    public void useSpecialAbility() {
        // 实现信使的特殊能力：远程传递手牌
    }
}