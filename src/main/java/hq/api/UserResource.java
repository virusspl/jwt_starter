package hq.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hq.domain.user.User;
import hq.dto.user.RoleToUserForm;
import hq.dto.user.UserCreateForm;
import hq.dto.user.UserEditForm;
import hq.dto.user.UserPasswordForm;
import hq.service.user.UserService;
import hq.util.HqUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserResource {

	private final UserService userService;
	private final HqUtil hqUtil;

	@GetMapping("/users/{active}")
	public ResponseEntity<List<User>> getUsers(@PathVariable boolean active) {
		return ResponseEntity.status(HttpStatus.OK).body(userService.findUsersIfActive(active));
	}

	@GetMapping("/users/get/{id}")
	public ResponseEntity<User> getUser(@PathVariable Long id) {
		return ResponseEntity.status(HttpStatus.OK).body(userService.findById(id));
	}
	
	@DeleteMapping("/users/delete/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable Long id) {
		userService.deleteById(id);
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@PostMapping("/users/create")
	public ResponseEntity<User> createUser(@RequestBody UserCreateForm form) {
		return ResponseEntity.created(hqUtil.getUriForPath("/api/users/create"))
				.body(userService.createUser(form));
	}

	@PutMapping("/users/update/{id}")
	public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserEditForm form) {
		return ResponseEntity.status(HttpStatus.OK).body(userService.updateUser(id, form));
	}

	@PutMapping("/users/password/{id}")
	public ResponseEntity<?> updateUserPassword(@PathVariable Long id, @RequestBody UserPasswordForm form) {
		User user = userService.findById(id);
		if (form.getPassword().equals(form.getRepeatPassword())) {
			userService.updatePassword(user, form.getPassword());
			return ResponseEntity.ok().body(user);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("error", String.format("Passwords do not match")));
		}
	}

	@PutMapping("/users/addroletouser")
	public ResponseEntity<?> addRoleToUser(@RequestBody RoleToUserForm form) {
		userService.addRoleToUser(form.getUsername(), form.getRolename());
		return ResponseEntity.status(HttpStatus.OK).build();

	}
	
	@PutMapping("/users/removerolefromuser")
	public ResponseEntity<?> removeRoleFromUser(@RequestBody RoleToUserForm form) {
		userService.removeRoleFromUser(form.getUsername(), form.getRolename());
		return ResponseEntity.status(HttpStatus.OK).build();
	}

}
