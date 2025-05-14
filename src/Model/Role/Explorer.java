package Model.Role;

public class Explorer extends Role {
    public Explorer() {
        super("探险家", "可以在对角线方向移动或固定地形");
    }

    @Override
    public void useSpecialAbility() {
        // 实现探险家的特殊能力：对角线移动
    }
}