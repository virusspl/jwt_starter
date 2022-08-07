package hq.repo.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import hq.domain.user.User;

public interface UserRepo extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);
	List<User> findByActive(boolean active);

}
