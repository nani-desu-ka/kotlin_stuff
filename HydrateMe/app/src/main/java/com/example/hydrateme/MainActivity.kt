package com.example.hydrateme

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Layout
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.marginEnd
import com.example.hydrateme.waterfall.*
import me.itangqi.waveloadingview.WaveLoadingView
import java.awt.font.TextAttribute
import java.io.*
import kotlin.math.ceil


class MainActivity : AppCompatActivity() {
    private lateinit var waterInfo: WaterInfo
    private lateinit var waterLoadingView: WaveLoadingView
    private val achievementList: MutableList<Achievment> = mutableListOf()
    private lateinit var profile: Profile
    private val drinkList: MutableList<Drink> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        waterLoadingView = findViewById(R.id.waveLoaderView)
        waterLoadingView.progressValue = 80
        waterLoadingView.bottomTitle = String.format("%d%%", 80)
        waterLoadingView.centerTitle = ""
        waterLoadingView.topTitle = ""


        dataLoader()

        fillAchievmentList()
        fillAchievments()

        fillDrinksList()
        fillDrinksInfo()

        lvlCheck()

        findViewById<TextView>(R.id.daysInRowTextView).text = profile.dayInRow.toString()
        findViewById<TextView>(R.id.achievementAmountView).text = getString(R.string.trophy_amount, profile.completedAchievmentsIdList.size)
        findViewById<TextView>(R.id.highestScoreView).text = getString(R.string.highest_score, profile.highestScore)
        findViewById<TextView>(R.id.lvlView).text = getString(R.string.lvl_info, profile.lvl)
        findViewById<EditText>(R.id.editTextTextPersonName).setText(profile.name, TextView.BufferType.EDITABLE)
        findViewById<TextView>(R.id.waterInfoView).text = getString(R.string.water_info, getFormula(profile.sex)(profile.weight, profile.actTime), 0F)
    }

    override fun onStop() {
        super.onStop()
        dataSaver()
    }

    fun dataLoader() {
        waterInfo = if (File(this.filesDir.absolutePath + "/water_info.dat").exists()) {
            val inputStream = ObjectInputStream(FileInputStream(this.filesDir.absolutePath + "/water_info_debug.dat"))
            WaterInfo(inputStream.readObject() as DataStorage)
        } else {
            WaterInfo(DataStorage())
        }

        profile = if (File(this.filesDir.absolutePath + "/profile_info.dat").exists()) {
            val inputStream = ObjectInputStream(FileInputStream(this.filesDir.absolutePath + "/profile_info_debug.dat"))
            inputStream.readObject() as Profile
        } else {
            Profile()
        }
    }

    fun dataSaver() {
        var outputStream = ObjectOutputStream(FileOutputStream(this.filesDir.absolutePath + "/water_info_debug.dat"))
        outputStream.writeObject(waterInfo.storage)

        outputStream = ObjectOutputStream(FileOutputStream(this.filesDir.absolutePath + "/profile_info_debug.dat"))
        outputStream.writeObject(profile)
    }

    private fun fillAchievmentList() {
        var achievement = Achievment(0, "Первые шаги",
                "Есть контакт! Первый день выполнения нормы",
                50,
                false,
                0) { dayInRowRecord: Int -> dayInRowRecord >= 1 }
        achievementList.add(achievement)
        achievement = Achievment(1, "Уверенное начало",
                "3 дня подряд. Маленькими шагами к здоровому будущему.",
                150,
                false,
                0) { dayInRowRecord: Int -> dayInRowRecord >= 3 }
        achievementList.add(achievement)
        achievement = Achievment(2, "В здоровом теле",
                "10 дней без пропусков. Отличный результат!",
                200,
                false,
                0) { dayInRowRecord: Int -> dayInRowRecord >= 10 }
        achievementList.add(achievement)
        achievement = Achievment(3, "Полезная привычка",
                "40 дней без перерыва. Это уже вошло в привычку!",
                300, false,
                0) { dayInRowRecord: Int -> dayInRowRecord >= 40 }
        achievementList.add(achievement)
        achievement = Achievment(4, "Просветлённый",
                "Ого! Целых 100 дней подряд. Вот это результат!",
                1000,
                false,
                0) { dayInRowRecord: Int -> dayInRowRecord >= 100 }
        achievementList.add(achievement)
        achievement = Achievment(5, "Тот, кого нельзя не называть",
                "Целый год! Вот это выдержка. Больше для вас нет преград!",
                2500,
                false,
                0) { dayInRowRecord: Int -> dayInRowRecord >= 365 }
        achievementList.add(achievement)
        achievement = Achievment(6, "Любитель разнообразия",
                "Вы попробовали все возможные напитки!",
                500,
                true,
                0) { level: Int -> level >= 8 }
        achievementList.add(achievement)
        achievement = Achievment(7, "Постижение основ",
                "Вы достигли 3-го уровня.",
                100,
                true,
                0) { level: Int -> level >= 3 }
        achievementList.add(achievement)
        achievement = Achievment(8, "Самоучка",
                "Вы достигли 10-го уровня.",
                150,
                true,
                0) { level: Int -> level >= 10 }
        achievementList.add(achievement)
        achievement = Achievment(9, "Старательный ученик",
                "Вы достигли 20-го уровня.",
                300,
                true,
                0) { level: Int -> level >= 20 }
        achievementList.add(achievement)
        achievement = Achievment(10, "Уверенный любитель",
                "Вы достигли 30-го уровня.",
                500,
                true,
                0) { level: Int -> level >= 30 }
        achievementList.add(achievement)
        achievement = Achievment(11, "Мудрый наставник",
                "Вы достигли 50-го уровня.",
                700,
                true,
                0) { level: Int -> level >= 50 }
        achievementList.add(achievement)
        achievement = Achievment(12, "Трезвость- моё второе имя",
                "Месяц без алкоголя.",
                250,
                true,
                0) { dayInRow: Int -> dayInRow >= 30 }
        achievementList.add(achievement)
        achievement = Achievment(13, "Спокойствие на максимум",
                "Месяц без кофе.",
                250,
                true,
                0) { dayInRow: Int -> dayInRow >= 30 }
        achievementList.add(achievement)
    }

    private fun fillDrinksList() {
        var drink = Drink(0, "Вода", R.drawable.check, 1.0F)
        drinkList.add(drink)
        drink = Drink(1, "Вода с газом", R.drawable.check, 0.8F)
        drinkList.add(drink)
        drink = Drink(2, "Чай", R.drawable.check, 0.85F)
        drinkList.add(drink)
        drink = Drink(0, "Кофе", R.drawable.check, 0.6F)
        drinkList.add(drink)
        drink = Drink(0, "Алкоголь", R.drawable.check, -1.6F)
        drinkList.add(drink)
    }

    fun updateAchievments() {
        var updated = false
        val idToDelete: MutableList<Int> = mutableListOf()
        for (achievement in achievementList) {
            println(achievementList.size)
            when (achievement.id) {
                in 0 until 6 -> {
                    println("0 6")
                    val achState = achievement.isUpdated(profile.dayInRow)
                    if (achState.first) {
                        profile.completedAchievmentsIdList.add(achievement.id)
                        profile.currentExp += achievement.exp
                        Toast.makeText(this, achievement.name + "\n" + achievement.description, Toast.LENGTH_LONG).show()
                        updated = true
                    }
                }
                in 6 until 12 -> {
                    println("6 12")
                    val achState = achievement.isUpdated(profile.lvl)
                    if (achState.first) {
                        profile.completedAchievmentsIdList.add(achievement.id)
                        profile.currentExp += achievement.exp
                        Toast.makeText(this, achievement.name + "\n" + achievement.description, Toast.LENGTH_LONG).show()
                        updated = true
                    }
                }
                in 12 until 14 -> {
                    println("12 14")
                    val achState = achievement.isUpdated(profile.dayInRow)
                    if (achState.first) {
                        profile.completedAchievmentsIdList.add(achievement.id)
                        profile.currentExp += achievement.exp
                        Toast.makeText(this, achievement.name + "\n" + achievement.description, Toast.LENGTH_LONG).show()
                        updated = true
                    }
                }
            }
        }
        if (updated) {
            lvlCheck()
            clearAchievements()
            fillAchievments()
            findViewById<TextView>(R.id.achievementAmountView).text = getString(R.string.trophy_amount, profile.completedAchievmentsIdList.size)
            updateAchievments()
        }
    }

    private fun lvlCheck() {
        val expToLvlUp = { x: Int -> x * 50}
        while (expToLvlUp(profile.lvl) <= profile.currentExp) {
            profile.currentExp -= expToLvlUp(profile.lvl)
            profile.lvl += 1
        }
        findViewById<TextView>(R.id.lvlView).text = getString(R.string.lvl_info, profile.lvl)
        findViewById<TextView>(R.id.progressTextInfo).text = getString(R.string.progress_text, profile.currentExp, expToLvlUp(profile.lvl))
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.max = expToLvlUp(profile.lvl)
        progressBar.progress = profile.currentExp
    }

    private fun fillAchievments() {
        var i = 0
        for (achievement in achievementList) {
            val layout = LinearLayout(this)
            layout.setBackgroundResource(R.drawable.cross)
            val text = TextView(this)
            text.text = if (achievement.isSecret) "?" else achievement.name
            text.minLines = 2
            text.maxLines = 2
            text.ellipsize = TextUtils.TruncateAt.END
            text.gravity = Gravity.CENTER
            for (id in profile.completedAchievmentsIdList) {
                if (id == achievement.id) {
                    layout.setBackgroundResource(R.drawable.check)
                    text.text = achievement.name
                }
            }

            when (i % 3) {
                0 -> {
                    findViewById<LinearLayout>(R.id.achiv1).addView(layout)
                    findViewById<LinearLayout>(R.id.achiv1).addView(text)
                }
                1 -> {
                    findViewById<LinearLayout>(R.id.achiv2).addView(layout)
                    findViewById<LinearLayout>(R.id.achiv2).addView(text)
                }
                2 -> {
                    findViewById<LinearLayout>(R.id.achiv3).addView(layout)
                    findViewById<LinearLayout>(R.id.achiv3).addView(text)
                }
            }
            val mParams: ViewGroup.LayoutParams = layout.layoutParams as ViewGroup.LayoutParams
            mParams.height = 35.dpToPixels(this).toInt()
            mParams.width = 35.dpToPixels(this).toInt()
            layout.layoutParams = mParams
            layout.postInvalidate()

            val tParams: LinearLayout.LayoutParams = text.layoutParams as LinearLayout.LayoutParams
            tParams.setMargins(0, 0, 0, 10.dpToPixels(this).toInt())
            text.layoutParams = tParams
            text.postInvalidate()

            i += 1
        }
    }

    fun clearAchievements() {
        findViewById<LinearLayout>(R.id.achiv1).removeAllViews()
        findViewById<LinearLayout>(R.id.achiv2).removeAllViews()
        findViewById<LinearLayout>(R.id.achiv3).removeAllViews()
    }

    fun fillDrinksInfo() {
        var i = 0
        for (drink in drinkList) {
            val cardLayout = CardView(this)
            cardLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            cardLayout.radius = 5.dpToPixels(this)
            cardLayout.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_blue))
            cardLayout.elevation = 0F

            val horizontalLayout = LinearLayout(this)
            horizontalLayout.orientation = LinearLayout.HORIZONTAL

            val verticalLayout1 = LinearLayout(this)
            var vparams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
            verticalLayout1.layoutParams = vparams
            verticalLayout1.orientation = LinearLayout.VERTICAL
            verticalLayout1.gravity = Gravity.CENTER_VERTICAL

            val verticalLayout2 = LinearLayout(this)
            vparams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
            verticalLayout2.layoutParams = vparams
            verticalLayout2.orientation = LinearLayout.VERTICAL

            val iconLayout = LinearLayout(this)
            iconLayout.setBackgroundResource(drink.resourceId)

            val text = TextView(this)
            text.text = drink.name
            text.maxLines = 1
            text.isSingleLine = true
            text.ellipsize = TextUtils.TruncateAt.END

            val text2 = TextView(this)
            text2.text = "0.1"
            text2.maxLines = 1
            text2.isSingleLine = true
            text2.setTextColor(ContextCompat.getColor(this, R.color.blue_number))
            text2.textSize = 24F
            text2.setTypeface(null, Typeface.BOLD)

            verticalLayout1.addView(iconLayout)

            verticalLayout2.addView(text2)
            verticalLayout2.addView(text)

            horizontalLayout.addView(verticalLayout1)
            horizontalLayout.addView(verticalLayout2)

            cardLayout.addView(horizontalLayout)

            when (i % 2) {
                0 -> {
                    findViewById<LinearLayout>(R.id.todayDrinks1).addView(cardLayout)
                }
                1 -> {
                    findViewById<LinearLayout>(R.id.todayDrinks2).addView(cardLayout)
                }
            }
            val mParams: ViewGroup.LayoutParams = iconLayout.layoutParams as ViewGroup.LayoutParams
            mParams.height = 35.dpToPixels(this).toInt()
            mParams.width = 35.dpToPixels(this).toInt()
            iconLayout.layoutParams = mParams
            iconLayout.postInvalidate()

            val cardParams: ViewGroup.MarginLayoutParams = cardLayout.layoutParams as ViewGroup.MarginLayoutParams
            cardParams.setMargins(5.dpToPixels(this).toInt(), 5.dpToPixels(this).toInt(), 5.dpToPixels(this).toInt(), 5.dpToPixels(this).toInt())
            cardLayout.layoutParams = cardParams
            cardLayout.postInvalidate()

            val listViewParams: ViewGroup.MarginLayoutParams = verticalLayout1.layoutParams as ViewGroup.MarginLayoutParams
            listViewParams.setMargins(10.dpToPixels(this).toInt(), 5.dpToPixels(this).toInt(), 10.dpToPixels(this).toInt(), 5.dpToPixels(this).toInt())
            verticalLayout1.layoutParams = listViewParams
            verticalLayout2.layoutParams = listViewParams
            verticalLayout1.postInvalidate()
            verticalLayout2.postInvalidate()
            i += 1
        }
    }

    fun Int.dpToPixels(context: Context):Float = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,this.toFloat(),context.resources.displayMetrics
    )

    fun dayTest(view: View) {
        profile.dayInRow += 1
        findViewById<TextView>(R.id.daysInRowTextView).text = profile.dayInRow.toString()
        if (profile.dayInRow > profile.highestScore) {
            profile.highestScore = profile.dayInRow
            findViewById<TextView>(R.id.highestScoreView).text = getString(R.string.highest_score, profile.highestScore)
        }
        profile.currentExp += 50
        lvlCheck()
        updateAchievments()
    }

    fun expTest(view: View) {
        profile.currentExp += 1000
        lvlCheck()
        updateAchievments()
    }

    fun skipDay(view: View) {
        profile.dayInRow = 0
        findViewById<TextView>(R.id.daysInRowTextView).text = profile.dayInRow.toString()
    }
}