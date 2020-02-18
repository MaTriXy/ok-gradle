package me.scana.okgradle.data

import com.android.SdkConstants
import me.scana.okgradle.internal.dsl.api.ProjectBuildModel
import me.scana.okgradle.internal.dsl.api.dependencies.ArtifactDependencySpec
import com.android.tools.idea.gradle.util.GradleUtil
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.impl.DummyProject
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.ui.TextTransferable
import me.scana.okgradle.data.repository.Artifact
import me.scana.okgradle.internal.dsl.api.GradleBuildModel
import me.scana.okgradle.util.Notifier

private const val KAPT_PLUGIN = "kotlin-kapt"

object AddDependencyUseCaseFactory {
    fun create(project: Project?, notifier: Notifier): AddDependencyUseCase {
        return if (project != null) {
            AddDependencyUseCaseImpl(project, notifier)
        } else {
            val copyImpl = AddDependencyUseCaseImpl(DummyProject.getInstance(), notifier)
            CopyOnlyDependencyUseCase(copyImpl)
        }
    }
}

interface AddDependencyUseCase {
    fun addDependency(module: Module, artifact: Artifact)
    fun copyToClipboard(artifact: Artifact)
}

class AddDependencyUseCaseImpl(
        private val project: Project,
        private val notifier: Notifier
) : AddDependencyUseCase {

    override fun addDependency(module: Module, artifact: Artifact) {
        val buildGradleFile = findGradleFile(module)
        buildGradleFile?.let { gradleFile ->
            val gradleBuildModel = ProjectBuildModel.get(project).getModuleBuildModel(gradleFile)
            val dependencies = gradleBuildModel.dependencies()
            val dependencySpec = ArtifactDependencySpec.create(artifact.name, artifact.groupId, artifact.version)
            val dependencyStrategy = AddDependencyStrategyFactory.create(
                    dependencySpec,
                    withKotlinKaptSupport = gradleBuildModel.usesKotlinKapt
            )
            WriteCommandAction.runWriteCommandAction(project) {
                val addedDependencies = dependencyStrategy.addDependency(dependencySpec, dependencies)
                gradleBuildModel.applyChanges()
                val psiFile = PsiManager.getInstance(project).findFile(gradleFile)
                psiFile?.let {
                    CodeStyleManager.getInstance(project).adjustLineIndent(it, 0)
                }
                notifier.showDependenciesAddedMessage(module.name, addedDependencies)
            }
        }
    }

    override fun copyToClipboard(artifact: Artifact) {
        val dependencySpec = ArtifactDependencySpec.create(artifact.name, artifact.groupId, artifact.version)
        val dependencyStrategy = AddDependencyStrategyFactory.create(dependencySpec, withKotlinKaptSupport = false)
        CopyPasteManager.getInstance().setContents(TextTransferable(dependencyStrategy.getDependencyStatements(dependencySpec).joinToString("\n") as String?))
        notifier.showDependenciesStatementCopiedMessage()
    }

    private fun findGradleFile(module: Module): VirtualFile? {
        val buildGradleFile = GradleUtil.getGradleBuildFile(module)
        return buildGradleFile ?: module.moduleFile?.parent?.findChild(SdkConstants.FN_BUILD_GRADLE_KTS)
    }

    private val GradleBuildModel.usesKotlinKapt: Boolean
        get() = plugins().any { it.name().forceString() == KAPT_PLUGIN }
}

class CopyOnlyDependencyUseCase(
        private val addDependencyUseCase: AddDependencyUseCase
) : AddDependencyUseCase by addDependencyUseCase {

    override fun addDependency(module: Module, artifact: Artifact) {
        // just a stub
    }
}

