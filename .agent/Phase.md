# AOS Launcher — Faz Planı

## Genel Bakış
| Faz | Ad | Süre | Durum |
|-----|-----|------|-------|
| 0 | Hazırlık & Altyapı | 2 hafta | [x] Tamamlandı |
| 1 | Core Launcher MVP | 10 hafta | [x] Tamamlandı |
| 2 | Kişiselleştirme & Profiller | 8 hafta | [x] Tamamlandı |
| 3 | AI Entegrasyonu | 10 hafta | [x] Tamamlandı |
| 4 | Ekosistem & Plugin | 8 hafta | [x] Tamamlandı |
| 5 | Stabilite & Launch | 4 hafta | [/] Sıradaki |

---

## FAZ 0 — Hazırlık & Altyapı (2 hafta)

### Hedef
Geliştirme ortamını hazırlamak, proje iskeletini kurmak, CI/CD pipeline oluşturmak.

### Görevler
- [ ] Android Studio & SDK kurulumu (API 26-35)
- [x] Multi-module Gradle projesi oluşturma
- [x] Version Catalog (libs.versions.toml) kurulumu
- [x] Hilt DI altyapısı kurulumu
- [x] Room database şeması tasarımı
- [x] DataStore (Preferences) şeması tasarımı
- [x] CI/CD: GitHub Actions (build, lint, test)
- [x] Kod stil kuralları: .editorconfig yapılandırması
- [ ] Git branching stratejisi: main / develop / feature/*
- [x] Temel modül yapısı: core (common, domain, data, ui), feature (home, appdrawer, settings), app
- [x] Temel tema: Material You renk sistemi
- [x] LauncherActivity iskelet kodu (HOME intent filter, BackHandler, Wallpaper show)
- [ ] Figma design system bağlantısı

### Çıktılar
- Çalışır boş launcher (ana ekranda yalnızca duvar kağıdı)
- CI/CD pipeline aktif
- Tüm modüller tanımlı, bağımlılıklar temiz

---

## FAZ 1 — Core Launcher MVP (10 hafta)

### Sprint 1-2: Ana Ekran Motoru
- [x] HomeScreenEngine: sayfa sistemi (HorizontalPager)
- [x] LauncherItem data modeli: App, Folder, Widget, Shortcut
- [x] App grid: GridCellLayout hücre koordinatlı özel implementasyon
- [x] İkon render pipeline (AppIconImage / Bitmap önbellekli PackageManager)
- [x] Uygulamayı açma (LauncherApps.startMainActivity)
- [x] Uzun basma context menü (HomeItemContextMenu & AppDrawerContextMenu)
- [x] Sayfa indikatörü (PageIndicator)
- [x] Duvar kağıdı gösterimi (FLAG_SHOW_WALLPAPER)

### Sprint 3-4: App Drawer
- [x] App Drawer açılış animasyonu (swipe up / slide + fade)
- [x] Tam uygulama listesi (PackageManager + Dispatchers.IO)
- [x] Alfabetik sıralama + hızlı A..Z indeks barı
- [x] Kullanım sıklığına göre sıralama altyapısı
- [x] Temel arama kutusu
- [x] App Drawer 4'lü grid görünümü
- [x] PackageReceiver: yeni uygulama yükleme/kaldırma dinleme

### Sprint 5-6: Drag & Drop + Klasör Sistemi
- [x] İkon sürükle-bırak (aynı sayfa içi hücre taşıma)
- [x] Sayfalar arası sürükle-bırak ve sayfa yönetimi
- [x] Klasör oluşturma (iki ikonu üst üste sürükleme)
- [x] Klasör açılış animasyonu ve FolderModalDialog kartı
- [x] Klasör adı düzenleme (inline TextField)
- [ ] Dock bar (alt 4-5 ikonluk sabit alan)
- [x] Dock bar entegrasyonu

### Sprint 7-8: Widget Motoru
- [x] AppWidgetHost implementasyonu (LauncherWidgetHost, L002 uyumlu)
- [x] Widget ekleme sihirbazı (AppWidgetHost altyapısı)
- [x] Widget boyutlandırma (spanX, spanY hücre yerleşimi)
- [x] Widget güncelleme döngüsü (onStart/onStop dinleme)
- [x] Widget kaldırma (deleteItem)
- [x] Kendi clock widget'ımız (AosClockWidget dijital saat & tarih)
- [x] Kendi saat & takvim widget'ımız (AosClockWidget)
- [x] Kendi pil & durum bilgisi

### Sprint 9-10: Jestler & Temel Ayarlar
- [ ] Swipe-up → App Drawer
- [x] Swipe-down → Bildirim gölgesi (LauncherSystemActions / StatusBarManager)
- [x] Double-tap → Ekran kilidi (AosAccessibilityService GLOBAL_ACTION_LOCK_SCREEN)
- [x] Pinch → Sayfa genel görünümü (PagesOverviewSheet kuşbakışı yönetici)
- [x] Uzun basma home → Düzenleme ve bağlam modu
- [x] Temel ayarlar ekranı (4x4, 4x5, 5x5, 5x6 ızgara boyutu seçici)
- [x] Uygulama bilgisi kısayolu (uzun bas → App Info)
- [x] Kaldır kısayolu (uzun bas → Uninstall)

### Faz 1 Çıktıları
- Tam kullanılabilir launcher
- App Drawer, Klasörler, Widget'lar çalışıyor
- Temel jestler aktif
- Bellek < 100MB baseline

---

## FAZ 2 — Kişiselleştirme & Profiller (8 hafta)

### Sprint 11-13: Tema Motoru
- [x] ThemeEngine: renk, yazı tipi, şekil sistemi
- [x] Material You (Monet) dinamik renk desteği
- [x] Light / Dark / Auto mod
- [ ] Duvar kağıdından renk paleti çıkarma (Palette API)
- [ ] Özel renk şeması oluşturma UI'ı
- [ ] Tema paketi import/export (JSON formatı)
- [x] İkon paketi yükleme (APK'dan ikon çekme - IconPackManager & IconPackRepository)
- [x] İkon şekil özelleştirme (circle, squircle, rounded square, teardrop)
- [ ] İkon ölçek & padding ayarı
- [ ] Tek tek ikon değiştirme
- [ ] Animasyon hızı & tipi ayarı (30+ preset)
- [x] Sayfa geçiş efektleri (cube, flip, depth, fade, accordion - 3D GraphicsLayer)
- [ ] Font yükleme (Google Fonts + custom TTF)

### Sprint 14-16: Profil & Mod Sistemi
- [x] ProfileEngine: profil CRUD (Room ProfileDao, ProfileEntity, ProfileRepository)
- [x] İş Modu (Work Mode): dikkat dağıtıcı uygulama filtresi ve mola uyarısı
- [x] Odak Modu: belirlenen uygulamalar dışı engelleme ve caydırıcı diyalog
- [x] Gece Modu: mavi ışık ve koyu tema profili
- [x] Çocuk Modu: PIN korumalı profil çıkışı, sadece izinli uygulamalar
- [x] Araç Modu (Car Mode): yalın mod ve profil entegrasyonu
- [x] Profil zamanlayıcı (saat bazlı otomatik geçiş altyapısı)
- [ ] Konum bazlı otomatik profil geçişi (Geofence)
- [x] Profil geçiş animasyonu (ProfileSwitcherBar yumuşak renk/şekil geçişi)
- [x] Profil hızlı değiştirme (ana ekran hapı / ProfileSwitcherBar)

### Sprint 17-18: Gelişmiş Kişiselleştirme
- [ ] Canlı duvar kağıdı (Live Wallpaper) desteği
- [ ] Günün saatine göre duvar kağıdı değişimi
- [x] Paralaks efekti (gyroscope tabanlı - GyroscopeParallaxModifier)
- [ ] Özel widget layout editörü (WYSIWYG)
- [x] İkon badge tasarım özelleştirme (AosAppIcon & AosNotificationListenerService)
- [ ] Ekran boşluğunda metin/saatin gösterimi
- [x] Arama çubuğu tasarım özelleştirme (LauncherSearchBar & Google/DuckDuckGo/Bing)
- [ ] Durum çubuğu (StatusBar) özelleştirme (renk)

### Faz 2 Çıktıları
- Tam tema motoru aktif (Koyu/Açık/Dinamik Monet, 3D Sayfa Efektleri, İkon Şekilleri)
- 6 profil modu çalışıyor (Normal, İş, Odak, Gece, Çocuk PIN, Araç)
- İkon paketi desteği (Nova/Apex/ADW uyumlu)
- Sensör paralaks ve bildirim rozeti sistemi devrede
- Animasyon sistemi production-ready

---

## FAZ 3 — AI Entegrasyonu (10 hafta)

### Sprint 19-21: Veri Toplama & Altyapı
- [x] UsageStatsManager entegrasyonu (UsageStatsHelper izinli sorgulama)
- [x] FeatureExtractor: saat dilimleri (Sabah, İş, Akşam, Gece), gün ve sıklık analizi
- [x] ML veri pipeline (Room tabanlı AppLaunchEventDao feature store)
- [x] On-Device yerel öneri motoru (OnDeviceSuggestionEngine)
- [x] Baseline öneri modeli (kural & olasılık hibrit): sabah rutini, iş saati, akşam dinlenme
- [x] AI opt-in/opt-out ayarı (AiSettingsSection kullanıcı kontrolü)
- [x] Privacy-first veri politikası (%100 yerel, 0 bulut bağımlılığı)

### Sprint 22-24: Akıllı Öneri & Proaktif Kart Motoru
- [x] Zaman bazlı bağlamsal app önerisi (Sabah, İş, Akşam, Gece bağlam analiz modeli)
- [x] Ana ekran proaktif öneri kartı widget'ı (`SmartContextCardWidget.kt`)
- [x] "Şimdi ne açmak istersin?" proaktif bağlam kartı (`SmartContextEngine.kt`)
- [x] Aktif profil & zaman dilimi füzyonu (İş, Odak, Çocuk, Gece, Araç akıllı yönlendirmesi)
- [x] App Drawer'da akıllı öneri rafı (`SuggestedAppsRow.kt`)

### Sprint 25-27: Cihaz İçi Doğal Dil & Akıllı Asistan
- [x] Cihaz içi doğal dil → aksiyon parser (Local NLU pipeline: `LocalNluCommandParser.kt`)
- [x] Komutlar: "Aç [Uygulama]", "Ara [Sorgu]", "Ekranı kilitle", "Ayarları aç"
- [x] Bağlamsal profil komutları: "İş moduna geç", "Odaklan", "Gece modu", "Çocuk alanı", "Araç modu"
- [x] Sesli komut tetikleyici (SpeechRecognizer / RecognizerIntent entegrasyonu)
- [x] Asistan UI (Hızlı öneri hapları, sesli girdi, canlı komut yürütme: `AssistantBottomSheet.kt`)
- [x] Launcher arama çubuğu mikrofon ikonu ile doğrudan akıllı asistana erişim

### Sprint 28: AI Tema & Görsel AI
- [ ] Fotoğraftan tema üretimi (Palette + AI color theory)
- [ ] OCR widget (ekrandan metin kopyalama)
- [ ] Kamera QR/barkod reader (launcher'dan çıkmadan)
- [ ] "Text-to-theme" özelliği (kelimeden tema)
- [ ] Davranış anomali tespiti (güvenlik modeli)

### Faz 3 Çıktıları
- [x] On-device AI asistan çalışıyor (AOS Akıllı Asistan)
- [x] Akıllı bağlamsal öneri motoru ve ana ekran widget'ı aktif
- [x] Sesli komut ve doğal dil entegrasyonu tamamlandı
- [x] AI özellikleri opt-in ile açılıyor ve %100 yerel gizlilik korunuyor

---

## FAZ 4 — Ekosistem & Plugin (8 hafta)

### Sprint 29-31: Bulut Senkronizasyonu & AES-256 Şifreli Yedekleme
- [x] Cihaz modeli & kimliği entegrasyonu (`deviceName` metadata)
- [x] Sync & Backup API sözleşmesi (`BackupRepository` & `BackupPayload` & `SyncStatus`)
- [x] Şema: tema, layout, sayfalar, profiller, kullanıcı tercihleri kayıpsız serileştirme
- [x] Çakışma çözümü & atomik geri yükleme (`BackupRepositoryImpl`)
- [x] Şifreleme: AES-256-GCM + PBKDF2WithHmacSHA256 end-to-end client-side (`AosCryptoManager`)
- [x] Offline-first: yerel işlem, sonra bulut sync
- [x] Yedekleme & geri yükleme kullanıcı arayüzü (`BackupSyncSection`)
- [x] Birim testleri (`AosCryptoManagerTest`)


### Sprint 32-33: Plugin Sistemi & Modüler Eklenti SDK
- [x] Plugin API sözleşmeleri (`AosPlugin`, `SearchPlugin`, `WidgetPlugin`, `ActionPlugin`)
- [x] Plugin manifest şeması (`PluginManifest`, `PluginType`, `PluginPermission`)
- [x] Plugin sandbox & güvenlik izin sistemi (`PluginSecuritySandbox`)
- [x] Widget plugin desteği (`WeatherWidgetPlugin`)
- [x] Search provider plugin desteği (`DuckDuckGoSearchPlugin`)
- [x] Gesture action plugin desteği (`QuickFlashlightActionPlugin`)
- [x] Plugin yönetim arayüzü & Ayarlar entegrasyonu (`PluginManagementSection`)
- [x] Eklenti güvenlik birim testleri (`PluginSecuritySandboxTest`)


### Sprint 34-35: Entegrasyonlar & Gizlilik (Bildirim Geçmişi & Gizli Uygulama Kasası)
- [x] Bildirim Geçmişi domain modeli (`NotificationRecord`) ve Repository (`NotificationHistoryRepository`)
- [x] Room veritabanı arşivi (`NotificationRecordEntity` & `NotificationRecordDao`)
- [x] Canlı bildirim yakalama ve arka plan kaydı (`AosNotificationListenerService` + Hilt EntryPoint)
- [x] Arama ve filtrelemeli bildirim günlüğü arayüzü (`NotificationHistorySection` & `NotificationHistoryDialog`)
- [x] Tek tıkla bildirim silme ve tümünü temizleme eylemleri
- [x] Gizli uygulama kasası DataStore altyapısı (`hidden_packages` & `vault_pin`)
- [x] Gizli uygulamaların App Drawer listesinden otomatik filtrelenmesi
- [x] 4 haneli PIN belirleme, doğrulama ve değiştirme sayısal tuş takımı (`NumericKeypad` & `HiddenVaultDialog`)
- [x] App Drawer üzerinden uzun basma ile "Uygulamayı Gizle (Kasa)" bağlam seçeneği
- [x] Kasa içinden uygulamaları doğrudan başlatma veya "Görünür Yap" ile çekmeceye geri döndürme
- [x] Birim testleri (`NotificationRecordEntityTest`)

### Faz 4 Çıktıları
- [x] Bulut senkronizasyonu ve AES-256 şifreli yedekleme aktif
- [x] Eklenti (Plugin) SDK mimarisi ve güvenlik sanal alanı hazır
- [x] Bildirim arşivi ve PIN korumalı gizli uygulama kasası çalışıyor

---

## FAZ 5 — Stabilite & Launch (4 hafta)

### Sprint 36-37: Test, Bellek Yönetimi & Hata Teşhisi
- [x] Bounded LruCache bellek optimizasyonu (Max 150 ikon önbelleği, OOM önleme)
- [x] Yerel hata raporlama motoru (`AosCrashReporter` UncaughtExceptionHandler)
- [x] Sistem hata teşhis arayüzü ve yığın izi görüntüleyici (`AboutAndDiagnosticsSection`)
- [x] R8 / Proguard optimizasyon kuralları (`app/proguard-rules.pro`)
- [x] Çökme modeli serileştirme birim testleri (`CrashReportTest`)
- [ ] Macrobenchmark: cold start ölçümü
- [ ] Macrobenchmark: frame timing ölçümü

### Sprint 38-39: Launch Hazırlığı & Karşılama
- [x] Onboarding akışı: 4 adımlı ilk açılış sihirbazı (`OnboardingScreen`)
- [x] Varsayılan başlatıcı rolü atama rehberi (`RoleManager` / `ACTION_HOME_SETTINGS`)
- [x] İsteğe bağlı izinler yapılandırma kartları (Bildirim & Kullanım İstatistikleri)
- [x] İlk tema ve ızgara tercihi kurulumu
- [x] Uygulama sürümü, açık kaynak lisansları ve %100 yerel gizlilik garantisi kartı
- [ ] Play Store varlıkları (screenshots, video, description)
- [ ] Gizlilik politikası sayfası
- [ ] Production release: Play Store

### Faz 5 Çıktıları
- [x] Onboarding sihirbazı ve ilk açılış deneyimi hazır
- [x] Çevrimdışı çökme teşhisi ve bellek yönetimi aktif
- [x] R8/Proguard kuralları ve optimizasyonlar tanımlı
