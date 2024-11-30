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
import caselab.service.secutiry.AuthenticationService;
import caselab.service.util.UserUtilService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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

        Organization org = user.getOrganization();
        var updatedOrg = orgMapper.updateRequestToEntity(request);
        updatedOrg.setId(org.getId());
        updatedOrg.setTenantId(org.getTenantId());
        return orgMapper.entityToResponse(orgRepository.save(updatedOrg));
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

    private void checkOrganizationExistenceByTenantId(String tenantId) {
        if (orgRepository.existsByTenantId(tenantId)) {
            throw new OrganizationAlreadyExistsException(tenantId);
        }
    }
}
