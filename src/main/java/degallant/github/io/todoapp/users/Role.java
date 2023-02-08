package degallant.github.io.todoapp.users;

public enum Role {

    ROLE_USER,
    ROLE_ADMIN;

    public String simpleName() {
        return this.name().split("_")[1];
    }

}
