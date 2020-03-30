# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/ruben/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-verbose

#************************* mio  *************************
-keepattributes SourceFile,LineNumberTable  #para crashlitics
-keep public class * extends java.lang.Exception  #para crashlitics

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes RuntimeVisibleAnnotations
-keepattributes AnnotationDefault
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable

-keep, includedescriptorclasses class com.google.android.gms.internal.**{ *; }
-dontwarn com.google.android.gms.**

-keepclasseswithmembers class * {
    native <methods>;
}

-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}

-keep, includedescriptorclasses class com.android.vending.billing.**
-keep, includedescriptorclasses class jp.wasabeef.recyclerview.** {*;}

-dontwarn jp.wasabeef.recyclerview.**
-dontwarn com.google.api.**

#************************* stackovervlow  *************************
#For JodaTime
-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString

# support-v4
-dontwarn android.support.v4.**

# support-v7
-dontwarn android.support.v7.**

# support design
#@link http://stackoverflow.com/a/31028536
-dontwarn android.support.v13.**

# support design
#@link http://stackoverflow.com/a/31028536
-dontwarn android.support.design.**

#error : Note: the configuration refers to the unknown class 'com.google.vending.licensing.ILicensingService'
#solution : @link http://stackoverflow.com/a/14463528
-dontwarn com.google.android.gms.internal.zzbdo
-dontwarn com.google.firebase.messaging.zzc
-dontwarn com.google.common.cache.**
-dontwarn com.google.common.primitives.**
-dontwarn com.squareup.picasso.**
-dontnote com.google.vending.licensing.ILicensingService
-dontnote **ILicensingService

-optimizations !class/unboxing/enum

-dump obfuscation/class_files.txt
-printseeds obfuscation/seeds.txt
-printusage obfuscation/unused.txt
-printmapping obfuscation/mapping.txt

-keep, includedescriptorclasses class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

-keep class com.ap.** {*;}
-dontwarn com.ap.ApReceiver

-keep public class * extends java.lang.Exception



-dontnote com.afollestad.materialdialogs.internal.**
-dontnote org.joda.time.**
#-dontnote com.rubisoft.swingerscuddles.Classes.MultiSpinner
-dontnote com.google.firebase.**
-dontnote com.google.firebase.**
-dontnote com.facebook.**
-dontnote com.google.common.util.**
-dontnote  com.mikepenz.iconics.Iconics
-dontnote com.squareup.picasso.Utils
-dontnote com.android.vending.billing.**
-dontnote com.appyvet.rangebar.**
-dontnote com.google.ads.mediation.**

-ignorewarnings

-dontnote com.google.android.gms.**
-dontwarn com.fasterxml.jackson.databind.ext.**
-dontnote com.google.gson.internal.**
-dontnote  com.adcolony.sdk.ax
-dontnote  com.pollfish.layouts.**
-dontnote  com.rubisoft.swingerscuddles.Classes.MultiSpinner


######################### appodeal  #########################

# AdMediator
-keep class com.admediator.** { *; }

# Appodeal
-keep class com.appodeal.** { *; }
-dontwarn com.appodeal.**
-keep class com.appodealx.** { *; }
-dontwarn com.appodealx.**
-keepattributes EnclosingMethod, InnerClasses, Signature, JavascriptInterface

# Amazon
-keep class com.amazon.** { *; }
-dontwarn com.amazon.**

# Mopub
-keep public class com.mopub.**
-keepclassmembers class com.mopub.** { public *; }
-dontwarn com.mopub.**
-keep class * extends com.mopub.mobileads.CustomEventBanner {}
-keepclassmembers class com.mopub.mobileads.CustomEventBannerAdapter {!private !public !protected *;}
-keep class * extends com.mopub.mobileads.CustomEventInterstitial {}
-keep class * extends com.mopub.nativeads.CustomEventNative {}
-keep class * extends com.mopub.mobileads.CustomEventRewardedVideo {}
-keep class * extends com.mopub.nativeads.CustomEventRewardedAd {}
-keepclassmembers class ** { @com.mopub.common.util.ReflectionTarget *; }
-dontwarn com.mopub.volley.toolbox.**
-keepclassmembers,allowshrinking,allowobfuscation class com.android.volley.NetworkDispatcher {
    void processRequest();
}
-keepclassmembers,allowshrinking,allowobfuscation class com.android.volley.CacheDispatcher {
    void processRequest();
}
-keep public class android.webkit.JavascriptInterface {}
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Applovin
-keep class com.applovin.** { *; }
-dontwarn com.applovin.**

# Facebook
-keep class com.facebook.ads.** { *; }
-keeppackagenames com.facebook.*
-dontwarn com.facebook.ads.**

# Chartboost
-keep class com.chartboost.** { *; }
-dontwarn com.chartboost.**

# Unity Ads
-keepattributes SourceFile,LineNumberTable
-keep class com.unity3d.** { *; }
-dontwarn com.unity3d.**

# Yandex
-keep class com.yandex.metrica.** { *; }
-dontwarn com.yandex.metrica.**
-keep class com.yandex.mobile.ads.** { *; }
-dontwarn com.yandex.mobile.ads.**
-keepattributes *Annotation*
-keep class com.android.installreferrer.api.* { *; }
-dontwarn com.android.installreferrer.api.*

# StartApp
-keep class com.startapp.** { *;}
-keep class com.truenet.** { *;}
-dontwarn com.startapp.**
-dontwarn android.webkit.JavascriptInterface
-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile, LineNumberTable, *Annotation*, EnclosingMethod
-dontwarn org.jetbrains.annotations.**

