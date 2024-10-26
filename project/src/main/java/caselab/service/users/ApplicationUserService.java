package caselab.service.users;

import caselab.controller.users.payload.UserResponse;
import caselab.controller.users.payload.UserUpdateRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.service.secutiry.AuthenticationService;
import caselab.service.users.mapper.UserMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ApplicationUserService {

    private final ApplicationUserRepository userRepository;
    private final UserMapper mapper;
    private final AuthenticationService authService;

    public List<UserResponse> findAllUsers() {
        List<ApplicationUser> users = userRepository.findAll();
        return users.stream()
            .map(mapper::entityToResponse)
            .toList();
    }

    public UserResponse findUser(String email) {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));
        return mapper.entityToResponse(user);
    }

    public UserResponse updateUser(Authentication authentication, UserUpdateRequest updateRequest) {
        var userToUpdate = findUserByAuthentication(authentication);

        userToUpdate.setHashedPassword(authService.encodePassword(updateRequest.password()));
        userToUpdate.setDisplayName(updateRequest.displayName());

        return mapper.entityToResponse(userRepository.save(userToUpdate));
    }

    public void deleteUser(Authentication authentication) {
        userRepository.delete(findUserByAuthentication(authentication));
    }

    public ApplicationUser findUserByAuthentication(Authentication authentication) {
        var userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new UserNotFoundException(userDetails.getUsername()));
    }
}
