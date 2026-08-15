package com.example.fragments_of_life.data.local

import android.util.Log
import com.example.fragments_of_life.data.model.ImportantDate
import com.example.fragments_of_life.data.model.ImportantDateType
import com.example.fragments_of_life.data.model.Importance
import com.example.fragments_of_life.data.model.Moment
import com.example.fragments_of_life.data.model.Mood
import com.example.fragments_of_life.data.model.MomentType
import com.example.fragments_of_life.data.model.PartnerNote
import com.example.fragments_of_life.data.model.WhisperLetter
import com.example.fragments_of_life.data.model.WishItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

private const val TAG = "SeedData"

/** 播种互斥锁:防止多个入口同时补种导致重复数据 */
private val seedMutex = Mutex()

/**
 * 数据库为空(首次创建或破坏性迁移后)时,补种演示数据。
 * 挂起函数,可同步等待完成;所有日期相对「今天」生成。
 */
suspend fun AppDatabase.seedIfEmpty() = seedMutex.withLock {
    val dao = momentDao()
    try {
        // 已有数据则跳过;查询失败时先触发一次数据库打开自愈(重建缺失的表)
        var empty: Boolean
        try {
            empty = dao.getAllMoments().first().isEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "查询失败,触发数据库打开以自愈", e)
            openHelper.writableDatabase
            empty = dao.getAllMoments().first().isEmpty()
        }
        if (!empty) {
            Log.d(TAG, "已有数据,跳过播种")
            return@withLock
        }

            val today = LocalDate.now()
            val together = today.minusDays(520) // 在一起纪念日,正好凑一个甜甜的数字

            val demoMoments = listOf(
                Moment(
                    type = MomentType.ANNIVERSARY.name,
                    title = "在一起的第一天 💕",
                    content = "今天是我们在一起的日子!从今天开始,每一天都想记录下来。你笑起来的样子,是我见过最美的风景。",
                    date = together,
                    mood = Mood.ROMANTIC.name,
                    location = "我们的小窝",
                    tags = listOf("第一次", "纪念日"),
                ),
                Moment(
                    type = MomentType.DATE.name,
                    title = "第一次正式约会 🌸",
                    content = "你穿了一条碎花裙子,在咖啡店等我的样子可爱极了。我们聊了整整三个小时,从喜欢的电影聊到人生的梦想。",
                    date = together.plusDays(12),
                    mood = Mood.HAPPY.name,
                    location = "转角咖啡店",
                    tags = listOf("第一次", "约会"),
                ),
                Moment(
                    type = MomentType.FOOD.name,
                    title = "第一次一起做饭 🍳",
                    content = "你教我做了番茄炒蛋,我手忙脚乱地把鸡蛋打到了地上。你笑得前仰后合,最后我们一起吃了一顿不太完美但很温暖的晚餐。",
                    date = together.plusMonths(2),
                    mood = Mood.TOUCHED.name,
                    location = "家里的小厨房",
                    tags = listOf("第一次", "日常"),
                ),
                Moment(
                    type = MomentType.FIGHT.name,
                    title = "第一次闹别扭 💢",
                    content = "因为一件小事赌气,谁也不理谁。但晚上你发来一条消息:'我知道是我不好,但我真的很想你。' 瞬间心就软了。",
                    date = together.plusMonths(4),
                    mood = Mood.SAD.name,
                    tags = listOf("吵架和好"),
                ),
                Moment(
                    type = MomentType.MAKEUP.name,
                    title = "和好如初 💝",
                    content = "吵完架后的第一次见面,你带了我最喜欢的奶茶。我们坐在公园的长椅上,你靠在我肩膀上说:以后我们不要吵架了好不好。",
                    date = together.plusMonths(4).plusDays(2),
                    mood = Mood.TOUCHED.name,
                    location = "人民公园",
                    tags = listOf("吵架和好"),
                ),
                Moment(
                    type = MomentType.TRAVEL.name,
                    title = "第一次一起旅行 ✈️",
                    content = "去了你一直想去的海边城市。你在沙滩上奔跑的样子像个孩子,夕阳把你的影子拉得很长。那晚我们在海边看星星,你说希望时间停在那一刻。",
                    date = together.plusMonths(5).plusDays(11),
                    mood = Mood.ROMANTIC.name,
                    location = "厦门",
                    imageUri = "android.resource://com.example.fragments_of_life/drawable/sample_sunset",
                    tags = listOf("第一次", "旅行"),
                ),
                Moment(
                    type = MomentType.GIFT.name,
                    title = "收到你织的围巾 🧣",
                    content = "冬天快到了,你偷偷织了一条围巾给我。灰蓝色的,是我最喜欢的颜色。你说学了好久,手指都被针扎了好多次。戴上它,整个世界都是暖的。",
                    date = together.plusMonths(6).plusDays(20),
                    mood = Mood.GRATEFUL.name,
                    tags = listOf("小惊喜"),
                ),
                Moment(
                    type = MomentType.LETTER.name,
                    title = "写给你的小情书 💌",
                    content = "亲爱的:认识你之前,我以为浪漫很远。认识你之后,发现浪漫就是一起走过的每个黄昏。谢谢你,把我的「以后」变成了「我们」。",
                    date = together.plusMonths(8),
                    mood = Mood.ROMANTIC.name,
                    tags = listOf("情书"),
                ),
                Moment(
                    type = MomentType.DATE.name,
                    title = "樱花树下的约定 🌸",
                    content = "春天来了,我们去了植物园看樱花。花瓣落在你头发上的样子美极了。我们在树下约定:每年春天都要一起来看樱花。",
                    date = together.plusMonths(10),
                    mood = Mood.ROMANTIC.name,
                    location = "植物园",
                    imageUri = "android.resource://com.example.fragments_of_life/drawable/sample_park",
                    tags = listOf("约会", "旅行"),
                ),
                Moment(
                    type = MomentType.DAILY.name,
                    title = "一起逛超市 🛒",
                    content = "推着购物车在超市里慢慢逛,你像小孩一样往车里塞零食。结账的时候发现买了一堆计划外的东西,但谁在乎呢,开心就好。",
                    date = together.plusMonths(12),
                    mood = Mood.HAPPY.name,
                    location = "楼下超市",
                    tags = listOf("日常"),
                ),
                Moment(
                    type = MomentType.DAILY.name,
                    title = "深夜一起加班 ☕",
                    content = "你加班到很晚,我买了咖啡去找你。办公室里只剩你一个人,台灯下的侧脸认真又好看。陪着你直到工作做完,回家的路上你握着我的手说谢谢。",
                    date = together.plusMonths(14),
                    mood = Mood.TOUCHED.name,
                    location = "公司",
                    tags = listOf("日常"),
                ),
                Moment(
                    type = MomentType.DATE.name,
                    title = "去年今天,我们在看落日 🌅",
                    content = "去年的今天,我们一起在海边看了最美的落日。你说这是你经历过最浪漫的傍晚。夕阳把你的脸照得红扑扑的,比任何晚霞都好看。",
                    date = today.minusYears(1),
                    mood = Mood.ROMANTIC.name,
                    location = "鼓浪屿",
                    imageUri = "android.resource://com.example.fragments_of_life/drawable/sample_ocean",
                    tags = listOf("约会", "旅行"),
                ),
                Moment(
                    type = MomentType.FOOD.name,
                    title = "上个月的烛光晚餐 🕯️",
                    content = "亲手做了一桌菜,点上小蜡烛。你尝第一口的时候眼睛亮了一下,说比外面的餐厅好吃一百倍。",
                    date = today.minusMonths(1).minusDays(2),
                    mood = Mood.TOUCHED.name,
                    location = "家里",
                    tags = listOf("小惊喜", "日常"),
                ),
                Moment(
                    type = MomentType.DAILY.name,
                    title = "昨晚一起散步 🚶",
                    content = "晚饭后下楼散步,走了很远很远。晚风很凉,你把手塞进我口袋里,突然觉得这就是幸福的样子。",
                    date = today.minusDays(1),
                    mood = Mood.HAPPY.name,
                    location = "家楼下",
                    tags = listOf("日常"),
                ),
                // ── 以下 10 条为效果演示记录 ──
                Moment(
                    type = MomentType.FOOD.name,
                    title = "今天一起吃了火锅 🍲",
                    content = "你负责涮肉我负责蘸料,分工明确。辣锅底把我们都辣出了眼泪,却笑得很开心。你偷偷给我夹了第一片毛肚,说这叫偏爱。",
                    date = today,
                    mood = Mood.HAPPY.name,
                    location = "海底捞",
                    tags = listOf("日常", "约会"),
                ),
                Moment(
                    type = MomentType.MOVIE.name,
                    title = "周末电影之夜 🎬",
                    content = "窝在沙发上连看了两部老电影,爆米花撒了一地。你靠在我肩上睡着了,电影演了什么我一点没记住,只记得你呼吸轻轻的。",
                    date = today.minusDays(2),
                    mood = Mood.ROMANTIC.name,
                    location = "家里",
                    tags = listOf("约会", "日常"),
                ),
                Moment(
                    type = MomentType.GIFT.name,
                    title = "收到一束小雏菊 🌼",
                    content = "下班回来发现桌上多了一束小雏菊,卡片上写着:'路上看到它,觉得像你,就买回来了。' 花语是藏在心底的爱。",
                    date = today.minusDays(5),
                    mood = Mood.TOUCHED.name,
                    tags = listOf("小惊喜"),
                ),
                Moment(
                    type = MomentType.TRAVEL.name,
                    title = "老城区半日漫游 🚶",
                    content = "没有目的地,牵着手在老城区的小巷里乱逛。遇见一家开了三十年的糖水铺,你点了双皮奶,我点了姜撞奶,互相喂了第一口。",
                    date = today.minusDays(9),
                    mood = Mood.HAPPY.name,
                    location = "老城区",
                    imageUri = "android.resource://com.example.fragments_of_life/drawable/sample_park",
                    tags = listOf("旅行", "约会"),
                ),
                Moment(
                    type = MomentType.DAILY.name,
                    title = "深夜电台:单曲循环的歌 🎧",
                    content = "分享耳机,一起听最近单曲循环的那首歌。你说这首歌的歌词像在写我们。我假装没听懂,其实耳朵都红了。",
                    date = today.minusDays(14),
                    mood = Mood.ROMANTIC.name,
                    tags = listOf("日常"),
                ),
                Moment(
                    type = MomentType.DAILY.name,
                    title = "拼完 1000 片拼图 🧩",
                    content = "断断续续拼了两周,最后一片是你按上去的。你说缺的从来不是拼图,是愿意陪你慢慢拼的人。裱起来挂在客厅,是我们的第一件'共同作品'。",
                    date = today.minusDays(21),
                    mood = Mood.TOUCHED.name,
                    location = "家里",
                    tags = listOf("小惊喜", "日常"),
                ),
                Moment(
                    type = MomentType.DATE.name,
                    title = "下雨天,共撑一把伞 ☔",
                    content = "暴雨突然落下,一把伞两个人。你把伞往我这边斜了大半,自己的肩膀全湿了,还嘴硬说不冷。那一刻就想,这辈子就是你了。",
                    date = today.minusDays(28),
                    mood = Mood.ROMANTIC.name,
                    location = "人民路",
                    imageUri = "android.resource://com.example.fragments_of_life/drawable/sample_ocean",
                    tags = listOf("约会"),
                ),
                Moment(
                    type = MomentType.GIFT.name,
                    title = "给你做的手工曲奇 🍪",
                    content = "照着教程忙活了一下午,形状歪歪扭扭,还烤糊了一盘。你尝了一口说'这是我吃过最好吃的曲奇',然后把剩下的全打包走了。",
                    date = today.minusMonths(2).plusDays(1),
                    mood = Mood.GRATEFUL.name,
                    location = "家里的小厨房",
                    imageUri = "android.resource://com.example.fragments_of_life/drawable/sample_cafe",
                    tags = listOf("小惊喜", "日常"),
                ),
                Moment(
                    type = MomentType.TRAVEL.name,
                    title = "第一次一起看日出 🌅",
                    content = "凌晨四点起床爬山,困得睁不开眼。太阳跃出海面的瞬间,你突然转身抱住我。后来你说,日出没有你重要,你比日出好看。",
                    date = today.minusMonths(3).plusDays(3),
                    mood = Mood.EXCITED.name,
                    location = "海边山顶",
                    imageUri = "android.resource://com.example.fragments_of_life/drawable/sample_sunset",
                    tags = listOf("第一次", "旅行"),
                ),
                Moment(
                    type = MomentType.LETTER.name,
                    title = "每月一封的小情书 💌",
                    content = "亲爱的:这是这个月的第 N 封情书。没什么大事,就是想告诉你,今天的风很轻、云很白,而我比昨天更喜欢你一点。",
                    date = today.minusMonths(4).plusDays(5),
                    mood = Mood.ROMANTIC.name,
                    tags = listOf("情书"),
                ),
            )

            demoMoments.forEach { dao.insertMoment(it) }

            val demoDates = listOf(
                ImportantDate(
                    type = ImportantDateType.ANNIVERSARY.name,
                    title = "在一起纪念日",
                    date = together,
                    repeatYearly = true,
                    remindBeforeDays = 7,
                    importance = Importance.VERY.name,
                    note = "每年的这一天,都是我们的节日",
                ),
                ImportantDate(
                    type = ImportantDateType.BIRTHDAY.name,
                    title = "TA的生日",
                    date = today.plusDays(45),
                    repeatYearly = true,
                    remindBeforeDays = 7,
                    importance = Importance.VERY.name,
                    note = "提前准备惊喜!",
                ),
                ImportantDate(
                    type = ImportantDateType.FIRST_DATE.name,
                    title = "第一次约会纪念",
                    date = together.plusDays(12),
                    repeatYearly = true,
                    remindBeforeDays = 3,
                    importance = Importance.IMPORTANT.name,
                    note = "转角咖啡店,碎花裙子",
                ),
                ImportantDate(
                    type = ImportantDateType.FIRST_KISS.name,
                    title = "初吻纪念 💋",
                    date = together.plusMonths(2).plusDays(10),
                    repeatYearly = true,
                    remindBeforeDays = 1,
                    importance = Importance.IMPORTANT.name,
                    note = "那晚的星空特别亮",
                ),
                ImportantDate(
                    type = ImportantDateType.PERIOD.name,
                    title = "生理期提醒",
                    date = today.plusDays(12),
                    repeatYearly = false,
                    remindBeforeDays = 3,
                    importance = Importance.NORMAL.name,
                    note = "准备好红糖水和暖宝宝",
                ),
            )

            demoDates.forEach { dao.insertImportantDate(it) }

            val demoWishes = listOf(
                WishItem(title = "一起去迪士尼看烟花", emoji = "🎆"),
                WishItem(title = "一起看一次海上日出", emoji = "🌅"),
                WishItem(title = "一起养一只小猫", emoji = "🐱"),
                WishItem(title = "一起去北方看极光", emoji = "🌌"),
                WishItem(title = "一起做一顿烛光晚餐", emoji = "🕯️"),
                WishItem(title = "一起去海边捡贝壳", emoji = "🐚"),
                WishItem(title = "给对方写一封手写信", emoji = "💌"),
                WishItem(title = "一起去山顶看星空", emoji = "✨"),
                WishItem(title = "一起拍一组情侣写真", emoji = "📸"),
                WishItem(title = "一起在初雪天散步", emoji = "❄️"),
            )

            demoWishes.forEach { dao.insertWish(it) }

        Log.d(TAG, "播种完成:${demoMoments.size} 条碎片 / ${demoDates.size} 个纪念日 / ${demoWishes.size} 个愿望")
    } catch (e: Exception) {
        Log.e(TAG, "播种失败", e)
    }
}

