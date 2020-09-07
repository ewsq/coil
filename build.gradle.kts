import coil.by
import coil.groupId
import coil.versionName
import kotlinx.validation.ApiValidationExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import java.net.URL

buildscript {
    apply(from = "buildSrc/extra.gradle.kts")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        jcenter()
    }
    dependencies {
        classpath(rootProject.extra["androidPlugin"].toString())
        classpath(rootProject.extra["kotlinPlugin"].toString())
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.13.0")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.0")
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:0.3.0")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:9.4.1")
    }
}

apply(plugin = "binary-compatibility-validator")

extensions.configure<ApiValidationExtension> {
    ignoredProjects = mutableSetOf("coil-sample", "coil-test")
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    group = project.groupId
    version = project.versionName

    extensions.configure<KtlintExtension>("ktlint") {
        version by "0.40.0"
        disabledRules by setOf("indent", "max-line-length")
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            allWarningsAsErrors = true
            val arguments = mutableListOf("-progressive", "-Xopt-in=kotlin.RequiresOptIn")
            if (project.name != "coil-test") {
                arguments += "-Xopt-in=coil.annotation.ExperimentalCoilApi"
                arguments += "-Xopt-in=coil.annotation.InternalCoilApi"
            }
            freeCompilerArgs = arguments
            jvmTarget = "1.8"
        }
    }

    tasks.withType<Test>().configureEach {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events = setOf(TestLogEvent.SKIPPED, TestLogEvent.PASSED, TestLogEvent.FAILED)
            showStandardStreams = true
        }
    }

    tasks.withType<DokkaTask>().configureEach {
        dokkaSourceSets {
            configureEach {
                jdkVersion by 8
                reportUndocumented by false
                skipDeprecated by true
                skipEmptyPackages by true
                outputDirectory by file("$rootDir/docs/api")

                externalDocumentationLink {
                    url by URL("https://developer.android.com/reference/")
                    packageListUrl by URL("https://developer.android.com/reference/androidx/package-list")
                }
                externalDocumentationLink {
                    url by URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-android/")
                    packageListUrl by URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-android/package-list")
                }
                externalDocumentationLink {
                    url by URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/")
                    packageListUrl by URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/package-list")
                }
                externalDocumentationLink {
                    url by URL("https://square.github.io/okhttp/3.x/okhttp/")
                    packageListUrl by URL("https://square.github.io/okhttp/3.x/okhttp/package-list")
                }
                externalDocumentationLink {
                    url by URL("https://square.github.io/okio/2.x/okio/")
                    packageListUrl by URL("file://$rootDir/package-list-okio")
                }

                // Include the coil-base documentation link for extension artifacts.
                if (project.name != "coil-base") {
                    externalDocumentationLink {
                        url by URL("https://coil-kt.github.io/coil/api/coil-base/")
                        packageListUrl by URL("file://$rootDir/package-list-coil-base")
                    }
                }
            }
        }
    }
}
