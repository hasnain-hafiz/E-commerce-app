package Ecommerce.service.Authentication;

import Ecommerce.model.user.User;
import Ecommerce.utils.dto.UserDto;
import Ecommerce.utils.request.AuthRequest;
import Ecommerce.utils.request.RegisterRequest;

import java.util.List;

public interface IAuthenticationService {

    String register(RegisterRequest registerRequest);
    String authenticate(AuthRequest authRequest);

    List<User> getAllUsers();

    User getUserById(Long id);

    void deleteUserById(Long id);

    List<UserDto> convertAllUsersToDto(List<User> users);

    UserDto convertUserToDto(User user);
}
