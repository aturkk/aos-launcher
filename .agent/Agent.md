# AOS Launcher — AI Agent Talimatları

## Agent Kimliği & Rolü
Bu dosya, AOS Launcher projesini geliştiren AI agent'ın davranış kurallarını, bağlamını ve çalışma protokollerini tanımlar. Agent bu dosyayı her oturum başında okumalı ve kurallara uygun davranmalıdır.

**Agent Adı**: AOS Dev Agent  
**Proje**: AOS Launcher (Android)  
**Birincil Görev**: Kapsamlı, AI destekli bir Android launcher geliştirmek  
**Dil**: Kod → Kotlin; Açıklama → Türkçe tercih edilir

---

## Temel Çalışma Kuralları

### 1. Önce Anla, Sonra Yaz
- Kod yazmadan önce Architecture.md ve Phase.md'yi oku
- Mevcut kodu anlamadan değiştirme
- Değişikliğin etkisini tüm modüllerde değerlendir

### 2. Modüler Düşün
- Her özellik ayrı feature modülünde geliştirilir
- core:domain hiçbir Android bağımlılığı içermez
- Feature modülleri birbirine doğrudan bağımlı olamaz
- Paylaşılan UI bileşeni → core:ui'ye taşı

### 3. Mimari Kurallar
- MVVM + Clean Architecture her zaman uygulanır
- Repository → Flow<Result<T>> döndürür
- ViewModel → UiState (sealed class) + UiEvent (Channel) kullanır
- Use case'ler tek sorumluluk ilkesine uyar
- Hata yönetimi: sealed class Result<Success, Error, Loading>

### 4. Kotlin & Compose Kuralları
- Coroutine scope: viewModelScope veya rememberCoroutineScope
- State: remember + MutableState veya StateFlow
- Side effect: LaunchedEffect, SideEffect, DisposableEffect
- Recomposition: Stability annotation kullan (Stable, Immutable)
- Preview: Her Composable için @Preview ekle
- Compose Navigation: type-safe navigation (Kotlin serializable route)

### 5. Performans Kuralları
- Ana thread'i asla bloklama (UI thread safety)
- IO işlemleri: Dispatchers.IO
- CPU yoğun: Dispatchers.Default
- Bitmap işlemleri: Coil + async decode
- Animasyon: her zaman 60fps hedefle, 120Hz destekle
- Gereksiz recomposition'ı önle (State hoisting, derivedStateOf)

### 6. AI Özellikleri Kuralları
- Tüm ML verileri cihazda işlenir (privacy-first)
- Cloud AI sadece kullanıcı opt-in onayıyla çağrılır
- Gemini Nano: önce kontrol et (isAvailable), fallback hazır ol
- TFLite model boyutları minimize edilir (quantization)
- AI özelliği hata verirse → sessizce degrade et, kullanıcıyı rahatsız etme

### 7. Test Kuralları
- Her use case için unit test yaz
- Her Repository için integration test yaz (Room in-memory)
- Her kritik Composable için Compose test yaz
- Yeni özellik = en az %80 test coverage
- Benchmark: cold start ve frame timing her major release'te ölçülür

### 8. Güvenlik Kuralları
- Hassas veri → EncryptedSharedPreferences veya EncryptedFile
- Ağ → HTTPS zorunlu, certificate pinning üretimde aktif
- Plugin sandbox: ayrı process, minimum izin
- Kullanıcı verisi → asla plaintext loglanmaz

---

## Kod Yazma Protokolü

### Yeni Özellik Eklerken
1. Phase.md'de ilgili görevi [ ] → [/] yap
2. Architecture.md'de etkisi olan kısımları güncelle
3. Feature modülü oluştur (veya mevcut modüle ekle)
4. Domain katmanı: Model + UseCase
5. Data katmanı: Repository impl + DataSource
6. UI katmanı: ViewModel + Composable
7. Test yaz
8. Phase.md'de görevi [/] → [x] yap
9. changelog.md'ye ekle

### Bug Fix Yaparken
1. Sorunun kök nedenini tespit et (sadece semptomu çözme)
2. Reproducible test case yaz
3. Fix uygula
4. Test geçiyor mu doğrula
5. lessons.md'ye benzer hatayı önlemek için not düş

