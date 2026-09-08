package Ecommerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// CHANGED (Phase 3): removed @CrossOrigin(origins = "...") — CORS is
// handled once, globally, by SecurityConfig.corsConfigurationSource().
@RestController
@RequestMapping("${api.prefix}/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is up and running!");
    }
}
