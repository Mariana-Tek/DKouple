package com.seannajera.dkouple

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.KotlinMetadataUtils
import me.eugeniomarletti.kotlin.metadata.isPrimary
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(ComponentManagerProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class ComponentManagerProcessor : KotlinAbstractProcessor(), KotlinMetadataUtils {

    override fun process(
        annotations: Set<TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        if (roundEnv.processingOver()) {
            return true
        }
        val viewClassConstructors = roundEnv.getElementsAnnotatedWith(DKoupleView::class.java)

        generateComponentManager(viewClassConstructors)

        return true
    }

    private fun generateComponentManager(elements: Set<Element>) {
        val generatedSourcesRoot: String =
            options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()

        if (generatedSourcesRoot.isEmpty()) {
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Can't find the target directory for generated Kotlin files."
            )
            return
        }

        val dkouplePackageName = this::class.java.`package`.name

        val componentManagerClass = TypeSpec.classBuilder("ComponentManager_Impl").apply {
            addSuperinterface(ClassName(dkouplePackageName, "ComponentManager"))
        }

        val constructorBuilder = FunSpec.constructorBuilder()


        var name: String = "name"
        val constructorElements =
            elements.filter { it.kotlinMetadata is KotlinClassMetadata && it is TypeElement }
        constructorElements.forEach {

            val kotlinClassMetadata = it.kotlinMetadata as KotlinClassMetadata
            val mainConstructor =
                kotlinClassMetadata.data.classProto.constructorList.find { it.isPrimary }

            mainConstructor?.valueParameterOrBuilderList?.forEach { valueParameterOrBuilder: ProtoBuf.ValueParameterOrBuilder ->

                valueParameterOrBuilder.type
                val nameResolver = kotlinClassMetadata.data.nameResolver
                name = nameResolver.getString(valueParameterOrBuilder.name)
                val type = valueParameterOrBuilder.type
                if (nameResolver.getString(type.className) != "android.view.View") {
                    constructorBuilder
                        // TODO .addParameter(name, ??? TYPE ???)
                }

            }
        }

        val componentView = ClassName(dkouplePackageName, "ComponentView")
        val component = ClassName(dkouplePackageName, "Component")
        val producerArrayOfThings =
            componentView.parameterizedBy(WildcardTypeName.producerOf(component))

        val componentManagerImplFileBuilder =
            FileSpec.builder(dkouplePackageName, "ComponentManager_Impl")
                .addType(
                    componentManagerClass
                        .primaryConstructor(
                            constructorBuilder
                                .addParameter(name, String::class)
                                .build()
                        )
                        .addProperty(
                            PropertySpec.builder(name, String::class)
                                .initializer(name)
                                .build()
                        )
                        .addFunction(
                            FunSpec.builder("createView")
                                .addModifiers(KModifier.OVERRIDE)
                                .addParameter("layoutId", Int::class)
                                .addParameter("view", ClassName("android.view", "View"))
                                .returns(producerArrayOfThings)
                                .addCode(
                                    """|return when (layoutId) {
                                |   R.layout.component_item -> ItemView(view)
                                |   else -> throw IllegalArgumentException("Could not find layout resource with id: ${'$'}layoutId")
                                |}
                                |""".trimMargin()
                                )
                                .build()
                        )
                        .build()
                )

        File(generatedSourcesRoot).apply {
            parentFile.mkdirs()
            componentManagerImplFileBuilder.build().writeTo(this)
        }
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            DKoupleComponent::class.java.canonicalName,
            DKoupleView::class.java.canonicalName
        )
    }

    override fun getSupportedOptions(): MutableSet<String> {
        val supportedOptions = mutableSetOf<String>()
        supportedOptions.add(GRADLE_INCREMENTAL)
        if (options.containsKey(GRADLE_INCREMENTAL)) {
            supportedOptions.add("org.gradle.annotation.processing.isolating")
        }
        return supportedOptions
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        const val GRADLE_INCREMENTAL = "immutables.gradle.incremental"
    }
}
