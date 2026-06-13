package com.petcare.user.service;

import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.user.dto.PetUpsertRequest;
import com.petcare.user.service.impl.PetApplicationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for PetApplicationServiceImpl using mocks.
 * Covers failure paths that are difficult or impossible to trigger
 * through integration tests with a real database.
 */
@ExtendWith(MockitoExtension.class)
class PetApplicationServiceImplUnitTest {

    private final PetService petService = mock(PetService.class);
    private final PetApplicationServiceImpl service = new PetApplicationServiceImpl(petService);

    // ======================== HIGH-1: Write failure ========================

    @Nested
    @DisplayName("createCurrentUserPet — save returns false")
    class CreateWriteFailure {

        @Test
        @DisplayName("throws IllegalStateException when save returns false")
        void throwsWhenSaveReturnsFalse() {
            when(petService.save(any())).thenReturn(false);

            PetUpsertRequest request = validRequest();

            assertThatThrownBy(() -> service.createCurrentUserPet(1L, request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("宠物");

            verify(petService).save(any());
        }

        @Test
        @DisplayName("save returns true — no exception thrown")
        void noExceptionWhenSaveReturnsTrue() {
            when(petService.save(any())).thenReturn(true);

            PetUpsertRequest request = validRequest();

            // Should not throw — just verify it completes
            service.createCurrentUserPet(1L, request);

            verify(petService).save(any());
        }
    }

    // ======================== MEDIUM-1: Null safety ========================

    @Nested
    @DisplayName("createCurrentUserPet — null safety at service layer")
    class NullSafety {

        @Test
        @DisplayName("null request throws BusinessException with validation_error")
        void nullRequest() {
            assertThatThrownBy(() -> service.createCurrentUserPet(1L, null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.VALIDATION_ERROR);

            verify(petService, never()).save(any());
        }

        @Test
        @DisplayName("null name throws BusinessException with validation_error")
        void nullName() {
            PetUpsertRequest request = new PetUpsertRequest(
                    null, "CAT", null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> service.createCurrentUserPet(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.VALIDATION_ERROR);

            verify(petService, never()).save(any());
        }

        @Test
        @DisplayName("blank name throws BusinessException with validation_error")
        void blankName() {
            PetUpsertRequest request = new PetUpsertRequest(
                    "   ", "CAT", null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> service.createCurrentUserPet(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.VALIDATION_ERROR);

            verify(petService, never()).save(any());
        }

        @Test
        @DisplayName("null type throws BusinessException with validation_error")
        void nullType() {
            PetUpsertRequest request = new PetUpsertRequest(
                    "团子", null, null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> service.createCurrentUserPet(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code").isEqualTo(ErrorCode.VALIDATION_ERROR);

            verify(petService, never()).save(any());
        }
    }

    // ======================== Helpers ========================

    private static PetUpsertRequest validRequest() {
        return new PetUpsertRequest(
                "团子", "CAT", null, null, null, null, null, null, null, null);
    }
}
