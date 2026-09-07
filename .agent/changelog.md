# AOS Launcher — Değişiklik Günlüğü (Changelog)

> Her sprint sonunda veya önemli değişiklikte bu dosya güncellenir.
> Format: [TARİH] — [VERSİYON] — [AÇIKLAMA]

---

## Versiyon Şeması
`MAJOR.MINOR.PATCH`
- MAJOR: Breaking change veya büyük özellik lansmanı
- MINOR: Yeni özellik eklenmesi
- PATCH: Bug fix veya küçük iyileştirme

---

## [2026-09-07] — v0.1.0 — Proje Başlangıcı

### Eklendi
- Proje dokümantasyon yapısı oluşturuldu (.agent klasörü)
- Project.md: proje tanımı, vizyon, hedef kitle, kapsam
- Architecture.md: Clean Architecture + MVVM, tech stack, modül yapısı
- Phase.md: Faz 0-5 detaylı görev listesi
- Roadmap.md: v1.0 → v3.0 yol haritası, risk analizi
- Agent.md: AI agent çalışma kuralları ve protokolleri
- todo.md: Kapsamlı yapılacaklar listesi
- skills.md: Teknik kod snippetları ve beceri kataloğu
- lessons.md: Öğrenilen dersler kataloğu (L001-L021)

### Notlar
- Proje henüz kod aşamasında değil, planlama tamamlandı
- Bir sonraki adım: Faz 0 altyapı görevlerine başlamak

---

## [2026-09-07] — v0.2.0 — Faz 0: Altyapı & Çok Modüllü Mimari

### Eklendi
- Multi-module Gradle iskeleti (`settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`)
- Gradle Version Catalog (`gradle/libs.versions.toml`) ile AGP 8.7, Kotlin 2.0.20, Compose BOM, Hilt, Room, DataStore entegrasyonu
- `core:common`: `Result<T>` sınıfı ve Hilt `CoroutineDispatcher` sağlayıcıları
- `core:domain`: Saf Kotlin Clean Architecture alanı (`AppInfo`, `LauncherItem`, `PageInfo`, `Profile`, `ThemeConfig`, Repository arayüzleri)
- `core:data`: Room Database (`launcher_items`, `launcher_pages`, `profiles`, `theme_config`), DAO'lar, DataStore Preferences ve Repository implementasyonları
- `core:ui`: Jetpack Compose Material You dinamik tema sistemi, renkler, tipografi, şekiller ve `AosAppIcon` bileşeni
- `feature:home`: `HorizontalPager` sonsuz sayfa düzeni, `DockBar` alt çubuğu, `PageIndicator` ve `HomeViewModel`
- `feature:appdrawer`: Arama filtreli uygulama çekmecesi ızgarası (`LazyVerticalGrid`), `AppDrawerViewModel`
- `feature:settings`: Launcher tercihleri (etiket görünürlüğü, çift dokunma kilidi, ızgara bilgisi) ekranı ve `SettingsViewModel`
- `app`: `AosApplication` (`@HiltAndroidApp`), `LauncherActivity` (HOME & DEFAULT intent filtreleri, `FLAG_SHOW_WALLPAPER`, L001 `BackHandler`, L007 `enableOnBackInvokedCallback`)
- CI/CD GitHub Actions iş akışı (`.github/workflows/android.yml`), `.editorconfig`, `.gitignore`

---## [2026-09-07] — v0.3.0 — Faz 1 (Sprint 1-4): Hücre Gridi, Gerçek İkonlar, Canlı Paket Dinleyicisi ve Bağlam Menüsü

