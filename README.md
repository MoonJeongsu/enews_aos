# 이트레블뉴스 (WebView)

기본 WebView 앱입니다. 푸시/메시지함/설정 없이 `https://m.momonews.com/` 만 로드합니다.

## 구성

- `applicationId`: `kr.co.etravelnews.web`
- `compileSdk` / `targetSdk`: **36**
- `minSdk`: 24
- 스플래시 → WebView 진입
- 원본 앱 아이콘 적용

## Android Studio에서 실행

1. **Open** → `EtravelNewsWeb` 폴더 선택
2. Gradle Sync
3. Run ▶

URL 변경: `app/src/main/res/values/strings.xml` 의 `home_url`  
스플래시 대기시간: `SplashActivity.kt` 의 `splashDelayMs`
