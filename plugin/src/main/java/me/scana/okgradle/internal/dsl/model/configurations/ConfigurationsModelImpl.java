/*
 * Copyright (C) 2018 The Android Open Source Project
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
package me.scana.okgradle.internal.dsl.model.configurations;

import me.scana.okgradle.internal.dsl.api.configurations.ConfigurationModel;
import me.scana.okgradle.internal.dsl.api.configurations.ConfigurationsModel;
import me.scana.okgradle.internal.dsl.model.GradleDslBlockModel;
import me.scana.okgradle.internal.dsl.model.configurations.ConfigurationModelImpl;
import me.scana.okgradle.internal.dsl.parser.configurations.ConfigurationDslElement;
import me.scana.okgradle.internal.dsl.parser.configurations.ConfigurationsDslElement;
import me.scana.okgradle.internal.dsl.parser.elements.GradleNameElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationsModelImpl extends GradleDslBlockModel implements ConfigurationsModel {
  public ConfigurationsModelImpl(@NotNull ConfigurationsDslElement element) {
    super(element);
  }

  @Override
  @NotNull
  public List<ConfigurationModel> all() {
    return myDslElement.getPropertyElements(ConfigurationDslElement.class).stream().map(e -> new me.scana.okgradle.internal.dsl.model.configurations.ConfigurationModelImpl(e))
                       .collect(Collectors.toList());
  }

  @Override
  @NotNull
  public ConfigurationModel addConfiguration(@NotNull String name) {
    ConfigurationDslElement configElement = new ConfigurationDslElement(myDslElement, GradleNameElement.create(name));
    myDslElement.setNewElement(configElement);
    return new ConfigurationModelImpl(configElement);
  }

  @Override
  public void removeConfiguration(@NotNull String name) {
    myDslElement.removeProperty(name);
  }
}
