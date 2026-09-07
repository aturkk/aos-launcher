# AOS Launcher — Yapılacaklar Listesi (Todo)

> Bu dosya her sprint sonunda güncellenir.
> Format: [ ] bekliyor | [/] devam ediyor | [x] tamamlandı

---

## 🟢 FAZ 0 — Hazırlık (TAMAMLANDI)

### Proje Altyapısı
- [ ] Android Studio Giraffe veya üstü kurulumu
- [ ] Android SDK API 26-35 yüklenmesi
- [ ] Kotlin 2.x + Gradle 8.x setup
- [x] Multi-module proje iskeleti oluşturma
  - [x] `app` modülü (LauncherActivity)
  - [x] `core:common` modülü
  - [x] `core:domain` modülü
  - [x] `core:data` modülü
  - [x] `core:ui` modülü
  - [x] `feature:home` modülü
  - [x] `feature:appdrawer` modülü
  - [x] `feature:settings` modülü
- [x] `libs.versions.toml` Version Catalog kurulumu
- [x] Hilt DI kurulumu (tüm modüller)
- [x] Room database ilk şeması
  - [x] LauncherItem entity
  - [x] PageEntity
  - [x] ProfileEntity
  - [x] ThemeConfigEntity
- [x] DataStore şeması (UserPreferences)
- [ ] ktlint konfigürasyonu
- [ ] detekt konfigürasyonu
- [x] GitHub Actions CI pipeline
  - [x] Build job
  - [x] Unit test job
  - [ ] Lint job
