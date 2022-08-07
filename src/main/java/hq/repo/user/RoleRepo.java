package hq.repo.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hq.domain.user.Role;

public interface RoleRepo extends JpaRepository<Role, Long> {

	Optional<Role> findByName(String name);

}