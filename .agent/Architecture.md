# AOS Launcher — Teknik Mimari

## Genel Mimari: Clean Architecture + MVVM

```
app/
├── core/                    # Bağımsız temel modüller
│   ├── common/              # Util, extension, constants
│   ├── data/                # Repository implementasyonları
│   ├── domain/              # Use case'ler, model'ler, interfaces
│   └── ui/                  # Shared Compose bileşenleri, tema
│
├── feature/                 # Özellik modülleri (her biri bağımsız)
│   ├── home/                # Ana ekran, sayfa yönetimi
│   ├── appdrawer/           # Uygulama çekmecesi
│   ├── widget/              # Widget motoru
│   ├── search/              # Evrensel arama
│   ├── dock/                # Alt dock bar
│   ├── notifications/       # Bildirim merkezi
│   ├── profile/             # Profil / mod sistemi
│   ├── customization/       # Tema, ikon, animasyon ayarları
│   ├── ai/                  # AI asistan, öneri motoru
│   ├── privacy/             # Gizli alan, kilit sistemi
│   ├── analytics/           # Kullanım istatistikleri
│   └── settings/            # Genel ayarlar
│
├── plugin/                  # Plugin API ve yönetimi
├── sync/                    # Bulut senkronizasyonu
└── launcher/                # LauncherActivity, WallpaperManager
```

## Teknoloji Yığını (Tech Stack)

### Android Core
| Bileşen | Teknoloji | Versiyon |
|---------|-----------|----------|
| Dil | Kotlin | 2.x |
| UI | Jetpack Compose | Latest Stable |
| Navigation | Compose Navigation | 2.8+ |
| DI | Hilt | 2.51+ |
| Lifecycle | ViewModel + StateFlow | Compose lifecycle |
| Coroutines | Kotlin Coroutines + Flow | 1.8+ |

### Veri Katmanı
| Bileşen | Teknoloji |
|---------|-----------|
| Yerel DB | Room 2.6+ |
| Preferences | DataStore (Proto + Preferences) |
| JSON | Kotlinx Serialization |
| Ağ | Retrofit 2 + OkHttp 4 |
| Cache | Coil (resim), OkHttp cache |

### Launcher Özel
| Bileşen | Teknoloji |
|---------|-----------|
| Widget Host | AppWidgetHost (custom impl.) |
| Wallpaper | WallpaperManager API |
| App List | PackageManager + BroadcastReceiver |
| Gesture | PointerInput (Compose) + GestureDetector |
| Shortcuts | LauncherApps API |
| Notification | NotificationListenerService |
| Accessibility | AccessibilityService (isteğe bağlı) |

### AI & ML
| Bileşen | Teknoloji | Kullanım |
|---------|-----------|----------|
| On-Device LLM | Gemini Nano (MediaPipe LLM) | Asistan, öneri |
| ML Inference | TensorFlow Lite | Davranış analizi |
| OCR | ML Kit Text Recognition | Ekrandan metin okuma |
| Image Analysis | ML Kit Image Labeling | Foto'dan tema |
| Cloud AI | Gemini API (REST) | Gelişmiş özellikler |
| Embedding | TFLite Embedding Model | App öneri modeli |

### Animasyon & UI
| Bileşen | Teknoloji |
|---------|-----------|
| Animasyon | Compose Animation API + Transition |
| Physics | Compose Spring Physics |
| Lottie | Lottie Compose (JSON animasyonlar) |
| Blur | RenderScript / Vulkan blur |
| Shader | AGSL (Android Graphics Shading Language) |
| Ripple | Custom Compose Indication |

### Test
| Katman | Framework |
|--------|-----------|
| Unit | JUnit 5 + MockK |
| Compose UI | Compose Test (ComposeTestRule) |
| Integration | Hilt Test + Room in-memory |
| E2E | UI Automator + Espresso |
| Performance | Macrobenchmark |

## Launcher Engine Mimarisi

### LauncherActivity
```kotlin
// Ana activity - HOME category
class LauncherActivity : ComponentActivity() {
    // WindowInsets tam ekran yönetimi
    // Çoklu ekran desteği (foldable)
    // Back press: minimize (exit yok)
    // Home button: ana sayfaya dön
}
```

### HomeScreenEngine
- `PagerState` ile sonsuz sayfa yönetimi
- Her sayfa: `LauncherPage` (grid + items)
- `LauncherItem` sealed class: App, Shortcut, Widget, Folder, Custom
- Drag & Drop: `DragAndDropState` custom implementation
- Grid: `LazyVerticalGrid` + custom span

