package com.happytails.adoption.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.entity.ApplicationAnswers;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.enums.HousingType;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ApplicationRepositoryTest {

  @Autowired private ApplicationRepository applicationRepository;

  @Autowired private ApplicationAnswersRepository answersRepository;

  @Test
  void savesApplicationWithAnswers() {
    Application application = applicationRepository.saveAndFlush(newApplication(1L, 7L));
    answersRepository.saveAndFlush(
        new ApplicationAnswers(
            application,
            HousingType.HOUSE,
            true,
            false,
            false,
            true,
            true,
            4,
            "Искам да дам дом на животно."));

    ApplicationAnswers saved =
        answersRepository.findByApplicationId(application.getId()).orElseThrow();

    assertNotNull(application.getId());
    assertEquals(ApplicationStatus.SUBMITTED, application.getStatus());
    assertEquals("Столична", application.getMunicipality());
    assertTrue(saved.isHasYard());
    assertEquals(4, saved.getHoursAlonePerDay());
  }

  @Test
  void rejectsSecondActiveApplicationForSameAnimal() {
    applicationRepository.saveAndFlush(newApplication(1L, 7L));

    assertThrows(
        DataIntegrityViolationException.class,
        () -> applicationRepository.saveAndFlush(newApplication(1L, 7L)));
  }

  @Test
  void allowsNewApplicationAfterWithdrawal() {
    Application first = applicationRepository.saveAndFlush(newApplication(1L, 7L));
    first.setStatus(ApplicationStatus.WITHDRAWN);
    applicationRepository.saveAndFlush(first);

    assertDoesNotThrow(() -> applicationRepository.saveAndFlush(newApplication(1L, 7L)));
  }

  @Test
  void findsActiveApplicationForSameApplicantAndAnimal() {
    saveWithStatus(1L, 7L, ApplicationStatus.WAITING);

    assertTrue(
        applicationRepository.existsByApplicantIdAndAnimalIdAndStatusIn(
            1L, 7L, ApplicationStatus.ACTIVE));
  }

  @Test
  void ignoresInactiveApplicationsAndOtherAnimalsOrApplicants() {
    saveWithStatus(1L, 9L, ApplicationStatus.WAITING);
    saveWithStatus(1L, 7L, ApplicationStatus.WITHDRAWN);
    saveWithStatus(2L, 8L, ApplicationStatus.WAITING);

    assertFalse(
        applicationRepository.existsByApplicantIdAndAnimalIdAndStatusIn(
            1L, 7L, ApplicationStatus.ACTIVE));
    assertFalse(
        applicationRepository.existsByApplicantIdAndAnimalIdAndStatusIn(
            1L, 8L, ApplicationStatus.ACTIVE));
  }

  @Test
  void countsOnlyActiveApplicationsOfApplicant() {
    saveWithStatus(1L, 7L, ApplicationStatus.SUBMITTED);
    saveWithStatus(1L, 8L, ApplicationStatus.WAITING);
    saveWithStatus(1L, 9L, ApplicationStatus.APPROVED);
    saveWithStatus(1L, 10L, ApplicationStatus.REJECTED);

    assertEquals(
        3, applicationRepository.countByApplicantIdAndStatusIn(1L, ApplicationStatus.ACTIVE));
  }

  @Test
  void doesNotCountApplicationsOfOtherApplicants() {
    saveWithStatus(1L, 7L, ApplicationStatus.WAITING);
    saveWithStatus(2L, 8L, ApplicationStatus.WAITING);

    assertEquals(
        1, applicationRepository.countByApplicantIdAndStatusIn(1L, ApplicationStatus.ACTIVE));
  }

  private Application saveWithStatus(Long applicantId, Long animalId, ApplicationStatus status) {
    Application application = newApplication(applicantId, animalId);
    application.setStatus(status);
    return applicationRepository.saveAndFlush(application);
  }

  private Application newApplication(Long applicantId, Long animalId) {
    return new Application(
        applicantId, animalId, "ул. Витоша 1", "София", "Столична", Instant.now());
  }
}