/**
 * 悄悄话信箱为空时,放入两封演示信件:
 * 一封今天就能拆开的,一封 5 天后解锁的(展示火漆倒计时效果)。
 */
suspend fun AppDatabase.seedLettersIfEmpty() = seedMutex.withLock {
    val dao = momentDao()
    try {
        if (dao.getAllLetters().first().isNotEmpty()) return@withLock
        val today = LocalDate.now()
        dao.insertLetter(
            WhisperLetter(
                content = "亲爱的:\n\n写这封信的时候,窗外刚好在下雨。\n想说的话很多,落到纸上只剩一句——\n有你在,日子都是甜的。\n\n愿你每天都能睡个好觉,梦里也有我。",
                paperStyle = "cream",
                sign = "一个很爱你的人",
                unlockDate = today.minusDays(1),
                createdAt = System.currentTimeMillis() - 86400000L * 3,
            )
        )
        dao.insertLetter(
            WhisperLetter(
                content = "这是一封寄存了五天的悄悄话。\n\n等到你打开它的时候,应该是个好天气。\n我想说的是:\n谢谢你把普通的日子过成了诗,\n以后的路,也一起走吧。",
                paperStyle = "sakura",
                sign = "你的TA",
                unlockDate = today.plusDays(5),
                createdAt = System.currentTimeMillis(),
            )
        )
        Log.d(TAG, "悄悄话演示信件已放入信箱")
    } catch (e: Exception) {
        Log.e(TAG, "信件播种失败", e)
    }
}

