---

> ⚠️ 업데이트 시 반드시 ClaudePet 앱을 종료한 후 설치해 주세요.

## 다운로드

- **Windows**: `ClaudePet-*-windows.zip`
- **macOS**: `ClaudePet-*-macos.zip`

> ZIP 을 푼 뒤 같은 폴더의 **`설치방법.txt`** 에 상세 절차가 포함되어 있습니다.

## Windows

1. ZIP 파일 압축 풀기
2. `.msi` 더블클릭 → 설치 마법사 진행
3. 시작 메뉴 / 바탕화면 바로가기로 **ClaudePet** 실행

> SmartScreen 경고가 뜨면 "추가 정보" → "실행" 클릭 (Apple / Microsoft 서명 미적용 상태)

## macOS

1. ZIP 파일 압축 풀기
2. `.dmg` 열기 → 앱을 **Applications** 폴더로 드래그
3. 터미널에서 Gatekeeper 해제:
   ```
   xattr -cr /Applications/ClaudePet.app
   ```
4. Applications / Spotlight 에서 **ClaudePet** 실행
5. 첫 실행 시 접근성 권한 다이얼로그:
   - `시스템 설정 열기` → **개인정보 보호 및 보안 → 손쉬운 사용** 에서 ClaudePet 허용
   - 앱 다이얼로그의 `완료 (재시작)` 버튼 클릭 → JVM 재시작까지 자동 처리
