import java.net.URL
import java.net.HttpURLConnection
import java.io.PrintWriter
import java.io.OutputStreamWriter
import java.io.FileInputStream

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.example"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  // implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

tasks.register("uploadToGofile") {
    doLast {
        val apkFile = file("build/outputs/apk/debug/app-debug.apk")
        if (!apkFile.exists()) {
            throw GradleException("APK does not exist inside build/outputs/apk/debug/app-debug.apk. Please build first using assembleDebug.")
        }
        println("APK file size: ${apkFile.length()} bytes")

        var serverName: String? = null
        val endpoints = listOf(
            "https://api.gofile.io/getServer",
            "https://api.gofile.io/contents/getServer",
            "https://api.gofile.io/servers"
        )

        for (urlStr in endpoints) {
            try {
                println("Trying getServer endpoint: $urlStr")
                val url = URL(urlStr)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")
                val code = conn.responseCode
                if (code == 200) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    println("Response from $urlStr: $response")
                    val serverStartIndex = response.indexOf("\"server\":\"")
                    if (serverStartIndex != -1) {
                        val serverEndIndex = response.indexOf("\"", serverStartIndex + 10)
                        serverName = response.substring(serverStartIndex + 10, serverEndIndex)
                        println("Successfully found server: $serverName from $urlStr")
                        break
                    }
                    val nameStartIndex = response.indexOf("\"name\":\"")
                    if (nameStartIndex != -1) {
                        val nameEndIndex = response.indexOf("\"", nameStartIndex + 8)
                        serverName = response.substring(nameStartIndex + 8, nameEndIndex)
                        println("Successfully found server from list: $serverName from $urlStr")
                        break
                    }
                } else {
                    println("Endpoint $urlStr returned status code $code")
                }
            } catch (e: Exception) {
                println("Failed to fetch server from $urlStr: ${e.message}")
            }
        }

        if (serverName == null) {
            throw GradleException("Could not obtain an active upload server from Gofile API.")
        }

        var token: String? = null
        try {
            println("Creating a Gofile guest account to obtain an upload token...")
            val accountsUrl = URL("https://api.gofile.io/accounts")
            val conn = accountsUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            val code = conn.responseCode
            if (code == 200 || code == 201) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                println("Accounts response: $response")
                val tokenStartIndex = response.indexOf("\"token\":\"")
                if (tokenStartIndex != -1) {
                    val tokenEndIndex = response.indexOf("\"", tokenStartIndex + 9)
                    token = response.substring(tokenStartIndex + 9, tokenEndIndex)
                    println("Successfully created guest account token: $token")
                }
            } else {
                println("Accounts endpoint returned status code $code")
                val errResponse = conn.errorStream?.bufferedReader()?.use { it.readText() }
                println("Error response: $errResponse")
            }
        } catch (e: Exception) {
            println("Failed to create guest account token: ${e.message}")
        }

        println("Uploading APK to Gofile server: $serverName ...")
        var uploadSuccess = false
        val uploadPaths = listOf("/contents/upload", "/uploadFile", "/upload")
        
        for (uploadPath in uploadPaths) {
            try {
                println("Trying Gofile upload path: $uploadPath")
                val boundary = "GofileBoundary"
                val uploadUrl = URL("https://$serverName.gofile.io$uploadPath")
                val uploadConn = uploadUrl.openConnection() as HttpURLConnection
                uploadConn.doOutput = true
                uploadConn.requestMethod = "POST"
                uploadConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                uploadConn.setRequestProperty("User-Agent", "Mozilla/5.0")

                val outputStream = uploadConn.outputStream
                val writer = PrintWriter(OutputStreamWriter(outputStream, "UTF-8"), true)

                // Add token parameter (if available)
                if (token != null) {
                    writer.append("--$boundary").append("\r\n")
                    writer.append("Content-Disposition: form-data; name=\"token\"").append("\r\n")
                    writer.append("\r\n")
                    writer.append(token).append("\r\n")
                    writer.flush()
                }

                // Add file parameter
                writer.append("--$boundary").append("\r\n")
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"${apkFile.name}\"").append("\r\n")
                writer.append("Content-Type: application/vnd.android.package-archive").append("\r\n")
                writer.append("\r\n")
                writer.flush()

                // Write APK bytes
                val fileInputStream = FileInputStream(apkFile)
                val buffer = ByteArray(4096)
                while (true) {
                    val read = fileInputStream.read(buffer)
                    if (read == -1) break
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
                fileInputStream.close()

                writer.append("\r\n")
                writer.flush()
                writer.append("--$boundary--").append("\r\n")
                writer.flush()
                writer.close()

                val uploadResponseCode = uploadConn.responseCode
                println("Upload path $uploadPath returned status code $uploadResponseCode")
                
                val uploadResponse = if (uploadResponseCode >= 400) {
                    uploadConn.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error stream content."
                } else {
                    uploadConn.inputStream.bufferedReader().use { it.readText() }
                }
                println("Response from $uploadPath: $uploadResponse")

                // Parse downloadPage
                val pageIndex = uploadResponse.indexOf("\"downloadPage\":\"")
                if (pageIndex != -1) {
                    val pageEnd = uploadResponse.indexOf("\"", pageIndex + 16)
                    val downloadPage = uploadResponse.substring(pageIndex + 16, pageEnd).replace("\\/", "/")
                    println("----------------------------------------------")
                    println("SUCCESS_GOFILE_LINK: $downloadPage")
                    println("----------------------------------------------")
                    uploadSuccess = true
                    break
                }
            } catch (e: Exception) {
                println("Failed for path $uploadPath: ${e.message}")
            }
        }

        if (!uploadSuccess) {
            throw GradleException("Failed to upload the APK to any of the Gofile endpoints.")
        }
    }
}

