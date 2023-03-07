package degallant.github.io.todoapp.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public class BaseEntity {

    @Id
    protected UUID id;

    @Column(name = "created_at")
    protected OffsetDateTime createdAt;

    @Column(name = "updated_at")
    protected OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    protected OffsetDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        /*
        The  @GeneratedValue(strategy = GenerationType.UUID) annotation
        overrides the UUID even if you manually set in the entity
        the solution is just to generate it manually if absent
         */
        if (id == null) {
            //retardedly we need to call the setter so JPA don't complain
            setId(UUID.randomUUID());
        }
        this.createdAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

}