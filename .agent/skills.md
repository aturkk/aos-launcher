# AOS Launcher — Agent Yetenekleri (Skills)

Bu dosya, AOS Launcher projesinde agent'ın kullanabileceği yetenekleri, bilgi alanlarını ve özel prosedürleri tanımlar.

---

## 1. Android Launcher Geliştirme

### Launcher Intent Filter
```kotlin
// Her launcher activity'sinde zorunlu
<intent-filter>
    <action android:name="android.intent.action.MAIN"/>
    <category android:name="android.intent.category.HOME"/>
    <category android:name="android.intent.category.DEFAULT"/>
</intent-filter>
```

### PackageManager ile App Listesi
```kotlin
fun getInstalledApps(packageManager: PackageManager): List<AppInfo> {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    return packageManager.queryIntentActivities(intent, 0)
        .map { resolveInfo ->
            AppInfo(
                label = resolveInfo.loadLabel(packageManager).toString(),
                packageName = resolveInfo.activityInfo.packageName,
                icon = resolveInfo.loadIcon(packageManager)
            )
        }
        .sortedBy { it.label }
}
```

### Uygulama Açma
```kotlin
fun launchApp(context: Context, packageName: String) {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val userHandle = Process.myUserHandle()
    launcherApps.getActivityList(packageName, userHandle)
        .firstOrNull()
        ?.let { launcherApps.startMainActivity(it.componentName, userHandle, null, null) }
}
```

### WallpaperManager
```kotlin
// Duvar kağıdı meta verisi alma
val wallpaperManager = WallpaperManager.getInstance(context)
val wallpaperInfo = wallpaperManager.wallpaperInfo // Live wallpaper ise
val wallpaperDrawable = wallpaperManager.drawable  // Statik ise
```

---

## 2. Jetpack Compose Becerileri

### Özel Drag & Drop
```kotlin
// Compose 1.5+ ile built-in DnD
fun Modifier.dragSource(data: Any) = this.dragAndDropSource {
    detectTapGestures(onLongPress = {
        startTransfer(DragAndDropTransferData(ClipData.newPlainText("", "")))
    })
}

fun Modifier.dropTarget(onDrop: (Any) -> Boolean) = this.dragAndDropTarget(
    shouldStartDragAndDrop = { true },
    target = object : DragAndDropTarget {
        override fun onDrop(event: DragAndDropEvent): Boolean = onDrop(event.toAndroidDragEvent())
    }
)
```

### Gesture Detector (Compose)
```kotlin
Modifier.pointerInput(Unit) {
    detectTransformGestures { centroid, pan, zoom, rotation ->
        if (zoom < 0.85f) onPinchIn()
        if (zoom > 1.15f) onPinchOut()
    }
}

Modifier.pointerInput(Unit) {
    detectDragGestures(
        onDragStart = { offset -> },
        onDrag = { change, dragAmount -> },
        onDragEnd = { }
    )
}
```

### Spring Physics Animasyonu
```kotlin
val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.95f else 1f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
)
```

### Sayfa Geçiş Efektleri
```kotlin
HorizontalPager(
    state = pagerState,
    pageSpacing = 16.dp
) { page ->
    val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
    
    // Cube efekti örneği
    Box(
        modifier = Modifier.graphicsLayer {
            rotationY = pageOffset * 90f
            cameraDistance = 12 * density
        }
    ) { PageContent(page) }
}
```

---

## 3. Widget Engine Becerileri

### AppWidgetHost Kurulumu
```kotlin
class LauncherWidgetHost(context: Context) : AppWidgetHost(context, HOST_ID) {
    companion object { const val HOST_ID = 1024 }
    
    override fun onCreateView(
        context: Context, appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?
    ): AppWidgetHostView = LauncherWidgetHostView(context)
    
    fun startListening() = super.startListening()
    fun stopListening() = super.stopListening()
}
```

### Widget Ekleme Sihirbazı
```kotlin
// Kullanıcının widget seçmesi için
val widgetPickerIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, allocatedId)
}
startActivityForResult(widgetPickerIntent, REQUEST_PICK_WIDGET)
```

---

## 4. AI & ML Becerileri

### TFLite Model Yükleme
```kotlin
class AppSuggestionModel(context: Context) {
    private val interpreter: Interpreter
    
    init {
        val model = FileUtil.loadMappedFile(context, "app_suggestion.tflite")
        interpreter = Interpreter(model, Interpreter.Options().apply {
            numThreads = 4
            useNNAPI = true // Hardware acceleration
        })
    }
    
    fun predict(features: FloatArray): FloatArray {
        val output = Array(1) { FloatArray(TOP_K) }
        interpreter.run(arrayOf(features), output)
        return output[0]
    }
}
```

### Gemini Nano (MediaPipe LLM)
```kotlin
val options = LlmInference.LlmInferenceOptions.builder()
    .setModelPath("/data/local/tmp/llm/gemini-nano.bin")
    .setMaxTokens(512)
    .setTopK(40)
    .setTemperature(0.8f)
    .setRandomSeed(42)
    .build()

val llmInference = LlmInference.createFromOptions(context, options)

// Streaming yanıt
llmInference.generateResponseAsync(prompt) { partialResult, done ->
    // partialResult: kısmi token
    // done: true olduğunda tamamlandı
}
```

