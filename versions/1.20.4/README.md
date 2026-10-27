# Sky Shaders — 1.20.4

Yapımcı: **x1eqn**. Sürüm: **1.1.0+1.20.4**.

## Kurulum

Minecraft **1.20.4**, **Fabric Loader 0.19.5+**, **Java 17+** ve **Fabric API 0.97.3+1.20.4** gerekir.
Ayar ekranı için **Mod Menu 9.2.0** kullanın.

Profilin mods klasörüne sky-shaders-1.1.0+1.20.4.jar dosyasını koyun.
Oyunda **Modlar → Sky Shaders → Yapılandır** yolunu açın.
Önceki mod JAR'ını kaldırın; aynı anda yalnızca Minecraft sürümüne uygun bir JAR kullanın.

## Özellikler

9 efekt: Aurora, Galaxy, Black Hole, Thunderstorm, Nebula, Synthwave, Blood Moon, Dreamscape, Prism.
31 renk teması ve iki vurgu rengiyle Custom palet bulunur.
Hız, parlaklık, yoğunluk, ölçek, yıldızlar, dönüş, ufuk karışımı, sis, bulutlar, güneş/ay,
End ve Full / %75 / %50 çözünürlük seçenekleri korunur.
Shader dosyaları ve çizim motorunun efekt hesapları korunmuştur.

Türkçe/İngilizce ayar ekranında Kaydet değişiklikleri uygular; İptal/Esc taslağı bırakır.
Doğal renkler efektin özgün renklerini kullanır; Prism seçili temayı izler.
Diğer efektlerde tema için Doğal renkler'i kapatıp Tema renklerini kullan'ı açın.
Elle renk girmek için iki seçeneği de kapatın. Hex renkler ve theme / theme2 desteklenir.
Özel vurgu renkleri için Custom temasını seçin.

Ayar dosyası: config/sky-shaders.json. Önceki ayarlarınızın JSON içeriği uyumludur;
mevcut ayar dosyanızın bir kopyasını bu adla kaydederek aktarabilirsiniz.

## Derleme ve test

Standart Gradle derlemesi mod için Java 17 hedefler. Alternatif yardımcı araçlar JDK 21 ile çalışır; üretilen mod Java 17 uyumludur. Yerel Gradle önbelleği hazırsa alternatif:

    .\scripts\build-local.ps1 -JavaHome 'C:\path\to\jdk-21' -ValidateGpu

Teslim edilen JAR yerel Java derlemesiyle üretildi. Eski Minecraft sürümlerinde yayın çıktısı
Fabric intermediary adlarına yeniden eşlenmiştir; -named.jar dosyası geliştirme çıktısıdır.
9 efektin GPU derleme, bağlama, çizim ve piksel okuma testleri; tema/renk/ayar testleri geçti.
Dünya içinde elle görsel karşılaştırma ve Iris/Sodium uyumluluğu denenmedi.
Minecraft 1.20.4 hedefi için PoseStack ve eski BufferBuilder çizim çağrıları uyarlandı. 13 shader dosyası önceki sürümlerle byte düzeyinde aynıdır.

Java 17 üzerinde gerçek Minecraft açılışı, Mod Menu ayarlarının dört sayfası, 27 efekt/çözünürlük çizimi ve OpenGL durumunun geri yüklenmesi doğrulandı. Dünya içinde elle görsel karşılaştırma yapılmadı.
