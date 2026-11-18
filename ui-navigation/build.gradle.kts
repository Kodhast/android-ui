plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.roborazzi)
}

// Kompilering, SDK-nivåer, testflaggor och publicering står i rotens build.gradle.kts.
// Navigationsramen (flikarnas back stack, skärmbyten, AppNavHost) i en egen modul, så att
// ui-components inte drar in Navigation 3 och kotlinx-serialization.
android {
    namespace = "se.kodhast.ui.navigation"
}

// Testtemat och skärmdumpshjälparna delas med ui-components tester – en kopia, inte två.
androidComponents {
    onVariants { variant ->
        variant.hostTests.values.forEach { it.sources.kotlin?.addStaticSourceDirectory("../ui-components/src/test/kotlin/se/kodhast/ui/components/testing") }
    }
}

roborazzi {
    outputDir.set(file("src/test/screenshots"))
}

dependencies {
    api(project(":ui-components"))
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)
    api(libs.kotlinx.serialization.json)
    // ViewModel per skärm (NavDisplay:s dekoratör) – samma version som apparna.
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    // FakePhoto i de delade testhjälparna.
    testImplementation(libs.coil.compose)
}
