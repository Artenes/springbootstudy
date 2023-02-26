# Spring JPA

This page will just be a quick reference to some relevant details of Spring JPA.

## Dependency

````groovy
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
````

## Repositories

````java
@Repository
public interface TasksRepository extends JpaRepository<TaskEntity, UUID>, JpaSpecificationExecutor<TaskEntity>
````

When you need dynamic queries, don't forget to extend ``JpaSpecificationExecutor``.

## Queries

Write the query in plain english by starting with ``find`` and using the name of the properties in the entity. The arguments
must have the same name as the entity attributes, so they can be associated with the right elements in the query.

````java
Optional<TaskEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
````

## Raw queries

You can also create raw queries for more complex cases. Don't forget to use the ``nativeQuery`` flag to use SQL, otherwise 
JPA will use its default query language that it is similar to SQL but it references the entity attributes instead of the
table columns

````java
@Query(nativeQuery = true, value = "SELECT c.* FROM comments c INNER JOIN tasks t ON c.task_id = t.id WHERE t.id = :taskId AND c.id = :commentId AND t.user_id = :userId AND c.deleted_at IS NULL AND t.deleted_at IS NULL")
Optional<CommentEntity> findBy(UUID taskId, UUID commentId, UUID userId);
````

## Pagination, Sorting

Checkout the [pagination and sorting page](paging-filtering-sorting.md) for more details.

## Soft delete

There is no support for soft delete, you have to implement it yourself and make sure to always query from the database 
taking into account the column that marks an entry as deleted.

For that you can just add a ``deleted_at`` column in the column. When the item is deleted you can updated this field with
the current timestamp. If this filed is not empty, then that means it got deleted.

## Created at and Updated at

Add the following to the entity class:

````java
@Column(name = "created_at")
private OffsetDateTime createdAt;

@Column(name = "updated_at")
private OffsetDateTime updatedAt;

@PrePersist
public void prePersist() {
    this.createdAt = OffsetDateTime.now();
}

@PreUpdate
public void preUpdate() {
    this.updatedAt = OffsetDateTime.now();
}
````

## Enums

Some databases offer the enum data type, but by default JPA does not works well with this kind of data type, so to make things
easier just make it into a string and add the following to the entity:

````java
@Enumerated(value = EnumType.STRING)
private Priority priority;
````

## Relationships

Use annotations to describe relationships between entities:

````java
@OneToOne(fetch = FetchType.LAZY)
private UserEntity user;

@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
        name = "tasks_tags",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
)
private List<TagEntity> tags;

@OneToMany(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_id")
private List<TaskEntity> subTasks;

@OneToMany(fetch = FetchType.LAZY)
@JoinColumn(name = "task_id")
@Where(clause = "deleted_at IS NULL")
private List<CommentEntity> comments;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private UserEntity commenter;
````
Note the use of ``FetchType.LAZY``, this is to make sure that no query is run unless the attribute is called.

## Links

- [Hibernate ORM 6.1.7.Final User Guide](https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#basic-enums)