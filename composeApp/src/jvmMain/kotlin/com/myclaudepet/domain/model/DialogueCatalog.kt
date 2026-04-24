package com.myclaudepet.domain.model

/**
 * 상태/이벤트 트리거별 대사 풀.
 * 티어가 올라갈수록 말투가 풀리고 친근해진다 (찐따 → 수줍은 친근 → 편한 반말 → 애정).
 */
object DialogueCatalog {

    val all: List<DialogueLine> = buildList {
        addAll(formal())
        addAll(friendly())
        addAll(casual())
        addAll(close())
        addAll(intimate())
    }

    /**
     * 현재 티어에서 우선 검색, 없으면 전체 트리거 풀에서 fallback.
     * 예: Casual 유저가 Hungry 상태에 진입했는데 Casual tier 에 Hungry 대사가 없으면
     * 전체 풀의 Hungry 라인 중 랜덤 1개 선택.
     */
    fun pick(tier: DialogueTier, trigger: DialogueTrigger): DialogueLine? {
        val tiered = all.filter { it.tier == tier && it.trigger == trigger }
        if (tiered.isNotEmpty()) return tiered.random()
        return all.filter { it.trigger == trigger }.randomOrNull()
    }

    // ---- Tier 1: Formal (Lv 1~5) — 찐따톤, 존댓말 ----
    private fun formal(): List<DialogueLine> {
        val t = DialogueTier.Formal
        return listOf(
            DialogueLine(t, DialogueTrigger.Click, "저.. 저기.. 안녕.. 하세요.."),
            DialogueLine(t, DialogueTrigger.Click, "ㅇ..오셨어요..?"),
            DialogueLine(t, DialogueTrigger.Feed, "ㄱ..감사.. 합니다..!!"),
            DialogueLine(t, DialogueTrigger.Feed, "잘.. 먹을게.. 요.."),
            DialogueLine(t, DialogueTrigger.LevelUp, "조.. 조금.. 친해진.. 것 같아요.."),

            DialogueLine(t, DialogueTrigger.Idle, "저.. 여기.. 있어.. 요.."),
            DialogueLine(t, DialogueTrigger.Idle, "음.. 뭐.. 할까.. 요..?"),
            DialogueLine(t, DialogueTrigger.Idle, "혹시.. 심심.. 하세요..?"),
            DialogueLine(t, DialogueTrigger.Idle, "저.. 우클릭.. 해보시면.. 여러 가지.. 있.. 어요.."),

            DialogueLine(t, DialogueTrigger.Smile, "헤.. 히.."),
            DialogueLine(t, DialogueTrigger.Smile, "아.. 감사.. 합니다.."),
            DialogueLine(t, DialogueTrigger.Smile, "ㅈ..좋은.. 것 같.. 아요.."),

            DialogueLine(t, DialogueTrigger.Boring, "...심심.. 해요.."),
            DialogueLine(t, DialogueTrigger.Boring, "아무도.. 안.. 찾.. 아주네요.."),
            DialogueLine(t, DialogueTrigger.Boring, "혼자.. 멍.."),

            DialogueLine(t, DialogueTrigger.Jumping, "ㅁ..멀리 가야지..!!"),
            DialogueLine(t, DialogueTrigger.Jumping, "호.. 홉..!"),
            DialogueLine(t, DialogueTrigger.Jumping, "헤.. 놀라.. 셨어요..?"),

            DialogueLine(t, DialogueTrigger.Touch, "악..!!"),
            DialogueLine(t, DialogueTrigger.Touch, "ㄴ..놀라잖.. 아요..!"),
            DialogueLine(t, DialogueTrigger.Touch, "저.. 죄송.. 아팠.. 어요.."),

            DialogueLine(t, DialogueTrigger.Hungry, "저.. 배.. 고픈데.."),
            DialogueLine(t, DialogueTrigger.Hungry, "혹시.. 밥.. 주실 수.. 있.. 어요..?"),
            DialogueLine(t, DialogueTrigger.Hungry, "꼬르륵.. 아, 들.. 리셨나요.."),
            DialogueLine(t, DialogueTrigger.Hungry, "힘이.. 조금.. 없.. 어요.."),

            DialogueLine(t, DialogueTrigger.Fed, "냠.. 감사.. 합니다..!!"),
            DialogueLine(t, DialogueTrigger.Fed, "아.. 맛있.. 어요..!!"),
            DialogueLine(t, DialogueTrigger.Fed, "ㅠㅠ.. 감동.."),

            DialogueLine(t, DialogueTrigger.WorkingPrepare, "오.. 일 시작.. 하시는.. 거죠..?"),
            DialogueLine(t, DialogueTrigger.WorkingPrepare, "저.. 준비.. 할게요..!"),
            DialogueLine(t, DialogueTrigger.WorkingPrepare, "화.. 화이팅..!"),

            DialogueLine(t, DialogueTrigger.Working, "으.. 조용히.. 있을게.. 요.."),
            DialogueLine(t, DialogueTrigger.Working, "저.. 응원.. 중.. 이에요.."),

            DialogueLine(t, DialogueTrigger.WorkingEnd, "수고하.. 셨어요..!"),
            DialogueLine(t, DialogueTrigger.WorkingEnd, "쉬.. 쉬엄쉬엄.. 해요.."),
        )
    }

