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
import caselab.exception.OrganizationAdminMatchException;
import caselab.exception.OrganizationAlreadyBlockedException;
import caselab.exception.entity.not_found.BillNotFoundException;
import caselab.exception.entity.not_found.TariffNotFoundException;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    @DisplayName("Ошибка при получении счета: пользователь не администратор организации")
    public void getBillById_OrganizationAdminMismatch() {
        var user = createAdminUser();
        var bill = createBill();
        bill.getOrganization().setInn("9876543210"); // Измененный ИНН для несовпадения

        Authentication authentication = Mockito.mock(Authentication.class);
        when(userUtilService.findUserByAuthentication(authentication)).thenReturn(user);
        doNothing().when(userUtilService).checkUserGlobalPermission(user, GlobalPermissionName.ADMIN);
        when(billRepository.findById(bill.getId())).thenReturn(Optional.of(bill));

        assertThrows(
            OrganizationAdminMatchException.class,
            () -> billService.getBillById(bill.getId(), authentication)
        );

        verify(billRepository).findById(bill.getId());
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

    @Test
    @DisplayName("Создание счета для организации")
    public void createBillForOrganization_Success() {
        var user = createAdminUser();
        var organization = createOrganizationWithEmployees(10);
        var tariff = createTariff();

        when(tariffRepository.findAll()).thenReturn(List.of(tariff));
        when(billRepository.save(Mockito.any(Bill.class))).thenAnswer(invocation -> invocation.getArgument(0));

        billService.createBillForOrganization(user, organization);

        verify(tariffRepository).findAll();
        verify(billRepository).save(Mockito.any(Bill.class));
    }

    @Test
    @DisplayName("Ошибка при создании счета для организации из-за отсутствия подходящего тарифа")
    public void createBillForOrganization_TariffNotFound() {
        var user = createAdminUser();
        var organization = createOrganizationWithEmployees(100);

        // Репозиторий тарифов возвращает список, где ни один тариф не подходит
        when(tariffRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(
            TariffNotFoundException.class,
            () -> billService.createBillForOrganization(user, organization)
        );

        verify(tariffRepository).findAll();
        verifyNoInteractions(billRepository);
    }

    @Test
    @DisplayName("Блокировка организации")
    public void blockOrganization_Success() {
        var user = createSuperAdminUser();
        var organization = createActiveOrganization();
        var bill = createPaidBill(organization);

        Authentication authentication = Mockito.mock(Authentication.class);
        when(userUtilService.findUserByAuthentication(authentication)).thenReturn(user);
        doNothing().when(userUtilService).checkUserGlobalPermission(user, GlobalPermissionName.SUPER_ADMIN);
        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(billRepository.findByOrganization(organization)).thenReturn(Optional.of(bill));

        billService.blockOrganization(organization.getId(), authentication);

        assertFalse(organization.isActive());
        assertFalse(bill.getIsPaid());
        verify(organizationRepository).save(organization);
    }

    @Test
    @DisplayName("Ошибка при блокировке уже заблокированной организации")
    public void blockOrganization_AlreadyBlocked() {
        var user = createSuperAdminUser();
        var organization = createInactiveOrganization();

        Authentication authentication = Mockito.mock(Authentication.class);
        when(userUtilService.findUserByAuthentication(authentication)).thenReturn(user);
        doNothing().when(userUtilService).checkUserGlobalPermission(user, GlobalPermissionName.SUPER_ADMIN);
        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));

        assertThrows(
            OrganizationAlreadyBlockedException.class,
            () -> billService.blockOrganization(organization.getId(), authentication)
        );

        verify(organizationRepository).findById(organization.getId());
    }

    @Test
    @DisplayName("Активация организации")
    public void activateOrganization_Success() {
        var user = createSuperAdminUser();
        var organization = createInactiveOrganization();
        var bill = createPaidBill(organization);

        Authentication authentication = Mockito.mock(Authentication.class);
        when(userUtilService.findUserByAuthentication(authentication)).thenReturn(user);
        doNothing().when(userUtilService).checkUserGlobalPermission(user, GlobalPermissionName.SUPER_ADMIN);
        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(billRepository.findByOrganization(organization)).thenReturn(Optional.of(bill));

        billService.activateOrganization(organization.getId(), authentication);

        assertTrue(organization.isActive());
        verify(billRepository).save(bill);
    }

    @Test
    @DisplayName("Ошибка при активации организации: счет не найден")
    public void activateOrganization_BillNotFound() {
        var user = createSuperAdminUser();
        var organization = createInactiveOrganization();

        Authentication authentication = Mockito.mock(Authentication.class);
        when(userUtilService.findUserByAuthentication(authentication)).thenReturn(user);
        doNothing().when(userUtilService).checkUserGlobalPermission(user, GlobalPermissionName.SUPER_ADMIN);
        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(billRepository.findByOrganization(organization)).thenReturn(Optional.empty());

        assertThrows(
            BillNotFoundException.class,
            () -> billService.activateOrganization(organization.getId(), authentication)
        );

        verify(billRepository).findByOrganization(organization);
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

    private ApplicationUser createSuperAdminUser() {
        return ApplicationUser.builder()
            .id(2L)
            .email("superadmin@test.com")
            .organization(createOrganization())
            .build();
    }


    private Organization createOrganizationWithEmployees(int employeeCount) {
        var organization = createOrganization();
        organization.setEmployees(new ArrayList<>(Collections.nCopies(employeeCount, new ApplicationUser())));
        return organization;
    }

    private Organization createActiveOrganization() {
        var organization = createOrganization();
        organization.setActive(true);
        return organization;
    }

    private Organization createInactiveOrganization() {
        var organization = createOrganization();
        organization.setActive(false);
        return organization;
    }

    private Bill createPaidBill(Organization organization) {
        return Bill.builder()
            .id(1L)
            .organization(organization)
            .email("test@organization.com")
            .isPaid(true)
            .paidUntil(LocalDateTime.now().plusDays(30))
            .build();
    }
}
