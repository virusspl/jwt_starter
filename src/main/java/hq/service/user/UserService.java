package hq.service.user;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import hq.domain.user.Role;
import hq.domain.user.User;
import hq.dto.user.UserCreateForm;
import hq.dto.user.UserEditForm;

public interface UserService extends UserDetailsService {

	// implementation: UserServiceImpl
	
	public void initDatabase();
	
	Role createRole(String role);
	Role updateRole(Role role);
	
	User createUser(UserCreateForm user);
	User updateUser(Long id, UserEditForm user);
	void updatePassword(User user, String password);
	void deleteById(Long id);
	
	void removeRoleFromUser(String username, String rolename);
	void addRoleToUser(String username, String rolename);
	
	User findByUsername(String username);
	User findById(Long id);
	User findAuthenticatedUser();
	
	List<User> findAllUsers();
	List<User> findUsersIfActive(boolean active);	
	
}