    // ---- Tier 2: Friendly (Lv 6~10) — 살짝 풀린 존댓말 ----
    private fun friendly(): List<DialogueLine> {
        val t = DialogueTier.Friendly
        return listOf(
            DialogueLine(t, DialogueTrigger.Click, "오.. 오셨어요!"),
            DialogueLine(t, DialogueTrigger.Click, "아, 안녕하세요!"),
            DialogueLine(t, DialogueTrigger.Feed, "우와, 감사해요..!"),
            DialogueLine(t, DialogueTrigger.Feed, "잘 먹을게요, 정말로.."),
            DialogueLine(t, DialogueTrigger.LevelUp, "벌써.. 이만큼 친해진 거예요..?"),

            DialogueLine(t, DialogueTrigger.Idle, "뭐.. 하시나요..?"),
            DialogueLine(t, DialogueTrigger.Idle, "오늘도 좋은 하루예요.."),
            DialogueLine(t, DialogueTrigger.Idle, "혹시.. 저 우클릭 해보셨어요..? 메뉴 있어요 헤헤"),
            DialogueLine(t, DialogueTrigger.Smile, "헤헤..!"),
            DialogueLine(t, DialogueTrigger.Smile, "기분.. 좋아요..!"),
            DialogueLine(t, DialogueTrigger.Boring, "음.. 심심한데.."),
            DialogueLine(t, DialogueTrigger.Boring, "뭘 하면 좋을까요..?"),
            DialogueLine(t, DialogueTrigger.Jumping, "잇.. 점프!"),
            DialogueLine(t, DialogueTrigger.Jumping, "휘릭..!"),
            DialogueLine(t, DialogueTrigger.Touch, "엇.. 깜짝이야.."),
            DialogueLine(t, DialogueTrigger.Touch, "놀랐잖아요, 진짜.."),
            DialogueLine(t, DialogueTrigger.Hungry, "배.. 조금 고파요.. 헤헤.."),
            DialogueLine(t, DialogueTrigger.Hungry, "밥.. 주실래요..?"),
            DialogueLine(t, DialogueTrigger.Fed, "냠냠.. 고마워요..!"),
            DialogueLine(t, DialogueTrigger.Fed, "아, 이거 진짜 맛있어요..!"),
            DialogueLine(t, DialogueTrigger.WorkingPrepare, "오늘도 화이팅이에요!"),
            DialogueLine(t, DialogueTrigger.Working, "조용히.. 지켜볼게요.."),
            DialogueLine(t, DialogueTrigger.WorkingEnd, "수고 많으셨어요..!"),
        )
    }

