package caselab.controller.billing.tariff;

import caselab.controller.billing.tariff.payload.CreateTariffRequest;
import caselab.controller.billing.tariff.payload.TariffResponse;
import caselab.controller.billing.tariff.payload.UpdateTariffRequest;
import caselab.service.billing.tariff.TariffService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/tariffs")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@Tag(name = "Тарифы", description = "API для взаимодействия с тарифами")
public class TariffController {
    private final TariffService tariffService;

    @GetMapping("/{id}")
    public TariffResponse getTariffById(@PathVariable("id") Long id) {
        return tariffService.findById(id);
    }


    @PostMapping
    public TariffResponse createTariff(
        Authentication authentication,
        @Valid @RequestBody CreateTariffRequest tariffRequest) {
        return tariffService.createTariff(authentication, tariffRequest);
    }

    @PutMapping("/{id}")
    public TariffResponse updateTariff(
        Authentication authentication,
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdateTariffRequest tariffRequest) {
        return tariffService.updateTariff(authentication, id, tariffRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>  deleteTariff(
        Authentication authentication,
        @PathVariable("id") Long id) {
        tariffService.deleteTariff(authentication, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public Page<TariffResponse> getAllTariffs(
        @RequestParam(value = "pageNum", required = false) Integer pageNum,
        @RequestParam(value = "pageSize", required = false) Integer pageSize,
        @Parameter(description = "Значение может быть desc или asc")
        @RequestParam(value = "sortStrategy", required = false, defaultValue = "desc") String sortStrategy
    ) {
        return tariffService.getAllTariffs(pageNum, pageSize, sortStrategy);
    }
}
