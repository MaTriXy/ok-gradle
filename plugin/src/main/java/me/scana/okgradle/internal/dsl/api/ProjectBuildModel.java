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
package me.scana.okgradle.internal.dsl.api;

import static me.scana.okgradle.internal.dsl.api.GradleBuildModel.tryOrLog;

import me.scana.okgradle.internal.dsl.api.GradleBuildModel;
import me.scana.okgradle.internal.dsl.api.GradleModelProvider;
import me.scana.okgradle.internal.dsl.api.GradleSettingsModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings;
import org.jetbrains.plugins.gradle.settings.GradleSettings;

/**
 * A model representing a whole project. Multiple {@link me.scana.okgradle.internal.dsl.api.GradleBuildModel}s that are obtained via a {@link ProjectBuildModel} will present
 * the same view of the file along with any non-applied changes. Note: An exception to this is applying plugins, these are NOT persistent
 * across different models.
 */
public interface ProjectBuildModel {

  /**
   * This method should never be called on the UI thread, it will cause the parsing of Gradle build files which can take a long time.
   * The returned {@link ProjectBuildModel} is not thread safe. If you need to use the {@link ProjectBuildModel} from a multithreaded
   * environment {@link ProjectBuildModelHandler} provides some basic synchronization.
   *
   * @param project the project to create a model for.
   * @return the model for the project
   */
  @NotNull
  static ProjectBuildModel get(@NotNull Project project) {
    return GradleModelProvider.get().getProjectModel(project);
  }

  /**
   * This method should never be called on the UI thread, it will cause the parsing of Gradle build files which can take a long time.
   * The returned {@link ProjectBuildModel} is not thread safe. If you need to use the {@link ProjectBuildModel} from a multithreaded
   * environment {@link ProjectBuildModelHandler} provides some basic synchronization.
   * <p>
   * This method should be used when the {@link Project} object does not represent the Gradle build that you need to parse,
   * this is the case for composite build.
   *
   * @param hostProject       the host project, this is required to create psi elements
   * @param includedBuildRoot the root path to the included build that should be parsed
   * @return a build model representing the Gradle build files for the Gradle project at {@param includedBuildRoot},
   * or null if not build or settings files were found.
   */
  @Nullable
  static ProjectBuildModel getForCompositeBuild(@NotNull Project hostProject, @NotNull String includedBuildRoot) {
    return GradleModelProvider.get().getProjectModel(hostProject, includedBuildRoot);
  }

  /**
   * This method returns a list of all participating {@link ProjectBuildModel} from a given {@link Project}. This includes any included
   * builds. No ordering is guaranteed.
   * <p>
   * This method should never be called on the UI thread, it will cause the parsing of Gradle build files which can take a long time.
   * The returned {@link ProjectBuildModel} is not thread safe.
   *
   * @param project the project to obtain all the {@link ProjectBuildModel}s for
   * @return a list of all {@link ProjectBuildModel}s
   */
  @NotNull
  static List<ProjectBuildModel> getForIncludedBuilds(@NotNull Project project) {
    List<ProjectBuildModel> result = new ArrayList<>();
    result.add(get(project));

    String basePath = project.getBasePath();
    if (basePath == null) {
      return result;
    }

    GradleProjectSettings settings = GradleSettings.getInstance(project).getLinkedProjectSettings(basePath);
    if (settings == null) {
      return result;
    }

    GradleProjectSettings.CompositeBuild compositeBuild = settings.getCompositeBuild();
    if (compositeBuild == null) {
      return result;
    }

    compositeBuild.getCompositeParticipants().stream().map(build -> build.getRootPath())
      .forEach(path -> result.add(getForCompositeBuild(project, path)));
    return result;
  }

  /**
   * Attempts to get the {@link ProjectBuildModel} for the given project, null if ANY (including unchecked) exceptions occurred.
   * Exceptions will be logged via intellijs logger and Android Studios crash reporter.
   * <p>
   * This method should never be called on the UI thread, it will cause a parsing of Gradle build files which can take a long time.
   * If you need to use the {@link ProjectBuildModel} from a multithreaded environment {@link ProjectBuildModelHandler} provides some
   * basic synchronization.
   *
   * @param project the project to create a model for
   * @return the model for the project or null if something went wrong.
   */
  @Nullable
  static ProjectBuildModel getOrLog(@NotNull Project project) {
    return tryOrLog(() -> get(project));
  }

  /**
   * @return the {@link me.scana.okgradle.internal.dsl.api.GradleBuildModel} for this projects root build file, null if no file was found.
   */
  @Nullable
  me.scana.okgradle.internal.dsl.api.GradleBuildModel getProjectBuildModel();

  /**
   * This method should never be called on the UI thread, it can cause the parsing of Gradle build files which can take a long time.
   *
   * @param module the module to get the {@link me.scana.okgradle.internal.dsl.api.GradleBuildModel} for.
   * @return the resulting model, or null if the modules build.gradle file couldn't be found.
   */
  @Nullable
  me.scana.okgradle.internal.dsl.api.GradleBuildModel getModuleBuildModel(@NotNull Module module);

  /**
   * This method should never be called on the UI thread, it can cause the parsing of Gradle build files which can take a long time.
   */
  @Nullable
  me.scana.okgradle.internal.dsl.api.GradleBuildModel getModuleBuildModel(@NotNull File modulePath);

  /**
   * This method should never be called on the UI thread, it can cause the parsing of Gradle build files which can take a long time.
   */
  @NotNull
  me.scana.okgradle.internal.dsl.api.GradleBuildModel getModuleBuildModel(@NotNull VirtualFile file);

  /**
   * This method should never be called on the UI thread, it can cause the parsing of Gradle build files which can take a long time.
   *
   * @return the settings model for this project, or null if no settings file could be found.
   */
  @Nullable
  GradleSettingsModel getProjectSettingsModel();

  /**
   * Applies changes to all {@link me.scana.okgradle.internal.dsl.api.GradleBuildModel}s and the {@link GradleSettingsModel} that have been created by this model.
   */
  void applyChanges();

  /**
   * Resets the state of all {@link me.scana.okgradle.internal.dsl.api.GradleBuildModel}s and the {@link GradleSettingsModel}  that have been created by this model.
   */
  void resetState();

  /**
   * Reparses all {@link me.scana.okgradle.internal.dsl.api.GradleBuildModel}s and the {@link GradleSettingsModel} that have been created by this model.
   * <p>
   * This method should never be called on the UI thread, it will cause the parsing of Gradle build files which can take a long time.
   */
  void reparse();

  /**
   * This method may miss files that should be included in the build if we can't correctly parse the Gradle settings file,
   * this method will parse any files that have not yet been parsed.
   * <p>
   * This method does NOT include files from composite builds, for those another {@link ProjectBuildModel} should be obtained
   *
   * @return a list of all build models that can be created from Gradle build files, this does not include Gradle settings or
   * properties files.
   */
  @NotNull
  List<GradleBuildModel> getAllIncludedBuildModels();
}
