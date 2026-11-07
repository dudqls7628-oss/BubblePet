plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
 namespace = "com.example.bubblepet"
 compileSdk = 35
 defaultConfig { applicationId = "com.example.bubblepet"; minSdk = 26; targetSdk = 35; versionCode = 3; versionName = "0.3" }
}