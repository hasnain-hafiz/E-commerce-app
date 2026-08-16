package Ecommerce.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring Security 6's CsrfToken is resolved lazily - the XSRF-TOKEN cookie is
 * only written once something actually reads the token. This endpoint exists
 * purely so the SPA can hit it once on load and get the cookie set before it
 * ever needs to send a state-changing request.
 */
@RestController
@RequestMapping("${api.prefix}")
public class CsrfController {

    @GetMapping("/csrf")
    public void csrf(CsrfToken token) {
        // Injecting CsrfToken as a method parameter forces Spring Security to resolve
        // (and therefore cookie-ify) it. Nothing else to do here.
    }
}
