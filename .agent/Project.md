# AOS Launcher — Proje Tanımı

## Vizyon
Android ekosisteminde yapay zeka destekli, tam özelleştirilebilir, gizlilik odaklı ve akıllı bir launcher deneyimi sunmak. Kullanıcının telefon kullanım alışkanlıklarını öğrenen, bağlama göre davranışını ayarlayan ve zamanla daha akıllı hale gelen bir "kişisel mobil işletim ortamı" yaratmak.

## Misyon
- Kullanıcıya günde 15-20 dk zaman kazandırmak (akıllı öneriler, hızlı erişim)
- Telefon bağımlılığını azaltmak (odak modları, kullanım analitikleri)
- Tam kişiselleştirilebilirlik ile kullanıcı kimliğini yansıtan bir deneyim sunmak

## Hedef Kitle
| Segment | Profil |
|---------|--------|
| Power User | Kişiselleştirme, otomasyon, geliştirici eğilimli |
| Verimlilik Odaklı | İş/özel hayat ayrımı yapan profesyoneller |
| Gizlilik Bilinçli | Veri takibinden kaçınan kullanıcılar |
| Teknoloji Meraklısı | AI özelliklerini erken benimseyenler |
| Genel Kullanıcı | Güzel, sezgisel, hızlı launcher isteyenler |

## Kapsam — Ne Yapacağız
- Tam özellikli Android Launcher (HOME category intent filter)
- Özel widget motoru (AppWidgetHost + custom widgets)
- On-device AI asistan (Gemini Nano / TFLite)
- Cloud AI entegrasyonu (Gemini API)
- Tema & ikon paketi motoru (AOSP/Nova/Apex/ADW uyumlu)
- Plugin/eklenti sistemi (3. parti geliştirici API'si)
- Bulut senkronizasyonu
- Kullanım analitikleri dashboard

## Kapsam Dışı (v1.0)
- iOS sürümü
- Wear OS (v2.0)
- Root gerektiren özellikler (opsiyonel flag)

## Teknik Gereksinimler
- Minimum SDK: API 26 (Android 8.0)
- Target SDK: API 35 (Android 15)
- Dil: Kotlin %100
- UI: Jetpack Compose
- Min RAM: 2 GB (AI için 4 GB önerilir)
- Depolama: ~50 MB kurulum, ~200 MB AI modeli dahil

## Başarı Kriterleri
- Cold start < 300ms
- Ana ekran FPS: 60fps (120Hz cihazlarda 120fps)
- Crash rate < %0.1
- Play Store: 4.5+ puan
- 6. ayda 50.000+ aktif kullanıcı

## Doküman Sözleşmesi
- Mimari kararlar → Architecture.md
- Sprint değişiklikleri → changelog.md
- Dersler → lessons.md
- AI Agent talimatları → Agent.md
- Faz ilerlemesi → Phase.md
- Uzun vade → Roadmap.md
- Yapılacaklar → todo.md
- Agent yetenekleri → skills.md
