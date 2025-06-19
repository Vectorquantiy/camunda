/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.camunda.client.impl.search.filter;

import io.camunda.client.api.search.filter.RoleFilter;
import io.camunda.client.impl.search.request.TypedSearchRequestPropertyProvider;

public class RoleFilterImpl
    extends TypedSearchRequestPropertyProvider<io.camunda.client.protocol.rest.RoleFilter>
    implements RoleFilter {

  private final io.camunda.client.protocol.rest.RoleFilter filter;

  public RoleFilterImpl() {
    filter = new io.camunda.client.protocol.rest.RoleFilter();
  }

  @Override
  public RoleFilter roleId(final String roleId) {
    filter.setRoleId(roleId);
    return this;
  }

  @Override
  public RoleFilter name(final String name) {
    filter.setName(name);
    return this;
  }

  @Override
  protected io.camunda.client.protocol.rest.RoleFilter getSearchRequestProperty() {
    return filter;
  }
}
