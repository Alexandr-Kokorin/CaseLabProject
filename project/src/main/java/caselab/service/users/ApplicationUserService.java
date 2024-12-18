package caselab.service.users;

import caselab.controller.users.payload.UserResponse;
import caselab.controller.users.payload.UserUpdateRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.entity.search.GenericSpecifications;
import caselab.domain.entity.search.SearchRequest;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.service.secutiry.AuthenticationService;
import caselab.service.users.mapper.UserMapper;
import caselab.service.util.UserUtilService;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ApplicationUserService {

    private final AuthenticationService authService;
    private final UserUtilService userUtilService;

    private final ApplicationUserRepository userRepository;
    private final UserMapper mapper;

    public List<UserResponse> findAllUsers(SearchRequest searchRequest) {
        List<ApplicationUser> users =
            userRepository.findAll(GenericSpecifications.filterBy(searchRequest.getFilters()));
        return users.stream()
            .map(mapper::entityToResponse)
            .toList();
    }

    public List<UserResponse> findAllUsers() {
        return findAllUsers(new SearchRequest(null)); // Передаем фильтры как null, для совместимсти
    }

    public UserResponse findUser(String email) {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));
        return mapper.entityToResponse(user);
    }

    public UserResponse getCurrentUser(Authentication authentication) {
        var currentUser = userUtilService.findUserByAuthentication(authentication);

        return mapper.entityToResponse(currentUser);
    }

    public UserResponse updateUser(Authentication authentication, UserUpdateRequest updateRequest) {
        var userToUpdate = userUtilService.findUserByAuthentication(authentication);

        userToUpdate.setHashedPassword(authService.encodePassword(updateRequest.password()));
        userToUpdate.setDisplayName(updateRequest.displayName());

        return mapper.entityToResponse(userRepository.save(userToUpdate));
    }

    public void deleteUser(Authentication authentication, String email) {
        userUtilService.checkUserGlobalPermission(
            userUtilService.findUserByAuthentication(authentication), GlobalPermissionName.ADMIN);

        userRepository.delete(userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email)));
    }
}
