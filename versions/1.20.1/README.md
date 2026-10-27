# Sky Shaders — Minecraft 1.20.1

Yapımcı: **x1eqn**. Mod sürümü: **1.1.2+1.20.1**.

## Kurulum

- Minecraft 1.20.1 ve Java 17 veya üzeri.
- Fabric Loader 0.19.3 veya üzeri.
- Fabric API 0.92.6+1.20.1.
- Ayar ekranı için Mod Menu 7.2.2.

sky-shaders-1.1.2+1.20.1.jar dosyasını profilin mods klasörüne koyun.
Modlar → Sky Shaders → Yapılandır yolunu açın. Önceki mod dosyasını kaldırın;
ayrı Minecraft sürümlerine ait JAR dosyalarını birlikte kurmayın.

## Özellikler

9 efekt: Aurora, Galaxy, Black Hole, Thunderstorm, Nebula, Synthwave, Blood Moon, Dreamscape, Prism.
31 renk teması ve iki vurgu rengiyle Custom palet korunur.
Hız, parlaklık, yoğunluk, ölçek, yıldızlar, dönüş, ufuk karışımı, sis, bulutlar, güneş/ay,
End ve Full / %75 / %50 çözünürlük ayarları bulunur. 13 shader dosyası değiştirilmemiştir.

Türkçe ve İngilizce Mod Menu ekranında Kaydet değişiklikleri uygular, İptal/Esc taslağı bırakır.
Doğal renkler efektin özgün renklerini kullanır; Prism seçili temayı izler. Diğer efektlerde tema için
Doğal renkler seçeneğini kapatıp Tema renklerini kullan seçeneğini açın. Elle renk girmek için ikisini de kapatın.
Hex renkler ve theme / theme2 desteklenir. Özel vurgu renkleri için Custom temasını seçin.
Ayar dosyası config/sky-shaders.json konumundadır; önceki Sky Shaders JSON ayarları uyumludur.

## Derleme

Java 17 hedefleyen Gradle yapılandırması: gradlew.bat build.
Alternatif bağımlılık hazırlama ve derleme yardımcıları JDK 21 ile çalışır; çıktı Java 17 uyumludur:

    .\scripts\build-local.ps1 -JavaHome 'C:\path\to\jdk-21' -ValidateGpu

Bağımlılıklar resmî Minecraft, Fabric ve Mod Menu kaynaklarından indirilir; Minecraft istemcisi ile
resmî eşleme dosyasının SHA-1 özetleri doğrulanır. Yayın JAR dosyası Fabric intermediary adlarına
yeniden eşlenir. Adında -named bulunan dosya geliştirme çıktısıdır.
Test ve yardımcı sınıflar yayın JAR dosyasına dahil edilmez.

Dünya içinde elle görsel karşılaştırma ve Iris uyumluluğu denenmedi. Sodium testi aşağıdaki 1.1.2 notunda belirtilmiştir.
Java 17 üzerinde gerçek Minecraft 1.20.1 açılışı, Mod Menu ayarlarının dört sayfası, 27 efekt/çözünürlük çizimi, etkinlik/End/su altı kontrolleri ve OpenGL durumunun geri yüklenmesi doğrulandı.

## 1.1.1 düzeltmesi

1.1.1 sürümünde gereksiz Fabric Loader 0.19.5 şartı 0.19.3 olarak düzeltildi. Fabric Loader 0.19.3 ve Java 17 ile Minecraft açılışı, dört Mod Menu sayfası ve 27 çizim testi doğrulandı.

## 1.1.2 düzeltmesi

Sodium Extra 0.5.9 sky redirect çakışması giderildi. Sky Shaders gökyüzü çizimini WrapOperation ile sarar; efekt kapalıyken önceki modun çizim çağrısı korunur. Efekt açıkken özgün gökyüzü diskleri/yıldızlar gizlenir, güneş/ay tercihi korunur. Şafak çizimi de aynı biçimde sarılır. Fabric Loader 0.19.3 ile gelen MixinExtras 0.5.4 kullanılır. 13 GLSL dosyası, 9 efekt ve 31 tema değiştirilmedi.

Java 17, Loader 0.19.3, Sodium 0.5.13+mc1.20.1 ve Sodium Extra 0.5.9+mc1.20.1 ile açılış, kancaların birlikte uygulanması, 27 efekt/çözünürlük çizimi ve dört Mod Menu sayfası doğrulandı. Iris ve kullanıcının tam 145 modluk profili denenmedi.
