package caselab.service.signature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import caselab.controller.signature.payload.SignatureResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Signature;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.domain.repository.SignatureRepository;
import caselab.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class SignatureServiceTest {
    @InjectMocks
    private SignatureService signatureService;
    @Mock
    private SignatureRepository signatureRepository;
    @Mock
    private SignatureMapper signatureMapper;
    private Signature signature;
    @Mock
    private MessageSource messageSource;

    @BeforeEach
    public void setup() {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setEmail("test@example.com");

        signature = new Signature();
        signature.setId(1L);
        signature.setStatus(SignatureStatus.NOT_SIGNED);
        signature.setSignedAt(null);
        signature.setApplicationUser(user);
    }

    @Test
    public void testSignatureUpdate_Sign() {

        when(signatureRepository.findById(1L)).thenReturn(Optional.of(signature));
        SignatureResponse expectedResponse = new SignatureResponse(1L, "test", SignatureStatus.SIGNED, null,
            OffsetDateTime.now(),"test",1L);
        when(signatureMapper.entityToSignatureResponse(signature)).thenReturn(expectedResponse);

        SignatureResponse actualResponse = signatureService.signatureUpdate(1L, true);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(signature.getStatus()).isEqualTo(SignatureStatus.SIGNED);
        verify(signatureRepository, times(1)).findById(1L);
        verify(signatureMapper, times(1)).entityToSignatureResponse(signature);
    }

    @Test
    public void testSignatureUpdate_NotSign() {

        when(signatureRepository.findById(1L)).thenReturn(Optional.of(signature));
        SignatureResponse expectedResponse = new SignatureResponse(1L, "test", SignatureStatus.NOT_SIGNED, null,
            OffsetDateTime.now(),"test",1L);
        when(signatureMapper.entityToSignatureResponse(signature)).thenReturn(expectedResponse);

        SignatureResponse actualResponse = signatureService.signatureUpdate(1L, false);


        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(signature.getStatus()).isEqualTo(SignatureStatus.NOT_SIGNED);
        assertThat(signature.getSignedAt()).isNotNull();
        verify(signatureRepository, times(1)).findById(1L);
        verify(signatureMapper, times(1)).entityToSignatureResponse(signature);
    }

    @Test
    public void testSignatureUpdate_NotFound() {

        when(signatureRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception =
            assertThrows(EntityNotFoundException.class, () -> signatureService.signatureUpdate(1L,true));

        String expectedMessage =
            messageSource.getMessage("user.not.found", new Object[] {1L}, Locale.getDefault());
        assertEquals(expectedMessage, exception.getMessage());
    }
}
