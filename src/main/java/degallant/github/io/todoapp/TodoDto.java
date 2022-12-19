package degallant.github.io.todoapp;

public class TodoDto {

    public record Create(String description) {}

    public record PatchComplete(boolean complete) {}

}
