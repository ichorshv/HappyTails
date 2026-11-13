package com.happytails.adoption.applications.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.happytails.adoption.applications.domain.Application;
import com.happytails.adoption.applications.domain.ApplicationAnswers;
import com.happytails.adoption.applications.dto.HousingType;
import com.happytails.adoption.enums.ApplicationStatus;
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

  private Application newApplication(Long applicantId, Long animalId) {
    return new Application(
        applicantId, animalId, "ул. Витоша 1", "София", "Столична", Instant.now());
  }
}
