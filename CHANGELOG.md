# Changelog

## [1.1.8](https://github.com/HyeonSeok7/claude-pet/compare/v1.1.7...v1.1.8) (2026-04-28)


### 버그 수정

* **ci:** release-please pull-request-title-pattern 제거 — release 단계 abort 해소 ([#28](https://github.com/HyeonSeok7/claude-pet/issues/28)) ([24ee5b6](https://github.com/HyeonSeok7/claude-pet/commit/24ee5b69b494c64e7d3931911766b14bb1c98835))
* release-please PR 타이틀에 scope 가 박히지 않도록 pattern 명시 — chore(main) 표기 제거하고 ${version} 토큰 유지로 release 단계 abort 도 회피 ([#30](https://github.com/HyeonSeok7/claude-pet/issues/30)) ([a822ce4](https://github.com/HyeonSeok7/claude-pet/commit/a822ce49e1db126e56b3d881e42c56e70e4d0e8b))

## [1.1.7](https://github.com/HyeonSeok7/claude-pet/compare/v1.1.6...v1.1.7) (2026-04-27)


### 버그 수정

* 말풍선 길이로 인해 포만감/친밀도 UI가 가려지는 현상 수정 ([#25](https://github.com/HyeonSeok7/claude-pet/issues/25)) ([9ea0462](https://github.com/HyeonSeok7/claude-pet/commit/9ea04624b9f04cdcd7d6183a1487368b6ffa2091))

## [1.1.0](https://github.com/HyeonSeok7/claude-pet/compare/v1.0.1...v1.1.0) (2026-04-23)


### 새 기능

* ProcessHandle 기반으로 Claude CLI 감지 재구현 ([b321bf9](https://github.com/HyeonSeok7/claude-pet/commit/b321bf9b3c564a919eef4b5add110ee84ecc9251))
* Windows 포그라운드 감지 JNA 기반으로 구현 ([9b650a5](https://github.com/HyeonSeok7/claude-pet/commit/9b650a58e838145eae4d959f88a3e7549bd7e102))
* 작업 화이트리스트 도메인 분리 + Windows · AI 데스크탑 포함 ([8682412](https://github.com/HyeonSeok7/claude-pet/commit/868241268d8f4d5746e6ed768c34423942c58b96))

## [1.0.1](https://github.com/HyeonSeok7/claude-pet/compare/v1.0.0...v1.0.1) (2026-04-22)


### 버그 수정

* **build:** switch release-please marker to block form for Java Properties ([15d08cf](https://github.com/HyeonSeok7/claude-pet/commit/15d08cfbd3273f966b0f86b82c96abdecf8df1da))
