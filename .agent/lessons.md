# AOS Launcher — Öğrenilen Dersler (Lessons Learned)

> Bu dosya proje boyunca öğrenilen teknik ve süreç derslerini kaydeder.
> Her öğrenilen ders ileriki hatalara karşı koruma kalkanı olur.

---

## Android Launcher Geliştirme Tuzakları

### L001 — Launcher Activity Back Press Davranışı
**Sorun**: Normal Activity'lerde back press uygulamayı bitirir. Launcher'da bu istenmeyen bir davranıştır.  
**Çözüm**: `onBackPressed()` veya `BackHandler` override edilmeli; launcher minimize edilmemeli (çünkü launcher zaten en altta).  
**Uygulama**:
```kotlin
BackHandler(enabled = true) {
    // Ana sayfaya dön, uygulamayı kapatma
    if (currentPage != homePage) scrollToHome()
    // else: hiçbir şey yapma
}
```

### L002 — AppWidgetHost Memory Leak
**Sorun**: AppWidgetHost `startListening()` sonrası `stopListening()` çağrılmazsa bellek sızıntısı olur.  
**Çözüm**: Activity/ViewModel lifecycle'a bağla:
```kotlin
override fun onStart() { super.onStart(); widgetHost.startListening() }
override fun onStop() { super.onStop(); widgetHost.stopListening() }
```

### L003 — PackageManager Yavaş Çağrıları
**Sorun**: `packageManager.getInstalledApplications()` ana thread'de çağrılırsa ANR riski.  
**Çözüm**: Her zaman `Dispatchers.IO` ile arka planda çağır. İlk yükleme sonrası Room'da cache'le.

### L004 — PACKAGE_USAGE_STATS İzni
**Sorun**: Bu izin normal runtime permission değil; kullanıcı Settings > Apps > Special app access'ten manuel açmalı.  
**Çözüm**: İzin yoksa Settings ekranına yönlendir, özelliği devre dışı bırak (crash yapma):
```kotlin
fun hasUsagePermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    return mode == AppOpsManager.MODE_ALLOWED
}
```

### L005 — İkon Paketi APK İzinleri
**Sorun**: İkon paketi APK'sından drawable yüklemek için paketi `queryIntentActivities` ile keşfetmek gerekir; direkt ClassLoader çağrısı API 30+'da kısıtlandı.  
**Çözüm**: `PackageManager.getResourcesForApplication()` kullan, direkt reflection yapma.

### L006 — Duvar Kağıdı ve System UI
**Sorun**: Launcher, system wallpaper'ı göstermek için WindowManager'da özel flag gerektirir.  
**Çözüm**: 
```kotlin
window.setFlags(
    WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
    WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
)
```

### L007 — Predictive Back Gesture (API 33+)
**Sorun**: Android 13+'da öngörücü geri jesti varsayılan olarak açık; launcher'da garip animasyonlara yol açar.  
**Çözüm**: manifest'te `android:enableOnBackInvokedCallback="true"` ekle ve BackHandler'ı doğru kullan.

### L008 — Widget Boyut API 31+ Değişikliği
**Sorun**: API 31 ile birlikte widget boyut sistemi değişti; eski boyut hesaplama metodları deprecate.  
**Çözüm**: `AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH` yerine `SizeF` listesi kullan:
```kotlin
val widgetOptions = Bundle().apply {
    putParcelableArrayList(
        AppWidgetManager.OPTION_APPWIDGET_SIZES,
        arrayListOf(SizeF(120f, 80f), SizeF(180f, 120f))
    )
}
```

---

## Jetpack Compose Tuzakları

### L009 — Unstable Class Recomposition Problemi
**Sorun**: Data class'ların `var` property'si veya `List<T>` tipi Compose'u unstable olarak işaretler; gereksiz recomposition.  
**Çözüm**: `val` property + `@Stable`/`@Immutable` annotation + `ImmutableList` kullan.

