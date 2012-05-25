/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2012 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.core.filters;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.web.Criterion;
import org.sonar.api.web.Filter;
import org.sonar.api.web.FilterColumn;
import org.sonar.api.web.FilterTemplate;

/**
 * Default treemap filter.
 *
 * @since 3.1
 */
public class TreeMapFilter extends FilterTemplate {
  public static final String NAME = "Treemap";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Filter createFilter() {
    return Filter.create()
        .setDisplayAs(Filter.TREEMAP)
        .add(Criterion.createForQualifier(Qualifiers.PROJECT))
        .add(FilterColumn.create("name", null, FilterColumn.ASC, false))
        .add(FilterColumn.create("metric", CoreMetrics.NCLOC_KEY, FilterColumn.DESC, false))
        .add(FilterColumn.create("metric", CoreMetrics.VIOLATIONS_DENSITY_KEY, FilterColumn.DESC, false));
  }
}
