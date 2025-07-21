/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.it.rdbms.db.mapping;

import static io.camunda.it.rdbms.db.fixtures.GroupFixtures.createAndSaveGroup;
import static io.camunda.it.rdbms.db.fixtures.MappingRuleFixtures.*;
import static io.camunda.it.rdbms.db.fixtures.MappingRuleFixtures.createAndSaveMapping;
import static io.camunda.it.rdbms.db.fixtures.RoleFixtures.createAndSaveRole;
import static io.camunda.zeebe.protocol.record.value.EntityType.MAPPING;
import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.application.commons.rdbms.RdbmsConfiguration;
import io.camunda.db.rdbms.RdbmsService;
import io.camunda.db.rdbms.read.service.MappingRuleReader;
import io.camunda.db.rdbms.write.RdbmsWriter;
import io.camunda.db.rdbms.write.domain.GroupMemberDbModel;
import io.camunda.db.rdbms.write.domain.RoleMemberDbModel;
import io.camunda.it.rdbms.db.fixtures.GroupFixtures;
import io.camunda.it.rdbms.db.fixtures.MappingRuleFixtures;
import io.camunda.it.rdbms.db.fixtures.RoleFixtures;
import io.camunda.it.rdbms.db.util.RdbmsTestConfiguration;
import io.camunda.search.entities.MappingRuleEntity;
import io.camunda.search.filter.MappingRuleFilter;
import io.camunda.search.page.SearchQueryPage;
import io.camunda.search.query.MappingRuleQuery;
import io.camunda.search.sort.MappingRuleSort;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@Tag("rdbms")
@DataJdbcTest
@ContextConfiguration(classes = {RdbmsTestConfiguration.class, RdbmsConfiguration.class})
@AutoConfigurationPackage
@TestPropertySource(properties = {"spring.liquibase.enabled=false", "camunda.database.type=rdbms"})
public class MappingRuleSpecificFilterIT {

  @Autowired private RdbmsService rdbmsService;

  @Autowired private MappingRuleReader mappingRuleReader;

  private RdbmsWriter rdbmsWriter;

  @BeforeEach
  public void beforeAll() {
    rdbmsWriter = rdbmsService.createWriter(0L);
  }

  @Test
  public void shouldFilterMappingsForGroup() {
    // Create and save a mapping
    final var mappingRule1 = MappingRuleFixtures.createRandomized();
    final var mappingRule2 = MappingRuleFixtures.createRandomized();
    final var mappingRule3 = MappingRuleFixtures.createRandomized();
    createAndSaveMapping(rdbmsWriter, mappingRule1);
    createAndSaveMapping(rdbmsWriter, mappingRule2);
    createAndSaveMapping(rdbmsWriter, mappingRule3);

    final var group = GroupFixtures.createRandomized(b -> b);
    final var anotherGroup = GroupFixtures.createRandomized(b -> b);
    createAndSaveGroup(rdbmsWriter, group);
    createAndSaveGroup(rdbmsWriter, anotherGroup);

    assignMappingToGroup(group.groupId(), mappingRule1.mappingRuleId());
    assignMappingToGroup(group.groupId(), mappingRule2.mappingRuleId());
    assignMappingToGroup(anotherGroup.groupId(), mappingRule3.mappingRuleId());

    final var mappings =
        mappingRuleReader.search(
            new MappingRuleQuery(
                new MappingRuleFilter.Builder().groupId(group.groupId()).build(),
                MappingRuleSort.of(b -> b),
                SearchQueryPage.of(b -> b.from(0).size(5))));

    assertThat(mappings.total()).isEqualTo(2);
  }

  @Test
  public void shouldFilterMappingsForRole() {
    final var role = RoleFixtures.createRandomized(b -> b);
    final var anotherRole = RoleFixtures.createRandomized(b -> b);
    createAndSaveRole(rdbmsWriter, role);
    createAndSaveRole(rdbmsWriter, anotherRole);

    final var mappingRuleId1 = nextStringId();
    final var mappingRuleId2 = nextStringId();
    final var mappingRuleId3 = nextStringId();
    Arrays.asList(mappingRuleId1, mappingRuleId2, mappingRuleId3)
        .forEach(
            mappingId ->
                createAndSaveMapping(
                    rdbmsWriter, createRandomized(m -> m.mappingRuleId(mappingId))));

    addMappingToRole(role.roleId(), mappingRuleId1);
    addMappingToRole(anotherRole.roleId(), mappingRuleId2);
    addMappingToRole(anotherRole.roleId(), mappingRuleId3);

    final var mappings =
        mappingRuleReader.search(
            new MappingRuleQuery(
                new MappingRuleFilter.Builder().roleId(role.roleId()).build(),
                MappingRuleSort.of(b -> b),
                SearchQueryPage.of(b -> b.from(0).size(5))));

    assertThat(mappings.total()).isEqualTo(1);
    assertThat(mappings.items())
        .hasSize(1)
        .extracting(MappingRuleEntity::mappingRuleId)
        .containsOnly(mappingRuleId1);
  }

  private void assignMappingToGroup(final String groupId, final String mappingId) {
    rdbmsWriter.getGroupWriter().addMember(new GroupMemberDbModel(groupId, mappingId, "MAPPING"));
    rdbmsWriter.flush();
  }

  private void addMappingToRole(final String roledId, final String mappingId) {
    rdbmsWriter
        .getRoleWriter()
        .addMember(new RoleMemberDbModel(roledId, mappingId, MAPPING.name()));
    rdbmsWriter.flush();
  }
}