### Widget Engine
```
WidgetEngine
├── AppWidgetHost (sistem widget'ları)
├── CustomWidgetRegistry (kendi widget'larımız)
├── WidgetSizeNegotiator (boyut müzakeresi)
└── WidgetUpdateScheduler (WorkManager ile güncelleme)
```

### Gesture Engine
```
GestureEngine
├── SwipeUpDetector → App Drawer
├── SwipeDownDetector → Notifications
├── DoubleTapDetector → Sleep / Custom
├── PinchDetector → Page Overview
├── LongPressDetector → Context Menu
├── CornerSwipeDetector → Corner Actions
└── DrawGestureDetector → Pattern Actions (Z, O, etc.)
```

### Theme Engine
```
ThemeEngine
├── ThemeRepository (Room)
├── WallpaperColorExtractor (Palette API)
├── DynamicColorProvider (Material You)
├── IconPackLoader (PackageManager)
├── FontLoader (custom TTF/OTF)
└── AnimationPresetRegistry
```

## AI Mimarisi

### On-Device Pipeline
```
Kullanıcı Etkileşimi
    ↓
UsageTracker (UsageStatsManager)
    ↓
FeatureExtractor (zaman, konum, uygulama, bağlam)
    ↓
TFLite Inference Engine
    ↓
AppSuggestionRanker
    ↓
HomeScreen Öneri Kartları
```

### Gemini Nano Entegrasyonu
- MediaPipe LLM Inference API kullanımı
- Model: gemini-nano (on-device, ~150MB)
- Prompt template sistemi
- Streaming response desteği
- Fallback: Gemini API (internet varsa)

### Veri Akışı (Privacy-First)
- Tüm ML verisi cihazda işlenir
- Buluta sadece anonim aggregate metrikler
- Kullanıcı opt-in olmadan hiçbir kişisel veri gönderilmez

## Senkronizasyon Mimarisi

```
SyncEngine
├── SyncScheduler (WorkManager - periyodik)
├── ConflictResolver (last-write-wins + merge)
├── Compressor (GZIP payload)
├── Encryptor (AES-256 end-to-end)
└── Adapters:
    ├── ThemeSyncAdapter
    ├── LayoutSyncAdapter
    ├── ProfileSyncAdapter
    └── SettingsSyncAdapter
```

## Plugin Sistemi Mimarisi

```kotlin
// Plugin API (public)
interface LauncherPlugin {
    val id: String
    val version: Int
    fun onAttach(host: PluginHost)
    fun onDetach()
}

interface PluginHost {
    fun registerWidget(widget: CustomWidget)
    fun registerSearchProvider(provider: SearchProvider)
    fun registerGesture(gesture: CustomGesture)
}
```
- Plugin APK'ları ayrı process'te çalışır (sandbox)
- Binder IPC ile host ile iletişim
- Plugin izin sistemi (launcher manifest)

## Güvenlik Mimarisi

```
SecurityLayer
├── BiometricLockManager (Biometric Prompt API)
├── AppLockService (Accessibility Service tabanlı)
├── SecureStorage (EncryptedSharedPreferences)
├── AnonymousModeController
└── BehaviorAnomalyDetector (TFLite)
```

## Performans Hedefleri & Stratejiler

| Metrik | Hedef | Strateji |
|--------|-------|----------|
| Cold Start | < 300ms | Lazy initialization, splash optimization |
| Frame Drop | < 1 frame/sn | Compose stability, snapshot invalidation |
| Memory | < 150MB baseline | Profile-guided memory tuning |
| Battery | < %2 drain/saat | Doze-aware, optimized background |
| APK Size | < 20MB (50MB AI dahil) | R8 + Proguard, split APKs |

## Modül Bağımlılık Kuralı
- `core:domain` hiçbir Android import'u içermez
- `feature:*` modülleri birbirine doğrudan bağımlı olamaz
- `core:ui` shared Compose bileşenlerinin tek kaynağı
- Her feature kendi ViewModel'ına sahip; arası SharedViewModel yasak

## API Sözleşmesi (Internal)
- Repository'ler Flow<Result<T>> döndürür
- Use case'ler suspend fun veya Flow döndürür
- ViewModel: UiState sealed class + UiEvent one-shot channel
- Hata yönetimi: sealed class Result<T> (Success, Error, Loading)
