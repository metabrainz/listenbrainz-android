# Keep Jackson library classes
-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**
-keepattributes *Annotation*,EnclosingMethod,Signature
-keepnames class com.fasterxml.jackson.** { *; }

# Open-source project, no need to obfuscate.
-dontobfuscate
-keepattributes SourceFile,LineNumberTable