### Eklendi
- `GridCellLayout`: Ana ekran sayfalarında `cellX` ve `cellY` koordinatlarına göre kesin hücre yerleşim motoru
- Otomatik boş hücre bulma algoritması: App Drawer üzerinden "Ana Ekrana Ekle" dendiğinde aktif sayfadaki ilk boş hücreye, sayfa doluysa yeni sayfaya otomatik ekleme
- `AppIconDrawable` & `AppIconImage`: Sistem `PackageManager` üzerinden gerçek uygulama ikonlarını yükleyen ve in-memory `ConcurrentHashMap` ile önbelleğe alan yüksek performanslı ikon motoru (L016)
- `HomeItemContextMenu`: Ana ekrandaki öğelere uzun basıldığında açılan "Ana Ekrandan Kaldır", "Uygulama Bilgisi", "Uygulamayı Kaldır" bağlam menüsü
- `AppDrawerContextMenu`: Çekmecedeki uygulamalara uzun basıldığında açılan "Ana Ekrana Ekle", "Uygulama Bilgisi", "Uygulamayı Kaldır" bağlam menüsü
- Hızlı Alfabetik Kaydırma Çubuğu: App Drawer sağ kenarında A..Z hızlı gezinme çubuğu ve tıklanan harfe anında kaydırma
- `PackageReceiver`: Cihaza yeni uygulama yüklendiğinde, kaldırıldığında veya güncellendiğinde (`PACKAGE_ADDED`, `PACKAGE_REMOVED`, `PACKAGE_CHANGED`) anında arka planda `AppListRepository` tazeleyen BroadcastReceiver
- `LauncherActivity` sistem eylemleri: Uygulama başlatma (`LauncherApps`), Sistem uygulama bilgisi açma (`ACTION_APPLICATION_DETAILS_SETTINGS`), Uygulama kaldırma diyaloğu açma (`ACTION_DELETE`)

---
## [2026-09-07] — v0.4.0 — Faz 1 (Sprint 5-8): Sürükle-Bırak, Akıllı Klasörler, AppWidgetHost ve Saat Widget'ı

### Eklendi
- Drag & Drop (Sürükle & Bırak) Motoru: `GridCellLayout` içinde uzun basma sonrası serbest sürükleme (`detectDragGesturesAfterLongPress`), anlık piksel koordinat hesaplama ve bırakılan hücreye (`cellX`, `cellY`) yerleştirme
- Akıllı Klasör Oluşturma: Bir uygulama ikonu başka bir uygulamanın üzerine bırakıldığında otomatik birleşme ve Room veritabanına `FolderItem` (JSON serileştirilmiş uygulama listesi) olarak kaydetme
- `FolderIconView`: Ana ekranda klasörün içindeki ilk 4 uygulamanın minyatür ikonlarını 2x2 grid şeklinde gösteren özel klasör ikonu
- `FolderModalDialog`: Klasöre dokunulduğunda açılan şık Material 3 açılır penceresi; anında düzenlenebilir klasör başlığı, klasör içi uygulama ızgarası, tek dokunuşla başlatma ve klasörden uygulama çıkarma desteği
- `AosClockWidget`: Ana ekranın tepesinde çalışan dinamik dijital saat (HH:mm) ve Türkçe gün/ay tarih rozeti
- `LauncherWidgetHost`: Android sistem widget'larını barındıran custom AppWidgetHost motoru; bellek sızıntısını önlemek için `LauncherActivity` yaşam döngüsüne (`onStart`/`onStop`) bağlandı (L002)
- `SystemWidgetView`: Sistem widget'larını Jetpack Compose arayüzüne entegre eden `AndroidView` köprüsü

---
## [2026-09-07] — v0.5.0 — Faz 1: Core Launcher MVP Tamamlandı

### Eklendi
- `LauncherSystemActions`: Sistem bildirim çekmecesini indirme (`expandNotificationShade`) ve cihazı kilitleme (`lockDevice`) sistem yardımcıları
- `PinchGestureModifier`: İki parmakla içeri çimdikleme (`zoom < 0.85f`) ve çift dokunma (`onDoubleTap`) jest algılayıcıları
- `PagesOverviewSheet`: İki parmakla küçültüldüğünde açılan kuşbakışı sayfa yöneticisi; sayfaların yatay kartlar halinde listelenmesi, anında yeni sayfa ekleme (`+`), sayfaları silme ve seçilen sayfaya animasyonlu kaydırma
- Çift Dokunma ile Ekran Kilidi (`DoubleTapToSleep`): Ana ekranda boş alana çift dokunulduğunda ekranı karartan ve kilitleyen `AosAccessibilityService` entegrasyonu
- Aşağı Kaydırma Jesti (`SwipeDown`): Ana ekranda aşağı kaydırma ile bildirim panelini açma
- Gelişmiş Ayarlar Ekranı: 4x4, 4x5, 5x5, 5x6 ızgara boyutu seçicisi ve jest kılavuzu kartı
- `AosAccessibilityService`: Android erişilebilirlik servisi yapılandırması (`accessibility_service_config.xml` ve manifest kaydı)

