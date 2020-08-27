package com.seannajera.dkouple

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asTypeName
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.KotlinMetadataUtils
import me.eugeniomarletti.kotlin.metadata.extractFullName
import me.eugeniomarletti.kotlin.metadata.hasAnnotations
import me.eugeniomarletti.kotlin.metadata.isPrimary
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import java.io.File
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.tools.Diagnostic
import kotlin.reflect.KClass

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(ComponentManagerProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class ComponentManagerProcessor : KotlinAbstractProcessor(), KotlinMetadataUtils {

    override fun process(
        annotations: Set<TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        if (roundEnv.processingOver()) { return true }

        val dkoupleViews: Set<Element> = roundEnv
            .getElementsAnnotatedWith(DKoupleView::class.java)

        val dkoupleComponentAnnotations: List<Pair<Int, String>> = roundEnv
            .getElementsAnnotatedWith(DKoupleComponent::class.java)
            .map {

                val viewClassFq =
                    it.getAnnotationClassValue<DKoupleComponent> { viewClass }.asTypeName()
                        .toString()
                val layoutId = it.getAnnotation(DKoupleComponent::class.java).layoutId

                Pair(layoutId, viewClassFq)
            }


        generateComponentFactory(dkoupleViews, dkoupleComponentAnnotations)

        return true
    }

    private fun generateComponentFactory(
        dkoupleViewElements: Set<Element>,
        dkoupleComponentLayoutIdAndViewName: List<Pair<Int, String>>
    ) {

        val generatedSourcesRoot: String =
            options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()

        if (generatedSourcesRoot.isEmpty()) {
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                """Couldn't find the target directory for generated Kotlin files. Check the DKouple
                     README.md to ensure your generated build directory is discoverable"""
            )
            return
        }

        // Declare generated ComponentFactory class package
        val dkouplePackageName = this::class.java.`package`.name

        // Declare generated ComponentFactory class name
        val componentFactoryClassBuilder = TypeSpec
            .classBuilder(name = GENERATED_COMPONENT_FACTORY_CLASS_NAME)
            .apply {
                addSuperinterface(ClassName(dkouplePackageName, "ComponentFactory"))
            }

        // Process all DKoupleView constructor dependencies

        // Get all ComponentView constructors
        val dkoupleViewConstructors = dkoupleViewElements
            .filter { it.kotlinMetadata is KotlinClassMetadata && it is TypeElement }

        // create parameter list of all ComponentView dependencies

        val viewConstructorToParameterNames = hashMapOf<String, MutableList<Pair<String, String>>>()
        viewConstructorToParameterNames["$DKOUPLE_PACKAGE_NAME.StaticView"] = mutableListOf(
            "view" to "android.view.View"
        )

        val factoryConstructorParamTypes = mutableSetOf<String>()

        // this is just a map from factoryConstructorParams
        val typeNamesToFactoryParamNames = hashMapOf<String, String>()
            .apply {
                this["android.view.View"] = "view"
                this["$DKOUPLE_PACKAGE_NAME.$COMPONENT_ADAPTER_NAME"] =
                    "$DKOUPLE_PACKAGE_NAME.$COMPONENT_ADAPTER_NAME(this)"
            }

        var paramIndex = 0
        dkoupleViewConstructors.forEach { element ->

            val kotlinClassMetadata = (element.kotlinMetadata as KotlinClassMetadata).data

            val fqClassName =
                kotlinClassMetadata.nameResolver.getString(kotlinClassMetadata.classProto.fqName)
                    .replace('/', '.')

            viewConstructorToParameterNames[fqClassName] = mutableListOf()

            val mainConstructorParameterList = kotlinClassMetadata
                .classProto
                .constructorList
                .single { it.isPrimary }
                .valueParameterList

            val baseViewParam = mainConstructorParameterList
                .first {
                    it.type.extractFullName(kotlinClassMetadata) != "android.view.View"
                        && it.hasAnnotations
                }

            val baseViewArgumentName =
                kotlinClassMetadata.nameResolver.getString(baseViewParam.name)
            val baseViewFqName = baseViewParam.type.extractFullName(kotlinClassMetadata).replace("`","")

            viewConstructorToParameterNames[fqClassName]?.add(baseViewArgumentName to baseViewFqName)

            mainConstructorParameterList
                .filter {
                    it.type.extractFullName(kotlinClassMetadata).replace("`","") != "android.view.View"
                        && !it.hasAnnotations
                }
                .forEach { valueParameter ->
                    val argumentName =
                        kotlinClassMetadata.nameResolver.getString(valueParameter.name)
                    val typeQualifiedName = valueParameter.type.extractFullName(kotlinClassMetadata).replace("`","")

                    viewConstructorToParameterNames[fqClassName]?.add(argumentName to typeQualifiedName)

                    if (typeQualifiedName != "$DKOUPLE_PACKAGE_NAME.$COMPONENT_ADAPTER_NAME") {
                        typeNamesToFactoryParamNames[typeQualifiedName] = "p$paramIndex"
                        factoryConstructorParamTypes.add(typeQualifiedName)
                    }

                    ++paramIndex
                }
        }

        val componentView = ClassName(dkouplePackageName, "ComponentView")
        val component = ClassName(dkouplePackageName, "Component")
        val producerArrayOfThings =
            componentView.parameterizedBy(WildcardTypeName.producerOf(component))

        val stringBuilder = StringBuilder()
        dkoupleComponentLayoutIdAndViewName.forEach { layoutIdAndViewName ->
            val layoutId = layoutIdAndViewName.first

            val viewClassFqName = layoutIdAndViewName.second


            stringBuilder.append("$layoutId -> $viewClassFqName(")

            val viewClassArgs = viewConstructorToParameterNames[viewClassFqName]

            println("$viewClassFqName has ${viewClassArgs?.size} constructor params")
            viewClassArgs?.forEachIndexed { index, argumentNamesToFqClassName ->
                val argumentName = argumentNamesToFqClassName.first
                val parameter = typeNamesToFactoryParamNames[argumentNamesToFqClassName.second]
                stringBuilder.append("$argumentName = $parameter")

                println("$viewClassFqName has $argumentName & $parameter")

                if (index == viewClassArgs.lastIndex) {
                    stringBuilder.append(")")
                } else {
                    stringBuilder.append(", ")
                }
            }

            stringBuilder.append("\n")
        }

        val factoryPrimaryConstructor = FunSpec.constructorBuilder()


        factoryConstructorParamTypes.forEach {
            val paramName = typeNamesToFactoryParamNames[it] ?: return@forEach

            val typeName = it.substringAfterLast(".")
            val packageName = it.substringBeforeLast(".")
            val classType = ClassName(packageName, typeName)

            factoryPrimaryConstructor.addParameter(paramName, classType)

            componentFactoryClassBuilder.addProperty(
                PropertySpec.builder(paramName, classType)
                    .initializer(paramName)
                    .build()
            )
        }

        val componentManagerImplFileBuilder =
            FileSpec.builder(dkouplePackageName, GENERATED_COMPONENT_FACTORY_CLASS_NAME)
                .addType(
                    componentFactoryClassBuilder
                        .primaryConstructor(
                            factoryPrimaryConstructor.build()
                        )
                        .addFunction(
                            FunSpec.builder("createView")
                                .addModifiers(KModifier.OVERRIDE)
                                .addParameter("layoutId", Int::class)
                                .addParameter("view", ClassName("android.view", "View"))
                                .returns(producerArrayOfThings)
                                .addCode(
                                    """|return when (layoutId) {
                                |   $stringBuilder
                                |   else -> {
                                |       val msg = "Layout resource id: ${'$'}layoutId is not found in ComponentFactory"
                                |       throw IllegalArgumentException(msg)
                                |   }
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
        const val DKOUPLE_PACKAGE_NAME = "com.seannajera.dkouple"
        const val COMPONENT_ADAPTER_NAME = "ComponentAdapter"
        const val GENERATED_COMPONENT_FACTORY_CLASS_NAME = "DKoupleComponentFactory"
    }
}

inline fun <reified T : Annotation> Element.getAnnotationClassValue(f: T.() -> KClass<*>?) = try {
    this.getAnnotation(T::class.java).f()
    throw Exception("Expected Type Mirror")
} catch (e: MirroredTypeException) {
    e.typeMirror
}
