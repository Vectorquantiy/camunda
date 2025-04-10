/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {mockXmlGetRequest} from '../../mockRequest';

const mockFetchProcessDefinitionXml = (options?: {
  contextPath?: string;
  processDefinitionKey?: string;
}) => {
  const contextPath = options?.contextPath ?? '';
  const processDefinitionKey =
    options?.processDefinitionKey ?? ':processDefinitionKey';

  return mockXmlGetRequest(
    `${contextPath}/v2/process-definitions/${processDefinitionKey}/xml`,
  );
};

export {mockFetchProcessDefinitionXml};
