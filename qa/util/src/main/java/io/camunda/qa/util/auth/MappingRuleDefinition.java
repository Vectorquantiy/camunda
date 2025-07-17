/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.qa.util.auth;

import io.camunda.qa.util.multidb.CamundaMultiDBExtension;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for a mapping definition, that is picked up by the {@link
 * CamundaMultiDBExtension}. This is to clearly communicate that this mapping definition,
 * will be consumed and created (related permissions and memberships) by the {@link CamundaMultiDBExtension}.
 *
 *  <pre>{@code
 *  @Tag("multi-db-test")
 *  final class MyAuthMultiDbTest {
 *
 *    static final TestStandaloneBroker BROKER =
 *        new TestStandaloneBroker().withBasicAuth().withAuthorizationsEnabled();
 *
 *    @RegisterExtension
 *    static final CamundaMultiDBExtension EXTENSION = new CamundaMultiDBExtension(BROKER);
 *
 *    @MappingDefinition
 *    private static final TestMapping MAPPING =
 *      new TestMapping("mappingRuleId",
 *                   "claimName",
 *                   "claimValue",
 *                   List.of(new Permissions(AUTHORIZATION, PermissionTypeEnum.READ, List.of("*"))));
 *
 *    @Test
 *    void shouldHaveCreatedMapping(@Authenticated(ADMIN) final CamundaClient adminClient) {
 *      // The mapping and permissions are created before this test runs
 *    }
 *  }</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MappingRuleDefinition {}