# Flurry
-keep class com.flurry.** { *; }
-dontwarn com.flurry.**
-keepattributes *Annotation*,EnclosingMethod,Signature
-keepclasseswithmembers class * {
  public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Adcolony
-keep class com.jirbo.adcolony.** { *;}
-keep class com.adcolony.** { *;}
-keep class com.immersion.** { *;}
-dontnote com.immersion.**
-dontwarn android.webkit.**
-dontwarn com.jirbo.adcolony.**
-dontwarn com.adcolony.**
-keepclassmembers class com.adcolony.sdk.ADCNative** { *; }

# Vungle
-keep class com.vungle.warren.** { *; }
-dontwarn com.vungle.warren.error.VungleError$ErrorCode

# Vungle/Moat SDK
-keep class com.moat.** { *; }
-dontwarn com.moat.**

# Vungle/Fetch
-keepnames class com.tonyodev.fetch.Fetch

# Vungle/Okio
-keepnames class okio.Okio
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Vungle/Retrofit
-dontwarn okio.**
-dontwarn retrofit2.Platform$Java8
-keepnames class retrofit2.converter.gson.GsonConverterFactory
-keepnames class retrofit2.Retrofit

# Vungle/Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.examples.android.model.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Vungle/Google Android Advertising ID
-keep class com.google.android.gms.internal.** { *; }
-dontwarn com.google.android.gms.ads.identifier.**

# Vungle/okhttp3
-keep class okhttp3.logging.HttpLoggingInterceptor
-keepnames class okhttp3.HttpUrl

# MyTarget
-keep class com.my.target.** { *; }
-dontwarn com.my.target.**

# Mintegral
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mintegral.** {*; }
-keep interface com.mintegral.** {*; }
-keep class android.support.v4.** { *; }
-dontwarn com.mintegral.**
-keep class **.R$* { public static final int mintegral*; }
-keep class com.alphab.** {*; }
-keep interface com.alphab.** {*; }

# Admob
-keep class com.google.android.gms.ads.** { *; }

# Tapjoy
-keep class com.tapjoy.** { *; }
-keep class com.moat.** { *; }
-dontwarn com.tapjoy.**

# IronSource
-keepclassmembers class com.ironsource.sdk.controller.IronSourceWebView$JSInterface { public *; }
-keepclassmembers class * implements android.os.Parcelable { public static final android.os.Parcelable$Creator *; }
-keep public class com.google.android.gms.ads.** { public *; }
-dontwarn com.moat.**
-keep class com.moat.** { public protected private *; }
-keep class com.ironsource.adapters.** { *; }
-keepnames class com.ironsource.mediationsdk.IronSource { *; }
-dontwarn com.ironsource.**

# AdColonyV3
-keepclassmembers class * { @android.webkit.JavascriptInterface <methods>; }
-keep class com.adcolony.** { *; }
-dontwarn com.adcolony.**
-dontwarn android.app.Activity

# Inmobi
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**
-keep public class com.google.android.gms.**
-dontwarn com.squareup.picasso.**
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient{
     public *;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info{ *; }
-keep class com.squareup.picasso.** {*;}
-dontwarn com.squareup.picasso.**
-dontwarn com.squareup.okhttp.**
-keep class com.moat.** {*;}
-dontwarn com.moat.**
-keep class com.integralads.avid.library.* {*;}

# Ogury
-dontwarn io.presage.**

# Google
-keep class com.google.android.gms.common.GooglePlayServicesUtil {*;}
-keep class com.google.android.gms.ads.identifier.** { *; }
-dontwarn com.google.android.gms.**

# Legacy
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**
-dontwarn android.net.http.**

# Google Play Services library
-keep class * extends java.util.ListResourceBundle {
  protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
  public static final *** NULL;
}
-keepnames class * implements android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable {
  public static final *** CREATOR;
}
-keep @interface android.support.annotation.Keep
-keep @android.support.annotation.Keep class *
-keepclasseswithmembers class * {
  @android.support.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
  @android.support.annotation.Keep <methods>;
}
-keep @interface com.google.android.gms.common.annotation.KeepName
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
  @com.google.android.gms.common.annotation.KeepName *;
}
-keep @interface com.google.android.gms.common.util.DynamiteApi
-keep public @com.google.android.gms.common.util.DynamiteApi class * {
  public <fields>;
  public <methods>;
}
-keep class com.google.android.gms.common.GooglePlayServicesNotAvailableException {*;}
-keep class com.google.android.gms.common.GooglePlayServicesRepairableException {*;}

# Google Play Services library 9.0.0 only
-dontwarn android.security.NetworkSecurityPolicy
-keep public @com.google.android.gms.common.util.DynamiteApi class * { *; }

# support-v4
-keep class android.support.v4.app.Fragment { *; }
-keep class android.support.v4.app.FragmentActivity { *; }
-keep class android.support.v4.app.FragmentManager { *; }
-keep class android.support.v4.app.FragmentTransaction { *; }
-keep class android.support.v4.content.ContextCompat { *; }
-keep class android.support.v4.content.LocalBroadcastManager { *; }
-keep class android.support.v4.util.LruCache { *; }
-keep class android.support.v4.view.PagerAdapter { *; }
-keep class android.support.v4.view.ViewPager { *; }
-keep class android.support.v4.content.ContextCompat { *; }

# support-v7-widget
-keep class android.support.v7.widget.** { *; }

#MultiDex
-keepnames class android.support.multidex.MultiDex