package com.Ecom.E_commerce.app.service.Authentication;

import com.Ecom.E_commerce.app.model.user.User;
import com.Ecom.E_commerce.app.utils.dto.UserDto;
import com.Ecom.E_commerce.app.utils.request.AuthRequest;
import com.Ecom.E_commerce.app.utils.request.RegisterRequest;
import org.springframework.transaction.annotation.Transactional;

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