---
## [2026-09-07] — v0.6.0 — Faz 2 (Sprint 11-13): Tema Motoru, İkon Paketleri ve 3D Sayfa Geçişleri

### Eklendi
- `ThemeConfig` & Tercihler: Dinamik Material You renk desteği, Sistem/Açık/Koyu tema modları DataStore ve Domain katmanına entegre edildi
- Donanım Hızlandırmalı 3D Sayfa Geçiş Efektleri: `Cube`, `Depth`, `Flip`, `Accordion` ve `Standard` modları Jetpack Compose `HorizontalPager` `graphicsLayer` üzerinde 60/120 FPS ile çalışacak şekilde uyarlandı (`PageTransitions.kt`)
- İkon Şekli Özelleştirme: Sistem genelinde `Squircle`, `Circle`, `RoundedSquare` ve `Teardrop` şekil seçenekleri (`IconShapes.kt` & `AosAppIcon.kt`)
- İkon Paketi Motoru (`IconPackManager` & `IconPackRepository`): Cihazda kurulu Nova, ADW, Apex, GO Launcher uyumlu 3. parti ikon paketlerini otomatik tarama, metadata okuma ve ikon drawables'ı dinamik yükleme altyapısı (L003 & L005 uyumlu)
- `ThemeCustomizationSection` & `SettingsScreen` Güncellemesi: Tema modu seçici, dinamik renk anahtarı, interaktif ikon şekli seçici, 3D sayfa geçiş efektleri ve yüklü ikon paketi seçim diyaloğu
- `LauncherActivity` Canlı Tema Bağlantısı: `AOSTheme` doğrudan kullanıcı tercihlerindeki aktif karanlık mod ve dinamik renk ayarlarına bağlandı

---
## [2026-09-07] — v0.7.0 — Faz 2 (Sprint 14-16): Profil & Mod Sistemi (İş, Odak, Gece, Çocuk, Araç)

### Eklendi
- `Profile` & `ProfileType`: Normal, İş (Work), Odak (Focus), Gece (Night), Çocuk (Kids) ve Araç (Car) modlarını içeren kapsamlı domain modeli ve `isAppAllowed` filtreleme fonksiyonu
- Room Veritabanı Kalıcılığı (`ProfileDao` & `ProfileEntity`): Transaction ile aktif profil değiştirme (`switchActiveProfile`), 6 varsayılan profilin otomatik tohumlanması (`ensureDefaultProfiles`)
- `ProfileRepository` & `ProfileRepositoryImpl`: Profil akışları (`getProfiles`, `getActiveProfile`), güncelleme, silme ve oluşturma operasyonları
- `ProfileSwitcherBar`: Ana ekranda canlı animasyonlu, yarı saydam cam efektli hızlı profil geçiş çubuğu (Quick Pill Carousel)
- Çocuk Modu (Kids Mode) PIN Güvenliği: Çocuk modundayken profilden çıkış yapmak istendiğinde 4-6 haneli ebeveyn PIN kodu doğrulama diyaloğu
- Bilinçli Sürtünme (Mindful Friction) & Engelleme Diyaloğu: Odak veya İş modu devredeyken sınırlandırılmış uygulamalar başlatılmak istendiğinde caydırıcı mola diyaloğu; Çocuk modunda ise izinsiz uygulamaları doğrudan bloke etme
- App Drawer Profil Filtresi: Aktif profile göre kısıtlanmış uygulamaları otomatik gizleme ve filtre durumu bilgi rozeti
- `ProfileManagementSection` & Ayarlar Entegrasyonu: Her profil için ad, ebeveyn PIN kodu ve otomatik zamanlama ayarlarını düzenleme arayüzü

---
## [2026-09-07] — v0.8.0 — Faz 2 Tamamlandı (Sprint 17-18): Gelişmiş Deneyim, 3D Paralaks, Bildirim Rozetleri & Arama Çubuğu

