package degallant.github.io.todoapp.domain.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsersRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Finds a user by email.
     *
     * This method does not take into account the deleted_at collum because
     * since the email is a unique field, that means there are cases where
     * if we search by email and deleted_at, then no result is returned
     * even though the email already exists in the database, leading
     * to a conflict while creating a new user.
     *
     * @param email the email to search for
     * @return the found UserEntity
     */
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<UserEntity> findByRole(Role role);

}
