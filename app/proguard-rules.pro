# Firebase
-keep class com.google.firebase.** { *; }
-keepattributes *Annotation*

# Model classes
-keep class com.tomazela.adminpiscina.data.models.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
