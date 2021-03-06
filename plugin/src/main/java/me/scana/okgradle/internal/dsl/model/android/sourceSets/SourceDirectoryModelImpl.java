/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.scana.okgradle.internal.dsl.model.android.sourceSets;

import me.scana.okgradle.internal.dsl.api.android.sourceSets.SourceDirectoryModel;
import me.scana.okgradle.internal.dsl.api.ext.ResolvedPropertyModel;
import me.scana.okgradle.internal.dsl.model.GradleDslBlockModel;
import me.scana.okgradle.internal.dsl.parser.android.sourceSets.SourceDirectoryDslElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SourceDirectoryModelImpl extends GradleDslBlockModel implements SourceDirectoryModel {
  @NonNls private static final String EXCLUDE = "exclude";
  @NonNls private static final String INCLUDE = "include";
  @NonNls private static final String SRC_DIRS = "srcDirs";

  public SourceDirectoryModelImpl(@NotNull SourceDirectoryDslElement dslElement) {
    super(dslElement);
  }

  @Override
  @NotNull
  public String name() {
    return myDslElement.getName();
  }

  @Override
  @NotNull
  public ResolvedPropertyModel excludes() {
    return getModelForProperty(EXCLUDE, true);
  }

  @Override
  @NotNull
  public ResolvedPropertyModel includes() {
    return getModelForProperty(INCLUDE, true);
  }

  @Override
  @NotNull
  public ResolvedPropertyModel srcDirs() {
    return getModelForProperty(SRC_DIRS, true);
  }
}