/**
 * TA的小宇宙为空时,放入几条关于TA的演示备忘。
 */
suspend fun AppDatabase.seedPartnerNotesIfEmpty() = seedMutex.withLock {
    val dao = momentDao()
    try {
        if (dao.getAllPartnerNotes().first().isNotEmpty()) return@withLock
        val now = System.currentTimeMillis()
        val day = 86400000L
        val demoNotes = listOf(
            PartnerNote(
                content = "不喜欢吃辣,微辣也不行",
                category = "🚫 忌口 / 不喜欢",
                tags = listOf("她自己说的", "很重要"),
                importance = Importance.IMPORTANT.name,
                note = "上次吃火锅被辣出眼泪,点菜记得选鸳鸯锅",
                createdAt = now - day * 30,
            ),
            PartnerNote(
                content = "爱吃草莓和抹茶味的一切",
                category = "❤️ 喜欢的食物",
                tags = listOf("我观察到的"),
                importance = Importance.NORMAL.name,
                note = "蛋糕、奶茶、冰淇淋都首选这两个口味",
                createdAt = now - day * 28,
            ),
            PartnerNote(
                content = "喜欢跑步,每周三次,配速6分半",
                category = "🏃 兴趣爱好",
                tags = listOf("我观察到的"),
                importance = Importance.NORMAL.name,
                createdAt = now - day * 25,
            ),
            PartnerNote(
                content = "睡觉一定要抱着玩偶,床头有只小熊",
                category = "🛌 生活习惯",
                tags = listOf("需要记住"),
                importance = Importance.NORMAL.name,
                note = "出门旅行记得帮TA带上小熊",
                createdAt = now - day * 20,
            ),
            PartnerNote(
                content = "生气的时候不要讲道理,先抱抱",
                category = "💣 雷区 / 注意",
                tags = listOf("很重要", "需要记住"),
                importance = Importance.VERY.name,
                note = "气消了再慢慢说,冷战最伤感情",
                createdAt = now - day * 15,
            ),
            PartnerNote(
                content = "想要一双粉色的跑鞋",
                category = "🎁 愿望 / 想要的",
                tags = listOf("礼物灵感"),
                importance = Importance.IMPORTANT.name,
                note = "生日或者纪念日可以考虑",
                createdAt = now - day * 10,
            ),
            PartnerNote(
                content = "慢热,但在熟人面前话很多",
                category = "🧠 性格特点",
                tags = listOf("我观察到的"),
                importance = Importance.NORMAL.name,
                createdAt = now - day * 5,
            ),
            PartnerNote(
                content = "早上必须喝一杯温水,不然一整天不舒服",
                category = "🛌 生活习惯",
                tags = listOf("需要记住", "最近刚知道"),
                importance = Importance.NORMAL.name,
                note = "早起先烧水,顺手的事",
                createdAt = now - day * 2,
            ),
        )
        demoNotes.forEach { dao.insertPartnerNote(it) }
        Log.d(TAG, "关于TA的演示备忘已放入小宇宙")
    } catch (e: Exception) {
        Log.e(TAG, "关于TA备忘播种失败", e)
    }
}
