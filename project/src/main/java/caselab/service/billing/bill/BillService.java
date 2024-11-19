package caselab.service.billing.bill;

import caselab.controller.billing.bill.payload.BillResponse;
import caselab.controller.billing.bill.payload.CreateBillRequest;
import caselab.controller.billing.bill.payload.UpdateBillRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Bill;
import caselab.domain.entity.Tariff;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.BillRepository;
import caselab.domain.repository.TariffRepository;
import caselab.exception.entity.not_found.BillNotFoundException;
import caselab.exception.entity.not_found.TariffNotFoundException;
import caselab.service.billing.bill.mapper.BillMapper;
import caselab.service.util.UserUtilService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final TariffRepository tariffRepository;
    private final UserUtilService userUtilService;
    private final BillMapper billMapper;

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
        Bill bill = findBillById(id);
        return billMapper.toResponse(bill);
    }

    public BillResponse updateBill(Long id, UpdateBillRequest req, Authentication auth) {
        checkPermission(auth);

        Bill bill = findBillById(id);
        Tariff tariff = findTariffById(req.tariffId());

        bill.setTariff(tariff);
        billRepository.save(bill);

        return billMapper.toResponse(bill);
    }

    public void deleteBill(Long id, Authentication auth) {
        checkPermission(auth);

        Bill bill = findBillById(id);

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

}
