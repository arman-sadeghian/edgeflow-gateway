package com.sadeghian.edgeflow.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Value("${server.port}")
    private String port;

    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable Long id) {
        return Map.of(
                "id", id,
                "name", "Arman",
                "service", "user-service",
                "instance", port
        );
    }
}