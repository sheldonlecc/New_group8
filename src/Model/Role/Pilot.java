package Model.Role;

public class Pilot extends Role {
    public Pilot() {
        super("飞行员", "每回合可以飞到任意一个未沉没的地形格，此行动消耗一个移动点");
    }

    @Override
    public void useSpecialAbility() {
        // 实现飞行员的特殊能力：飞行移动
    }
}