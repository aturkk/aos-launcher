package com.aos.feature.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.vector.ImageVector

enum class SettingsSubPage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    Theme(
        title = "Görünüm & Tema",
        subtitle = "Karanlık mod, dinamik renkler, ikon paketi ve 3D geçiş efektleri",
        icon = Icons.Default.Palette
    ),
    HomeScreen(
        title = "Ana Ekran & Izgara",
        subtitle = "Izgara boyutu (4x5 vb.), etiketler, bildirim rozetleri, çift dokunma",
        icon = Icons.Default.Home
    ),
    AppDrawer(
        title = "Uygulama Çekmecesi & Kategoriler",
        subtitle = "Otomatik kategorilendirme, dikey/yatay sekmeler, A..Z gezinme",
        icon = Icons.Default.Apps
    ),
    SearchAndAi(
        title = "Akıllı Arama & Asistan",
        subtitle = "Arama motoru, AI sağlayıcıları, matematik hesaplayıcı, öneriler",
        icon = Icons.Default.Search
    ),
    NewsFeed(
        title = "Haber Akışı (RSS)",
        subtitle = "Yerleşik haber sayfası, kaynaklar, özel RSS beslemeleri",
        icon = Icons.AutoMirrored.Filled.Article
    ),
    Gestures(
        title = "Jestler & Kısayollar",
        subtitle = "Yukarı/aşağı kaydırma, çift dokunma ve simge hareketleri",
        icon = Icons.Default.TouchApp
    ),
    Widgets(
        title = "Araç Takımları & Yığınlar",
        subtitle = "Widget yığınları (stacks) ve açılır pop-up widget'ları",
        icon = Icons.Default.Widgets
    ),
    Profiles(
        title = "Profiller & Odak Modları",
        subtitle = "İş modu, odaklanma, çocuk alanı ve zamanlayıcılar",
        icon = Icons.Default.Group
    ),
    Privacy(
        title = "Gizlilik & Bildirim Günlüğü",
        subtitle = "Bildirim geçmişi arşivi ve kilitli gizli kasa",
        icon = Icons.Default.Lock
    ),
    BackupSync(
        title = "Yedekleme & Senkronizasyon",
        subtitle = "AES-256 şifreli yedek alma, geri yükleme ve bulut",
        icon = Icons.Default.CloudSync
    ),
    Plugins(
        title = "Eklentiler (Plugins)",
        subtitle = "Modüler eklenti yönetimi ve güvenlik izinleri",
        icon = Icons.Default.Extension
    ),
    UpdatesAndAbout(
        title = "Güncellemeler & Teşhis",
        subtitle = "GitHub otomatik güncelleme, hata raporları ve sürüm",
        icon = Icons.Default.SystemUpdate
    )
}
