/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.application.commons.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(ConditionalOnSecondaryStorage.DatabaseTypeNotNone.class)
public @interface ConditionalOnSecondaryStorage {

  class DatabaseTypeNotNone implements Condition {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseTypeNotNone.class);

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
      final Environment env = context.getEnvironment();
      final String dbType = env.getProperty("camunda.database.type");
      if ("none".equalsIgnoreCase(dbType)) {
        LOG.warn(
            "Secondary storage is disabled (camunda.database.type=none). Some features such as webapps will not start unless a secondary storage is configured. See camunda.database.type config.");
        return false;
      }
      return true;
    }
  }
}
