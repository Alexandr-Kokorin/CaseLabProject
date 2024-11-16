package caselab.service.billing.tariff;

import caselab.domain.entity.Tariff;
import caselab.domain.repository.TariffRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TariffService {

    private final TariffRepository tariffRepository;

}
