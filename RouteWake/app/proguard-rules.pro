# Add project specific ProGuard rules here.

# Keep OSMDroid classes (it uses reflection internally for tile providers).
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**