### Eklendi
- `GyroscopeParallaxModifier`: Cihazın ivme ve jiroskop sensör hareketlerini okuyarak ana ekrandaki sayfalara ve widget'lara akıcı 3D derinlik kayması kazandıran sensör motoru
- `AosNotificationListenerService`: Gelen sistem bildirimlerini arka planda dinleyerek paket bazlı okunmamış bildirim sayaçlarını toplayan servis
- Bildirim Rozeti (Notification Badges) Desteği: Uygulama ikonlarının (`AosAppIcon`) sağ üst köşesinde okunmamış bildirim adedini gösteren kırmızı rozet göstergesi
- `LauncherSearchBar`: Ana ekranda Google, DuckDuckGo veya Bing arama motorlarına tek tıkla sorgu gönderebilen ve sesli arama başlatan şık arama çubuğu
- `AdvancedCustomizationSection`: Ayarlar ekranında paralaks aç/kapa, bildirim rozetleri aç/kapa ve arama motoru tercihi seçenekleri
- **FAZ 2 (Kişiselleştirme & Profiller) %100 Başarıyla Tamamlandı**

---

## [2026-09-07] — v0.9.0 — Faz 3 (Sprint 19-21): AI Altyapısı & Akıllı Uygulama Öneri Motoru

### Eklendi
- `AppSuggestion`: Cihaz içi yapay zeka öneri modeli (paket adı, etiket, skor, bağlam sebebi: "Sabah Rutini", "İş & Verimlilik", "Sık Kullanılan")
- `UsageStatsHelper`: Android `UsageStatsManager` entegrasyonu ile son 24 saatlik ön plan kullanım sürelerini (foreground time) analiz eden ve `OPSTR_GET_USAGE_STATS` iznini kontrol eden yardımcı motor
- `OnDeviceSuggestionEngine`: Saat dilimlerine göre (06-12 Sabah, 12-18 İş, 18-23 Akşam, 23-06 Gece) ve yerel başlatma sıklıklarına göre skorlama yapan %100 gizlilik odaklı (yerel) AI tahmin algoritması
- Room Feature Store (`AppLaunchEventEntity` & `AppLaunchEventDao`): Uygulama başlatma zamanlarını ve saatlerini yerel SQLite veritabanında saklayan ML özellik deposu
- `SuggestedAppsRow`: Uygulama çekmecesinin (App Drawer) en üstünde kullanıcının o anda açmak isteyeceği ilk 5 uygulamayı sunan parlayan AI öneri rafı
- `AiSettingsSection` & Ayarlar Entegrasyonu: AI tahmin anahtarı, sistem kullanım erişimi izni durumu/kısayolu ve öğrenilen yerel verileri sıfırlama seçeneği
- `LauncherActivity` Uygulama Başlatma İzleyicisi: Ana ekrandan veya çekmeceden her uygulama açıldığında yerel AI motoruna öğrenme olayının kaydedilmesi

---

## [2026-09-07] — v1.0.0 — Faz 3: On-Device AI Akıllı Kartlar & Doğal Dil Asistanı

### Eklendi
- `SmartContextCard` & `AssistantCommand`: AI bağlamsal durum kartları ve doğal dil komut sonuçları için saf Kotlin domain modelleri
- `SmartContextEngine`: Saat dilimini (Sabah, İş, Akşam, Gece), aktif profili (Standart, İş, Odak, Gece, Çocuk, Araç) ve en çok kullanılan uygulamayı füzyonlayarak proaktif kartlar üreten bağlam motoru
- `LocalNluCommandParser`: %100 cihaz içinde çalışan, sıfır gecikmeli doğal dil komut ayrıştırıcı ("aç [app]", "ara [sorgu]", "iş moduna geç", "odaklan", "gece modu", "ekranı kilitle", "ayarları aç")
- `SmartAssistantRepository` & `SmartAssistantRepositoryImpl`: Clean Architecture veri katmanı asistan köprüsü
- `SmartContextCardWidget`: Ana ekranda saat widget'ının altında proaktif olarak beliren, parlayan rozetli ve doğrudan eylem butonlu glassmorphic akıllı kart bileşeni
- `AssistantBottomSheet`: Arama çubuğundaki mikrofon ikonu ile açılan, hızlı öneri hapları, canlı doğal dil komut girişi ve tek dokunuşla eylemi yürüten Material 3 asistan paneli
- `LauncherActivity` Asistan Yürütme Hattı: Asistan komutlarına göre uygulama başlatma, profil değiştirme, web'de arama yapma, cihaz kilitleme ve ayarları açma işlemleri

---

