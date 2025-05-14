package Model.Role;

public class Engineer extends Role {
    public Engineer() {
        super("工程师", "每回合可以使用两次'固定'行动来加固地形");
    }

    @Override
    public void useSpecialAbility() {
        // 实现工程师的特殊能力：额外的固定行动
    }
}