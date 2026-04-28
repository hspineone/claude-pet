package com.myclaudepet.domain.model

/**
 * Pet 이 장착할 수 있는 액세서리.
 *
 * MVP(사이클 A.0): Default 상태에서만 시각 적용. 다른 상태(Smile/Jumping/...)
 * 로 전이되면 base PNG 가 노출되어 액세서리는 자동으로 사라진다.
 * 다중 동시 착용은 자산 분리(별도 사이클) 후 도입.
 *
 * `id` 는 리소스 파일명과 1:1 (idle_<id>.png) 로 매핑되어, ui 레이어가
 * `pet/idle_${id}.png` 로 직접 로드한다.
 */
sealed interface Accessory {
    val id: String

    data object RoundGlasses : Accessory { override val id: String = "round_glasses" }
    data object YellowBeanie : Accessory { override val id: String = "yellow_beanie" }
    data object RedScarf : Accessory { override val id: String = "red_scarf" }
    data object MiniLaptop : Accessory { override val id: String = "mini_laptop" }
    data object Headphones : Accessory { override val id: String = "headphones" }

    companion object {
        val ALL: List<Accessory> = listOf(
            RoundGlasses,
            YellowBeanie,
            RedScarf,
            MiniLaptop,
            Headphones,
        )

        fun fromId(id: String?): Accessory? = id?.let { value ->
            ALL.firstOrNull { it.id == value }
        }
    }
}
