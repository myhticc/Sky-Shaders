# Sky Shaders — Minecraft 26.2

Yapımcı: **x1eqn**. Mod sürümü: **1.1.0+26.2**.

## Kurulum

- Minecraft 26.2 ve Java 25 veya üzeri.
- Fabric Loader 0.19.5 veya üzeri.
- Fabric API 0.161.0+26.2.
- Ayar ekranı için Mod Menu 20.0.3.
- Grafik API: **OpenGL**.

sky-shaders-1.1.0+26.2.jar dosyasını profilin mods klasörüne koyun.
Modlar → Sky Shaders → Yapılandır yolunu açın. Önceki mod dosyasını kaldırın;
ayrı Minecraft sürümlerine ait JAR dosyalarını birlikte kurmayın.

Bu sürüm özgün GLSL/OpenGL motorunu kullanır. Grafik API ayarında OpenGL seçip oyunu yeniden başlatın.
Vulkan efekt desteği bulunmaz; Vulkan seçiliyken standart gökyüzü ve bulutlar kullanılır.

## Özellikler

9 efekt: Aurora, Galaxy, Black Hole, Thunderstorm, Nebula, Synthwave, Blood Moon, Dreamscape, Prism.
31 renk teması ve iki vurgu rengiyle Custom palet korunur.
Hız, parlaklık, yoğunluk, ölçek, yıldızlar, dönüş, ufuk karışımı, sis, bulutlar, güneş/ay,
End ve Full / %75 / %50 çözünürlük ayarları bulunur. 13 shader dosyası değiştirilmemiştir.

Türkçe/İngilizce Mod Menu ekranında Kaydet değişiklikleri uygular, İptal/Esc taslağı bırakır.
Doğal renkler efektin özgün renklerini kullanır; Prism seçili temayı izler. Diğer efektlerde tema için
Doğal renkler seçeneğini kapatıp Tema renklerini kullan seçeneğini açın. Elle renk girmek için ikisini de kapatın.
Hex renkler ve theme / theme2 desteklenir. Özel vurgu renkleri için Custom temasını seçin.
Ayar dosyası config/sky-shaders.json konumundadır; önceki Sky Shaders JSON ayarları uyumludur.

## Sürüme uyarlanan bağlantılar

26.2 LevelRenderer.render, GameRenderer.mainRenderTarget, Gui.setScreen ve indeksli OpenGL blend/renk maskesi
çağrılarına uyarlanmıştır. Diğer renk hedeflerinin OpenGL durumuna dokunulmaz.
Beş mixin, 12 enjeksiyon noktası ve projection accessor kullanılır.

## Derleme

JDK 25 ile gradlew.bat build veya yerel Gradle önbelleği hazırsa:

    .\scripts\build-local.ps1 -JavaHome 'C:\path\to\jdk-25' -ValidateGpu

26.2 adları açık olduğundan yayın JAR dosyası için yeniden eşleme gerekmez.
Test ve yardımcı sınıflar yayın JAR dosyasına dahil edilmez.

9 efektin GPU derleme/bağlama/çizim/piksel okuma testleri, 31 tema, renk ayarları ve enjeksiyon imzaları doğrulandı.
Dünya içinde elle görsel karşılaştırma ve Iris/Sodium uyumluluğu denenmedi.
Java 25 ve OpenGL ile gerçek Minecraft 26.2 açılışı, Mod Menu ayarlarının dört sayfası, 27 efekt/çözünürlük çizimi, etkinlik/End/su altı kontrolleri ve OpenGL durumunun geri yüklenmesi doğrulandı.
