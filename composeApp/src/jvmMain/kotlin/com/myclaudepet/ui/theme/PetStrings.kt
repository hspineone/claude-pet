package com.myclaudepet.ui.theme

import com.myclaudepet.domain.model.Accessory

object PetStrings {
    const val AppName = "Claude Pet"
    const val TrayShow = "펫 보이기"
    const val TrayHide = "펫 숨기기"
    const val TrayFeed = "🍚 밥 주기"
    const val TrayQuit = "종료"
    const val TrayStats = "상태 보기"
    const val TrayWardrobe = "👕 옷장"
    const val AccessoryNone = "없음"
    const val AccessoryRoundGlasses = "동그란 안경"
    const val AccessoryYellowBeanie = "노란 비니"
    const val AccessoryRedScarf = "빨간 머플러"
    const val AccessoryMiniLaptop = "미니 노트북"
    const val AccessoryHeadphones = "헤드폰"

    // 리셋 UX
    const val MenuReset = "처음부터 시작…"
    const val ResetDialogTitle = "처음부터 시작할까요?"
    const val ResetDialogBody =
        "레벨, 포만감, 타이핑 카운트가 모두 초기화됩니다.\n정말 처음부터 키우시겠어요?"
    const val ResetDialogConfirm = "초기화"
    const val ResetDialogCancel = "취소"

    // 접근성 권한 다이얼로그
    const val PermissionDialogTitle = "접근성 권한이 필요해요"
    const val PermissionDialogBody =
        "타이핑 카운터를 켜려면 접근성 권한이 필요합니다.\n" +
            "시스템 설정 → 개인정보 보호 및 보안 → 손쉬운 사용 에서\n" +
            "이 앱을 허용하신 뒤 다시 실행해 주세요."
    const val PermissionDialogOpen = "시스템 설정 열기"
    const val PermissionDialogDismiss = "나중에"
    const val PermissionDialogRestart = "완료 (재시작)"

    // 업데이트 다이얼로그
    const val UpdateDialogTitle = "새 버전이 나왔어요"
    const val UpdateDialogDownload = "다운로드 페이지 열기"
    const val UpdateDialogDismiss = "나중에"
    fun updateDialogBody(current: String, latest: String): String =
        "현재 버전: $current\n최신 버전: $latest\n\nGitHub Releases 페이지에서 새 버전을 받아보세요."

    // 현재 상태 라벨 (StatsOverlay 에 표시). 빈 문자열은 노출 안 함.
    const val LabelDefault = ""
    const val LabelSmile = ""
    const val LabelBoring = "심심해요…"
    const val LabelJumping = ""
    const val LabelTouch = ""
    const val LabelWorkingPrepare = "일할 준비 중…"
    const val LabelWorking = "일하는 중…"
    const val LabelWorkingEnd = "마무리 중…"
    const val LabelHungry = "배고파요…"
    const val LabelFed = "맛있어요!"
    const val LabelWalking = "산책 중…"

    fun accessoryLabel(accessory: Accessory): String = when (accessory) {
        Accessory.RoundGlasses -> AccessoryRoundGlasses
        Accessory.YellowBeanie -> AccessoryYellowBeanie
        Accessory.RedScarf -> AccessoryRedScarf
        Accessory.MiniLaptop -> AccessoryMiniLaptop
        Accessory.Headphones -> AccessoryHeadphones
    }

    /**
     * Windows 우클릭 컨텍스트 메뉴는 서브메뉴/체크박스 미지원 → 옷장을 평면 항목으로 풀고
     * 현재 장착 항목에 ✓ 마크를 prefix 로 표기해 라디오 의미를 살린다.
     */
    fun wardrobeContextItem(accessory: Accessory?, equipped: Accessory?): String {
        val name = if (accessory == null) AccessoryNone else accessoryLabel(accessory)
        val mark = if (accessory == equipped) "  ✓" else "      "
        return "👕 $name$mark"
    }
}
