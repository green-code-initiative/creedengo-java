import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

public interface DataInHibernateMustBePaginatedNoIssue  {

    List<Object> findAll();

    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<Object> loadAll();

    @Query("SELECT u FROM User u")
    List<Object> getUsers();

    Page<Object> findAll(Pageable pageable); // OK

    @Query(value = "SELECT * FROM users", countQuery = "SELECT count(*) FROM users", nativeQuery = true)
    Page<Object> findAllNative(Pageable pageable); // OK
}