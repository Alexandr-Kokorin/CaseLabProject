package caselab.service.users;

import caselab.controller.users.payload.UserResponse;
import caselab.controller.users.payload.UserUpdateRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.service.secutiry.AuthenticationService;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationUserService {
    private final ApplicationUserRepository userRepository;
    private final UserMapper mapper;
    private final AuthenticationService authService;
    private final MessageSource messageSource;

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
            .orElseThrow(() -> new NoSuchElementException(
                messageSource.getMessage("user.not.found", new Object[] {id}, Locale.getDefault())
            ));
    }

    private void updatePassword(ApplicationUser userToUpdate, String password) {
        if (password != null && !password.isEmpty()) {
            String hashedPassword = authService.encodePassword(password);
            userToUpdate.setHashedPassword(hashedPassword);
        }
    }
}
