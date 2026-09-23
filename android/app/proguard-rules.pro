# Add project specific ProGuard rules here.

# Keep attributes required by Gson and Retrofit reflection
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep fields annotated with SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep API data models in parasha package
-keep class com.shahartal.myquietchannel.parasha.** { *; }
-keep interface com.shahartal.myquietchannel.parasha.JsonHebCalShabbatApi { *; }