## [2026-09-07] — v1.1.0 — Faz 4 (Sprint 29-31): AES-256 Şifreli Yedekleme & Bulut Senkronizasyonu

### Eklendi
- `BackupPayload` & `SyncStatus`: Tüm başlatıcı durumunu (sayfalar, dock, klasörler, 6 profil, tema konfigürasyonu ve ızgara ayarları) kapsayan dışa aktarılabilir ve eşitlenebilir domain modelleri
- `BackupRepository`: Şifreli dışa aktarma, içe aktarma, atomik geri yükleme ve bulut senkronizasyonu repository arayüzü
- `AosCryptoManager`: Client-side **AES-256-GCM** ve **PBKDF2WithHmacSHA256** (10.000 iterasyon, rastgele 16-byte tuz) tabanlı sıfır-bilgi (zero-knowledge) şifreleme ve bütünlük doğrulama motoru
- `BackupRepositoryImpl`: Tüm Room ve DataStore verilerini kayıpsız JSON biçimine serileştiren ve atomik olarak geri yükleyen repository implementasyonu
- `BackupSyncSection`: Ayarlar ekranında yer alan, tek tıkla şifreli yedek alma (Base64 pano kopyalama), parolalı geri yükleme ve canlı bulut senkronizasyonu durum kartı
- `AosCryptoManagerTest`: AES-256 doğru parola, yanlış parola, tahrif edilmiş veri ve geçersiz başlık senaryolarını test eden kapsamlı birim test paketi

---

## [2026-09-07] — v1.2.0 — Faz 4 (Sprint 32-33): Modüler Plugin Mimarisi & Geliştirici SDK

### Eklendi
- `PluginManifest`, `PluginType` & `PluginPermission`: 3. parti eklenti kimlik, tür ve güvenlik yetki sözleşmeleri
- `AosPlugin`, `SearchPlugin`, `WidgetPlugin`, `ActionPlugin`: Arama motorları, özel widget'lar ve jest eylemleri için saf Kotlin eklenti arayüzleri
- `PluginSecuritySandbox`: Eklentilerin yetkisiz işlem yapmasını engelleyen güvenlik izin kontrol mekanizması
- `BuiltinPlugins`: Canlı hava durumu widget'ı (`WeatherWidgetPlugin`), DuckDuckGo gizli arama sağlayıcısı (`DuckDuckGoSearchPlugin`) ve fener aksiyonu (`QuickFlashlightActionPlugin`) hazır eklentileri
- `PluginRepositoryImpl`: Eklenti durumlarını (açık/kapalı) yöneten ve aktif arama/widget/aksiyon eklentilerini yetki kontrolüyle filtreleyen repository uygulaması
- `PluginManagementSection`: Ayarlar ekranında eklentileri listeleyen, tür/izin rozetlerini gösteren ve anında açıp kapatmayı sağlayan arayüz paneli
- `PluginSecuritySandboxTest`: İzin kontrolü ve yetkisiz eylemlerin engellenmesini doğrulayan birim test paketi

---

## [2026-09-07] — v1.3.0 — Faz 4 (Sprint 34-35): Bildirim Geçmişi & Gizli Uygulama Kasası (Hidden Vault)

### Eklendi
- `NotificationRecord` & `NotificationHistoryRepository`: Saf Kotlin domain katmanında bildirim arşivi veri modelleri ve akış sözleşmeleri
- `NotificationRecordEntity` & `NotificationRecordDao`: Gelen bildirimleri cihaz üzerinde yerel olarak saklayan Room veritabanı tablosu ve DAO arayüzü (`AosLauncherDatabase` ve `DatabaseModule` entegrasyonu)
- `AosNotificationListenerService`: Hilt `EntryPoint` üzerinden gelen anlık bildirimleri (başlık, içerik, uygulama adı, zaman damgası) arka planda asenkron olarak veritabanına kaydeden servis güncellemesi
- `NotificationHistorySection` & `NotificationHistoryDialog`: Ayarlar ekranında canlı sayaç rozeti, en son bildirim önizlemesi ve tam ekran arama, filtreleme, tek tek silme ve "Tümünü Temizle" diyaloğu
- `HiddenAppsRepository` & `HiddenAppsRepositoryImpl`: Gizlenen uygulamaların paket adlarını ve 4 haneli kasa PIN kodunu yöneten repository implementasyonu
- `UserPreferencesDataStore` Genişletmesi: `HIDDEN_PACKAGES` ve `VAULT_PIN` anahtarları ile reaktif `Flow<Set<String>>` ve `Flow<String?>` desteği
- Gizli Uygulama Kasası (`HiddenVaultDialog`):
  - 4 haneli PIN belirleme, onaylama ve doğrulama akışları
  - Özel sayısal tuş takımı (`NumericKeypad`: 0-9, Backspace, hata animasyonları)
  - Kilitli kasa açıldığında gizli uygulamaları listeleme, doğrudan kasadan başlatma veya "Görünür Yap" ile çekmeceye iade etme
  - Anında PIN değiştirme ve tek dokunuşla kasayı kilitleme
