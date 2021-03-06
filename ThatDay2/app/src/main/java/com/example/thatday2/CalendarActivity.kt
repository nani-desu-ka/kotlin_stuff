package com.example.thatday2

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.applandeo.materialcalendarview.CalendarView
//import com.haibin.calendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.example.thatday2.Processor.DataStorage
import com.example.thatday2.Processor.PeriodsInfo
import kotlinx.android.synthetic.main.activity_calendar.*
import java.io.*
import java.util.*


class CalendarActivity : AppCompatActivity() {
    private lateinit var periodsInfo: PeriodsInfo
    private lateinit var calendarView: CalendarView
    private lateinit var mCalendarView: CalendarView
    private var averageCycleDuration = 0
    private var averagePeriodsDuration = 0

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        calendarView = findViewById(R.id.periodCalendar)

        periodLoader()
        showPeriodInfo()
        calculateNextPeriodDays()

        calendarView.setOnDayClickListener { eventDay ->
            val clickedDayCalendar: Calendar = eventDay.calendar
            val time = clickedDayCalendar.timeInMillis
            if (time <= Calendar.getInstance().timeInMillis) {
                periodsInfo.addOrRemoveDate(time)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val outputStream = ObjectOutputStream(FileOutputStream(this.filesDir.absolutePath + "/per_info.dat"))
        outputStream.writeObject(periodsInfo.savedData)
    }

    fun graphTransit(view: View) {
        val graphIntent = Intent(this, GraphActivity::class.java)
        startActivity(graphIntent)
    }

    private fun periodLoader() {
        periodsInfo = if (File(this.filesDir.absolutePath + "/per_info.dat").exists()) {
            val inputStream = ObjectInputStream(FileInputStream(this.filesDir.absolutePath + "/per_info.dat"))
            PeriodsInfo(inputStream.readObject() as DataStorage)
        } else {
            PeriodsInfo(DataStorage())
        }
    }

    private fun showPeriodInfo() {
        periodsInfo.updateStat()
//        Toast.makeText(this@CalendarActivity, periodsInfo.savedData.periodsDurations.size.toString(), Toast.LENGTH_SHORT).show()
        averageCycleDuration = periodsInfo.averageCycleDuration
        averagePeriodsDuration = periodsInfo.averagePeriodsDuration
        cycle_medium_number.text = averageCycleDuration.toString()
        period_medium_number.text = averagePeriodsDuration.toString()

        val selectedDates = mutableListOf<Calendar>()
        for (date in periodsInfo.getPeriodDays()) {
            selectedDates.add(Calendar.getInstance())
            selectedDates.last().timeInMillis = date
        }
        calendarView.selectedDates = selectedDates
    }

    private fun testEvents() {
        val events: MutableList<EventDay> = mutableListOf()
//        events.add(EventDay(clickedDayCalendar, R.drawable.blood_icon))
//        events.add(EventDay(clickedDayCalendar, R.drawable.ovul_icon))
        calendarView.setEvents(events)
    }
    
    private fun calculateNextPeriodDays() {
        val events: MutableList<EventDay> = mutableListOf()
        val selectedDates = mutableListOf<Calendar>()
        if (periodsInfo.getLatestPeriodsFirstDay() > 0) {
            val lastPeriods = periodsInfo.getLatestPeriodsFirstDay()
            for (i in 1..10) {
                for (j in 0 until averagePeriodsDuration) {
                    selectedDates.add(Calendar.getInstance())
                    selectedDates.last().timeInMillis = lastPeriods
                    selectedDates.last().set(Calendar.DAY_OF_MONTH, selectedDates.last().get(Calendar.DATE) + averageCycleDuration * i + j)
                    events.add(EventDay(selectedDates.last(), R.drawable.blood_icon))
                }
            }
            calendarView.setEvents(events)
        }
    }
}
