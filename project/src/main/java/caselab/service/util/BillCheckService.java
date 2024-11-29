package caselab.service.util;

import caselab.domain.entity.Bill;
import caselab.domain.entity.Organization;
import caselab.domain.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillCheckService {

    private final BillRepository billRepository;

    @Scheduled(cron = "0 0 0 15 * ?")
    public void checkAllBills() {
        List<Bill> unpaidBills = billRepository.findAllByIsPaidFalse();

        for (Bill bill : unpaidBills) {
            Organization organization = bill.getUser().getOrganization();
            if (organization != null && organization.isActive()) {
                organization.setActive(false);
                log.info("Организация '{}' (ID: {}) деактивирована из-за неоплаченного счета (ID: {}).",
                    organization.getName(), organization.getId(), bill.getId());
            }
        }
    }

}
