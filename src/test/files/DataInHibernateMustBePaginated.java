import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAll(); // Noncompliant {{Hibernate queries must be paginated to avoid excessive data loading}}

    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<User> loadAll(); // Noncompliant {{Hibernate queries must be paginated to avoid excessive data loading}}

    @Query("SELECT u FROM User u")
    List<User> getUsers(); // Noncompliant {{Hibernate queries must be paginated to avoid excessive data loading}}

    Page<User> findAll(Pageable pageable); // OK

    @Query(value = "SELECT * FROM users", countQuery = "SELECT count(*) FROM users", nativeQuery = true)
    Page<User> findAllNative(Pageable pageable); // OK
}
