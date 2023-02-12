package degallant.github.io.todoapp.admin;

import degallant.github.io.todoapp.authentication.AuthDto;
import degallant.github.io.todoapp.comments.CommentsRepository;
import degallant.github.io.todoapp.common.LinkBuilder;
import degallant.github.io.todoapp.projects.ProjectsRepository;
import degallant.github.io.todoapp.tags.TagsRepository;
import degallant.github.io.todoapp.tasks.TasksRepository;
import degallant.github.io.todoapp.users.Role;
import degallant.github.io.todoapp.users.UserEntity;
import degallant.github.io.todoapp.users.UsersRepository;
import degallant.github.io.todoapp.validation.PrimitiveFieldParser;
import degallant.github.io.todoapp.validation.FieldValidator;
import degallant.github.io.todoapp.validation.Sanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

}
