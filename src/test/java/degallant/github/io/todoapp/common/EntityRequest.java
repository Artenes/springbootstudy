package degallant.github.io.todoapp.common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @noinspection ClassCanBeRecord
 */
public class EntityRequest {

    private final Request request;

    public EntityRequest(Request request) {
        this.request = request;
    }

    public Factory asUser(String email) {
        return new Factory(email, request);
    }

    /**
     * @noinspection UnusedReturnValue
     */
    public static class Factory {

        private final String email;
        private final Request request;

        public Factory(String email, Request request) {
            this.email = email;
            this.request = request;
        }

        public List<Identifier> makeTasks(String... titles) {
            var list = new ArrayList<Identifier>();
            for (String title : titles) {
                var uri = request.asUser(email)
                        .to("tasks")
                        .withField("title", title)
                        .post().isCreated().getLocation();
                list.add(new Identifier(uri));
            }
            return list;
        }

        public List<Identifier> makeNTasks(int amount) {
            var tasks = new String[amount];
            for (int index = 0; index < amount; index++) {
                tasks[index] = "Task " + (index + 1);
            }
            return makeTasks(tasks);
        }

        public Identifier makeTask(String... body) {
            var authRequest = request.asUser(email).to("tasks");
            parseListAsFields(authRequest, body);
            var uri = authRequest.post().isCreated().getLocation();
            return new Identifier(uri);
        }

        public List<Identifier> commentOnTask(UUID taskId, String... comments) {
            var list = new ArrayList<Identifier>();
            for (String comment : comments) {
                list.add(commentOnTask(taskId, comment));
            }
            return list;
        }

        public Identifier commentOnTask(UUID taskId, String comment) {
            var list = new ArrayList<Identifier>();
            var uri = request.asUser(email).to("tasks/" + taskId + "/comments")
                    .withField("text", comment)
                    .post().isCreated().getLocation();
            return new Identifier(uri);
        }

        private void parseListAsFields(Request.Destination request, String... parts) {
            for (int index = 0; index < parts.length; index = index + 2) {
                var field = parts[index];
                var value = parts[index + 1];
                request.withField(field, value);
            }
        }

    }

}
