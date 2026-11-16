package com.happytails.adoption.validation;

import com.happytails.adoption.enums.ApplicationStatus;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class ApplicationStatusTransitionValidator {

    private static final Map<ApplicationStatus, Set<ApplicationStatus>>
            ALLOWED_TRANSITIONS = new EnumMap<>(ApplicationStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(
                ApplicationStatus.SUBMITTED,
                EnumSet.of(
                        ApplicationStatus.WAITING,
                        ApplicationStatus.YOUR_TURN,
                        ApplicationStatus.REJECTED
                )
        );

        ALLOWED_TRANSITIONS.put(
                ApplicationStatus.WAITING,
                EnumSet.of(
                        ApplicationStatus.YOUR_TURN,
                        ApplicationStatus.WITHDRAWN,
                        ApplicationStatus.REJECTED
                )
        );

        ALLOWED_TRANSITIONS.put(
                ApplicationStatus.YOUR_TURN,
                EnumSet.of(
                        ApplicationStatus.WALK_SCHEDULED,
                        ApplicationStatus.EXPIRED,
                        ApplicationStatus.WITHDRAWN,
                        ApplicationStatus.REJECTED
                )
        );

        ALLOWED_TRANSITIONS.put(
                ApplicationStatus.WALK_SCHEDULED,
                EnumSet.of(
                        ApplicationStatus.APPROVED,
                        ApplicationStatus.REJECTED,
                        ApplicationStatus.WITHDRAWN,
                        ApplicationStatus.YOUR_TURN
                )
        );

        ALLOWED_TRANSITIONS.put(
                ApplicationStatus.APPROVED,
                EnumSet.of(
                        ApplicationStatus.COMPLETED,
                        ApplicationStatus.WITHDRAWN
                )
        );
    }

    public boolean isAllowed(
            ApplicationStatus from,
            ApplicationStatus to
    ) {
        if (from == null || to == null) {
            return false;
        }

        return ALLOWED_TRANSITIONS
                .getOrDefault(from, Set.of())
                .contains(to);
    }

    public void validate(
            ApplicationStatus from,
            ApplicationStatus to
    ) {
        if (!isAllowed(from, to)) {
            throw new IllegalArgumentException(
                    "Invalid application status transition: "
                            + from + " -> " + to
            );
        }
    }
}