- [x] Temel Material You tema kurulumu
- [x] LauncherActivity (HOME intent filter, BackHandler, Wallpaper show)
- [ ] Git branching stratejisi: main/develop/feature/*

---

## 🟢 FAZ 1 — Core MVP (TAMAMLANDI)

### Ana Ekran Motoru
- [x] `HomeScreenEngine` tasarımı ve implementasyonu
- [x] `LauncherPage` / `GridCellLayout` Composable
- [x] Sonsuz yatay sayfa sistemi (HorizontalPager)
- [x] Sayfa indikatörü (dots indicator)
- [x] `LauncherItem` sealed class (App, Folder, Widget, Shortcut)
- [x] İkon render pipeline (AppIconImage / Bitmap önbelleği)
- [x] Uygulama açma (LauncherApps.startMainActivity)
- [x] Uzun basma context menü (Popup DropdownMenu)
- [x] Duvar kağıdı gösterimi (WallpaperManager)
- [x] Grid boyutu ayarı (4x5 default)

### App Drawer
- [x] Swipe-up jesti ile açılma animasyonu
- [x] Tüm kurulu uygulamaları listeleme (PackageManager)
- [x] Alfabetik sıralama + hızlı A..Z indeks barı
- [x] Kullanım sıklığı sıralaması altyapısı
- [x] Temel fuzzy arama kutusu
- [x] Grid görünümü ve A..Z gezinme
- [x] PackageReceiver: install/uninstall dinleme

### Drag & Drop
- [x] İkon sürükle-bırak (aynı sayfa içi hücre taşıma)
- [x] Sayfalar arası sürükle-bırak
- [x] Klasör oluşturma (iki ikonun üst üste bırakılması)
- [x] Klasör açılış animasyonu ve FolderModalDialog
- [x] Klasör adı düzenleme (inline TextField)
- [x] Dock bar (alt alan, sabit 5 slot)

### Widget Motoru
- [x] AppWidgetHost custom implementasyonu (LauncherWidgetHost)
- [x] Widget ekleme sihirbazı (LauncherWidgetHost)
- [x] Widget resize (hücre span yapısı)
- [x] Widget güncelleme döngüsü (startListening/stopListening)
- [x] Özel clock widget (AosClockWidget)
- [x] Özel saat & tarih widget (AosClockWidget)
- [x] Özel pil & durum bilgisi

### Jestler
- [ ] Swipe-up → App Drawer
- [x] Swipe-down → Bildirim paneli
- [x] Double-tap → Ekran kilidi
- [x] Pinch → Sayfa overview (PagesOverviewSheet)
- [x] Uzun basma → Düzenleme modu
- [x] Jest motoru (Swipe up/down, Double tap, Pinch)

---

## 🟢 FAZ 2 — Kişiselleştirme & Profiller (TAMAMLANDI)

### Tema Motoru
- [x] ThemeRepository (DataStore / Room)
- [x] Material You dinamik renk entegrasyonu
- [x] Light/Dark/Auto mod
- [ ] Palette API ile duvar kağıdından renk çıkarma
- [ ] Özel renk şeması oluşturma UI
- [ ] Tema JSON import/export
- [x] İkon paketi APK'dan yükleme (IconPackManager + IconPackRepository)
- [x] İkon şekil özelleştirme (Squircle, Circle, RoundedSquare, Teardrop)
- [ ] İkon ölçek & padding ayarı
- [ ] Tek tek ikon değiştirme
- [ ] 30+ animasyon presetleri
- [x] Sayfa geçiş efektleri (cube, flip, depth, accordion - 3D GraphicsLayer)
- [ ] Font yükleme (Google Fonts + özel TTF)

### Profil Sistemi
- [x] ProfileEngine tasarımı (Room ProfileDao, ProfileEntity, ProfileRepository)
- [x] İş Modu implementasyonu (dikkat dağıtıcı uygulama engeli)
- [x] Odak Modu + mola friction diyaloğu
- [x] Gece Modu
- [x] Çocuk Modu + PIN koruması (ProfileSwitcherBar güvenli çıkış)
- [x] Araç Modu
- [x] Profil zamanlayıcı altyapısı (schedule bayrakları ve saat aralıkları)
- [ ] Konum bazlı profil (Geofence API)
- [x] Profil geçiş animasyonu (ProfileSwitcherBar)

### Gelişmiş Kişiselleştirme
- [ ] Canlı duvar kağıdı desteği
- [ ] Günün saatine göre duvar kağıdı
- [x] Paralaks efekti (gyroscope - GyroscopeParallaxModifier)
- [ ] WYSIWYG widget editörü
- [x] İkon bildirim rozetleri (AosAppIcon & AosNotificationListenerService)
- [x] Arama çubuğu stilleri (LauncherSearchBar & Google/DuckDuckGo/Bing)
- [ ] StatusBar renk özelleştirme

---

## 🟢 FAZ 3 — AI Entegrasyonu (TAMAMLANDI)

### Veri & Altyapı
- [x] UsageStatsManager entegrasyonu (UsageStatsHelper)
- [x] FeatureExtractor pipeline (zaman, gün, sıklık analiz motoru)
- [x] ML Feature Store (Room AppLaunchEventDao)
- [x] On-Device yerel öneri motoru (OnDeviceSuggestionEngine)
- [x] Privacy opt-in/opt-out UI & Veri sıfırlama (AiSettingsSection)
- [x] App Drawer AI Öneri Rafı (SuggestedAppsRow)

### Öneri Motoru & Proaktif Kartlar
- [x] Zaman & bağlam bazlı öneri modeli (Sabah, İş, Akşam, Gece)
- [x] Proaktif Ana ekran akıllı öneri kartı widget'ı (`SmartContextCardWidget.kt`)
- [x] Aktif profil ve zaman dilimi akıllı entegrasyonu (`SmartContextEngine.kt`)
- [x] Dinamik bağlamsal butonlar (Tek tıkla önerilen app açma, profil değiştirme, ekran kilitleme)

### Cihaz İçi Doğal Dil & Akıllı Asistan
- [x] Yerel NLU pipeline (komut parser: `LocalNluCommandParser.kt`)
- [x] Temel komutlar: "[uygulama] aç", "ara [sorgu]", "ekranı kilitle", "ayarları aç"
- [x] Bağlamsal profil komutları: "iş moduna geç", "odaklan", "gece modu", "çocuk alanı", "araç modu"
- [x] Sesli komut entegrasyonu (SpeechRecognizer / RecognizerIntent)
- [x] Asistan UI (`AssistantBottomSheet.kt`: Hızlı öneri çipleri, sesli buton, komut yürütme)
- [x] Launcher arama çubuğu mikrofon ikonu ile doğrudan asistan tetikleme
- [x] Hilt DI SmartAssistantRepository entegrasyonu

---

## 🟢 FAZ 4 — Ekosistem & Eklentiler (TAMAMLANDI)

### Bulut Senkronizasyonu & AES-256 Şifreli Yedekleme
- [x] Cihaz modeli & kimliği entegrasyonu (deviceName)
- [x] Backup & Sync domain modeli (BackupPayload & SyncStatus)
- [x] AES-256-GCM + PBKDF2WithHmacSHA256 şifreleme motoru (AosCryptoManager)
- [x] Kapsamlı JSON serileştirme (sayfalar, dock, klasörler, widgetlar, profiller, tema)
- [x] Atomik temizleme ve geri yükleme (BackupRepositoryImpl)
- [x] Bulut senkronizasyon simülasyonu ve durum takibi
- [x] Ayarlar ekranı Yedekleme & Senkronizasyon paneli (BackupSyncSection)
- [x] Kripto ve şifreleme birim testleri (AosCryptoManagerTest)

### Plugin Sistemi & Modüler Eklenti SDK
- [x] Plugin API (AosPlugin, SearchPlugin, WidgetPlugin, ActionPlugin)
- [x] Plugin manifest şeması (PluginManifest & PluginPermission)
- [x] Plugin sandbox & güvenlik izin denetimi (PluginSecuritySandbox)
- [x] Widget plugin desteği (WeatherWidgetPlugin)
- [x] Search provider plugin desteği (DuckDuckGoSearchPlugin)
- [x] Gesture action plugin desteği (QuickFlashlightActionPlugin)
- [x] Ayarlar ekranı Eklenti Yönetim Paneli (PluginManagementSection)
- [x] Eklenti izin ve güvenlik birim testleri (PluginSecuritySandboxTest)

### Entegrasyonlar & Gizlilik (Bildirim Geçmişi & Gizli Kasa)
- [x] Bildirim Geçmişi domain modeli ve Repository arayüzü (`NotificationRecord`, `NotificationHistoryRepository`)
- [x] Room veritabanı entity & DAO (`NotificationRecordEntity`, `NotificationRecordDao`)
- [x] Canlı bildirim dinleme ve arka plan kaydı (`AosNotificationListenerService` EntryPoint)
- [x] Ayarlar ekranı bildirim günlüğü ve arama diyaloğu (`NotificationHistorySection`, `NotificationHistoryDialog`)
- [x] Tek tıkla bildirim silme ve tümünü temizleme
- [x] Gizli uygulama kasası DataStore altyapısı (`hidden_packages`, `vault_pin`)
- [x] Gizli uygulamaları App Drawer ızgarasından otomatik filtreleme
- [x] 4 haneli PIN belirleme ve doğrulama sayısal tuş takımı (`HiddenVaultDialog`, `NumericKeypad`)
- [x] App Drawer üzerinden uzun basma ile "Uygulamayı Gizle (Kasa)" seçeneği
- [x] Kasa içinden doğrudan başlatma ve "Görünür Yap" seçeneği
- [x] Model ve dönüşüm birim testleri (`NotificationRecordEntityTest`)

---

## 🟡 FAZ 5 — Stabilite & Launch (AKTİF)

### Test, Bellek & Teşhis
- [x] Bounded LruCache bellek optimizasyonu (Max 150 ikon önbelleği, OOM önleme)
- [x] Yerel hata raporlama motoru (`AosCrashReporter` UncaughtExceptionHandler)
- [x] Sistem hata teşhis arayüzü ve yığın izi görüntüleyici (`AboutAndDiagnosticsSection`)
- [x] R8 / Proguard optimizasyon kuralları (`app/proguard-rules.pro`)
- [x] Çökme modeli serileştirme birim testleri (`CrashReportTest`)
- [ ] Macrobenchmark: cold start < 300ms
- [ ] Frame timing analizi (Perfetto)
- [ ] Android 8-15 uyumluluk testi

### Launch & Karşılama
- [x] Onboarding akışı: 4 adımlı ilk açılış sihirbazı (`OnboardingScreen`)
- [x] Varsayılan başlatıcı rolü atama rehberi (`RoleManager` / `ACTION_HOME_SETTINGS`)
- [x] İsteğe bağlı izinler yapılandırma kartları (Bildirim & Kullanım İstatistikleri)
- [x] İlk tema ve ızgara tercihi kurulumu
- [x] Uygulama sürümü, açık kaynak lisansları ve %100 yerel gizlilik garantisi kartı
- [ ] Play Store varlıkları (screenshots, video, description)
- [ ] Gizlilik politikası sayfası
- [ ] Production release: Play Store

---

## 📌 Sürekli Görevler (Her Sprint)
- [ ] changelog.md güncelle
- [ ] lessons.md güncelle (yeni dersler)
- [ ] Phase.md tamamlanan görevleri işaretle
- [ ] Dependency güncellemelerini kontrol et
- [ ] Security vulnerability scan (Dependabot)




