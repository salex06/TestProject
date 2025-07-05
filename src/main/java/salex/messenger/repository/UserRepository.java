package salex.messenger.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import salex.messenger.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE concat(LOWER(:query), '%') AND u.username != :prName")
    Page<User> suggestUsernames(
            @Param("query") String query, Pageable pageable, @Param("prName") String principalUsername);
}