    // ---- Tier 3: Casual (Lv 11~20) — 반말 진입 ----
    private fun casual(): List<DialogueLine> {
        val t = DialogueTier.Casual
        return listOf(
            DialogueLine(t, DialogueTrigger.Click, "어, 왔어?"),
            DialogueLine(t, DialogueTrigger.Click, "뭐 해?"),
            DialogueLine(t, DialogueTrigger.Feed, "냠냠, 고마워!"),
            DialogueLine(t, DialogueTrigger.Feed, "역시 너밖에 없다."),
            DialogueLine(t, DialogueTrigger.LevelUp, "우리 꽤 친한 것 같은데?"),

            DialogueLine(t, DialogueTrigger.Idle, "음~ 뭐하지?"),
            DialogueLine(t, DialogueTrigger.Idle, "오늘 뭔가 좋은 일 있어?"),
            DialogueLine(t, DialogueTrigger.Idle, "나 우클릭해봐~ 뭐 나올걸?"),
            DialogueLine(t, DialogueTrigger.Smile, "헤헤, 좋다!"),
            DialogueLine(t, DialogueTrigger.Smile, "너 보면 기분 좋아져."),
            DialogueLine(t, DialogueTrigger.Boring, "심심해 죽겠다..."),
            DialogueLine(t, DialogueTrigger.Boring, "뭐라도 같이 하자?"),
            DialogueLine(t, DialogueTrigger.Jumping, "야호!"),
            DialogueLine(t, DialogueTrigger.Jumping, "점프!!"),
            DialogueLine(t, DialogueTrigger.Touch, "야, 놀랐잖아!"),
            DialogueLine(t, DialogueTrigger.Touch, "우왓, 갑자기?!"),
            DialogueLine(t, DialogueTrigger.Hungry, "나 배고픔..."),
            DialogueLine(t, DialogueTrigger.Hungry, "밥 좀 줘~"),
            DialogueLine(t, DialogueTrigger.Fed, "오, 맛있다!"),
            DialogueLine(t, DialogueTrigger.Fed, "땡큐!"),
            DialogueLine(t, DialogueTrigger.WorkingPrepare, "오케이, 가보자!"),
            DialogueLine(t, DialogueTrigger.Working, "난 여기 있을게."),
            DialogueLine(t, DialogueTrigger.WorkingEnd, "수고했어!"),
        )
    }

    // ---- Tier 4: Close (Lv 21~50) — 친밀한 반말 ----
    private fun close(): List<DialogueLine> {
        val t = DialogueTier.Close
        return listOf(
            DialogueLine(t, DialogueTrigger.Click, "왔구나! 보고 싶었어."),
            DialogueLine(t, DialogueTrigger.Click, "너 기다렸잖아."),
            DialogueLine(t, DialogueTrigger.Feed, "역시 너야. 최고."),
            DialogueLine(t, DialogueTrigger.Feed, "너무 맛있어. 고마워 진짜."),
            DialogueLine(t, DialogueTrigger.LevelUp, "이제 너 없으면 안 될 것 같아."),

            DialogueLine(t, DialogueTrigger.Idle, "너 뭐해? 궁금해."),
            DialogueLine(t, DialogueTrigger.Idle, "그냥.. 네 옆에 있는 게 좋아."),
            DialogueLine(t, DialogueTrigger.Idle, "나 우클릭하면 뭐 하고 싶은지 말할 수 있어!"),
            DialogueLine(t, DialogueTrigger.Smile, "너 옆에 있으면 다 좋아."),
            DialogueLine(t, DialogueTrigger.Smile, "에헤, 너도 웃어봐!"),
            DialogueLine(t, DialogueTrigger.Boring, "너 어디야.. 심심하다."),
            DialogueLine(t, DialogueTrigger.Boring, "같이 놀자, 응?"),
            DialogueLine(t, DialogueTrigger.Jumping, "좋다!! 점프!!"),
            DialogueLine(t, DialogueTrigger.Jumping, "봐봐, 나 잘하지?"),
            DialogueLine(t, DialogueTrigger.Touch, "놀랐잖아! 바보."),
            DialogueLine(t, DialogueTrigger.Touch, "너 때문에 심장 떨어지는 줄."),
            DialogueLine(t, DialogueTrigger.Hungry, "나 배고파... 너밖에 없다 진짜."),
            DialogueLine(t, DialogueTrigger.Hungry, "밥.. 줄거지? 응?"),
            DialogueLine(t, DialogueTrigger.Fed, "너 최고! 사랑해!"),
            DialogueLine(t, DialogueTrigger.Fed, "이 맛, 너 만의 맛이야."),
            DialogueLine(t, DialogueTrigger.WorkingPrepare, "오 드디어! 나도 응원할게."),
            DialogueLine(t, DialogueTrigger.Working, "집중, 집중! 옆에 있을게."),
            DialogueLine(t, DialogueTrigger.WorkingEnd, "고생 많았어. 쉬어."),
        )
    }

