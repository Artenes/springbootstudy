package degallant.github.io.todoapp.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

public class AdminDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Statistics {

        @JsonProperty("total_users")
        long totalUsers;

        @JsonProperty("total_tasks")
        long totalTasks;

        @JsonProperty("total_comments")
        long totalComments;

        @JsonProperty("total_tags")
        long totalTags;

        @JsonProperty("total_projects")
        long totalProjects;

    }

}
