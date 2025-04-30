package Model.Role;

public abstract class Role {
    private String name;
    private String ability;

    public Role(String name, String ability) {
        this.name = name;
        this.ability = ability;
    }

    public String getName() {
        return name;
    }

    public String getAbility() {
        return ability;
    }

    // 每个角色都需要实现的特殊能力方法
    public abstract void useSpecialAbility();
}