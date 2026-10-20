# ProGuard / R8 rules for CallBridge Android App

# 1. Project-specific classes
-dontwarn com.callbridge.personal.**
-keep class com.callbridge.personal.data.local.entity.** { *; }
-keep class com.callbridge.personal.data.remote.dto.** { *; }

# 2. Twilio Voice Android SDK
-keep class com.twilio.voice.** { *; }
-dontwarn com.twilio.voice.**
-keepattributes *Annotation*, EnclosingMethod, Signature, InnerClasses
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# 3. Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.EntityDeletionOrUpdateAdapter { *; }
-keep class * extends androidx.room.EntityInsertionAdapter { *; }

# 4. Retrofit & Gson
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}

# 5. Kotlin Coroutines
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
