import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

public interface UserRepository  {

    List<User> findAll();

    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<User> loadAll();

    @Query("SELECT u FROM User u")
    List<User> getUsers();

    Page<User> findAll(Pageable pageable); // OK

    @Query(value = "SELECT * FROM users", countQuery = "SELECT count(*) FROM users", nativeQuery = true)
    Page<User> findAllNative(Pageable pageable); // OK
}