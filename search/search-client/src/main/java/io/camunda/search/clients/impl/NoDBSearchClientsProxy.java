/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.clients.impl;

import io.camunda.search.clients.SearchClientsProxy;
import io.camunda.search.entities.AuthorizationEntity;
import io.camunda.search.entities.BatchOperationEntity;
import io.camunda.search.entities.BatchOperationEntity.BatchOperationItemEntity;
import io.camunda.search.entities.DecisionDefinitionEntity;
import io.camunda.search.entities.DecisionInstanceEntity;
import io.camunda.search.entities.DecisionRequirementsEntity;
import io.camunda.search.entities.FlowNodeInstanceEntity;
import io.camunda.search.entities.FormEntity;
import io.camunda.search.entities.GroupEntity;
import io.camunda.search.entities.GroupMemberEntity;
import io.camunda.search.entities.IncidentEntity;
import io.camunda.search.entities.JobEntity;
import io.camunda.search.entities.MappingRuleEntity;
import io.camunda.search.entities.MessageSubscriptionEntity;
import io.camunda.search.entities.ProcessDefinitionEntity;
import io.camunda.search.entities.ProcessFlowNodeStatisticsEntity;
import io.camunda.search.entities.ProcessInstanceEntity;
import io.camunda.search.entities.RoleEntity;
import io.camunda.search.entities.RoleMemberEntity;
import io.camunda.search.entities.SequenceFlowEntity;
import io.camunda.search.entities.TenantEntity;
import io.camunda.search.entities.TenantMemberEntity;
import io.camunda.search.entities.UserEntity;
import io.camunda.search.entities.UserTaskEntity;
import io.camunda.search.entities.VariableEntity;
import io.camunda.search.exception.NoSecondaryStorageException;
import io.camunda.search.filter.ProcessDefinitionStatisticsFilter;
import io.camunda.search.query.AuthorizationQuery;
import io.camunda.search.query.BatchOperationItemQuery;
import io.camunda.search.query.BatchOperationQuery;
import io.camunda.search.query.DecisionDefinitionQuery;
import io.camunda.search.query.DecisionInstanceQuery;
import io.camunda.search.query.DecisionRequirementsQuery;
import io.camunda.search.query.FlowNodeInstanceQuery;
import io.camunda.search.query.FormQuery;
import io.camunda.search.query.GroupQuery;
import io.camunda.search.query.IncidentQuery;
import io.camunda.search.query.JobQuery;
import io.camunda.search.query.MappingRuleQuery;
import io.camunda.search.query.MessageSubscriptionQuery;
import io.camunda.search.query.ProcessDefinitionQuery;
import io.camunda.search.query.ProcessInstanceQuery;
import io.camunda.search.query.RoleQuery;
import io.camunda.search.query.SearchQueryResult;
import io.camunda.search.query.SequenceFlowQuery;
import io.camunda.search.query.TenantQuery;
import io.camunda.search.query.UsageMetricsQuery;
import io.camunda.search.query.UserQuery;
import io.camunda.search.query.UserTaskQuery;
import io.camunda.search.query.VariableQuery;
import io.camunda.security.auth.SecurityContext;
import java.util.List;

public class NoDBSearchClientsProxy implements SearchClientsProxy {

  @Override
  public SearchQueryResult<AuthorizationEntity> searchAuthorizations(
      final AuthorizationQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<BatchOperationEntity> searchBatchOperations(
      final BatchOperationQuery query) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<BatchOperationItemEntity> searchBatchOperationItems(
      final BatchOperationItemQuery query) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<DecisionDefinitionEntity> searchDecisionDefinitions(
      final DecisionDefinitionQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<DecisionInstanceEntity> searchDecisionInstances(
      final DecisionInstanceQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<DecisionRequirementsEntity> searchDecisionRequirements(
      final DecisionRequirementsQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<FlowNodeInstanceEntity> searchFlowNodeInstances(
      final FlowNodeInstanceQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<FormEntity> searchForms(final FormQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<GroupEntity> searchGroups(final GroupQuery query) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<GroupMemberEntity> searchGroupMembers(final GroupQuery query) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<IncidentEntity> searchIncidents(final IncidentQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<MappingRuleEntity> searchMappingRules(final MappingRuleQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<ProcessDefinitionEntity> searchProcessDefinitions(
      final ProcessDefinitionQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public List<ProcessFlowNodeStatisticsEntity> processDefinitionFlowNodeStatistics(
      final ProcessDefinitionStatisticsFilter filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<ProcessInstanceEntity> searchProcessInstances(
      final ProcessInstanceQuery query) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public List<ProcessFlowNodeStatisticsEntity> processInstanceFlowNodeStatistics(
      final long processInstanceKey) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<RoleEntity> searchRoles(final RoleQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<RoleMemberEntity> searchRoleMembers(final RoleQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<TenantEntity> searchTenants(final TenantQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<TenantMemberEntity> searchTenantMembers(final TenantQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<UserEntity> searchUsers(final UserQuery userQuery) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<UserTaskEntity> searchUserTasks(final UserTaskQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<VariableEntity> searchVariables(final VariableQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<SequenceFlowEntity> searchSequenceFlows(final SequenceFlowQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<MessageSubscriptionEntity> searchMessageSubscriptions(
      final MessageSubscriptionQuery filter) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchClientsProxy withSecurityContext(final SecurityContext securityContext) {
    return this;
  }

  @Override
  public Long countAssignees(final UsageMetricsQuery query) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public Long countProcessInstances(final UsageMetricsQuery query) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public Long countDecisionInstances(final UsageMetricsQuery query) {
    throw new NoSecondaryStorageException();
  }

  @Override
  public SearchQueryResult<JobEntity> searchJobs(final JobQuery query) {
    throw new NoSecondaryStorageException();
  }
}
