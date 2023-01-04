package degallant.github.io.todoapp;

public record AuthenticatedUser(UserEntity user, boolean isNew, String accessToken, String refreshToken) {
}
