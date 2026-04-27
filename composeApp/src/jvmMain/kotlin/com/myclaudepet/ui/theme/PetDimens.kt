package com.myclaudepet.ui.theme

import androidx.compose.ui.unit.dp

object PetDimens {
    val PetSize = 140.dp
    val WindowWidth = 260.dp
    val WindowHeight = 260.dp

    val BubblePadding = 12.dp
    val BubbleMaxWidth = 220.dp
    val BubbleCorner = 14.dp

    val StatsBarHeight = 6.dp
    val StatsCardCorner = 10.dp
    val StatsCardPadding = 8.dp

    /**
     * StatsOverlay 가 차지하는 세로 공간 추정치. PetScreen 에서 펫·말풍선 영역과
     * stats 영역을 격리할 때 bottom padding 산정에 쓰인다.
     * 구성: 상단 padding 8 + 텍스트 14 + spacedBy 4 + bar 6 + spacedBy 4 + bar 6 + 하단 padding 8 = 50.
     */
    val StatsAreaHeight = 50.dp
}
