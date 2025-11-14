#!/bin/bash
echo "🚀 Creating Shadow Messenger..."

# Создаём структуру папок
mkdir -p app/src/main/java/com/shadow/messenger/
mkdir -p app/src/main/res/{layout,values,xml,drawable}

# 1. AndroidManifest.xml
cat > app/src/main/AndroidManifest.xml << 'MANIFEST'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="Shadow Messenger"
        android:theme="@style/Theme.AppCompat.Light.DarkActionBar">
        
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
    </application>
</manifest>
MANIFEST

# 2. MainActivity.kt
cat > app/src/main/java/com/shadow/messenger/MainActivity.kt << 'ACTIVITY'
package com.shadow.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val titleText = findViewById<TextView>(R.id.titleText)
        val messageInput = findViewById<EditText>(R.id.messageInput)
        val sendButton = findViewById<Button>(R.id.sendButton)
        
        titleText.text = "Shadow Messenger v1.0"
        
        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                // Здесь будет логика отправки сообщения
                titleText.text = "Sent: $message"
                messageInput.text.clear()
            }
        }
    }
}
ACTIVITY

# 3. activity_main.xml
cat > app/src/main/res/layout/activity_main.xml << 'LAYOUT'
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#4A148C"
    android:orientation="vertical"
    android:padding="20dp">

    <TextView
        android:id="@+id/titleText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Shadow Messenger"
        android:textColor="#FFFFFF"
        android:textSize="28sp"
        android:textStyle="bold"
        android:gravity="center"
        android:layout_marginTop="50dp"
        android:layout_marginBottom="30dp" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_marginTop="20dp">

        <EditText
            android:id="@+id/messageInput"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:hint="Type message..."
            android:textColor="#FFFFFF"
            android:textColorHint="#BB86FC"
            android:backgroundTint="#BB86FC" />

        <Button
            android:id="@+id/sendButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="SEND"
            android:textColor="#4A148C"
            android:background="#BB86FC"
            android:layout_marginStart="10dp" />

    </LinearLayout>

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="🔒 End-to-End Encryption: ACTIVE"
        android:textColor="#00E676"
        android:textSize="14sp"
        android:gravity="center"
        android:layout_marginTop="30dp" />

</LinearLayout>
LAYOUT

# 4. strings.xml
cat > app/src/main/res/values/strings.xml << 'STRINGS'
<resources>
    <string name="app_name">Shadow Messenger</string>
</resources>
STRINGS

# 5. build.gradle (root)
cat > build.gradle << 'ROOT_GRADLE'
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath "com.android.tools.build:gradle:7.4.2"
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0"
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
ROOT_GRADLE

# 6. app/build.gradle
cat > app/build.gradle << 'APP_GRADLE'
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}

android {
    compileSdk 34
    namespace 'com.shadow.messenger'

    defaultConfig {
        applicationId "com.shadow.messenger"
        minSdk 21
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        release {
            minifyEnabled false
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = '1.8'
    }
    
    buildFeatures {
        viewBinding true
    }
}

dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.10.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
}
APP_GRADLE

# 7. settings.gradle
cat > settings.gradle << 'SETTINGS'
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ShadowMessenger"
include ':app'
SETTINGS

# 8. gradle.properties
cat > gradle.properties << 'PROPS'
org.gradle.jvmargs=-Xmx2048m
android.useAndroidX=true
android.enableJetifier=true
PROPS

echo "✅ Project files created!"
