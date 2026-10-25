plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.civicpulse.app"
    compileSdk = 35
    defaultConfig { applicationId = "com.civicpulse.app"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "1.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildTypes { release { isMinifyEnabled = false } }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
}
android { testOptions { unitTests.isIncludeAndroidResources = true } }

tasks.withType<Test>().configureEach {
    systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
    // Respect proxy options supplied by the build environment, when present.
    listOf("https.proxyHost", "https.proxyPort", "http.proxyHost", "http.proxyPort", "http.nonProxyHosts").forEach { key ->
        System.getProperty(key)?.let { systemProperty(key, it) }
    }
}
