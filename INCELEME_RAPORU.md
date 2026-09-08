# AOS Launcher — Kod inceleme raporu

Tarih: 8 Eylül 2026

**Sonuç: 28 bulgu; 10 yüksek öncelikli (P1), 18 orta öncelikli (P2).** İlk 16 bulguya tasarım ve UI incelemesinden 12 yeni bulgu eklendi; ayrıca 4 cihaz testi gerektiren tasarım riski listelendi. .agent denetimindeki 12 dokümantasyon/süreç bulgusu ayrı D01–D12 bölümünde yer alıyor. Bulgular mevcut kaynak kod ve çağrı zincirleri üzerinden doğrulandı. Cihaz üzerinde yeniden üretim yapılmadı; aşağıdaki adımlar doğrulama senaryolarıdır.

Kapsam: app, core/common, core/domain, core/data, core/ui, feature/home, feature/appdrawer, feature/settings; Gradle, manifest, CI, veri saklama, yedekleme, uygulama açma, profiller, gizli kasa, widget, tema, öneriler ve mevcut testlerin incelemesi. Bu rapor tüm olası hataların bulunduğu veya her ekranın cihazda test edildiği anlamına gelmez.

## Yüksek öncelikli bulgular

### 1. [P1] Android 8–9'da kullanım izni kontrolü çökebilir

Kaynak: [core/data/src/main/java/com/aos/core/data/ai/UsageStatsHelper.kt:20](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/ai/UsageStatsHelper.kt:20>)

minSdk 26 olmasına rağmen API 29'da eklenen unsafeCheckOpNoThrow sürüm kontrolü olmadan çağrılıyor. Android API 26–28'de bu yol NoSuchMethodError üretir. AI öneri motoru ve ayarlardaki izin sorgusu buraya ulaşır; getRecentUsageStats içindeki try bloğu da bu çağrıdan sonra başlar.