### L010 — LazyList + NestedScroll Çakışması
**Sorun**: `LazyColumn` içinde `HorizontalPager` veya iç içe scroll; jestler çakışır.  
**Çözüm**: `NestedScrollConnection` özelleştir; hangi gestur'ın hangi scroll'ı tükettiğini manuel yönet.

### L011 — remember vs rememberSaveable
**Sorun**: `remember` ile saklanan state, configuration change'de (döndürme) sıfırlanır.  
**Çözüm**: Kritik state için `rememberSaveable` kullan; ViewModel'de saklamak daha güvenli.

### L012 — Compose Preview Performansı
**Sorun**: `@Preview` annotation'lı Composable'lar build süresini uzatır.  
**Çözüm**: Preview'ları ayrı `preview` source set'e koy veya `debugImplementation` altında tut.

---

## AI & ML Tuzakları

### L013 — Gemini Nano Cihaz Uyumluluğu
**Sorun**: Gemini Nano tüm cihazlarda mevcut değil; özellikle düşük RAM'li cihazlarda.  
**Çözüm**: Her zaman availability check yap:
```kotlin
val availability = GenerativeModel.checkAvailability()
when (availability) {
    Availability.Available -> useGeminiNano()
    else -> fallbackToTFLite()
}
```

### L014 — TFLite Model Boyutu
**Sorun**: Büyük TFLite modelleri APK boyutunu şişirir.  
**Çözüm**: Dynamic delivery (Play Feature Delivery) ile model'i ayrı indir. INT8 quantization ile boyutu %4'e düşür.

### L015 — UsageStats Veri Gizliliği
**Sorun**: UsageStats verisi hassas; yanlışlıkla loglama veya cloud'a gönderme ciddi gizlilik ihlali.  
**Çözüm**: Tüm feature extraction cihazda yapılır. Sadece anonim aggregate (hangi saat en çok kullanıldı) gönderilir, o da opt-in ile.

---

## Performans Dersleri

### L016 — Icon Loading Stratejisi
**Sorun**: 200+ uygulamayı aynı anda ikon yüklemek OOM veya lag'a neden olur.  
**Çözüm**: Coil ile lazy loading + `diskCachePolicy(CachePolicy.ENABLED)` + `memoryCachePolicy`.

### L017 — Drag & Drop Frame Drop
**Sorun**: Drag & Drop sırasında her frame'de ağır hesaplama yapılırsa 60fps altına düşülür.  
**Çözüm**: Drag pozisyonu hesaplamasını `derivedStateOf` ile minimize et; layout hesabı sadece bırakma anında yap.

### L018 — WorkManager ve Pil
**Sorun**: Widget güncelleme için çok sık WorkManager görevi pili hızlı tüketir.  
**Çözüm**: Widget güncelleme minimum 15 dakika aralıklı; `Constraints.Builder().setRequiresBatteryNotLow(true)` kullan.

---

## Süreç Dersleri

### L019 — Erken Abstraction Tuzağı
**Sorun**: Henüz net olmayan API için karmaşık soyutlama katmanı oluşturmak tekrar yazımı zorlaştırır.  
**Çözüm**: "Make it work, make it right, make it fast" prensibi. Önce çalıştır, sonra soyutla.

### L020 — Android API Seviye Kırılganlığı
**Sorun**: Sadece son API'de test etmek, eski cihazlarda sürpriz crash'lere neden olur.  
**Çözüm**: Her major özellik API 26, 28, 31, 33 ve 35'te test edilmeli. Firebase Test Lab matrisi.

### L021 — Accessibility Service Kötüye Kullanım Uyarısı
**Sorun**: Play Store, Accessibility Service'i gereksiz kullanan uygulamaları reddediyor.  
**Çözüm**: Accessibility Service'i sadece gerçekten gerekli özellikler için kullan (uygulama kilidi gibi). README ve store description'da açıkça belirt.
