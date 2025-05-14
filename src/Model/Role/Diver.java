package Model.Role;

public class Diver extends Role {
    public Diver() {
        super("潜水员", "可以穿过一个或多个相邻的已淹没地形格，只消耗一个移动点");
    }

    @Override
    public void useSpecialAbility() {
        // 实现潜水员的特殊能力：水下移动
    }
}