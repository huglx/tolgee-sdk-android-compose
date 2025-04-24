# Tolgee SDK for Android

A modern Android SDK for integrating Tolgee translation services into your Android applications. This SDK provides a seamless way to manage translations and localization in your Android apps using Jetpack Compose.

## Features

- Jetpack Compose integration with ready-to-use components
- Easy translation management with `Translate` composable
- Language switching with `TolgeeLanguageDropdown` component
- Room database integration for offline translations
- Koin dependency injection
- Retrofit for API communication
- DataStore for preferences

## Requirements

- Android Studio Hedgehog | 2023.1.1 or newer
- Kotlin 1.9.0 or newer
- Android Gradle Plugin 8.2.0 or newer
- Minimum SDK version: 26 (Android 8.0)
- Target SDK version: 35 (Android 15)

## Dependencies

The SDK has two types of dependencies:

### Required Dependencies


### Internal Dependencies
These dependencies are used internally by the SDK:

```kotlin
dependencies {
    // DataStore (for preferences)
    implementation("androidx.datastore:datastore-preferences:1.1.4")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")
    
    // Koin (for dependency injection)
    implementation("io.insert-koin:koin-core:3.5.3")
    implementation("io.insert-koin:koin-android:3.5.3")
    implementation("io.insert-koin:koin-androidx-compose:3.5.3")
    
    // Retrofit (for API communication)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Room (for database)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}
```

## Installation

### 1. Set up GitHub Authentication

To use GitHub Packages, you need to authenticate with a personal access token. Here's how to set it up:

1. Create a Personal Access Token:
   - Go to GitHub Settings → Developer Settings → Personal Access Tokens → Tokens (classic)
   - Click "Generate new token (classic)"
   - Give it a name (e.g., "Android Package Access")
   - Select the following scopes:
     - `read:packages` (to download packages)
     - `write:packages` (to publish packages)
     - `delete:packages` (to delete packages)
   - Click "Generate token"
   - Copy the token immediately (you won't be able to see it again)

2. Configure Authentication:
   You have two options to set up authentication:

   **Option 1: Environment Variables**
   ```bash
   # Add to your ~/.bash_profile or ~/.zshrc
   export GITHUB_USERNAME=your_github_username
   export GITHUB_TOKEN=your_personal_access_token
   ```

   **Option 2: Gradle Properties**
   Create or edit `~/.gradle/gradle.properties`:
   ```
   gpr.user=your_github_username
   gpr.key=your_personal_access_token
   ```

### 2. Configure Project Settings

Add the following configuration to your project's `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/huglx/tolgee-sdk")
            credentials {
                username = providers.gradleProperty("gpr.user").getOrElse(System.getenv("GITHUB_USERNAME"))
                password = providers.gradleProperty("gpr.key").getOrElse(System.getenv("GITHUB_TOKEN"))
            }
        }
    }
}

rootProject.name = "your_project_name"
include(":app")
```

### 3. Add Dependency

Add the following dependency in your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.huglx:tolgeesdk:0.0.3")
}
```

### 4. Verify Installation

To verify that your authentication is working correctly, you can add a debug task to your `build.gradle.kts`:

```kotlin
tasks.register("printAuth") {
    doLast {
        println("Username: ${project.findProperty("gpr.user") ?: System.getenv("GITHUB_USERNAME")}")
        println("Token exists: ${(project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")) != null}")
    }
}
```

Run it with:
```bash
./gradlew printAuth
```

## Usage

### Basic Setup

1. Add the following metadata to your `AndroidManifest.xml` inside the `<application>` tag:

```xml
<application>
    <!-- Tolgee SDK Configuration -->
    <meta-data
        android:name="cz.fit.cvut.sdk.BASE_URL"
        android:value="https://app.tolgee.io/" />
    <meta-data
        android:name="cz.fit.cvut.sdk.API_KEY"
        android:value="your-api-key" />
    <meta-data
        android:name="cz.fit.cvut.sdk.MODE"
        android:value="DEBUG" />
</application>
```

2. Initialize the SDK in your Application class:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TolgeeSdkInitializer.initializeFromMetadata(this)
    }
}
```

2. Wrap your app with `TolgeeProvider` and use the theme:

```kotlin
@Composable
fun MyApp() {
    TolgeeSdkTheme {
        TolgeeProvider {
            // Your app content
        }
    }
}
```

3. Use translations in your Compose UI:

```kotlin
@Composable
fun MyScreen() {
    Column {
        // Simple translation
        Translate(
            keyName = "welcome_message"
        )
        
        // Translation with parameters
        Translate(
            keyName = "formatted_message",
            params = mapOf(
                "name" to "John",
                "count" to "5"
            )
        )
        
        // Language selector
        TolgeeLanguageDropdown()
    }
}
```

### Advanced Features

#### Navigation Integration

The SDK provides navigation integration, which is necessary to add context to translations. This allows the Tolgee platform to understand the navigation context of your app, enhancing the translation management process:

```kotlin
val navController = rememberNavController()
navController.registerAsRouteProviderForTolgee()
```

### Usage

#### Debug Mode

In debug mode, the Tolgee SDK adds a floating button to your app. When you tap this button, it displays all translations currently visible on the screen. If you long-press on a translation, a dialog will open, allowing you to edit the translation key directly. This feature is particularly useful for developers and translators to quickly access and modify translations during development.

## Using Tolgee SDK

To use the Tolgee SDK, you need to have a Tolgee API key. You can create an API key by signing up for an account on Tolgee. Follow the guide on how to create an API key [here](https://docs.tolgee.io/platform/account_settings/api_keys_and_pat_tokens).

### Adding Translations

In your Tolgee project, you can add translation keys to manage the text displayed in your application. You can add new translation keys by following the guide [here](https://docs.tolgee.io/platform/translation_keys/keys). Once you have added a translation key, you can use the key name in your components.

### Example Usage

Here's an example of how you can use a translation key in a Jetpack Compose component:

```kotlin
Text(
    text = t("app_subscriptions"),
    modifier = modifier
)
```
Alternatively, you can use:
```kotlin
Translate(
    keyName = "app_subscriptions"
)
```

In this example, `t("app_subscriptions")` is used to fetch the translation for the key `app_subscriptions`. Make sure that this key is added to your Tolgee project with the appropriate translations.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Support

For support, please open an issue in the GitHub repository or contact the maintainers.

## Acknowledgments

- [Tolgee](https://tolgee.io) for the translation platform
