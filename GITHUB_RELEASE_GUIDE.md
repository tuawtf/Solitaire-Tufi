# GitHub Actions Otomatik Build & Release Rehberi

Bu projede GitHub Actions üzerinden otomatik olarak **Release APK** ve **Google Play AAB (App Bundle)** derlenmesi ve **GitHub Releases** sayfasına yüklenmesi için tam donanımlı CI/CD pipeline'ı (`.github/workflows/release.yml`) kurulmuştur.

---

## 1. İmzalama Anahtarı (Keystore) Bilgileri

Uygulamanın release imzalaması için geçerli bir keystore oluşturuldu ve hazırlandı:

- **Keystore Dosyası**: `my-upload-key.jks`
- **Base64 Dosyası**: `my-upload-key.base64`
- **Key Alias**: `upload`
- **Store Password**: `solitaire_release_pass`
- **Key Password**: `solitaire_release_pass`

> **Sıfır Yapılandırma Desteği:** `my-upload-key.base64` dosyası depoda bulunduğu için GitHub Actions hiçbir ek ayar yapmasanız bile otomatik olarak anahtarı çözer, imzalar ve release oluşturur!

---

## 2. GitHub Secrets (İsteğe Bağlı & Önerilen Güvenlik Adımı)

İsterseniz anahtarınızı GitHub Secrets'a ekleyerek de yönetebilirsiniz:

1. GitHub'da deponuza gidin: **Settings** -> **Secrets and variables** -> **Actions** -> **New repository secret**.
2. Aşağıdaki 3 secret'ı ekleyin:
   - `KEYSTORE_BASE64`: `my-upload-key.base64` dosyasının içindeki metin.
   - `STORE_PASSWORD`: `solitaire_release_pass`
   - `KEY_PASSWORD`: `solitaire_release_pass`

Workflow bu secret'lar tanımlıysa öncelikli olarak onları kullanır, tanımlı değilse projedeki anahtarla sorunsuz devam eder.

---

## 3. GitHub'da Release Nasıl Yayınlanır?

### Yöntem A: Git Tag ile (En Yaygın Yöntem)
Terminalden yeni bir sürüm etiketi oluşturup göndermeniz yeterlidir:
```bash
git tag v1.0.0
git push origin v1.0.0
```
GitHub Actions otomatik olarak:
1. Kodu çeker ve Java 17 ortamını kurar.
2. Keystore'u bağlar.
3. İmzalı `Solitaire-v1.0.0.apk` ve `Solitaire-v1.0.0.aab` dosyalarını derler.
4. GitHub'ın **Releases** sekmesinde `v1.0.0` sürümünü oluşturup bu dosyaları indirmeye hazır olarak ekler!

### Yöntem B: GitHub Web Arayüzünden Tek Tıkla (Manuel)
1. GitHub deponuzda **Actions** sekmesine gidin.
2. Sol menüden **Build & Release Android App** workflow'unu seçin.
3. Sağdaki **Run workflow** butonuna tıklayın:
   - `Release Tag Name`: örn. `v1.0.0`
   - `Run workflow` butonuna basın.

Derleme bittiğinde:
- **GitHub Releases** sekmesinde release yayınlanır.
- Ayrıca **Actions** sekmesindeki çalışma detayından **Solitaire-Release-Binaries** zip dosyası olarak da indirilebilir.
