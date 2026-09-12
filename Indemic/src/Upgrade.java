public abstract class Upgrade {

    private String name;

    public Upgrade(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract boolean apply(world world);
}

