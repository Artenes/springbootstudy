package degallant.github.io.todoapp.admin;

import degallant.github.io.todoapp.authentication.AuthDto;
import degallant.github.io.todoapp.domain.comments.CommentsRepository;
import degallant.github.io.todoapp.common.LinkBuilder;
import degallant.github.io.todoapp.domain.projects.ProjectsRepository;
import degallant.github.io.todoapp.domain.tags.TagsRepository;
import degallant.github.io.todoapp.domain.tasks.TasksRepository;
import degallant.github.io.todoapp.domain.users.Role;
import degallant.github.io.todoapp.domain.users.UserEntity;
import degallant.github.io.todoapp.domain.users.UsersRepository;
import degallant.github.io.todoapp.exceptions.InvalidStateException;
import degallant.github.io.todoapp.sanitization.parsers.PrimitiveFieldParser;
import degallant.github.io.todoapp.sanitization.FieldValidator;
import degallant.github.io.todoapp.sanitization.Sanitizer;
import degallant.github.io.todoapp.sanitization.parsers.UsersFieldParser;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

/**
 * @noinspection ClassCanBeRecord, unused
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin")
public class AdminController {

    private final Sanitizer sanitizer;
    private final FieldValidator rules;
    private final PrimitiveFieldParser parser;
    private final LinkBuilder link;
    private final UsersRepository usersRepository;
    private final TasksRepository tasksRepository;
    private final TagsRepository tagsRepository;
    private final CommentsRepository commentsRepository;
    private final ProjectsRepository projectsRepository;
    private final UsersFieldParser userParser;

    //TODO add endpoint - POST /admin/admins - create new admin user
    //TODO add endpoint - DELETE /admin/admins - delete an admin user
    //TODO add endpoint - POST /admin/keys - make a new api key
    //TODO add endpoint - DELETE /admin/keys - delete api key
    //TODO add endpoint - POST /admin/tokens - make a new access token for tests that last 1 day

    @PostMapping("/promote")
    public ResponseEntity<?> promote(@RequestBody AuthDto.Promote request, Authentication authentication) {
        var user = (UserEntity) authentication.getPrincipal();

        var result = sanitizer.sanitize(

                sanitizer.field("user_id").withRequiredValue(request.getUserId()).sanitize(value -> {
                    var id = parser.toUUID(value);
                    rules.check(usersRepository.existsById(id)).orThrow("validation.do_not_exist", id);
                    rules.check(!user.getId().equals(id)).orThrow("validation.cannot_change_role_current_user", id);
                    return id;
                })

        );

        var userToUpdate = usersRepository.findById(result.get("user_id").value()).orElseThrow();
        userToUpdate.setRole(Role.ROLE_ADMIN);
        usersRepository.save(userToUpdate);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> statistics(Authentication authentication) {

        var totalUsers = usersRepository.count();
        var totalTasks = tasksRepository.count();
        var totalComments = commentsRepository.count();
        var totalTags = tagsRepository.count();
        var totalProjects = projectsRepository.count();

        var linkSelf = link.to("admin").slash("statistics").withSelfRel();
        var model = EntityModel.of(AdminDto.Statistics.builder()
                .totalComments(totalComments)
                .totalProjects(totalProjects)
                .totalTags(totalTags)
                .totalTasks(totalTasks)
                .totalUsers(totalUsers)
                .build(), linkSelf);

        return ResponseEntity.ok(model);

    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> removeUser(@PathVariable String id, Authentication authentication) {

        var admin = (UserEntity) authentication.getPrincipal();
        var user = userParser.toUserOrThrowNoSuchElement(id);

        if (admin.getId().equals(user.getId())) {
            throw new InvalidStateException("error.cannot_delete_current_user");
        }

        user.setDeletedAt(OffsetDateTime.now());
        usersRepository.save(user);
        return ResponseEntity.noContent().build();

    }

    @PatchMapping("/users/{id}/restore")
    public ResponseEntity<?> restoreUser(@PathVariable String id, Authentication authentication) {

        var admin = (UserEntity) authentication.getPrincipal();
        var user = userParser.toAbsoluteUserOrThrowNoSuchElement(id);

        if (admin.getId().equals(user.getId())) {
            throw new InvalidStateException("error.cannot_restore_current_user");
        }

        if (!user.isDeleted()) {
            throw new InvalidStateException("error.cannot_restore_not_deleted_user");
        }

        user.setDeletedAt(null);
        usersRepository.save(user);
        return ResponseEntity.ok().build();

    }

}
