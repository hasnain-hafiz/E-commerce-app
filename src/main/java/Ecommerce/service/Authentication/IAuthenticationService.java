package Ecommerce.service.Authentication;

import Ecommerce.model.user.User;
import Ecommerce.utils.dto.UserDto;
import Ecommerce.utils.request.AuthRequest;
import Ecommerce.utils.request.RegisterRequest;
import Ecommerce.utils.response.AuthResponse;

import java.util.List;

public interface IAuthenticationService {

    AuthResponse register(RegisterRequest registerRequest);
    AuthResponse authenticate(AuthRequest authRequest);

    // NEW (Phase 3)
    AuthResponse refreshAccessToken(String refreshTokenValue);

    List<User> getAllUsers();

    User getUserById(Long id);

    void deleteUserById(Long id);

    List<UserDto> convertAllUsersToDto(List<User> users);

    UserDto convertUserToDto(User user);
}
