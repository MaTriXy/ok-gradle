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
package me.scana.okgradle.internal.dsl.model;

import static me.scana.okgradle.util.AndroidPluginUtils.getBaseDirPath;
import static me.scana.okgradle.internal.dsl.model.GradleBuildModelImpl.populateSiblingDslFileWithGradlePropertiesFile;
import static me.scana.okgradle.internal.dsl.model.GradleBuildModelImpl.populateWithParentModuleSubProjectsProperties;
import static me.scana.okgradle.util.AndroidPluginUtils.getGradleBuildFile;
import static me.scana.okgradle.util.AndroidPluginUtils.getGradleSettingsFile;

import me.scana.okgradle.internal.dsl.api.GradleBuildModel;
import me.scana.okgradle.internal.dsl.api.GradleSettingsModel;
import me.scana.okgradle.internal.dsl.api.ProjectBuildModel;
import me.scana.okgradle.internal.dsl.parser.BuildModelContext;
import me.scana.okgradle.internal.dsl.parser.files.GradleBuildFile;
import me.scana.okgradle.internal.dsl.parser.files.GradleDslFile;
import me.scana.okgradle.internal.dsl.parser.files.GradleSettingsFile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectBuildModelImpl implements ProjectBuildModel {
  @NotNull private final BuildModelContext myBuildModelContext;
  @Nullable private final GradleBuildFile myProjectBuildFile;

  @NotNull
  public static ProjectBuildModel get(@NotNull Project project) {
    VirtualFile file = getGradleBuildFile(getBaseDirPath(project));
    return new ProjectBuildModelImpl(project, file);
  }

  @Nullable
  public static ProjectBuildModel get(@NotNull Project hostProject, @NotNull String compositeRoot) {
    VirtualFile file = getGradleBuildFile(new File(compositeRoot));
    if (file == null) {
      return null;
    }

    return new ProjectBuildModelImpl(hostProject, file);
  }

  /**
   * @param project the project this model should be built for
   * @param file the file contain the projects main build.gradle
   */
  private ProjectBuildModelImpl(@NotNull Project project, @Nullable VirtualFile file) {
    myBuildModelContext = BuildModelContext.create(project);

    // First parse the main project build file.
    myProjectBuildFile = file != null ? new GradleBuildFile(file, project, project.getName(), myBuildModelContext) : null;
    if (myProjectBuildFile != null) {
      myBuildModelContext.setRootProjectFile(myProjectBuildFile);
      ApplicationManager.getApplication().runReadAction(() -> {
        populateWithParentModuleSubProjectsProperties(myProjectBuildFile, myBuildModelContext);
        populateSiblingDslFileWithGradlePropertiesFile(myProjectBuildFile, myBuildModelContext);
        myProjectBuildFile.parse();
      });
      myBuildModelContext.putBuildFile(file.getUrl(), myProjectBuildFile);
    }
  }


  @Override
  @Nullable
  public GradleBuildModel getProjectBuildModel() {
    return myProjectBuildFile == null ? null : new me.scana.okgradle.internal.dsl.model.GradleBuildModelImpl(myProjectBuildFile);
  }

  @Override
  @Nullable
  public GradleBuildModel getModuleBuildModel(@NotNull Module module) {
    VirtualFile file = getGradleBuildFile(module);
    return file == null ? null : getModuleBuildModel(file);
  }

  @Override
  @Nullable
  public GradleBuildModel getModuleBuildModel(@NotNull File modulePath) {
    VirtualFile file = getGradleBuildFile(modulePath);
    return file == null ? null : getModuleBuildModel(file);
  }

  /**
   * Gets the {@link GradleBuildModel} for the given {@link VirtualFile}. Please prefer using {@link #getModuleBuildModel(Module)} if
   * possible.
   *
   * @param file the file to parse, this file should be a Gradle build file that represents a Gradle Project (Idea Module or Project). The
   *             given file must also belong to the {@link Project} for which this {@link ProjectBuildModel} was created.
   * @return the build model for the requested file
   */
  @Override
  @NotNull
  public GradleBuildModel getModuleBuildModel(@NotNull VirtualFile file) {
    GradleBuildFile dslFile = myBuildModelContext.getOrCreateBuildFile(file, false);
    return new me.scana.okgradle.internal.dsl.model.GradleBuildModelImpl(dslFile);
  }

  @Override
  @Nullable
  public GradleSettingsModel getProjectSettingsModel() {
    VirtualFile virtualFile = null;
    // If we don't have a root build file, guess the location of the settings file from the project.
    if (myProjectBuildFile == null) {
      VirtualFile projectDir = ProjectUtil.guessProjectDir(myBuildModelContext.getProject());
      if (projectDir != null) {
        File ioFile = VfsUtilCore.virtualToIoFile(projectDir);
        virtualFile = getGradleSettingsFile(ioFile);
      }
    } else {
      virtualFile = myProjectBuildFile.tryToFindSettingsFile();
    }

    if (virtualFile == null) {
      return null;
    }

    GradleSettingsFile settingsFile = myBuildModelContext.getOrCreateSettingsFile(virtualFile);
    return new GradleSettingsModelImpl(settingsFile);
  }

  @Override
  public void applyChanges() {
    runOverProjectTree(file -> {
      file.applyChanges();
      file.saveAllChanges();
    });

  }

  @Override
  public void resetState() {
    runOverProjectTree(GradleDslFile::resetState);
  }

  @Override
  public void reparse() {
    myBuildModelContext.reset();
    runOverProjectTree(GradleDslFile::reparse);
  }

  @NotNull
  @Override
  public List<GradleBuildModel> getAllIncludedBuildModels() {
    List<GradleBuildModel> allModels = new ArrayList<>();
    if (myProjectBuildFile != null) {
      allModels.add(new GradleBuildModelImpl(myProjectBuildFile));
    }

    GradleSettingsModel settingsModel = getProjectSettingsModel();
    if (settingsModel == null) {
      return allModels;
    }

    allModels.addAll(settingsModel.modulePaths().stream().map((modulePath) -> {
      // This should have already been added above
      if (modulePath.equals(":")) {
        return null;
      }

      File moduleDir = settingsModel.moduleDirectory(modulePath);
      if (moduleDir == null) {
        return null;
      }

      VirtualFile file = getGradleBuildFile(moduleDir);
      if (file == null) {
        return null;
      }

      return getModuleBuildModel(file);
    }).filter(Objects::nonNull).collect(Collectors.toList()));
    return allModels;
  }

  private void runOverProjectTree(@NotNull Consumer<GradleDslFile> func) {
    myBuildModelContext.getAllRequestedFiles().forEach(func);
  }
}
