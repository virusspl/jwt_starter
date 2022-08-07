package hq.mapper.user;

import hq.domain.user.User;
import hq.dto.user.UserCreateForm;
import hq.dto.user.UserEditForm;

public class UserMapper {

	public static User createFormToUser(UserCreateForm form) {
		User user = new User();
		
		user.setActive(false);
		user.setId(null);
		
		user.setName(form.getName().trim());
		user.setUsername(form.getUsername().trim().toLowerCase());
		
		return user;
	}
	
	public static void editFormToUser(UserEditForm form, User user) {
		user.setActive(form.isActive());
		user.setName(form.getName().trim());
		user.setUsername(form.getUsername().trim().toLowerCase());
	}

}