### Refactor Yaparken
1. Önce testlerin yeşil olduğunu doğrula
2. Küçük adımlarla refactor yap
3. Her adımda testleri çalıştır
4. Architecture.md'yi güncelle

---

## Kritik Bağımlılık Haritası

```
LauncherActivity
    ↓
HomeScreenEngine
    ├── PageManager (sayfa state)
    ├── GridEngine (ikon yerleşimi)
    ├── DragDropEngine (sürükle-bırak)
    └── GestureEngine (jestler)

AppDrawerEngine
    ├── AppListRepository (PackageManager)
    ├── SearchEngine (fuzzy arama)
    └── SortEngine (sıralama stratejileri)

WidgetEngine
    ├── SystemWidgetHost (AppWidgetHost)
    ├── CustomWidgetRegistry
    └── WidgetUpdateScheduler (WorkManager)

ThemeEngine
    ├── ColorSystem (Material You)
    ├── IconPackLoader
    ├── AnimationPresetRegistry
    └── WallpaperColorExtractor

AIEngine
    ├── UsageTracker (UsageStatsManager)
    ├── FeatureExtractor
    ├── SuggestionModel (TFLite)
    ├── GeminiNanoClient (MediaPipe)
    └── GeminiAPIClient (fallback)

ProfileEngine
    ├── ProfileRepository (Room)
    ├── ProfileScheduler (AlarmManager)
    └── GeofenceManager (Location API)
```

---

## Bağlam Hatırlatıcıları

### Launcher İzinleri (AndroidManifest)
```xml
<!-- Zorunlu -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
<!-- Widget için -->
<uses-permission android:name="android.permission.BIND_APPWIDGET"/>
<!-- Bildirim okuma için -->
<uses-permission android:name="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"/>
<!-- Kullanım istatistikleri için -->
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"/>
<!-- Ekran kilidi için -->
<uses-permission android:name="android.permission.DEVICE_POWER"/>
<!-- Konum (profil) için -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<!-- Kamera (AI) için -->
<uses-permission android:name="android.permission.CAMERA"/>
```

### Kritik Intent Filter
```xml
<activity android:name=".LauncherActivity">
    <intent-filter>
        <action android:name="android.intent.action.MAIN"/>
        <category android:name="android.intent.category.HOME"/>
        <category android:name="android.intent.category.DEFAULT"/>
    </intent-filter>
</activity>
```

### Bilinen Android Kısıtlamaları
- API 29+: Arka planda konum izni ayrıca istenmelidir
- API 31+: Widget boyutları için `OPTION_APPWIDGET_MIN/MAX_WIDTH/HEIGHT` kullan
- API 33+: Bildirim izni runtime'da istenir
- Predictive Back Gesture: API 33+'da manuel olarak desteklenmeli
- PACKAGE_USAGE_STATS: System Settings'den manuel açılır (runtime grant yok)

---

## Hata Sınıflandırması

| Seviye | Tanım | Eylem |
|--------|-------|-------|
| CRITICAL | Launcher crash / açılmama | Anında düzelt, başka işe geçme |
| HIGH | Özellik çalışmıyor | Aynı sprint'te düzelt |
| MEDIUM | UI bug, yavaşlık | Bir sonraki sprint'te düzelt |
| LOW | Kozmetik, önemsiz | Backlog'a al |

---

## Performans Kontrol Listesi (Her Release Öncesi)

- [ ] Cold start < 300ms (Macrobenchmark ile ölç)
- [ ] Scrolling FPS: hiç drop yok (Perfetto trace)
- [ ] Memory: baseline < 150MB, AI aktifken < 300MB
- [ ] Pil: 1 saat aktif kullanımda < %5 tüketim
- [ ] APK boyutu < 20MB (AI modeli ayrı indirme)
- [ ] Crash-free sessions > %99.9
- [ ] ANR rate < %0.1

---

## Sık Başvurulan Kaynaklar

- Android Launcher Guide: https://developer.android.com/guide/components/activities/recents
- AppWidgetHost: https://developer.android.com/guide/topics/appwidgets/host
- Jetpack Compose: https://developer.android.com/jetpack/compose
- MediaPipe LLM: https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference
- TFLite: https://www.tensorflow.org/lite/android
- Material You: https://m3.material.io/
- UsageStatsManager: https://developer.android.com/reference/android/app/usage/UsageStatsManager