- App Drawer Entegrasyonu:
  - Uzun basma menüsüne (`AppDrawerContextMenu`) "Uygulamayı Gizle (Kasa)" seçeneği eklendi
  - Arama çubuğuna kasa kilit durumu ikonu eklendi (Kilitli/Açık/Dolu göstergeleri)
  - Gizlenen uygulamalar App Drawer ana ızgarasından otomatik ve tamamen filtrelendi
- `NotificationRecordEntityTest`: Entity ve Domain modeli çift yönlü dönüşüm doğrulama testleri

---

## [2026-09-07] — v1.4.0 — Faz 5 (Sprint 36-39): Stabilite, Bellek Optimizasyonu, Onboarding ve Teşhis

### Eklendi
- `OnboardingScreen`: 4 adımlı modern ilk açılış karşılama sihirbazı:
  - Sayfa 1 (Hoş Geldiniz): AOS Launcher minimalist felsefesi, 120 FPS ultra akıcılık ve %100 yerel gizlilik vurguları
  - Sayfa 2 (Varsayılan Başlatıcı): `RoleManager` (API 29+) veya `ACTION_HOME_SETTINGS` ile tek tıkla ana ekran ataması
  - Sayfa 3 (İzinler & Yetkiler): Bildirim erişimi ve kullanım istatistiği erişimini açma rehberi kartları
  - Sayfa 4 (İlk Tercihler): Karanlık mod ve Material You dinamik renk anahtarları
- `isOnboardingCompleted`: Kullanıcı ilk kurulumu tamamladığında veya atladığında `UserPreferencesDataStore`'da saklanan reaktif durum
- `AosCrashReporter`: Global `Thread.UncaughtExceptionHandler` tabanlı, kullanıcı gizliliğini %100 koruyarak beklenmeyen çökmeleri yerel JSON formatında dosyalara kaydeden offline hata teşhis motoru
- `CrashReport` & `CrashDiagnosticsRepository`: Saf Kotlin domain çökme modeli ve repository sözleşmesi
- `AboutAndDiagnosticsSection`: Ayarlar ekranında yer alan sürüm bilgisi (v1.4.0), açık kaynak lisansları, gizlilik sertifikası ve çökme günlüklerini inceleme/temizleme diyaloğu
- `AppIconCache` (L016 Optimizasyonu): `ConcurrentHashMap` yerine maksimum 150 eleman kapasiteli `android.util.LruCache<String, Bitmap>` ve bellek sızıntısını önleyici `clear()` desteği
- `app/proguard-rules.pro`: Release yapılandırması için Kotlinx Serialization, Room, Hilt ve Compose R8 optimizasyon kuralları
- `CrashReportTest`: Çökme modeli JSON serileştirme ve veri bütünlüğü birim testleri

---

## [GELECEK] — v2.0.0 — Production Release: Play Store Lansmanı

### Planlandı
- Play Store ekran görüntüleri ve tanıtım materyalleri
- Otomasyonlu Firebase Test Lab doğrulama koşusu
- Nihai production release


## Değişiklik Kategorileri (Kullanım Kılavuzu)

Bu dosyaya yeni giriş eklerken aşağıdaki kategorileri kullan:

- **Eklendi** — Yeni özellikler
- **Değiştirildi** — Mevcut özelliklerde değişiklik
- **Düzeltildi** — Bug fix
- **Kaldırıldı** — Artık mevcut olmayan özellikler
- **Güvenlik** — Güvenlik açığı kapatma
- **Performans** — Performans iyileştirmesi
- **Deprecate** — Yakında kaldırılacak özellikler




