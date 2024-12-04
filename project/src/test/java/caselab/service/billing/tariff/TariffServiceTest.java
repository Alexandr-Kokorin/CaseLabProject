package caselab.service.billing.tariff;

import caselab.controller.billing.tariff.payload.CreateTariffRequest;
import caselab.controller.billing.tariff.payload.TariffResponse;
import caselab.controller.billing.tariff.payload.UpdateTariffRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.GlobalPermission;
import caselab.domain.entity.Tariff;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.TariffRepository;
import caselab.exception.PermissionDeniedException;
import caselab.exception.entity.not_found.EntityNotFoundException;
import caselab.service.billing.tariff.mapper.TariffMapper;
import caselab.service.util.UserUtilService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.misusing.PotentialStubbingProblem;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TariffServiceTest {
    @Mock
    private TariffRepository tariffRepository;
    @Mock
    private UserUtilService userUtilService;
    @Mock
    private TariffMapper tariffMapper;

    @InjectMocks
    private TariffService tariffService;

    @Test
    @DisplayName("Успешное создание тарифа")
    public void successCreateTariff() {
        var createRequest = createTariffRequest();
        var tariff = createTariff();
        var user = createUser();
        var tariffResponse = createTariffResponse();

        Authentication authentication = Mockito.mock(Authentication.class);
        when(tariffRepository.save(Mockito.any(Tariff.class))).thenReturn(tariff);
        when(userUtilService.findUserByAuthentication(authentication)).thenReturn(user);
        doNothing().when(userUtilService).checkUserGlobalPermission(user, GlobalPermissionName.SUPER_ADMIN);
        when(tariffMapper.entityToResponse(tariff)).thenReturn(tariffResponse);

        TariffResponse tariffResponseTest = tariffService.createTariff(authentication, createRequest);

        assertAll(
            () -> assertEquals(1L, tariffResponseTest.id()),
            () -> assertEquals("name", tariffResponseTest.name()),
            () -> assertEquals("details", tariffResponseTest.tariffDetails()),
            () -> assertEquals(1, tariffResponseTest.userCount()),
            () -> assertEquals(1.0, tariffResponseTest.price())
        );

        verify(userUtilService).checkUserGlobalPermission(user, GlobalPermissionName.SUPER_ADMIN);
        verify(tariffRepository).save(Mockito.any(Tariff.class));
        verify(tariffMapper).entityToResponse(tariff);

    }
    @Test
    @DisplayName("Ошибка при создании тарифа: недостаточно прав")
    public void createTariffWithoutAdminPermission() {
        var createRequest = createTariffRequest();
        Authentication authentication = Mockito.mock(Authentication.class);

        doThrow(new PermissionDeniedException()).when(userUtilService)
            .checkUserGlobalPermission(Mockito.any(ApplicationUser.class), eq(GlobalPermissionName.ADMIN));

        assertThrows(
            PotentialStubbingProblem.class,
            () -> tariffService.createTariff(authentication, createRequest),
            "Доступ к этому ресурсу разрешен только администраторам"
        );
    }

    @Test
    @DisplayName("Поиск тарифа по ID")
    public void findById() {
        var tariff = createTariff();
        var tariffResponse = createTariffResponse();

        when(tariffRepository.findById(1L)).thenReturn(java.util.Optional.of(tariff));
        when(tariffMapper.entityToResponse(tariff)).thenReturn(tariffResponse);

        TariffResponse result = tariffService.findById(1L);

        assertAll(
            () -> assertEquals(1L, result.id()),
            () -> assertEquals("name", result.name()),
            () -> assertEquals("details", result.tariffDetails()),
            () -> assertEquals(1, result.userCount()),
            () -> assertEquals(1.0, result.price())
        );

        verify(tariffRepository).findById(1L);
        verify(tariffMapper).entityToResponse(tariff);
    }
    @Test
    @DisplayName("Ошибка при поиске тарифа: не найден по ID")
    public void findTariffByIdNotFound() {
        long tariffId = 999L;

        when(tariffRepository.findById(tariffId)).thenReturn(Optional.empty());

        assertThrows(
            EntityNotFoundException.class,
            () -> tariffService.findById(tariffId),
            "Тариф не существует"
        );

        verify(tariffRepository).findById(tariffId);
    }

    @Test
    @DisplayName("Ошибка при обновлении тарифа: недостаточно прав")
    public void updateTariffWithoutAdminPermission() {
        var createRequest = createUpdateTariffRequest();
        var tariff = createTariff();

        Authentication authentication = Mockito.mock(Authentication.class);

        doThrow(new PermissionDeniedException()).when(userUtilService)
            .checkUserGlobalPermission(Mockito.any(ApplicationUser.class), eq(GlobalPermissionName.ADMIN));

        assertThrows(
            PotentialStubbingProblem.class,
            () -> tariffService.updateTariff(authentication,tariff.getId(), createRequest),
            "Доступ к этому ресурсу разрешен только администраторам"
        );
    }

    @Test
    @DisplayName("Успешное обновление тарифа")
    public void updateTariff() {
        var tariff = createTariff();
        var updatedTariff = createUpdatedTariff();
        var user = createUser();
        var updateRequest = createUpdateTariffRequest();
        var tariffResponse = createUpdatedTariffResponse();

        Authentication authentication = Mockito.mock(Authentication.class);
        when(userUtilService.findUserByAuthentication(authentication)).thenReturn(user);
        doNothing().when(userUtilService).checkUserGlobalPermission(user, GlobalPermissionName.SUPER_ADMIN);
        when(tariffRepository.findById(1L)).thenReturn(java.util.Optional.of(tariff));
        when(tariffMapper.entityFromRequest(updateRequest)).thenReturn(updatedTariff);
        when(tariffMapper.entityToResponse(updatedTariff)).thenReturn(tariffResponse);
        when(tariffRepository.save(updatedTariff)).thenReturn(updatedTariff);

        TariffResponse result = tariffService.updateTariff(authentication, 1L, updateRequest);

        assertAll(
            () -> assertEquals(1L, result.id()),
            () -> assertEquals("updatedName", result.name()),
            () -> assertEquals("updatedDetails", result.tariffDetails()),
            () -> assertEquals(2, result.userCount()),
            () -> assertEquals(2.0, result.price())
        );

        verify(userUtilService).checkUserGlobalPermission(user, GlobalPermissionName.SUPER_ADMIN);
        verify(tariffRepository).findById(1L);
        verify(tariffRepository).save(updatedTariff);
        verify(tariffMapper).entityFromRequest(updateRequest);
        verify(tariffMapper).entityToResponse(updatedTariff);
    }

    @Test
    @DisplayName("Успешное удаление тарифа")
    public void deleteTariff() {
        var tariff = createTariff();
        var user = createUser();

        Authentication authentication = Mockito.mock(Authentication.class);
        when(userUtilService.findUserByAuthentication(authentication)).thenReturn(user);
        doNothing().when(userUtilService).checkUserGlobalPermission(user, GlobalPermissionName.SUPER_ADMIN);
        when(tariffRepository.findById(1L)).thenReturn(java.util.Optional.of(tariff));

        tariffService.deleteTariff(authentication, 1L);

        verify(userUtilService).checkUserGlobalPermission(user, GlobalPermissionName.SUPER_ADMIN);
        verify(tariffRepository).findById(1L);
        verify(tariffRepository).delete(tariff);
    }

    @Test
    @DisplayName("Успешное получение всех тарифов")
    public void getAllTariffs() {
        var tariffs = List.of(createTariff());
        var tariffResponses = List.of(createTariffResponse());

        when(tariffRepository.findAll(Mockito.any(Pageable.class))).thenReturn(new PageImpl<>(tariffs));
        when(tariffMapper.entityToResponse(Mockito.any(Tariff.class))).thenReturn(tariffResponses.get(0));

        Page<TariffResponse> result = tariffService.getAllTariffs(0, 10, "ASC");

        assertEquals(1, result.getContent().size());
        assertEquals("name", result.getContent().get(0).name());

        verify(tariffRepository).findAll(Mockito.any(Pageable.class));
        verify(tariffMapper).entityToResponse(Mockito.any(Tariff.class));
    }



    private ApplicationUser createUser() {
        GlobalPermission permission = new GlobalPermission();
        permission.setName(GlobalPermissionName.ADMIN);

        return ApplicationUser.builder()
            .displayName("admin")
            .email("admin@gmail.com")
            .globalPermissions(
                List.of(
                    permission
                )
            )
            .build()
            ;
    }

    private CreateTariffRequest createTariffRequest(){
        return CreateTariffRequest.builder()
            .name("name")
            .tariffDetails("details")
            .price(1.0)
            .userCount(1)
            .build();
    }

    private TariffResponse createTariffResponse() {
        return TariffResponse.builder()
            .id(1L)
            .name("name")
            .tariffDetails("details")
            .price(1.0)
            .userCount(1)
            .build();
    }

    private Tariff createTariff(){
        return Tariff.builder()
            .id(1L)
            .tariffDetails("details")
            .name("name")
            .price(1.0)
            .userCount(1)
            .build();
    }
    private Tariff createUpdatedTariff() {
        return Tariff.builder()
            .id(1L)
            .tariffDetails("updatedDetails")
            .name("updatedName")
            .price(2.0)
            .userCount(2)
            .build();
    }

    private TariffResponse createUpdatedTariffResponse() {
        return TariffResponse.builder()
            .id(1L)
            .name("updatedName")
            .tariffDetails("updatedDetails")
            .price(2.0)
            .userCount(2)
            .build();
    }

    private UpdateTariffRequest createUpdateTariffRequest() {
        return UpdateTariffRequest.builder()
            .name("updatedName")
            .tariffDetails("updatedDetails")
            .price(2.0)
            .userCount(2)
            .build();
    }

}
