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
package me.scana.okgradle.internal.dsl.model.android.externalNativeBuild;

import me.scana.okgradle.internal.dsl.api.android.externalNativeBuild.CMakeModel;
import me.scana.okgradle.internal.dsl.model.android.externalNativeBuild.AbstractBuildModelImpl;
import me.scana.okgradle.internal.dsl.parser.android.externalNativeBuild.CMakeDslElement;
import org.jetbrains.annotations.NotNull;

/**
 * This model is incomplete.
 */
public class CMakeModelImpl extends AbstractBuildModelImpl implements CMakeModel {
  public CMakeModelImpl(@NotNull CMakeDslElement dslElement) {
    super(dslElement);
  }
}