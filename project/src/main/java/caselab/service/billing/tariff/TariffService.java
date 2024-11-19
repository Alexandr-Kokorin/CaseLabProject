package caselab.service.billing.tariff;

import caselab.controller.billing.tariff.payload.CreateTariffRequest;
import caselab.controller.billing.tariff.payload.TariffResponse;
import caselab.controller.billing.tariff.payload.UpdateTariffRequest;
import caselab.domain.entity.Tariff;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.TariffRepository;
import caselab.exception.entity.already_exists.TariffAlreadyExistsException;
import caselab.exception.entity.not_found.TariffNotFoundException;
import caselab.service.billing.tariff.mapper.TariffMapper;
import caselab.service.util.PageUtil;
import caselab.service.util.UserUtilService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.ZoneId;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
@Transactional
@RequiredArgsConstructor
public class TariffService {

    private final TariffRepository tariffRepository;
    private final UserUtilService userUtilService;
    private final TariffMapper tariffMapper;

    public TariffResponse createTariff(Authentication authentication, @Valid CreateTariffRequest tariffRequest) {
        userUtilService.checkUserGlobalPermission(
            userUtilService.findUserByAuthentication(authentication), GlobalPermissionName.ADMIN);
        checkTariffExists(tariffRequest);
        Tariff tariff = new Tariff();
        tariff.setName(tariffRequest.name());
        tariff.setPrice(tariffRequest.price());
        tariff.setUserCount(tariffRequest.userCount());
        tariff.setTariffDetails(tariffRequest.tariffDetails());
        tariff.setCreatedAt(
            Instant
                .ofEpochMilli(System.currentTimeMillis())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        );

        var savedTariff = tariffRepository.save(tariff);

        return tariffMapper.entityToResponse(savedTariff);
    }


    public TariffResponse findById(Long id) {
        return tariffMapper.entityToResponse(findTariffById(id));
    }

    public TariffResponse updateTariff(
        Authentication authentication,
        @PathVariable Long id,
        UpdateTariffRequest tariffRequest
    ) {
        userUtilService.checkUserGlobalPermission(
            userUtilService.findUserByAuthentication(authentication), GlobalPermissionName.ADMIN);

        var tariff = findTariffById(id);
        var updatedTariff = tariffMapper.entityFromRequest(tariffRequest);

        updatedTariff.setId(tariff.getId());
        tariffRepository.save(updatedTariff);

        return tariffMapper.entityToResponse(updatedTariff);
    }


    public void deleteTariff(Authentication authentication, Long id) {

        userUtilService.checkUserGlobalPermission(
            userUtilService.findUserByAuthentication(authentication), GlobalPermissionName.ADMIN);

        var tariff = findTariffById(id);

        tariffRepository.delete(tariff);
    }

    public Page<TariffResponse> getAllTariffs(
        Integer pageNum,
        Integer pageSize,
        String sortStrategy) {
        Pageable pageable = PageUtil.toPageable(pageNum, pageSize, Sort.by("name"), sortStrategy);

        return tariffRepository.findAll(pageable)
            .map(tariffMapper::entityToResponse);
    }

    private Tariff findTariffById(Long id) {
        return tariffRepository.findById(id)
            .orElseThrow(() -> new TariffNotFoundException(id));
    }

    private void checkTariffExists(CreateTariffRequest tariffRequest) {
        var tariff = tariffRepository.findByName(tariffRequest.name());
        if (tariff.isPresent()) {
            throw new TariffAlreadyExistsException(tariff.get().getId());
        }
    }
}
