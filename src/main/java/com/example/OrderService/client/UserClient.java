package com.example.OrderService.client;

import com.example.OrderService.dto.UserDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "${application.config.users-url}")
public interface UserClient {

    @GetMapping("/api/users/internal/search")
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    UserDTO getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users/internal/search")
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByEmailFallback")
    UserDTO getUserByEmail(@RequestParam("email") String email);

    default UserDTO getUserFallback(Long id, Throwable exception) {
        return new UserDTO(id, "unknown@error.com", "Unknown", "Service Unavailable");
    }

    default UserDTO getUserByEmailFallback(String email, Throwable exception) {
        return new UserDTO(null, email, "Unknown", "Service Unavailable");
    }
}