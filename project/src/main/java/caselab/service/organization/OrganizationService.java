package caselab.service.organization;

import caselab.controller.organization.payload.CreateOrganizationRequest;
import caselab.controller.organization.payload.OrganizationResponse;
import caselab.controller.organization.payload.UpdateOrganizationRequest;
import caselab.controller.secutiry.payload.RegisterRequest;
import caselab.domain.entity.Organization;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.OrganizationRepository;
import caselab.exception.entity.already_exists.OrganizationAlreadyExistsException;
import caselab.service.organization.mapper.OrganizationMapper;
import caselab.service.security.AuthenticationService;
import caselab.service.util.UserUtilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository orgRepository;
    private final OrganizationMapper orgMapper;
    private final AuthenticationService authService;
    private final UserUtilService userUtilService;

    public OrganizationResponse createOrganization(CreateOrganizationRequest request, String tenantId) {
        Organization organization = orgMapper.createRequestToEntity(request);

        checkOrganizationExistenceByTenantId(tenantId);
        checkInnUniqueness(request.inn(), null);

        organization.setTenantId(tenantId);
        organization = orgRepository.save(organization);

        registerOrganizationAdmin(request, organization);

        return orgMapper.entityToResponse(organization);
    }

    public OrganizationResponse getOrganization(Authentication auth) {
        var user = userUtilService.findUserByAuthentication(auth);
        return orgMapper.entityToResponse(user.getOrganization());
    }

    public OrganizationResponse updateOrganization(UpdateOrganizationRequest request, Authentication auth) {
        var user = userUtilService.findUserByAuthentication(auth);

        userUtilService.checkUserGlobalPermission(
            user, GlobalPermissionName.ADMIN);

        var currentOrg = user.getOrganization();
        checkInnUniqueness(request.inn(), currentOrg.getInn());

        orgMapper.updateEntityFromRequest(currentOrg, request);

        return orgMapper.entityToResponse(orgRepository.save(currentOrg));
    }

    public void deleteOrganization(Authentication auth) {
        var user = userUtilService.findUserByAuthentication(auth);
        var org = user.getOrganization();

        userUtilService.checkUserGlobalPermission(
            user, GlobalPermissionName.ADMIN);

        orgRepository.delete(org);
    }

    private void registerOrganizationAdmin(CreateOrganizationRequest request, Organization organization) {
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email(request.email())
            .displayName(request.displayName())
            .password(request.password())
            .build();

        authService.registerOrgAdmin(registerRequest, organization);
    }

    private void checkInnUniqueness(String inn, String currentInn) {
        if (!inn.equals(currentInn) && orgRepository.existsByInn(inn)) {
            throw new OrganizationAlreadyExistsException("ИНН", inn);
        }
    }

    private void checkOrganizationExistenceByTenantId(String tenantId) {
        if (orgRepository.existsByTenantId(tenantId)) {
            throw new OrganizationAlreadyExistsException("id арендатора", tenantId);
        }
    }
}
