plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

afterEvaluate {
    publishing {
        publications {
            // "release"라는 Maven publication을 생성합니다.
            create<MavenPublication>("release") {
                // release 빌드 변형을 위한 컴포넌트 적용.
                from(components["release"])

                // publication의 속성을 커스터마이즈합니다.
                groupId = "com.github.hyunjine"
                artifactId = "final-release" // 여기서 실제 원하는 artifactId로 설정하세요.
                version = "1.0.0"
            }
            // "debug"라는 Maven publication을 생성합니다.
            create<MavenPublication>("debug") {
                // debug 빌드 변형을 위한 컴포넌트 적용.
                from(components["debug"])

                groupId = "com.github.hyunjine"
                artifactId = "final-debug"
                version = "1.0.0"
            }
        }
    }
}



android {
    namespace = "com.example.kokoapractice"
    compileSdk = 34

    defaultConfig {
        minSdk = 29
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
}

group="com.github.kou-jintetsu"
version = "0.9.1"
