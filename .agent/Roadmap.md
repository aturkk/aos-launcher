# AOS Launcher — Yol Haritası (Roadmap)

## Uzun Vadeli Vizyon (2 Yıl)

```
2026 Q3-Q4  → Faz 0-1: Altyapı + Core MVP
2026 Q4     → Faz 2: Kişiselleştirme + Profiller
2027 Q1     → Faz 3: AI Entegrasyonu
2027 Q1-Q2  → Faz 4: Ekosistem + Plugin + Launch
2027 Q2     → v1.0 Play Store Yayını
2027 Q3     → v1.5: Wear OS + Tablet optimizasyonu
2027 Q4     → v2.0: Gelişmiş AI + Plugin Mağazası
2028        → v3.0: Multi-device ekosistemi
```

---

## v1.0 — Foundation (Faz 0-5 sonu)
**Hedef Tarih**: 2027 Q2

### Temel Özellikler
- [x] Temel launcher engine (sonsuz sayfalar, grid)
- [x] App Drawer (arama, sıralama)
- [x] Klasör sistemi
- [x] Widget motoru (sistem + özel)
- [x] Drag & Drop
- [x] Dock bar
- [x] Jestler sistemi
- [x] Tema motoru (Material You + özel)
- [x] İkon paketi desteği
- [x] Animasyon sistemi (30+ preset)
- [x] Profil/Mod sistemi (6 mod)
- [x] Konum & zaman tabanlı profil değişimi
- [x] On-device AI öneri motoru
- [x] Gemini Nano asistan (temel)
- [x] Sesli komut
- [x] Bulut senkronizasyonu
- [x] Gizli uygulama alanı
- [x] Kullanım analitikleri
- [x] Plugin API v1

---

## v1.5 — Expansion (2027 Q3)

### Yeni Özellikler
- [ ] **Tablet Optimizasyonu**: büyük ekran layout, iki panel mod
- [ ] **Wear OS Companion**: akıllı saat uyumu, bildirim yönetimi
- [ ] **Foldable Desteği**: iç/dış ekran ayrı layout
- [ ] **Video Duvar Kağıdı**: MP4 live wallpaper motoru
- [ ] **Gelişmiş Widget Editörü**: pixel-perfect yerleştirme
- [ ] **Çizim Jesti Motoru**: özel şekil çizme → aksiyon bağlama
- [ ] **Smart Home Entegrasyonu**: Google Home / Matter protokolü
- [ ] **Spotify Deep Integration**: mini player, şarkı sözleri widget
- [ ] **Advanced Analytics**: haftalık rapor bildirimi, hedef koyma
- [ ] **Özelleştirilebilir Arama**: provider ekleme/kaldırma
- [ ] **İkon Renk Özelleştirme**: monotone, tinted, gradient ikonlar

---

## v2.0 — AI-First (2027 Q4)

### AI Sıçrama Özellikleri
- [ ] **Predictive Layout**: AI ekranı kullanıcıya göre yeniden düzenler
- [ ] **Mood Detection**: kamera + ses ile ruh hali tespiti → otomatik tema
- [ ] **Proactive Cards**: "Toplantın 10 dk sonra, Map'i aç?" kartları
- [ ] **Conversation Memory**: Asistan geçmiş bağlamı hatırlar
- [ ] **Natural Language Search**: "Geçen haftaki notlarım" gibi sorgular
- [ ] **Auto Routine Builder**: AI günlük rutini öğrenip layout önerir
- [ ] **Screen Digest**: gün boyunca ekranı özetleyen günlük rapor

### Plugin Ekosistemi
- [ ] **Plugin Mağazası v2**: inceleme, puanlama, ücretli plugin desteği
- [ ] **Plugin Studio**: basit plugin yapımı için no-code araç
- [ ] **Tema Mağazası**: community tema paylaşımı
- [ ] **Widget Kütüphanesi**: hazır widget galerisi

### Platform Genişleme
- [ ] **Windows Widget**: PC masaüstüne launcher widget köprüsü
- [ ] **Web Dashboard**: tarayıcıdan launcher ayarlarını yönetme
- [ ] **API v2**: daha zengin üçüncü parti entegrasyon yüzeyi

---

## v3.0 — Ecosystem (2028)

### Uzun Vadeli Hedefler
- [ ] **Multi-Device Mesh**: telefon, tablet, TV, saat arasında akıllı bağlam geçişi
- [ ] **AI Agent Integration**: launcher'dan görev delegasyonu ("Bu e-postayı yanıtla")
- [ ] **AR Launcher Mode**: kamera ile augmented reality overlay
- [ ] **Personal AI Model**: kullanıcının kendi cihazında fine-tuned model
- [ ] **Cross-Platform Sync**: Android dışı cihazlarla ayar senkronizasyonu

---

## Teknoloji Evrimi

| Dönem | Odak |
|-------|------|
| 2026 | Compose stabilizasyonu, Kotlin 2.x |
| 2027 | Gemini Nano API olgunlaşması, Material You v3 |
| 2027 | Android 16 yeni API'leri (Predictive Back, Health Connect) |
| 2028 | On-device multimodal AI, AR API olgunlaşması |

---

## Gelir Modeli Yol Haritası

### v1.0 (Ücretsiz + Freemium)
- Temel özellikler: ücretsiz
- Premium: AI asistan, bulut sync, gelişmiş temalar
- Fiyat: ~2-3 USD/ay veya 15 USD/yıl

### v1.5 (Plugin Geliri)
- Plugin mağazasında %30 pay (geliştiriciye %70)
- Featured tema/plugin satışı

### v2.0 (Kurumsal)
- MDM entegrasyonu ile kurumsal lisans
- Beyaz etiket (white-label) launcher lisansı

---

## Risk & Azaltma Stratejileri

| Risk | Olasılık | Etki | Azaltma |
|------|----------|------|---------|
| Google Launcher API kısıtlaması | Orta | Yüksek | AOSP API'ye bağlı kal, proprietary API kullanma |
| Gemini Nano cihaz uyumsuzluğu | Yüksek | Orta | TFLite fallback model her zaman hazır |
| Play Store politika ihlali | Düşük | Yüksek | Accessibility Service kullanımını minimize et |
| Bellek sızıntısı (widget host) | Orta | Yüksek | Sürekli profiling, LeakCanary CI entegrasyonu |
| İkon paketi telif hakkı | Düşük | Orta | Sadece uyumlu, açık kaynak ikon paketleri |
| AI model boyutu (200MB+) | Yüksek | Orta | Modeli isteğe bağlı indirme (opt-in download) |

---

## Başarı Metrikleri (Zaman Çizelgesi)

| Tarih | Hedef |
|-------|-------|
| Faz 1 sonu | İlk internal beta yayını |
| Faz 2 sonu | 100 beta tester |
| Faz 3 sonu | 1.000 beta tester, AI özellik geri bildirimi toplandı |
| v1.0 launch | 5.000 ilk indirme |
| +1 ay | 20.000 aktif kullanıcı |
| +3 ay | 50.000 aktif kullanıcı, 4.5+ Play Store puanı |
| +6 ay | 150.000 aktif kullanıcı, premium dönüşüm %5 |
