package caselab.service.users;

import caselab.controller.users.payload.UserResponse;
import caselab.controller.users.payload.UserUpdateRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.exception.entity.UserEmailNotFoundException;
import caselab.exception.entity.UserNotFoundException;
import caselab.service.secutiry.AuthenticationService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
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

    public UserResponse findUser(Long id) {
        ApplicationUser user = getUserById(id);
        return mapper.entityToResponse(user);
    }

    public UserResponse findUserByEmail(String email) {
        ApplicationUser user = userRepository.findByEmail(email).orElseThrow(() ->
            new UserEmailNotFoundException(email));
        return mapper.entityToResponse(user);
    }

    public UserResponse updateUser(Long id, UserUpdateRequest updateRequest) {
        ApplicationUser userToUpdate = getUserById(id);

        updatePassword(userToUpdate, updateRequest.password());
        mapper.updateUserFromUpdateRequest(updateRequest, userToUpdate);
        return mapper.entityToResponse(userRepository.save(userToUpdate));
    }

    public void deleteUser(Long id) {
        ApplicationUser user = getUserById(id);
        userRepository.delete(user);
    }

    private ApplicationUser getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }

    private void updatePassword(ApplicationUser userToUpdate, String password) {
        if (Objects.nonNull(password) && !password.isEmpty()) {
            String hashedPassword = authService.encodePassword(password);
            userToUpdate.setHashedPassword(hashedPassword);
        }
    }
}
