/*
 * This file is part of LSPosed.
 *
 * LSPosed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSPosed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSPosed.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2021 LSPosed Contributors
 */

import org.apache.commons.codec.binary.Hex
import org.apache.tools.ant.filters.FixCrLfFilter
import org.apache.tools.ant.filters.ReplaceTokens
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.*

plugins {
    id("com.android.library")
}

val moduleName = "LSPosed"
val moduleBaseId = "lsposed"
val authors = "LSPosed Developers"

val riruModuleId = "lsposed"
val moduleMinRiruApiVersion = 25
val moduleMinRiruVersionName = "25.0.1"
val moduleMaxRiruApiVersion = 25

val injectedPackageName = "com.android.shell"
val injectedPackageUid = 2000

val defaultManagerPackageName: String by rootProject.extra
val apiCode: Int by rootProject.extra
val verCode: Int by rootProject.extra
val verName: String by rootProject.extra

val androidTargetSdkVersion: Int by rootProject.extra
val androidMinSdkVersion: Int by rootProject.extra
val androidBuildToolsVersion: String by rootProject.extra
val androidCompileSdkVersion: Int by rootProject.extra
val androidCompileNdkVersion: String by rootProject.extra
val androidSourceCompatibility: JavaVersion by rootProject.extra
val androidTargetCompatibility: JavaVersion by rootProject.extra

android {
    compileSdk = androidCompileSdkVersion
    ndkVersion = androidCompileNdkVersion
    buildToolsVersion = androidBuildToolsVersion

    flavorDimensions += "api"

    buildFeatures {
        prefab = true
    }

    defaultConfig {
        minSdk = androidMinSdkVersion
        targetSdk = androidTargetSdkVersion
        multiDexEnabled = false

        externalNativeBuild {
            ndkBuild {
                arguments += "INJECTED_AID=$injectedPackageUid"
                arguments += "VERSION_CODE=$verCode"
                arguments += "VERSION_NAME=$verName"
                arguments += "-j${Runtime.getRuntime().availableProcessors()}"
            }
        }

        buildConfigField("int", "API_CODE", "$apiCode")
        buildConfigField(
            "String",
            "DEFAULT_MANAGER_PACKAGE_NAME",
            """"$defaultManagerPackageName""""
        )
        buildConfigField("String", "MANAGER_INJECTED_PKG_NAME", """"$injectedPackageName"""")
        buildConfigField("int", "MANAGER_INJECTED_UID", """$injectedPackageUid""")
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
        }
    }
    externalNativeBuild {
        ndkBuild {
            path("src/main/cpp/Android.mk")
        }
    }

    compileOptions {
        targetCompatibility(androidTargetCompatibility)
        sourceCompatibility(androidSourceCompatibility)
    }

    buildTypes {
        all {
            externalNativeBuild {
                ndkBuild {
                    arguments += "NDK_OUT=${File(buildDir, ".cxx/$name").absolutePath}"
                }
            }
        }
    }

    productFlavors {
        all {
            externalNativeBuild {
                ndkBuild {
                    arguments += "MODULE_NAME=${name.toLowerCase()}_$moduleBaseId"
                    arguments += "API=${name.toLowerCase()}"
                }
            }
            buildConfigField("String", "API", """"$name"""")
        }

        create("Riru") {
            dimension = "api"
            externalNativeBuild {
                ndkBuild {
                    arguments += "API_VERSION=$moduleMaxRiruApiVersion"
                }
            }
        }

        create("Zygisk") {
            dimension = "api"
            externalNativeBuild {
                ndkBuild {
                    arguments += "API_VERSION=1"
                }
            }
        }
    }

}


dependencies {
    // keep this dep since it affects ccache
    implementation("dev.rikka.ndk:riru:26.0.0")
    implementation("dev.rikka.ndk.thirdparty:cxx:1.2.0")
    implementation("io.github.vvb2060.ndk:dobby:1.2")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("de.upb.cs.swt:axml:2.1.2")
    compileOnly("androidx.annotation:annotation:1.3.0")
    compileOnly(project(":hiddenapi-stubs"))
    implementation(project(":hiddenapi-bridge"))
    implementation(project(":manager-service"))
    implementation(project(":daemon-service"))
}
