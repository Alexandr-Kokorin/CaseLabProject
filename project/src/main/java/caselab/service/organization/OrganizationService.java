package caselab.service.organization;

import caselab.controller.organization.payload.CreateOrganizationRequest;
import caselab.controller.organization.payload.OrganizationResponse;
import caselab.controller.organization.payload.UpdateOrganizationRequest;
import caselab.controller.secutiry.payload.RegisterRequest;
import caselab.domain.entity.Organization;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.OrganizationRepository;
import caselab.exception.entity.already_exists.OrganizationAlreadyExistsException;
import caselab.exception.entity.not_found.OrganizationNotFoundException;
import caselab.multitenancy.TenantContext;
import caselab.service.organization.mapper.OrganizationMapper;
import caselab.service.security.AuthenticationService;
import caselab.service.util.UserUtilService;
import java.util.ArrayList;
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
    private final UserUtilService userUtilService;
    private final AuthenticationService authService;

    public OrganizationResponse createOrganization(CreateOrganizationRequest request, Authentication auth) {
        var user = userUtilService.findUserByAuthentication(auth);

        userUtilService.checkUserGlobalPermission(user, GlobalPermissionName.SUPER_ADMIN);

        var orgName = request.name();
        if (orgRepository.existsByName(orgName)) {
            throw new OrganizationAlreadyExistsException("именем", orgName);
        }

        Organization organization = orgMapper.createRequestToEntity(request);
        checkOrganizationExistenceByTenantId(orgName);
        checkInnUniqueness(request.inn(), null);
        organization.setTenantId(orgName);
        organization.setEmployees(new ArrayList<>());

        registerOrganizationAdmin(request, orgName, organization);

        return orgMapper.entityToResponse(organization);
    }

    private void registerOrganizationAdmin(
        CreateOrganizationRequest request,
        String tenantId,
        Organization organization
    ) {
        TenantContext.setTenantId(tenantId);
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email(request.email())
            .displayName(request.displayName())
            .password(request.password())
            .build();

        authService.registerOrgAdmin(registerRequest, organization);
    }

    public OrganizationResponse getOrganization(Long id, Authentication auth) {
        var user = userUtilService.findUserByAuthentication(auth);
        userUtilService.checkUserGlobalPermission(user, GlobalPermissionName.SUPER_ADMIN);

        return orgMapper.entityToResponse(findOrganizationById(id));
    }

    public OrganizationResponse updateOrganization(Long id, UpdateOrganizationRequest request, Authentication auth) {
        var user = userUtilService.findUserByAuthentication(auth);
        userUtilService.checkUserGlobalPermission(
            user, GlobalPermissionName.SUPER_ADMIN);

        var currentOrg = findOrganizationById(id);
        checkInnUniqueness(request.inn(), currentOrg.getInn());

        orgMapper.updateEntityFromRequest(currentOrg, request);

        return orgMapper.entityToResponse(orgRepository.save(currentOrg));
    }

    public void deleteOrganization(Long id, Authentication auth) {
        var user = userUtilService.findUserByAuthentication(auth);
        userUtilService.checkUserGlobalPermission(
            user, GlobalPermissionName.SUPER_ADMIN);

        var org = findOrganizationById(id);

        orgRepository.delete(org);
    }

    private Organization findOrganizationById(Long id) {
        return orgRepository.findById(id).orElseThrow(() -> new OrganizationNotFoundException(id));
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
