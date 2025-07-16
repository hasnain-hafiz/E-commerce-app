package com.Ecom.E_commerce.app.service.Authentication;

import com.Ecom.E_commerce.app.utils.request.AuthRequest;
import com.Ecom.E_commerce.app.utils.request.RegisterRequest;

public interface IAuthenticationService {

    String register(RegisterRequest registerRequest);
    String authenticate(AuthRequest authRequest);
}