### UsageStats (Öneri için veri)
```kotlin
fun getUsageStats(context: Context): Map<String, Long> {
    val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val startTime = endTime - (7 * 24 * 60 * 60 * 1000L) // Son 7 gün
    
    return usageStatsManager
        .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        .associate { it.packageName to it.totalTimeInForeground }
}
```

---

## 5. Tema & Ikon Paketi Becerileri

### İkon Paketi Yükleme
```kotlin
fun loadIconFromPack(
    packageManager: PackageManager,
    iconPackPackage: String,
    targetPackage: String
): Drawable? {
    return try {
        val iconPackResources = packageManager.getResourcesForApplication(iconPackPackage)
        val drawableName = targetPackage.replace(".", "_")
        val resId = iconPackResources.getIdentifier(drawableName, "drawable", iconPackPackage)
        if (resId != 0) ResourcesCompat.getDrawable(iconPackResources, resId, null)
        else null
    } catch (e: PackageManager.NameNotFoundException) { null }
}
```

### Material You Dinamik Renk
```kotlin
// DynamicColors API (Material3)
DynamicColors.applyToActivitiesIfAvailable(application)

// Compose'da
val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    if (darkTheme) dynamicDarkColorScheme(LocalContext.current)
    else dynamicLightColorScheme(LocalContext.current)
} else {
    if (darkTheme) darkColorScheme() else lightColorScheme()
}
```

### Duvar Kağıdından Renk Çıkarma
```kotlin
fun extractColors(bitmap: Bitmap): Palette {
    return Palette.from(bitmap)
        .maximumColorCount(16)
        .generate()
}

// Dominant renk al
val dominantColor = palette.getDominantColor(defaultColor)
val vibrantColor = palette.getVibrantColor(defaultColor)
```

---

## 6. Güvenlik Becerileri

### Biometrik Kilit
```kotlin
val biometricPrompt = BiometricPrompt(
    activity,
    ContextCompat.getMainExecutor(activity),
    object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onSuccess()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            onError(errString.toString())
        }
    }
)

val promptInfo = BiometricPrompt.PromptInfo.Builder()
    .setTitle("Kilidi Aç")
    .setNegativeButtonText("İptal")
    .build()

biometricPrompt.authenticate(promptInfo)
```

### Şifreli Depolama
```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val securePrefs = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

---

## 7. Performans Becerileri

### Macrobenchmark (Cold Start)
```kotlin
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule val benchmarkRule = MacrobenchmarkRule()
    
    @Test
    fun startupCold() = benchmarkRule.measureRepeated(
        packageName = "com.aos.launcher",
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD
    ) {
        pressHome()
        startActivityAndWait()
    }
}
```

### Composition Stability
```kotlin
// Unstable class → tüm properties val ve Stable olmalı
@Stable
data class AppItemState(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap
)

// Liste için → persistentListOf (kotlinx.collections.immutable)
val apps: ImmutableList<AppItemState> = persistentListOf()
```

### derivedStateOf (gereksiz recomposition önleme)
```kotlin
val visibleItems by remember {
    derivedStateOf {
        allItems.filter { it.isVisible }
    }
}
```

---

## 8. Test Becerileri

### Compose UI Test
```kotlin
@Test
fun appDrawer_showsAllApps() {
    composeTestRule.setContent { AppDrawerScreen(viewModel = fakeViewModel) }
    
    composeTestRule
        .onNodeWithText("WhatsApp")
        .assertIsDisplayed()
    
    composeTestRule
        .onNodeWithContentDescription("Ara")
        .performTextInput("what")
    
    composeTestRule
        .onNodeWithText("WhatsApp")
        .assertIsDisplayed()
}
```

### Repository Unit Test (Room in-memory)
```kotlin
@Before
fun setup() {
    db = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        LauncherDatabase::class.java
    ).build()
    dao = db.launcherItemDao()
}

@Test
fun insertAndRetrieveItem() = runTest {
    val item = LauncherItemEntity(id = 1, packageName = "com.test.app", page = 0, position = 0)
    dao.insert(item)
    val result = dao.getItemsForPage(0).first()
    assertThat(result).contains(item)
}
```

---

## 9. Sık Kullanılan Gradle Bağımlılıkları

```toml
# libs.versions.toml

[versions]
kotlin = "2.0.0"
compose-bom = "2024.09.00"
hilt = "2.51.1"
room = "2.6.1"
datastore = "1.1.1"
retrofit = "2.11.0"
coil = "2.7.0"
tflite = "2.14.0"
mediapipe = "0.10.14"
lottie = "6.4.0"

[libraries]
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
tflite = { group = "org.tensorflow", name = "tensorflow-lite", version.ref = "tflite" }
mediapipe-llm = { group = "com.google.mediapipe", name = "tasks-genai", version.ref = "mediapipe" }
coil = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
lottie = { group = "com.airbnb.android", name = "lottie-compose", version.ref = "lottie" }
```
