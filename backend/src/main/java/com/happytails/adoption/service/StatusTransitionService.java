
package com.happytails.adoption.service;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.repository.ApplicationRepository;
import com.happytails.adoption.validation.ApplicationStatusTransitionValidator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatusTransitionService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusTransitionValidator validator =
            new ApplicationStatusTransitionValidator();

    public StatusTransitionService(
            ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public Application changeStatus(
            Long applicationId,
            ApplicationStatus newStatus) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Application not found: " + applicationId
                ));

        validator.validate(application.getStatus(), newStatus);

        application.setStatus(newStatus);

        return applicationRepository.save(application);
    }
}
