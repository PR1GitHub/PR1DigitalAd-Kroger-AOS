##---------------------------------------------------------------------------------
## PR1DigitalAd consumer rules.
##
## These ship inside the AAR and are applied to the consuming app's R8/ProGuard run.
## The library itself is published unminified, but the client's release build shrinks
## and obfuscates our classes along with its own, so anything reached reflectively has
## to be kept here.
##---------------------------------------------------------------------------------

# API responses are mapped by Gson, which matches JSON keys to field names via
# reflection. If R8 renames these fields every response deserializes to nulls and the
# ad silently fails to load in the client's release build.
-keep class com.purered.pr1digitaladclassic.WeeklyAd { <fields>; }
-keep class com.purered.pr1digitaladclassic.AdPage { <fields>; }
-keep class com.purered.pr1digitaladclassic.PageContent { <fields>; }
-keep class com.purered.pr1digitaladclassic.HotMaps { <fields>; }
-keep class com.purered.pr1digitaladclassic.MapArea { <fields>; }
-keep class com.purered.pr1digitaladclassic.MapAreaContent { <fields>; }
-keep class com.purered.pr1digitaladclassic.OfferDetails { <fields>; }

# Serialized into the body of the savelogs POST.
-keep class com.purered.pr1digitaladclassic.SaveLogs { <fields>; }
-keep class com.purered.pr1digitaladclassic.SaveLogDetails { <fields>; }

# Handed to the client's onHotSpotClick callback.
-keep class com.purered.pr1digitaladclassic.SpotClickPayload { <fields>; }

# Retrofit implements ApiService as a runtime dynamic proxy and reads the generic
# return type of each suspend function to pick a converter, so the interface and its
# generic signatures must survive.
-keep,allowobfuscation interface com.purered.pr1digitaladclassic.ApiService
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