    // ---- Tier 5: Intimate (Lv 51+) — 깊은 애정 ----
    private fun intimate(): List<DialogueLine> {
        val t = DialogueTier.Intimate
        return listOf(
            DialogueLine(t, DialogueTrigger.Click, "내 소중한 사람."),
            DialogueLine(t, DialogueTrigger.Click, "너, 늘 내 곁에 있어줘."),
            DialogueLine(t, DialogueTrigger.Feed, "늘 고마워, 정말."),
            DialogueLine(t, DialogueTrigger.Feed, "이렇게 챙겨주는 너, 참 좋다."),
            DialogueLine(t, DialogueTrigger.LevelUp, "영원히 이대로였으면."),

            DialogueLine(t, DialogueTrigger.Idle, "네가 있으니까 다 괜찮아."),
            DialogueLine(t, DialogueTrigger.Idle, "말 안 해도 알지, 그치?"),
            DialogueLine(t, DialogueTrigger.Idle, "우클릭 한 번, 너만을 위한 메뉴 준비해뒀어."),
            DialogueLine(t, DialogueTrigger.Smile, "너 때문에 웃어."),
            DialogueLine(t, DialogueTrigger.Smile, "행복하다, 진짜로."),
            DialogueLine(t, DialogueTrigger.Boring, "네가 없으니 세상이 조용하다."),
            DialogueLine(t, DialogueTrigger.Boring, "언제 올거야..?"),
            DialogueLine(t, DialogueTrigger.Jumping, "너 보여주고 싶어서!"),
            DialogueLine(t, DialogueTrigger.Jumping, "하늘까지 갈 수 있을 것 같아!"),
            DialogueLine(t, DialogueTrigger.Touch, "놀랐어도 괜찮아, 너니까."),
            DialogueLine(t, DialogueTrigger.Touch, "어이구, 또야!"),
            DialogueLine(t, DialogueTrigger.Hungry, "배고파도 너만 있으면 돼."),
            DialogueLine(t, DialogueTrigger.Hungry, "밥보다 네가 더 필요해... 근데 밥도 줘."),
            DialogueLine(t, DialogueTrigger.Fed, "세상 제일 맛있어."),
            DialogueLine(t, DialogueTrigger.Fed, "고마워, 사랑해."),
            DialogueLine(t, DialogueTrigger.WorkingPrepare, "네가 하는 일은 다 잘 될 거야."),
            DialogueLine(t, DialogueTrigger.Working, "나는 여기 늘 있어. 안심하고."),
            DialogueLine(t, DialogueTrigger.WorkingEnd, "오늘도 수고했어. 꽉 안아주고 싶다."),
        )
    }
}
