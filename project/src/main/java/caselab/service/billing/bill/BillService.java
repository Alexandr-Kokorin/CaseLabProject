package caselab.service.billing.bill;

import caselab.controller.billing.bill.payload.BillResponse;
import caselab.controller.billing.bill.payload.UpdateBillRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Bill;
import caselab.domain.entity.Organization;
import caselab.domain.entity.Tariff;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.BillRepository;
import caselab.domain.repository.OrganizationRepository;
import caselab.domain.repository.TariffRepository;
import caselab.exception.OrganizationAdminMatchException;
import caselab.exception.OrganizationAlreadyBlockedException;
import caselab.exception.entity.not_found.BillNotFoundException;
import caselab.exception.entity.not_found.OrganizationNotFoundException;
import caselab.exception.entity.not_found.TariffNotFoundException;
import caselab.service.billing.bill.mapper.BillMapper;
import caselab.service.util.UserUtilService;
import java.time.LocalDateTime;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BillService {

    private final BillRepository billRepository;
    private final TariffRepository tariffRepository;
    private final UserUtilService userUtilService;
    private final BillMapper billMapper;
    private final OrganizationRepository organizationRepository;


    private final static int BILLING_PERIOD = 30;
    private final static int ONE_DAY = 1;

    public BillResponse getBillById(Long id, Authentication auth) {
        var user = checkPermission(auth);
        var bill = findBillById(id);
        if (!user.getOrganization().getInn().toString().equals(bill.getOrganization().getInn().toString())) {
            throw new OrganizationAdminMatchException(user.getId(), bill.getOrganization().getId());
        }
        return billMapper.toResponse(bill);
    }

    public BillResponse updateBill(Long id, UpdateBillRequest req, Authentication auth) {
        checkPermission(auth);

        var bill = findBillById(id);
        var tariff = findTariffById(req.tariffId());

        bill.setTariff(tariff);
        billRepository.save(bill);

        return billMapper.toResponse(bill);
    }

    public void deleteBill(Long id, Authentication auth) {
        checkPermission(auth);

        var bill = findBillById(id);

        billRepository.delete(bill);
    }

    private ApplicationUser checkPermission(Authentication auth) {
        ApplicationUser user = userUtilService.findUserByAuthentication(auth);
        userUtilService.checkUserGlobalPermission(user, GlobalPermissionName.ADMIN);
        return user;
    }

    private Bill findBillById(Long id) {
        return billRepository.findById(id)
            .orElseThrow(() -> new BillNotFoundException(id));
    }

    private Tariff findTariffById(Long id) {
        return tariffRepository.findById(id)
            .orElseThrow(() -> new TariffNotFoundException(id));
    }

    //метод для создания счета при создании организации
    public void createBillForOrganization(ApplicationUser user, Organization organization) {
        int employeeCount = organization.getEmployees().size();

        Tariff tariff = tariffRepository.findAll().stream()
            .filter(t -> t.getUserCount() >= employeeCount)
            .min(Comparator.comparingInt(Tariff::getUserCount))
            .orElseThrow(() -> new TariffNotFoundException(organization.getId()));

        Bill bill = Bill.builder()
            .tariff(tariff)
            .email(user.getEmail())
            .issuedAt(LocalDateTime.now())
            .paidUntil(LocalDateTime.now().plusDays(BILLING_PERIOD + 1))
            .organization(organization)
            .build();

        billRepository.save(bill);
        log.info("Организация {} зарегестрировала счет с ID {}, срок действия счета до {}",
            organization.getName(), bill.getId(), bill.getPaidUntil()
        );
    }

    //Метод для блокировки организации
    public void blockOrganization(Long organizationId, Authentication authentication) {
        ApplicationUser user = userUtilService.findUserByAuthentication(authentication);
        userUtilService.checkUserGlobalPermission(user, GlobalPermissionName.SUPER_ADMIN);

        Organization organization = findOrganizationById(organizationId);

        if (!organization.isActive()) {
            log.info("Не получилось заблокировать организацию {} с ID {} (уже заблокирована)",
                organization.getName(), organizationId);
            throw new OrganizationAlreadyBlockedException(organization.getName());
        }

        organization.setActive(false);
        Bill bill = billRepository.findByOrganization(organization)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));

        bill.setIsPaid(false);

        bill.setPaidUntil(LocalDateTime.now().minusDays(ONE_DAY));
        organizationRepository.save(organization);

        log.info("Организация с ID {} была заблокирована пользователем {}.",
            organizationId, user.getEmail());
    }



    // Метод для восстановления организации в статус активной
    public void activateOrganization(Long organizationId, Authentication authentication) {
        ApplicationUser user = userUtilService.findUserByAuthentication(authentication);
        userUtilService.checkUserGlobalPermission(user, GlobalPermissionName.SUPER_ADMIN);

        Organization organization = findOrganizationById(organizationId);
        organization.setActive(true);
        organizationRepository.save(organization);

        Bill bill = billRepository.findByOrganization(organization)
            .orElseThrow(() -> new BillNotFoundException(organizationId));

        // Обновить срок действия счёта
        bill.setPaidUntil(LocalDateTime.now().plusDays(BILLING_PERIOD));
        billRepository.save(bill);

        log.info("Организация с ID {} активирована, срок действия счета продлен до {}",
            organizationId, bill.getPaidUntil()
        );
    }


    private Organization findOrganizationById(Long id) {
        return organizationRepository.findById(id)
            .orElseThrow(() -> new OrganizationNotFoundException(id));
    }

}
