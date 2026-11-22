package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.happytails.adoption.dto.ApplicationAddress;
import com.happytails.adoption.dto.ApplicationCreatedResponse;
import com.happytails.adoption.dto.ApplicationRequest;
import com.happytails.adoption.dto.QuestionnaireAnswers;
import com.happytails.adoption.entity.Application;
import com.happytails.adoption.entity.ApplicationAnswers;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.enums.HousingType;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.repository.ApplicationAnswersRepository;
import com.happytails.adoption.repository.ApplicationRepository;
import com.happytails.adoption.validation.ValidationResult;
import com.happytails.adoption.validation.ValidationRule;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmissionServiceTest {

  @Mock private ValidationRule firstRule;
  @Mock private ValidationRule secondRule;
  @Mock private ApplicationRepository applicationRepository;
  @Mock private ApplicationAnswersRepository answersRepository;
  @Mock private StatusTransitionService statusTransitionService;
  @Mock private CurrentUserService currentUserService;

  private ApplicationSubmissionService service;

  @BeforeEach
  void setUp() {
    service =
        new ApplicationSubmissionService(
            List.of(firstRule, secondRule),
            applicationRepository,
            answersRepository,
            statusTransitionService,
            currentUserService);
    when(currentUserService.getCurrentUserId()).thenReturn(1L);
  }

  @Test
  void savesApplicationAndMovesItToWaitingWhenAllRulesPass() {
    when(firstRule.validate(eq(1L), any())).thenReturn(ValidationResult.pass());
    when(secondRule.validate(eq(1L), any())).thenReturn(ValidationResult.pass());
    when(applicationRepository.save(any(Application.class)))
        .thenReturn(application(15L, ApplicationStatus.SUBMITTED));
    when(statusTransitionService.changeStatus(15L, ApplicationStatus.WAITING))
        .thenReturn(application(15L, ApplicationStatus.WAITING));

    ApplicationCreatedResponse response = service.submit(request());

    assertEquals(15L, response.id());
    assertEquals(ApplicationStatus.WAITING, response.status());
    verify(answersRepository).save(any(ApplicationAnswers.class));
  }

  @Test
  void rejectsDuplicateWithConflictAndSavesNothing() {
    when(firstRule.validate(eq(1L), any()))
        .thenReturn(
            ValidationResult.fail(
                "APPLICATION_DUPLICATE", "Вече имате активно заявление за това животно."));

    ApiException ex = assertThrows(ApiException.class, () -> service.submit(request()));

    assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    assertEquals("APPLICATION_DUPLICATE", ex.getCode());
    verifyNoInteractions(secondRule, applicationRepository, answersRepository);
  }

  @Test
  void rejectsLimitReachedWithUnprocessableEntityAndSavesNothing() {
    when(firstRule.validate(eq(1L), any())).thenReturn(ValidationResult.pass());
    when(secondRule.validate(eq(1L), any()))
        .thenReturn(
            ValidationResult.fail(
                "APPLICATION_LIMIT_REACHED", "Можете да имате най-много 3 активни заявления."));

    ApiException ex = assertThrows(ApiException.class, () -> service.submit(request()));

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatus());
    assertEquals("APPLICATION_LIMIT_REACHED", ex.getCode());
    verifyNoInteractions(applicationRepository, answersRepository);
  }

  private Application application(Long id, ApplicationStatus status) {
    Application application =
        new Application(1L, 7L, "ул. Витоша 10", "София", "Столична", Instant.now());
    ReflectionTestUtils.setField(application, "id", id);
    application.setStatus(status);
    return application;
  }

  private ApplicationRequest request() {
    return new ApplicationRequest(
        7L,
        new QuestionnaireAnswers(
            HousingType.HOUSE, true, false, false, true, true, 4, "Искаме да дадем дом."),
        new ApplicationAddress("ул. Витоша 10", "София", "Столична"));
  }
}
