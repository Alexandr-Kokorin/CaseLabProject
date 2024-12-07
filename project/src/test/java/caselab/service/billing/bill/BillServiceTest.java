package caselab.service.billing.bill;

import caselab.controller.billing.bill.payload.BillResponse;
import caselab.controller.billing.tariff.payload.TariffResponse;
import caselab.controller.organization.payload.OrganizationResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Bill;
import caselab.domain.entity.Organization;
import caselab.domain.entity.Tariff;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.BillRepository;
import caselab.domain.repository.OrganizationRepository;
import caselab.domain.repository.TariffRepository;
import caselab.exception.entity.not_found.BillNotFoundException;
import caselab.service.billing.bill.mapper.BillMapper;
import caselab.service.util.UserUtilService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BillServiceTest {
    @Mock
    private BillRepository billRepository;
    @Mock
    private TariffRepository tariffRepository;
    @Mock
    private UserUtilService userUtilService;
    @Mock
    private BillMapper billMapper;
    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private BillService billService;


    @Test
    @DisplayName("Успешное получение счета по ID")
    public void getBillById_Success() {
        var bill = createBill();
        var user = createAdminUser();
        var billResponse = createBillResponse();

        Authentication authentication = Mockito.mock(Authentication.class);
        when(userUtilService.findUserByAuthentication(authentication)).thenReturn(user);
        doNothing().when(userUtilService).checkUserGlobalPermission(user, GlobalPermissionName.ADMIN);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(billMapper.toResponse(bill)).thenReturn(billResponse);


        BillResponse result = billService.getBillById(1L, authentication);

        assertAll(
            () -> assertEquals(1L, result.id()),
            () -> assertEquals("test@org.com", result.email()),
            () -> assertEquals(1L, result.organization().getId())
        );

        verify(billRepository).findById(1L);
        verify(billMapper).toResponse(bill);
    }

    @Test
    @DisplayName("Ошибка при попытке получить счет с ID, которого не существует")
    public void getBillById_NotFound() {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(userUtilService.findUserByAuthentication(authentication)).thenReturn(createAdminUser());
        when(billRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
            BillNotFoundException.class,
            () -> billService.getBillById(999L, authentication)
        );

        verify(billRepository).findById(999L);
    }


    @Test
    @DisplayName("Успешное удаление счета")
    public void deleteBill_Success() {
        var bill = createBill();
        var user = createAdminUser();

        Authentication authentication = Mockito.mock(Authentication.class);
        when(userUtilService.findUserByAuthentication(authentication)).thenReturn(user);
        doNothing().when(userUtilService).checkUserGlobalPermission(user, GlobalPermissionName.ADMIN);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        billService.deleteBill(1L, authentication);

        verify(billRepository).findById(1L);
        verify(billRepository).delete(bill);
    }

    @Test
    @DisplayName("Ошибка при удалении счета, которого не существует")
    public void deleteBill_NotFound() {
        var user = createAdminUser();

        Authentication authentication = Mockito.mock(Authentication.class);
        when(userUtilService.findUserByAuthentication(authentication)).thenReturn(user);
        when(billRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
            BillNotFoundException.class,
            () -> billService.deleteBill(1L, authentication)
        );

        verify(billRepository).findById(1L);
    }


    // Вспомогательные методы для создания объектов
    private Bill createBill() {
        var organization = createOrganization();
        var tariff = createTariff();
        return Bill.builder()
            .id(1L)
            .email("test@org.com")
            .tariff(tariff)
            .organization(organization)
            .build();
    }


    private BillResponse createBillResponse() {
        return BillResponse.builder()
            .id(1L)
            .email("test@org.com")
            .organization(createOrganizationResponse())
            .tariff(createTariffResponse())
            .build();
    }


    private Tariff createTariff() {
        return Tariff.builder()
            .id(1L)
            .name("Basic Tariff")
            .userCount(10)
            .price(100.0)
            .build();
    }

    private TariffResponse createTariffResponse() {
        return TariffResponse.builder()
            .id(1L)
            .name("Basic Tariff")
            .userCount(10)
            .price(100.0)
            .build();
    }

    private Organization createOrganization() {
        return Organization.builder()
            .id(1L)
            .name("Test Organization")
            .inn("1234567890")
            .build();
    }

    private OrganizationResponse createOrganizationResponse() {
        return OrganizationResponse.builder()
            .id(1L)
            .name("Test Organization")
            .inn("1234567890")
            .build();
    }
    private ApplicationUser createAdminUser() {
        return ApplicationUser.builder()
            .id(1L)
            .email("admin@test.com")
            .organization(createOrganization())
            .build();
    }
}
