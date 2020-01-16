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
package me.scana.okgradle.internal.dsl.parser.android;

import me.scana.okgradle.internal.dsl.parser.elements.GradleDslElement;
import me.scana.okgradle.internal.dsl.parser.elements.GradleDslSimpleExpression;
import me.scana.okgradle.internal.dsl.parser.elements.GradleNameElement;
import me.scana.okgradle.internal.dsl.parser.elements.GradlePropertiesDslElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class DexOptionsDslElement extends GradlePropertiesDslElement {
  @NonNls public static final String DEX_OPTIONS_BLOCK_NAME = "dexOptions";

  public DexOptionsDslElement(@NotNull GradleDslElement parent) {
    super(parent, null, GradleNameElement.create(DEX_OPTIONS_BLOCK_NAME));
  }

  @Override
  public void addParsedElement(@NotNull GradleDslElement element) {
    if (element instanceof GradleDslSimpleExpression && (element.getName().equals("additionalParameters"))) {
      addAsParsedDslExpressionList((GradleDslSimpleExpression)element);
      return;
    }
    super.addParsedElement(element);
  }
}