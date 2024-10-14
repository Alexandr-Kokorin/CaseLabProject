package caselab.controller.secutiry;

import caselab.controller.users.payload.UserResponse;
import caselab.service.users.ApplicationUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/example")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class ExampleUserController {

    private final ApplicationUserService userService;

    @GetMapping
    public UserResponse currentUserName(Authentication authentication) {
        var userDetails = (UserDetails) authentication.getPrincipal();
        return userService.findUserByEmail(userDetails.getUsername());
    }
}
