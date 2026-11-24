package com.happytails.adoption.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on {@code @Scheduled} jobs. */
@Configuration
@EnableScheduling
public class SchedulingConfig {}
