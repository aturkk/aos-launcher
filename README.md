# 🚀 AOS Launcher (v1.4.0)

[![Android CI & APK Build](https://github.com/aturkk/aos-launcher/actions/workflows/android.yml/badge.svg)](https://github.com/aturkk/aos-launcher/actions/workflows/android.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-8.7-34A853.svg?style=flat&logo=android)](https://developer.android.com/build)
[![Compose BOM](https://img.shields.io/badge/Jetpack%20Compose-2024.10.00-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-green.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

AOS Launcher, **Clean Architecture**, **%100 Yerel Cihaz İçi Yapay Zeka (On-Device AI)**, **AES-256-GCM Şifreli Bulut Yedekleme**, **Modüler Eklenti (Plugin) SDK** ve **Gizli Uygulama Kasası** ile donatılmış, modern ve ultra akıcı (120 FPS) bir Android başlatıcısıdır.

---

## 📱 APK İndirme & Test Etme

Her `git push` işleminde GitHub Actions projeyi otomatik olarak derler ve kurulabilir **Debug APK** üretir:

1. Bu GitHub reposunda [Actions](https://github.com/aturkk/aos-launcher/actions) sekmesine gidin.
2. En son çalışan **"Android CI & APK Build"** iş akışına tıklayın.
3. Sayfanın en altındaki **Artifacts** bölümünden `aos-launcher-v1.4.0-debug` paketini indirin.
4. ZIP dosyasını açıp içindeki `.apk` dosyasını Android cihazınıza (API 26+) yükleyin.

---

## ✨ Özellikler

### 🏠 1. Çekirdek Başlatıcı & Ana Ekran Motoru
- **Hücre Izgara Motoru (GridCellLayout)**: `cellX` ve `cellY` koordinatlarına göre kesin hücre yerleşimi (4x4, 4x5, 5x5, 5x6).
- **Drag & Drop (Sürükle-Bırak)**: İkonları serbest taşıma ve sayfalar arası taşıma.
- **Akıllı Klasörler**: İkonları birbiri üzerine sürükleyerek anında klasör oluşturma ve düzenleme.
- **Sistem Widget Desteği**: `AppWidgetHost` mimarisiyle tam boyutlandırılabilir Android widget'ları ve yerel dijital saat widget'ı.
- **Jestler & Kısayollar**:
  - *Yukarı Kaydır*: Uygulama Çekmecesini açar
  - *Aşağı Kaydır*: Bildirim panelini indirir
  - *Çift Dokun*: Ekranı kapatır ve kilitler (Erişilebilirlik Servisi)
  - *İki Parmak Küçült (Pinch)*: Kuşbakışı sayfa yöneticisini açar

### 🎨 2. Kişiselleştirme & Profil Modları
- **Material You Dinamik Renkler**: Duvar kağıdından otomatik renk paleti oluşturma.
- **3D Sayfa Geçiş Efektleri**: Küp (Cube), Derinlik (Depth), Akordeon (Accordion), Çevirme (Flip).
- **İkon Şekilleri**: Squircle, Daire, Yuvarlak Kare ve Damla şekilleri.
- **İkon Paketleri**: Nova, Apex, ADW ve Go Launcher uyumlu tüm 3. parti paketleri otomatik algılama ve uygulama.
- **6 Farklı Profil Modu**: Normal, İş, Odak, Gece, Çocuk (PIN korumalı) ve Araç modu.

### 🤖 3. Cihaz Üzerinde AI Asistan & Tahminleme
- **Zamana Dayalı Uygulama Tahmini**: Cihaz üzerinde `UsageStats` analiziyle sabah, öğle ve akşam kullanılan uygulamaları tahmin etme.
- **Öneri Rafı**: Uygulama çekmecesinde dinamik parlayan akıllı öneri rafı.
- **Akıllı Bağlam Kartı**: Hava durumu, yaklaşan etkinlikler ve zaman bazlı durum kartı widget'ı.
- **Türkçe & İngilizce NLU Komut Yürütücü**: *"Kamerayı aç"*, *"Google'da ara"*, *"İş moduna geç"*, *"Ekranı kilitle"* sesli ve metin komutları.

### 🔒 4. Güvenlik, Gizlilik & Eklenti Ekosistemi
- **AES-256-GCM Şifreli Yedekleme**: PBKDF2WithHmacSHA256 (10.000 iterasyon) ile istemci tarafında sıfır-bilgi (zero-knowledge) şifreli yedekleme ve bulut eşitleme.
- **Modüler Eklenti (Plugin) SDK**: Arama motorları, canlı widget'lar ve jest eylemleri geliştirmek için `PluginSecuritySandbox` izin kontrolü.
- **Bildirim Günlüğü & Geçmişi**: Kaçırılan veya silinen bildirimleri cihaz üzerindeki Room veritabanında arşivleme, arama ve filtreleme.
- **Gizli Uygulama Kasası (Hidden Vault)**:
  - 4 haneli PIN ile korunan özel kasa
  - Gizlenen uygulamaları App Drawer'dan otomatik ve tamamen gizleme
  - Dahili sayısal tuş takımı ve kasadan tek dokunuşla açma/görünür yapma

### ⚡ 5. Stabilite, Optimizasyon & İlk Açılış
- **Bounded LruCache (150 İkon)**: Düşük bellekli cihazlarda OOM riskini engelleyen ve 120 FPS kaydırmayı koruyan bellek optimizasyonu.
- **İlk Açılış Onboarding Sihirbazı**: 4 adımlı karşılama, varsayılan ana ekran atama ve izin kurulumu.
- **Çevrimdışı Hata Teşhis Motoru (AosCrashReporter)**: Beklenmeyen çökmeleri cihaz üzerinde yerel loglayan ve yığın izini görüntüleyen offline teşhis paneli.

---

## 🏗️ Mimari & Modül Yapısı

Proje katı **Clean Architecture + Multi-Module** prensiplerine göre inşa edilmiştir:

```
AOS Launcher
├── app                  # Başlatıcı ana Activity, Manifest, Onboarding & Servisler
├── core
│   ├── common           # Result<T>, CoroutineDispatcher sağlayıcıları, sistem yardımcıları
│   ├── domain           # Pure Kotlin modülü (Sıfır Android bağımlılığı, veri modelleri, Repository arayüzleri)
│   ├── data             # Room DB, DataStore, Kripto, AI Motoru, Plugin Sandbox, Repositories
│   └── ui               # Jetpack Compose teması, Material You, LruCache AppIcon bileşenleri
└── feature
    ├── home             # Ana ekran, sayfa motoru, klasörler, widget host, dock bar, akıllı asistan
    ├── appdrawer        # Uygulama çekmecesi, A..Z gezinme, öneri rafı, gizli uygulama kasası
    └── settings         # Özelleştirme, profiller, yedekleme, eklenti yöneticisi, bildirim günlüğü, teşhis
```

---

## 🛠️ Derleme & Gereksinimler

- **Minimum SDK**: 26 (Android 8.0 Oreo)
- **Hedef SDK**: 35 (Android 15)
- **JDK**: Java 17
- **Gradle**: 8.9 (AGP 8.7)
- **Kotlin**: 2.0.20

### Yerel Derleme:
```bash
./gradlew assembleDebug
```

---

## 📄 Lisans
Bu proje [MIT Lisansı](LICENSE) altında lisanslanmıştır.
