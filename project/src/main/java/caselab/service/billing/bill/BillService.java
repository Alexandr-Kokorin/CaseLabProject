package caselab.service.billing.bill;

import caselab.controller.billing.bill.payload.BillResponse;
import caselab.controller.billing.bill.payload.CreateBillRequest;
import caselab.controller.billing.bill.payload.UpdateBillRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Bill;
import caselab.domain.entity.Organization;
import caselab.domain.entity.Tariff;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.BillRepository;
import caselab.domain.repository.OrganizationRepository;
import caselab.domain.repository.TariffRepository;
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

    public BillResponse createBill(CreateBillRequest req, Authentication auth) {
        ApplicationUser user = checkPermission(auth);

        Tariff tariff = findTariffById(req.tariffId());

        Bill bill = Bill.builder()
            .tariff(tariff)
            .user(user)
            .issuedAt(LocalDateTime.now())
            .build();

        Bill save = billRepository.save(bill);

        return billMapper.toResponse(save);
    }

    public BillResponse getBillById(Long id, Authentication auth) {
        checkPermission(auth);
        var bill = findBillById(id);
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
    public BillResponse createBillForOrganization(ApplicationUser user, Organization organization) {
        int employeeCount = organization.getEmployees().size();

        Tariff tariff = tariffRepository.findAll().stream()
            .filter(t -> t.getUserCount() >= employeeCount)
            .min(Comparator.comparingInt(Tariff::getUserCount))
            .orElseThrow(() -> new TariffNotFoundException(organization.getId()));

        Bill bill = Bill.builder()
            .tariff(tariff)
            .user(user)
            .issuedAt(LocalDateTime.now())
            .paidUntil(LocalDateTime.now().plusDays(31))
            .build();

        Bill saved = billRepository.save(bill);

        return billMapper.toResponse(saved);

    }

    //Метод для восстановления организации в статус активной
    public void activateOrganization(Long organizationId, Authentication authentication) {
        ApplicationUser user = userUtilService.findUserByAuthentication(authentication);
        userUtilService.checkUserGlobalPermission(user, GlobalPermissionName.ADMIN);

        Organization organization = findOrganizationById(organizationId);
        organization.setActive(true);
        organizationRepository.save(organization);

        Bill bill = billRepository.findByUserOrganizationId(organizationId)
            .orElseThrow(() -> new BillNotFoundException(organizationId));

        bill.setPaidUntil(LocalDateTime.now().plusDays(30));
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