Doğrulama: API 28 cihazda kurulumu tamamlayıp önerileri etkinleştirin veya arama/AI ayarlarını açın. Çözüm: API 29 altında checkOpNoThrow kullanın ve bu sürümler için test ekleyin. API bilgisi [Android resmi API 29 değişiklik kaydı](https://developer.android.com/sdk/api_diff/29/changes/android.app.AppOpsManager) ile doğrulandı.

### 2. [P1] Release imzalama anahtarı ve erişim bilgileri depoda

Kaynak: [app/build.gradle.kts:25](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/app/build.gradle.kts:25>), [app/build.gradle.kts:38](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/app/build.gradle.kts:38>)

keystore/aos_launcher.keystore dosyasının Git tarafından izlendiği doğrulandı. Release ve debug aynı anahtarı kullanıyor; erişim bilgileri Gradle dosyasında bulunuyor. Depoya erişebilen biri aynı sertifikayla APK imzalayabilir; dolayısıyla bu anahtar yayın kimliğini güvenilir biçimde korumuyor.

Çözüm: Release anahtarını güvenli yayın ortamında yönetin; debug anahtarını ayırın. Mevcut kurulumların güncelleme uyumluluğunu gözeterek anahtar geçişini planlayın. Dosyayı yalnızca son commit'ten kaldırmak geçmiş kopyalarını ortadan kaldırmaz.

### 3. [P1] Gizli uygulamalar öneri rafında PIN olmadan gösterilip açılabiliyor

Kaynak: [feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerViewModel.kt:109](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerViewModel.kt:109>), [feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerScreen.kt:219](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerScreen.kt:219>)

Gizli paket filtresi ana uygulama listesine uygulanıyor; suggestedApps ise tüm kurulu uygulamalardan hesaplanıp doğrudan ekrana veriliyor. Öneriye tıklama yolu kasa PIN kontrolü içermiyor.

Doğrulama: Önerilerde bulunan bir uygulamayı gizleyin; öneri rafından tekrar açın. Çözüm: Önerileri gizli paket ve profil akışlarıyla birlikte filtreleyin; gizleme değişince rafı anında güncelleyin.

### 4. [P1] Çocuk modundan çıkış PIN'i ayarlar ve asistan üzerinden atlanabiliyor

Kaynak: [feature/settings/src/main/java/com/aos/feature/settings/components/ProfileManagementSection.kt:81](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/settings/src/main/java/com/aos/feature/settings/components/ProfileManagementSection.kt:81>), [core/data/src/main/java/com/aos/core/data/repository/ProfileRepositoryImpl.kt:99](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/repository/ProfileRepositoryImpl.kt:99>), [app/src/main/java/com/aos/launcher/LauncherActivity.kt:292](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/app/src/main/java/com/aos/launcher/LauncherActivity.kt:292>)

PIN kontrolü ProfileSwitcherBar bileşeninde bulunuyor. Ayarlarda profil seçimi ve asistanın SwitchProfile sonucu doğrudan repository üzerinden profil değiştiriyor.

Doğrulama: Çocuk modundayken Ayarlar → Profiller üzerinden Standart profili seçin. Çözüm: Çocuk modundan çıkış yetkilendirmesini ortak işlem katmanında zorunlu kılın; tüm giriş noktaları aynı denetimi kullanmalı.

### 5. [P1] Asistan, aktif profilin engellediği uygulamayı açabiliyor

Kaynak: [app/src/main/java/com/aos/launcher/LauncherActivity.kt:286](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/app/src/main/java/com/aos/launcher/LauncherActivity.kt:286>)

OpenApp sonucu doğrudan launchApplication çağırıyor. Uygulama çekmecesinde ve ana ekranın normal tıklama yolunda kullanılan isAppBlockedByActiveProfile kontrolü bu yolda yok.

Doğrulama: Bir uygulamayı profil üzerinden engelleyin, ardından asistandan o uygulamayı açmasını isteyin. Çözüm: İzin kontrolünü merkezi uygulama başlatma işlevine taşıyın.

### 6. [P1] İlk ana sayfa veritabanına yazılmıyor; yeni sayfa ve yedek kapsamı bozuluyor

Kaynak: [core/data/src/main/java/com/aos/core/data/repository/LauncherRepositoryImpl.kt:33](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/repository/LauncherRepositoryImpl.kt:33>), [feature/home/src/main/java/com/aos/feature/home/HomeViewModel.kt:190](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/HomeViewModel.kt:190>), [core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:58](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:58>)

Boş veritabanında sayfa 0 yalnızca sanal bir liste elemanı olarak döndürülüyor. İlk yeni sayfa index 1 ile kaydedilince veritabanında yalnızca bu kayıt kalıyor; sayfa sayısı hâlâ 1 oluyor. Ekran sayfa konumundan homeIndex türetiyor, kayıtların gerçek pageIndex değerlerini kullanmıyor. Sonraki ekleme tekrar index 1 üretebiliyor. Yedekleme ise doğrudan kayıtlı sayfaları dolaştığından sayfa 0'daki öğeler yedeğe girmiyor; hiç sayfa kaydı yokken de boş liste için fallback çalışmıyor.

Doğrulama: Temiz kurulumda sayfa 0'a uygulama koyun, yeni sayfa ekleyin ve yedek alın. Çözüm: İlk sayfayı atomik olarak kalıcılaştırın; pageIndex için benzersizlik ve tutarlılık sağlayın.

### 7. [P1] Widget yığınları geri yüklemede boş uygulama simgesine dönüşüyor

Kaynak: [core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:233](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:233>), [core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:393](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:393>), [core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:422](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:422>)

Dışa aktarma WIDGET_STACK türünü yazıyor. deserializePayload içinde bu türü karşılayan dal yok; else dalı boş paket ve etiketle AppItem oluşturuyor. Yığının widget listesi kayboluyor. Ayrıca dışa yazılan popupWidgetId normal uygulama dalında geri okunmuyor.

Doğrulama: İki widget'ı yığın yapın, şifreli yedek alıp geri yükleyin. Çözüm: Tüm LauncherItem türleri için simetrik serileştirme kullanın ve round-trip testleri ekleyin.

### 8. [P1] Bulut eşitlemesi veri göndermeden başarı gösteriyor

Kaynak: [core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:165](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:165>), [feature/settings/src/main/java/com/aos/feature/settings/components/BackupSyncSection.kt:130](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/settings/src/main/java/com/aos/feature/settings/components/BackupSyncSection.kt:130>)

syncWithCloud yalnızca 1200 ms bekleyip Success döndürüyor. Ağ isteği, şifreli yedek üretimi veya uzak depolama yok. Arayüz buna rağmen son senkronizasyon zamanını gösteriyor; kullanıcı verisinin bulutta korunduğunu düşünebilir.

Doğrulama: Ağ kapalıyken Eşitle'ye basın. Çözüm: Gerçek hizmet tamamlanana kadar başarı bildirmeyin ve özelliği kullanılamaz olarak gösterin.

## Orta öncelikli bulgular

### 9. [P2] Geri yükleme atomik değil; hata mevcut düzeni silebilir

Kaynak: [core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:110](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:110>)

Önce mevcut öğeler ve sayfalar siliniyor, sonra ayrı DAO çağrılarıyla yeni veriler ekleniyor. Room transaction kullanılmıyor. Aradaki veritabanı hatası veya süreç sonlanması eski düzeni kaybettirip kısmi veri bırakabilir. Profile kayıtları da eski liste temizlenmeden ekleniyor; yedek dışındaki profiller kalabilir.

Doğrulama: Silme sonrası ekleme aşamasında kontrollü hata üretin. Çözüm: Ön doğrulama ve Room transaction kullanın; DataStore ile veritabanı arasındaki kısmi başarısızlık için kurtarma stratejisi belirleyin.

### 10. [P2] Yedek tüm kullanıcı ayarlarını taşımıyor

Kaynak: [core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:291](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:291>), [core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:515](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/repository/BackupRepositoryImpl.kt:515>)

Yalnızca sınırlı tercih/tema alanları yazılıp okunuyor. Örneğin enableMathCalculator, enableContactsSearch ve enableNewsFeed yedekte yok; geri okuma model varsayılanlarını getiriyor ve updateThemeConfig bunları mevcut ayarların üzerine yazıyor. Gizli paket listesi ve kasa PIN'i de özel yedek akışına dahil değil.

Doğrulama: Haber ve hesap makinesini kapatıp yedek alın, ayarları değiştirip geri yükleyin. Çözüm: Sürüm kontrollü, tam tercih modeli ve eski sürümler için açık geçiş kuralları kullanın.

### 11. [P2] Uygulama yerleştirme dolu hücreye yazabiliyor

Kaynak: [feature/home/src/main/java/com/aos/feature/home/HomeViewModel.kt:168](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/HomeViewModel.kt:168>), [feature/home/src/main/java/com/aos/feature/home/components/GridCellLayout.kt:654](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/components/GridCellLayout.kt:654>)

Çekmeceden yerleştirme hedef koordinatı doğrudan placeAppAt'e geçiriyor. Bu işlev doluluk kontrolü yapmadan yeni kayıt ekliyor; aynı hücrede iki öğe oluşabiliyor. Otomatik boş hücre araması da widget'ın kapladığı alan yerine yalnızca başlangıç hücresini kontrol ediyor.

Doğrulama: Yeni uygulamayı mevcut ikon veya 2×2 widget üzerine bırakın. Çözüm: Merkezi spanX/spanY tabanlı çakışma kontrolü ve tutarlı yer değiştirme/klasör davranışı uygulayın.

### 12. [P2] Widget kaldırmak Android host kaydını serbest bırakmıyor

Kaynak: [feature/home/src/main/java/com/aos/feature/home/HomeViewModel.kt:412](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/HomeViewModel.kt:412>), [feature/home/src/main/java/com/aos/feature/home/HomeViewModel.kt:400](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/HomeViewModel.kt:400>), [app/src/main/java/com/aos/launcher/LauncherActivity.kt:77](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/app/src/main/java/com/aos/launcher/LauncherActivity.kt:77>)

Widget, yığın ve popup kaldırma yolları yalnızca Room kayıtlarını değiştiriyor. deleteAppWidgetId yalnızca seçim/yapılandırma iptali yollarında çağrılıyor. Silinen widget host kimlikleri sağlayıcıya bağlı kalabilir ve gereksiz güncellemeler sürer.

Doğrulama: Widget ekleyip silin; host'a bağlı kimliklerin listesiyle karşılaştırın. Çözüm: Silinen widget kimliklerini, yığın alt öğeleri ve popup'lar dahil, host üzerinden temizleyin.

### 13. [P2] İkon paketi seçimi ekrandaki ikonlara uygulanmıyor

Kaynak: [core/ui/src/main/java/com/aos/core/ui/components/AppIconDrawable.kt:104](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/ui/src/main/java/com/aos/core/ui/components/AppIconDrawable.kt:104>), [core/data/src/main/java/com/aos/core/data/iconpack/IconPackManager.kt:49](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/iconpack/IconPackManager.kt:49>)

İkon çizimi her zaman PackageManager.getApplicationIcon kullanıyor. Seçili paket tercihi yükleyiciye ulaşmıyor; IconPackManager.loadIcon çağrılmıyor. AosAppIcon'un iconUri parametresi de çizimde kullanılmıyor.

Doğrulama: Kurulu bir ikon paketini ayarlardan seçin; ana ekran ve çekmece ikonlarını karşılaştırın. Çözüm: Paket tercihini ikon yükleme akışına ve cache anahtarına dahil edin.

### 14. [P2] Otomatik profil zamanlaması uygulanmamış

Kaynak: [feature/settings/src/main/java/com/aos/feature/settings/components/ProfileManagementSection.kt:232](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/settings/src/main/java/com/aos/feature/settings/components/ProfileManagementSection.kt:232>)

Ekran belirlenen saatlerde otomatik geçiş vaat ediyor; isScheduleEnabled yalnızca kaydedilip okunuyor. Kaynak taramasında saatleri değerlendirip profil değiştiren zamanlayıcı veya servis bulunmadı.

Doğrulama: Zamanlamayı etkinleştirip profilin başlangıç saatinde geçişi izleyin. Çözüm: Zamanlama yürütücüsü, yeniden başlatma ve saat değişikliği yönetimi ekleyin veya tamamlanana kadar seçeneği kaldırın.

### 15. [P2] Eklenti açık/kapalı tercihleri yeniden başlatmada kayboluyor

Kaynak: [core/data/src/main/java/com/aos/core/data/repository/PluginRepositoryImpl.kt:25](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/repository/PluginRepositoryImpl.kt:25>)

Tüm eklentiler her repository oluşturulmasında enabled=true olarak başlatılıyor. togglePlugin yalnızca bellekteki StateFlow'u değiştiriyor; kalıcı kayıt yok.

Doğrulama: Eklentiyi kapatın, uygulama sürecini tamamen sonlandırıp tekrar açın. Çözüm: Durumları DataStore'da saklayıp başlangıçta yükleyin. Ayrıca aktif eklenti sorgularının çalışma akışlarına bağlandığı bir çağrı noktası bulunmadı; özellik entegrasyonu ayrı kabul testi gerektiriyor.

### 16. [P2] CI test hatalarını başarılı kabul ediyor

Kaynak: [.github/workflows/android.yml:37](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.github/workflows/android.yml:37>)

Birim test komutu sonundaki “|| true” test başarısızlığını sıfır çıkış koduna dönüştürüyor. Böylece bozuk testlere rağmen APK yayımlama adımı devam edebiliyor.

Doğrulama: Bir testi kasıtlı başarısız yapıp iş akışını çalıştırın. Çözüm: Hata yutmayı kaldırın; yayın adımını başarılı test ve lint sonuçlarına bağlayın.

## Doğrulama durumu ve sınırlar

- Başlangıçta Git çalışma ağacı temizdi. Uygulama kaynak kodu değiştirilmedi.
- Mevcut test envanteri: 5 dosya, 13 @Test; matematik, kripto, bildirim entity dönüşümü, crash raporu ve eklenti izin kontrolü.
- İncelenen testlerde launcher sayfaları, yedek round-trip, profil yetkilendirmesi veya widget yaşam döngüsünü kapsayan test bulunmadı.
- Denenen komut: gradlew.bat testDebugUnitTest lintDebug --offline --console=plain.
- Gradle daemon başlatma aşamasında “java.io.IOException: Unable to establish loopback connection” ile durdu. Testler/lint tamamlanmadı; derlemenin başarılı veya başarısız olduğuna ilişkin proje düzeyinde sonuç çıkarılmadı.
- Emülatör/fiziksel cihaz testleri, performans ölçümü, canlı bulut/güncelleme indirme denemesi ve bağımlılık güvenlik taraması yapılmadı.
- Öncelik: önce API uyumluluğu ve yetkilendirme, ardından sayfa/yedek veri bütünlüğü ve yayın güvenliği; sonra eksik ayar bağlantıları ve yaşam döngüsü sorunları.

## Tasarım ve UI incelemesi — ek bulgular

Bu bölüm 12 yeni bulgu içerir: 2 P1 ve 10 P2. Önceki 16 bulguyla toplam 28 bulguya ulaşılmıştır. Bulgular Compose yerleşimi, durum yönetimi ve olay bağlantıları üzerinden incelendi. Görsel ekran görüntüsü, çalışan emülatör, TalkBack veya gerçek dokunma testi kullanılmadı. Aşağıdaki doğrulama adımları henüz cihazda çalıştırılmamıştır. Renk beğenisi veya stil tercihi hata olarak sayılmadı.

### 17. [P2] Açık temada ayarlar menüsünün yazıları ve okları zeminde kayboluyor

Kaynak: [feature/settings/src/main/java/com/aos/feature/settings/SettingsScreen.kt:354](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/settings/src/main/java/com/aos/feature/settings/SettingsScreen.kt:354>), [feature/settings/src/main/java/com/aos/feature/settings/SettingsScreen.kt:391](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/settings/src/main/java/com/aos/feature/settings/SettingsScreen.kt:391>), [core/ui/src/main/java/com/aos/core/ui/theme/Theme.kt:34](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/ui/src/main/java/com/aos/core/ui/theme/Theme.kt:34>)

Scaffold tema yüzeyini kullanırken ayar kartları beyazın %5 opaklığıyla, başlıklar tamamen beyaz, açıklamalar LightGray ve yön okları %40 beyazla çiziliyor. Light seçildiğinde zemin açık renge dönüyor fakat bu renkler değişmiyor; menünün okunabilirliği ciddi biçimde azalıyor.

Doğrulama: Dinamik renkleri kapatın, Açık temayı seçin ve ayarlar ana menüsüne dönün. Çözüm: surface/onSurface/onSurfaceVariant gibi eşleşen tema renklerini kullanın; sabit koyu zeminli bileşenleri ayrı ve tutarlı ele alın.

### 18. [P2] Arama sırasında görünmeyen kategori filtresi sonuçları eksiltiyor

Kaynak: [feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerScreen.kt:75](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerScreen.kt:75>), [feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerScreen.kt:104](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerScreen.kt:104>)

displayedApps her zaman selectedCategory ile filtreleniyor. Yazı girildiğinde kategori çubuğu ve kategori başlığı gizleniyor, ancak seçili kategori sıfırlanmıyor. Arama alanı “Uygulama ara...” derken kullanıcı yalnızca önceki kategoriyi arıyor; filtrenin etkin olduğuna dair gösterge de kalmıyor.

Doğrulama: Oyunlar kategorisini seçin, ardından başka kategorideki kurulu bir uygulamanın adını yazın. Çözüm: Aramada tüm kategorileri kapsayın veya etkin filtreyi görünür ve kaldırılabilir tutun.

### 19. [P2] Kaydırma ve iki parmak küçültme jestleri yavaş hareketlerde algılanmıyor

Kaynak: [feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:198](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:198>), [feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:211](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:211>)

Dikey kaydırma her olayın dragAmount değerini ±45 piksel ile karşılaştırıyor; hareketin toplam mesafesi biriktirilmiyor. Çok sayıda küçük hareketten oluşan uzun bir kaydırma bu eşiğe hiç ulaşmayabilir. Pinch için de tek olayın zoom değerinin 0.85 altına düşmesi bekleniyor; kademeli küçültme birikmiyor. Eşiklerin piksel cinsinden olması yoğunluklar arasında davranışı da değiştiriyor.

Doğrulama: Boş alanda yavaşça uzun bir yukarı kaydırma ve kademeli pinch yapın, hızlı hareketlerle karşılaştırın. Çözüm: Jest boyunca toplam mesafe/ölçek tutun, uygun hareket eşiği uygulayın ve eylemi her jestte bir kez tetikleyin.

### 20. [P1] Geometrik düzene geçince uygulamalar ve widget'lar görünmez oluyor

Kaynak: [feature/home/src/main/java/com/aos/feature/home/components/FlowerLayout.kt:57](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/components/FlowerLayout.kt:57>), [feature/home/src/main/java/com/aos/feature/home/components/FlowerLayout.kt:105](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/components/FlowerLayout.kt:105>), [feature/home/src/main/java/com/aos/feature/home/components/GeometricalLayouts.kt:59](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/components/GeometricalLayouts.kt:59>), [feature/home/src/main/java/com/aos/feature/home/components/GeometricalLayouts.kt:141](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/components/GeometricalLayouts.kt:141>), [feature/home/src/main/java/com/aos/feature/home/components/GeometricalLayouts.kt:234](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/components/GeometricalLayouts.kt:234>)

Flower yalnızca merkez + 6 çevre öğesi, Arch 7 ve Honeycomb 10 öğe çiziyor. Fazla öğeler için devam sayfası veya taşma davranışı bulunmuyor. Flower kodu 8 çevre öğesi seçmesine rağmen bunların yalnızca 6'sını çiziyor. Geometrik slotlar yalnızca AppItem ve FolderItem türlerini destekliyor; widget/yığın türleri boş bırakılıyor.

Doğrulama: İlk sayfaya 11 uygulama ve bir widget yerleştirip her geometrik düzene geçin. Etki: Veriler silinmiyor fakat ana ekrandan erişilemez hale geliyor. Çözüm: Kapasite aşımını başka sayfalara taşıyın veya devam görünümü sunun; desteklenmeyen öğeleri sessizce gizlemeyin.

### 21. [P2] Geometrik düzende “uygulama ekle” akışı yerleştirme aşamasında kalıyor

Kaynak: [feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:430](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:430>), [feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:440](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:440>), [feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:477](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:477>)

Geometrik düzenin boş slotu çekmeceyi açıyor. Çekmecede uzun basış pendingPlacedApp oluşturup ana ekrana dönüyor. Ancak bu durumu işleyip koordinat seçtiren arayüz yalnızca GridCellLayout'a bağlı; Flower/Honeycomb/Arch'a aktarılmıyor. Kullanıcı seçtiği uygulamayı bu sayfaya yerleştiremiyor.

Doğrulama: Flower görünümünde boş “+” slotuna dokunun, çekmecede bir uygulamaya uzun basın. Çözüm: Geometrik slot seçimini ekleme akışına bağlayın veya yerleştirme sırasında açıkça geçici grid sunun.

### 22. [P1] Açık klasör güncel veriyi izlemiyor; kaldırılan uygulamalar geri gelebiliyor

Kaynak: [feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:583](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:583>), [feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:591](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:591>), [feature/home/src/main/java/com/aos/feature/home/HomeViewModel.kt:288](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/HomeViewModel.kt:288>)

activeFolder tıklama anındaki nesneyi saklıyor. Veritabanı güncellense de açık diyalog bu eski nesneyi kullanıyor. Dört öğeli klasörden A'yı kaldırınca ekran eski listeyi göstermeye devam edebilir; ardından B kaldırılırsa işlem yine orijinal dört öğe üzerinden hesaplanır ve A tekrar yazılır. Ad değiştirme de eski folder.items listesini geri kaydedebilir.

Doğrulama: Dört uygulamalı klasörü açın; diyaloğu kapatmadan iki farklı uygulamayı sırayla çıkarın veya ilk çıkarma sonrası adını değiştirin. Çözüm: Diyalogda yalnızca klasör ID'sini saklayın, içeriği canlı durumdan bulun; kaldırma işlemini repository'nin güncel içeriği üzerinden yapın.

### 23. [P2] Kasanın tarif ettiği uzun basış menüsüne ulaşılamıyor

Kaynak: [feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerScreen.kt:251](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerScreen.kt:251>), [feature/appdrawer/src/main/java/com/aos/feature/appdrawer/components/HiddenVaultDialog.kt:307](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/appdrawer/src/main/java/com/aos/feature/appdrawer/components/HiddenVaultDialog.kt:307>)

Boş kasa ekranı uygulamaya uzun basıp “Uygulamayı Gizle (Kasa)” seçmeyi söylüyor. Gerçekte çekmecedeki uzun basış doğrudan ana ekrana yerleştirmeyi başlatıyor. AppDrawerContextMenu tanımlı fakat çağrılmıyor; ViewModel.hideApp için de UI çağrısı bulunmuyor. Çekmecenin uygulama bilgisi/kaldırma callback'leri de kullanılmıyor.

Doğrulama: Boş kasadaki yönergeyi uygulayın. Çözüm: Yerleştirme ve bağlam menüsü için ayrı, keşfedilebilir etkileşimler sağlayın; yardım metnini gerçek davranışla eşleştirin.

### 24. [P2] “Grupla” düğmesi işlem yapmadan başarı mesajı veriyor

Kaynak: [feature/home/src/main/java/com/aos/feature/home/components/OxygenEditModeBars.kt:49](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/components/OxygenEditModeBars.kt:49>), [feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:237](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/HomeScreen.kt:237>)

Düğmenin adı “Grupla”, callback'in tek yaptığı “Simgeler otomatik hizalandı” Toast'ı göstermek. Öğeleri gruplama, taşıma veya hizalama işlemi yok. Etiket, mesaj ve gerçekleşen davranış birbirini tutmuyor.

Doğrulama: Düzensiz ikon yerleşiminde düzenleme modunu açıp Grupla'ya dokunun. Çözüm: Gerçek işlevi bağlayın; işlev yokken başarı mesajını ve etkin düğmeyi göstermeyin.

### 25. [P2] Seçilen ikon şekli çekmeceye uygulanmıyor

Kaynak: [feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerScreen.kt:245](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerScreen.kt:245>), [core/ui/src/main/java/com/aos/core/ui/components/AosAppIcon.kt:36](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/ui/src/main/java/com/aos/core/ui/components/AosAppIcon.kt:36>)

Ana ekran seçili iconShape değerini kullanıyor; çekmece AosAppIcon çağrısına shape vermiyor, bu nedenle hep varsayılan Squircle çiziliyor. Kullanıcı global tema ayarını değiştirince aynı uygulama iki ekranda farklı biçimde görünüyor.

Doğrulama: İkon şeklini Daire seçip ana ekran ve çekmeceyi karşılaştırın. Çözüm: Ortak tema ayarını çekmece ikonlarına da taşıyın; kapsama özel ayar isteniyorsa arayüzde bunu belirtin. Bu, önceki 13. bulgudaki ikon paketi yükleme sorunundan farklıdır.

### 26. [P2] Çekmecede yükleme hatası ve sonuç bulunamaması açıklanmıyor

Kaynak: [feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerViewModel.kt:102](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerViewModel.kt:102>), [feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerScreen.kt:228](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/appdrawer/src/main/java/com/aos/feature/appdrawer/AppDrawerScreen.kt:228>)

ViewModel errorMessage üretmesine rağmen ekran bunu kullanmıyor. Yükleme bitince doğrudan grid çiziliyor; boş arama sonucu için de mesaj veya filtre temizleme eylemi yok. Kullanıcı boş alanın hata mı, filtre sonucu mu olduğunu anlayamıyor.

Doğrulama: Eşleşmeyen bir ad arayın; ayrı testte uygulama listeleme repository'sinden hata döndürün. Çözüm: Yükleniyor, hata, boş liste ve sonuç yok durumlarını ayrı gösterin; uygun yeniden deneme/arama temizleme eylemleri ekleyin.

### 27. [P2] Etiketler kapalıyken geometrik ikonların erişilebilir adı kayboluyor

Kaynak: [core/ui/src/main/java/com/aos/core/ui/components/AosAppIcon.kt:43](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/ui/src/main/java/com/aos/core/ui/components/AosAppIcon.kt:43>), [core/ui/src/main/java/com/aos/core/ui/components/AosAppIcon.kt:93](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/ui/src/main/java/com/aos/core/ui/components/AosAppIcon.kt:93>), [feature/home/src/main/java/com/aos/feature/home/components/FlowerLayout.kt:156](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/components/FlowerLayout.kt:156>)

AosAppIcon'a verilen label yalnızca görünür Text için kullanılıyor. showLabel=false olduğunda metin kaldırılıyor; ikon görüntüsünün contentDescription değeri de null. Geometrik düzenler bu bileşeni ek bir erişilebilir ad olmadan kullanıyor. Böylece uygulama simgesi tıklanabilir olsa da TalkBack'in okuyacağı uygulama adı bulunmuyor; bildirim rozeti varsa yalnızca sayı kalabilir. GridCellLayout'taki ayrı semantik tanım bu bulgunun kapsamı dışındadır.

Doğrulama: Etiketleri kapatın, Flower görünümünde TalkBack odağını simgeler arasında taşıyın. Çözüm: Görünür etiketten bağımsız contentDescription ve uygun eylem semantiği sağlayın.

### 28. [P2] Izgara küçültme kenardaki öğeleri ekran dışına taşıyor

Kaynak: [feature/home/src/main/java/com/aos/feature/home/HomeViewModel.kt:184](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/HomeViewModel.kt:184>), [feature/home/src/main/java/com/aos/feature/home/components/GridCellLayout.kt:182](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/components/GridCellLayout.kt:182>)

Izgara değişimi yalnızca satır/sütun tercihini kaydediyor. Mevcut cellX/cellY/span değerleri yeniden yerleştirilmiyor. Örneğin 5 sütunlu düzende cellX=4 konumundaki öğe, 4 sütuna geçince x=ekran genişliği konumuna çiziliyor ve görünür alanın dışında kalıyor. Widget'ların sınırları da yeniden doğrulanmıyor.

Doğrulama: 5×6 düzenin son satır/sütununa öğe koyun, 4×4'e geçin. Çözüm: Boyut değişmeden önce yerleşimi hesaplayın, sığmayan öğeleri yeni sayfalara taşıyın ve sonucu atomik kaydedin.

## Ekran üzerinde ayrıca doğrulanması gereken tasarım riskleri

Aşağıdakiler 28 kesin bulgu sayısına dahil edilmedi; ölçü kısıtları ve eksik inset/scroll yönetimi nedeniyle hedef cihazda kontrol edilmelidir.

| Alan | Koddan görülen risk | Önerilen doğrulama |
|---|---|---|
| Dar ekran çekmecesi | 58 dp kategori şeridi, dört sabit sütun, alfabetik şerit; arama alanında üç ayrı ikon düğmesi var. Metin ve ikonlara kalan alan daralıyor. | 320 ve 360 dp genişlikte uzun uygulama isimleriyle kontrol edin. |
| Alttaki arama ve klavye | Çekmecede imePadding yok; Activity adjustPan kullanıyor. Üst içerik ekran dışına kayabilir; arama sonuçları ile alanın birlikte erişilebilirliği belirsiz. | Alt aramayı etkinleştirip klavyeyi açın; son sonuçlara ve kapatma eylemine erişimi kontrol edin. |
| Yatay ekran/büyük yazı | Kategori ve alfabetik sütunlar kaydırılmıyor. Yedi kategori, çok sayıda harf ve üstte kişiler/öneriler sınırlı yüksekliği paylaşıyor. | Yatay yön, bölünmüş ekran ve %200 yazı ölçeğinde son kategori/harfe erişimi kontrol edin. |
| Geometrik ölçüler | Arch 160 dp sabit yarıçap ve 68 dp slot kullanıyor; uçları yaklaşık 345 dp toplam genişlik istiyor. BoxWithConstraints mevcut olsa da yarıçap maxWidth'e uyarlanmıyor. | 320 dp içerik genişliğinde kenar ikonları, etiket çakışmaları ve dokunma bölgelerini kontrol edin. |

Önerilen kabul matrisi: açık/koyu tema, 320/360/600 dp genişlik, dikey/yatay yön, %100/%200 yazı ölçeği, klavye açık/kapalı, TalkBack açık/kapalı. Önce 20 ve 22 numaralı erişim/veri tutarlılığı sorunları, sonra arama ve yerleştirme yolları, ardından tema ve erişilebilirlik düzeltilmeli.

## .agent klasörü denetimi — dokümantasyon ve süreç eksikleri

İncelenen 10 dosya: Agent.md, Architecture.md, Project.md, Phase.md, Roadmap.md, todo.md, skills.md, lessons.md, changelog.md ve Smart_Launcher_Kapsamli_Rapor.md.

**12 dokümantasyon/süreç bulgusu tespit edildi.** Bunlar önceki 28 kod/UI bulgusundan ayrı sayılmıştır; aynı uygulama hataları tekrar toplam sayıya eklenmemiştir. .agent dosyalarının içeriği ve tamamlandı işaretleri bu denetimde değiştirilmedi.

### D01 — Tamamlanma durumları kendi içinde ve belgeler arasında çelişiyor

Kanıt: [.agent/todo.md:58](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/todo.md:58>), [.agent/todo.md:84](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/todo.md:84>), [.agent/Phase.md:27](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Phase.md:27>), [.agent/todo.md:36](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/todo.md:36>), [.agent/Roadmap.md:31](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Roadmap.md:31>).

Swipe-up aynı todo dosyasında hem tamamlandı hem bekliyor. Phase.md build/lint/test CI'ını tamamlandı gösterirken todo.md lint işini açık tutuyor; workflow'da lint adımı yok. Roadmap'te 30+ animasyon ve konum/zaman tabanlı profil geçişi tamamlandı; Phase'te 30+ preset ve Geofence açık. Faz başlıkları tamamlandı olmasına rağmen altında bitmemiş işler var; bunların ertelendiği veya faz kapsamından çıkarıldığı açıklanmıyor.

Eksik: Tek esas görev listesi, kararlı görev ID'leri ve açık “planlandı / kısmi / uygulandı / doğrulandı / ertelendi” durumları. Öncelik: yüksek; mevcut ilerleme yüzdeleri güvenilir değil.

### D02 — Veri güvenliği garantileri uygulamayı olduğundan ileri gösteriyor

Kanıt: [.agent/Phase.md:194](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Phase.md:194>), [.agent/Phase.md:227](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Phase.md:227>), [.agent/changelog.md:155](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/changelog.md:155>), [.agent/todo.md:167](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/todo.md:167>).

“Kayıpsız serileştirme”, “çakışma çözümü & atomik geri yükleme” ve “bulut senkronizasyonu aktif” ifadeleri işaretli. Oysa önceki 6–10. bulgular sayfa/ayar/widget kayıplarını, transaction eksikliğini ve yalnızca gecikme üreten sync yolunu gösteriyor. todo.md simülasyon olduğunu açıkça söylemesi bakımından daha doğru; diğer belgeler bu sınırı taşımıyor.

Eksik: Garanti başına kabul testi ve kanıt bağlantısı; gerçek bulut desteği ile yerel şifreleme/simülasyonun ayrı durumları. Öncelik: yüksek.

### D03 — Eklenti izin listesi gerçek süreç izolasyonu gibi anlatılıyor

Kanıt: [.agent/Architecture.md:206](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Architecture.md:206>), [.agent/Agent.md:66](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Agent.md:66>), [core/data/src/main/java/com/aos/core/data/plugin/PluginSecuritySandbox.kt:11](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/plugin/PluginSecuritySandbox.kt:11>).

Mimari ayrı process ve Binder IPC kullanan üçüncü parti eklenti sandbox'ı tarif ediyor. Mevcut kod yerleşik Kotlin nesnelerinin kendi manifestindeki requiredPermissions listesini kontrol ediyor; bu, süreç izolasyonu veya dış eklenti çalıştırma sınırı değil. “SDK hazır” ifadelerinde bu fark belirtilmiyor.

Eksik: Mevcut güven sınırının dürüst tarifi; dış eklenti yükleme, kimlik doğrulama, kullanıcı izin onayı, izolasyon, IPC ve API uyumluluğu için ayrı plan. Öncelik: yüksek; mevcut kontrollerin sağlamadığı güvence vaat edilmemeli.

### D04 — Mimari belgesi mevcut yapıyı değil, hedef yapıyı gösteriyor

Kanıt: [.agent/Architecture.md:5](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Architecture.md:5>), [.agent/Architecture.md:48](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Architecture.md:48>), [.agent/Architecture.md:67](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Architecture.md:67>), [.agent/Architecture.md:87](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Architecture.md:87>), [settings.gradle.kts:25](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/settings.gradle.kts:25>).

Belge core/feature dizinlerini app altında gösteriyor ve widget/search/dock/ai/privacy gibi bağımsız feature modülleri listeliyor. Gerçekte sekiz Gradle modülü var ve core/feature kök dizinde. Retrofit/OkHttp, Proto DataStore, JUnit 5, TFLite/MediaPipe gibi bileşenler mevcut yığın şeklinde anlatılmış. İncelenen kod Preferences DataStore, doğrudan HttpURLConnection, JUnit 4.13.2 ve yerel kural tabanlı NLU/öneri motoru kullanıyor.

Eksik: “Mevcut mimari” ve “hedef mimari” ayrımı; gerçek giriş dosyaları ve bağımlılık kataloğuna bağlantılar. Yokluğu bir eksiklik olan gelecek özellikleri, kurulmuş bağımlılıklar gibi göstermeyin.

### D05 — Kod sözleşmeleri için uygulanabilir örnek ve istisna kaydı yok

Kanıt: [.agent/Agent.md:28](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Agent.md:28>), [.agent/Architecture.md:238](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Architecture.md:238>), [core/domain/src/main/java/com/aos/core/domain/repository/ProfileRepository.kt:7](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/domain/src/main/java/com/aos/core/domain/repository/ProfileRepository.kt:7>), [feature/home/src/main/java/com/aos/feature/home/HomeUiState.kt:11](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/feature/home/src/main/java/com/aos/feature/home/HomeUiState.kt:11>).

Tüm repository'lerin Flow<Result<T>>, tüm UI durumlarının sealed class ve olayların Channel olması zorunlu yazılmış. Gerçek arayüzler Flow<List<Profile>>, Flow<Profile?>, suspend dönüşler gibi farklı sözleşmeler; UI durumu data class. Bunlar tek başına hata değildir, fakat talimatın mutlak hali mevcut proje örüntüsüyle çelişir ve sonraki değişikliklerde gereksiz yeniden yazıma yol açabilir.

Eksik: Sorgu/komut/hata/tek seferlik olay ayrımını açıklayan güncel sözleşme ve gerekçeli mimari karar kaydı.

### D06 — skills.md örnekleri mevcut kodla doğrudan kullanılamıyor

Kanıt: [.agent/skills.md:27](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/skills.md:27>), [.agent/skills.md:353](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/skills.md:353>), [.agent/skills.md:375](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/skills.md:375>), [.agent/skills.md:382](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/skills.md:382>).

AppInfo örneği olmayan icon parametresini kullanıyor ve zorunlu activityName'i vermiyor. Room örneği LauncherDatabase, page, position ve dao.insert isimlerini kullanıyor; gerçek karşılıkları AosLauncherDatabase, cellX/cellY/pageIndex ve insertItem. Compose test örneği AppDrawerScreen'in zorunlu callback'lerini sağlamıyor. Jest örneği de önceki UI bulgusu 19'daki tek olay zoom eşiğini öneriyor.

Eksik: Örnekleri “çalışan proje örneği” veya “sözde kod” olarak etiketleme; derlenen örneklere/testlere bağlantı ve bağımlılık sürümü. Güncel olmayan snippet'ları yeni kod için şablon olarak kullanmayın.

### D07 — Test ve kalite hedeflerinin çalıştırılabilir karşılığı eksik

Kanıt: [.agent/Agent.md:56](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Agent.md:56>), [.agent/Phase.md:144](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Phase.md:144>), [.github/workflows/android.yml:37](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.github/workflows/android.yml:37>).

Her repository için Room entegrasyon testi, kritik Compose testleri ve %80 kapsam hedefi var. Depoda bulunan mevcut testler beş dosya/13 testle sınırlı; ilgili UI, repository entegrasyon ve benchmark testleri bulunmadı. Coverage ölçümü/eşiği tanımlı değil; CI test hatalarını yutuyor. “production-ready” bir hedef/iddia olarak kalıyor, doğrulama sonucu eklenmemiş.

Eksik: Çalıştırılacak komutlar, cihaz/API matrisi, test raporu bağlantıları, başarılı/başarısız son koşu kaydı ve tamamlanma ölçütleri. Bu denetimde testler yeniden çalıştırılmadı; önceki Gradle ortam engeli geçerli sınır olarak korunuyor.

### D08 — Sürüm ve yayın planı ortak bir referansa bağlı değil

Kanıt: [.agent/changelog.md:196](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/changelog.md:196>), [.agent/changelog.md:214](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/changelog.md:214>), [.agent/Roadmap.md:10](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Roadmap.md:10>), [app/build.gradle.kts:18](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/app/build.gradle.kts:18>).

Kod sürümü 1.5.0, changelog'un son tarihli sürümü 1.4.0. Roadmap ilk Play Store yayınını v1.0/2027 Q2, changelog gelecek yayınını v2.0.0 olarak adlandırıyor. Bu farkın geliştirme sürümü/mağaza sürümü ayrımı olduğu açıklanmıyor. Güncel geometrik düzenler, kategori şeridi ve güncelleme akışı için 1.5.0 değişiklik kaydı eksik.

Eksik: Tek sürüm kaynağı, Unreleased bölümü, geliştirme/mağaza sürümleri arasındaki eşleme ve her sürüm için değişiklik/test/migrasyon notları. Geçmiş kayıtların tarihsel niteliğini koruyarak yeni düzeltme kayıtları ekleyin.

### D09 — Bazı tamamlandı işaretleri yalnızca UI veya veri modeli varlığına dayanıyor

Kanıt: [.agent/Phase.md:248](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Phase.md:248>), [.agent/todo.md:62](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/todo.md:62>), [.agent/todo.md:199](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/todo.md:199>), [app/src/main/java/com/aos/launcher/ui/onboarding/OnboardingScreen.kt:379](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/app/src/main/java/com/aos/launcher/ui/onboarding/OnboardingScreen.kt:379>).

“İlk tema ve ızgara tercihi kurulumu” tamamlandı; ancak onboarding tema anahtarları yalnızca remember durumunu değiştiriyor, kaydetme callback'i yok ve bu sayfada ızgara seçimi bulunmuyor. “Fuzzy arama” gerçekte contains ile alt dize araması. Güncel plan 150 ikon cache'i diyor; kodda sınır 400. Bu örnekler arayüzün bulunmasıyla özelliğin uçtan uca çalışmasının karıştırıldığını gösteriyor.

Eksik: Her tamamlandı maddesi için kullanıcı senaryosu ve gözlenen sonuç. Onboarding tercihlerinin kaybolması bu inceleme sırasında saptanan ek kod sorunudur; burada tamamlanma iddiasının kanıtı olarak kaydedildi, önceki 28'lik kod/UI sayısına ayrıca eklenmedi.

### D10 — Tasarım ve erişilebilirlik kabul ölçütleri eksik

Kanıt: [.agent/Phase.md:33](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Phase.md:33>), [.agent/Smart_Launcher_Kapsamli_Rapor.md:1](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Smart_Launcher_Kapsamli_Rapor.md:1>).

Rakip tasarım anlatımı var, fakat AOS için uygulanabilir renk rolleri, yazı ölçeği, minimum içerik alanı, yerleşim kapasitesi/taşma davranışı, klavye insets, TalkBack etiketleri ve boş/hata/yükleme durumlarını tanımlayan bir UI sözleşmesi bulunmadı. Figma bağlantısı da açık görev.

Eksik: Ayrı tasarım aracı zorunlu olmadan, mevcut Compose tema/bileşenlerini esas alan kısa bir tasarım sistemi ve önceki UI bölümündeki cihaz matrisi. 17–28. bulguları bu kabul ölçütlerine bağlayın.

### D11 — Rakip analizinin kaynak ve doğrulama izi bulunmuyor

Kanıt: [.agent/Smart_Launcher_Kapsamli_Rapor.md:1](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Smart_Launcher_Kapsamli_Rapor.md:1>).

Raporda Smart Launcher özellikleri, Pro sınırları ve sürüm/platform ayrımları ayrıntılı anlatılıyor; kaynak URL, erişim tarihi, test edilen uygulama sürümü veya ekran kanıtı bulunmuyor. Bu denetim rakip hakkındaki iddiaların yanlış olduğunu ileri sürmez; doğrulanabilir olmadıklarını tespit eder.

Eksik: İddia → kaynak → sürüm/tarih → doğrulama yöntemi eşlemesi ve “AOS'a alınacak / ertelenen / alınmayacak” karar tablosu. Rakip betimlemesi tek başına AOS ürün gereksinimi olarak kullanılmamalı.

### D12 — Güvenlik, veri geçişi ve regresyonlar için operasyonel kontrol listesi yok

Kanıt: [.agent/Agent.md:63](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/Agent.md:63>), [.agent/lessons.md:142](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/.agent/lessons.md:142>), [core/data/src/main/java/com/aos/core/data/database/AosLauncherDatabase.kt:26](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/database/AosLauncherDatabase.kt:26>), [core/data/src/main/java/com/aos/core/data/database/di/DatabaseModule.kt:27](<C:/Users/Abdülhamid TÜRK/Desktop/AOS Launcher/core/data/src/main/java/com/aos/core/data/database/di/DatabaseModule.kt:27>).

Genel güvenlik hedefleri ve API test öğütleri var, fakat release anahtarı yönetimi, eski kurulumdan güncelleme, Room şema değişimi, yedek formatı uyumluluğu ve başarısız geri yüklemeden kurtarma için somut prosedür bulunmadı. Kod exportSchema=false ve destructive fallback kullanıyor; bu yüzden sürüm yükseltme kabul senaryoları özellikle gerekli. Yeni tespit edilen bulgular da .agent görevleriyle ilişkilendirilmemiş.

Eksik: Yayın öncesi kontrol listesi; şema/yedek sürüm geçiş tablosu; hata ID'si, etkilenen sürüm, düzeltme commit'i ve regresyon testi bağlantısı. lessons.md'deki her ders için “hangi kod/test bunu koruyor?” bağlantısı eklenmeli.

### Önerilen doküman düzenleme sırası

1. D01–D03: Yanlış tamamlandı ve güvenlik/veri bütünlüğü garantilerini düzeltin; simülasyonları açıkça ayırın.
2. D04–D06: Mevcut mimariyi, sözleşmeleri ve örnekleri gerçek kaynaklarla eşleştirin.
3. D07–D09: Sürüm kaydı, kabul senaryosu ve test kanıtını tamamlanma şartı yapın.
4. D10–D12: UI kabul matrisi, kaynaklı ürün gereksinimleri ve yayın/migrasyon kontrol listelerini ekleyin.

Önerilen görev şablonu: **ID | kullanıcı senaryosu | durum | kod referansı | kabul testi | son doğrulama tarihi/sonucu | engel | hedef sürüm**. Aynı görevi üç belgede bağımsız işaretlemek yerine tek listede tutup diğer belgelerden ona bağlantı verin.

