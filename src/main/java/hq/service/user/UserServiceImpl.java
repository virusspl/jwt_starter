package hq.service.user;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import hq.domain.user.Role;
import hq.domain.user.User;
import hq.dto.user.UserCreateForm;
import hq.dto.user.UserEditForm;
import hq.exception.UserServiceException;
import hq.mapper.user.UserMapper;
import hq.repo.user.RoleRepo;
import hq.repo.user.UserRepo;
import hq.security.JwtAuthenticationException;
import hq.security.JwtUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

	private final UserRepo userRepo;
	private final RoleRepo roleRepo;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepo.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found " + username));
	}

	@Override
	public User createUser(UserCreateForm form) {
		// if trying to create second ADMIN account
		if (form.getUsername().equalsIgnoreCase("ADMIN") && this.getRawUserOrNull("ADMIN") != null) {
			throw new UserServiceException(HttpStatus.BAD_REQUEST, "Cannot create 'ADMIN' user");
		} else if (!UsernameValidator.isValid(form.getUsername())) {
			throw new UserServiceException(HttpStatus.BAD_REQUEST, UsernameValidator.VALID_USERNAME_SYNTAX_MESSAGE);
		}

		try {
			User user = UserMapper.createFormToUser(form);
			user.setPassword(passwordEncoder.encode(""));
			userRepo.save(user);
			Role userRole = roleRepo.findByName("ROLE_USER")
					.orElseThrow(() -> new UserServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
							"User role 'ROLE_USER' not found!"));
			user.getRoles().add(userRole);
			return user;
		} catch (org.springframework.dao.DataIntegrityViolationException ex) {
			throw new UserServiceException(HttpStatus.BAD_REQUEST, "Username '%s' already taken", form.getUsername());
		}

	}

	@Override
	public User updateUser(Long id, UserEditForm form) {
		User user = this.findById(id);
		// if trying to rename ADMIM to other user
		if (user.getUsername().equalsIgnoreCase("ADMIN") && !form.getUsername().equalsIgnoreCase("ADMIN")) {
			throw new UserServiceException(HttpStatus.BAD_REQUEST, "Cannot change 'ADMIN' username");
		} else if (!UsernameValidator.isValid(form.getUsername())) {
			throw new UserServiceException(HttpStatus.BAD_REQUEST, UsernameValidator.VALID_USERNAME_SYNTAX_MESSAGE);
		}
		try {
			UserMapper.editFormToUser(form, user);
			return userRepo.saveAndFlush(user);
		} catch (Exception ex) {
			throw new UserServiceException(HttpStatus.BAD_REQUEST, "Username '%s' already taken", form.getUsername());
		}

	}

	@Override
	public void updatePassword(User user, String password) {
		user.setPassword(passwordEncoder.encode(password));
		userRepo.save(user);
	}

	@Override
	public void deleteById(Long id) {
		User userToDelete = userRepo.findById(id)
				.orElseThrow(() -> new UserServiceException(HttpStatus.BAD_REQUEST, "User %s not found", id));
		if (userToDelete.getUsername().equalsIgnoreCase("ADMIN")) {
			throw new UserServiceException(HttpStatus.BAD_REQUEST, "Cannot delete 'ADMIN' user");
		}
		userRepo.deleteById(id);
	}

	@Override
	public Role createRole(String role) {
		return roleRepo.save(new Role(null, role));
	}

	@Override
	public Role updateRole(Role role) {
		return roleRepo.save(role);
	}

	@Override
	public void addRoleToUser(String username, String rolename) {
		User user = userRepo.findByUsername(username)
				.orElseThrow(() -> new UserServiceException(HttpStatus.BAD_REQUEST, "User %s not found", username));
		Role role = roleRepo.findByName(rolename)
				.orElseThrow(() -> new UserServiceException(HttpStatus.BAD_REQUEST, "Role %s not found", rolename));
		user.getRoles().add(role);
	}

	@Override
	public void removeRoleFromUser(String username, String rolename) {
		if (username.equalsIgnoreCase("ADMIN") && rolename.equalsIgnoreCase("ROLE_ADMIN")) {
			throw new UserServiceException(HttpStatus.BAD_REQUEST, "Cannot remove 'ROLE_ADMIN' from 'ADMIN' user");
		}

		User user = userRepo.findByUsername(username)
				.orElseThrow(() -> new UserServiceException(HttpStatus.BAD_REQUEST, "User %s not found", username));
		Role role = roleRepo.findByName(rolename)
				.orElseThrow(() -> new UserServiceException(HttpStatus.BAD_REQUEST, "Role %s not found", rolename));
		user.getRoles().remove(role);
	}

	@Override
	public User findByUsername(String username) {
		return userRepo.findByUsername(username)
				.orElseThrow(() -> new UserServiceException(HttpStatus.BAD_REQUEST, "User %s not found", username));
	}

	@Override
	public User findById(Long id) {
		return userRepo.findById(id)
				.orElseThrow(() -> new UserServiceException(HttpStatus.BAD_REQUEST, "User %s not found", id));
	}

	@Override
	public List<User> findAllUsers() {
		return userRepo.findAll();
	}

	@Override
	public List<User> findUsersIfActive(boolean active) {
		return userRepo.findByActive(active);
	}

	@Override
	public User findAuthenticatedUser() {
		try {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
					.getRequest();
			String token = jwtUtil.extractTokenFromHttpRequest(request);
			return userRepo.findByUsername(jwtUtil.extractUsernameFromToken(token)).orElse(null);
		} catch (JwtAuthenticationException e) {
			return null;
		}
	}

	private User getRawUserOrNull(String username) {
		return userRepo.findByUsername(username).orElse(null);
	}

	@Override
	public void initDatabase() {

		if (this.getRawUserOrNull("ADMIN") == null) {
			this.createRole("ROLE_USER");
			this.createRole("ROLE_ADMIN");

			User admin = this.createUser(new UserCreateForm("admin", "Administrator"));
			admin.setActive(true);
			this.updatePassword(admin, "admin");
			this.addRoleToUser("admin", "ROLE_ADMIN");
		}
	}

}
