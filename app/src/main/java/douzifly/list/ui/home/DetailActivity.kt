package douzifly.list.ui.home

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.github.clans.fab.FloatingActionButton
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.nineoldandroids.animation.ObjectAnimator
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import douzifly.list.R
import douzifly.list.model.Thing
import douzifly.list.model.ThingGroup
import douzifly.list.model.ThingsManager
import douzifly.list.settings.Settings
import douzifly.list.utils.*
import douzifly.list.widget.ColorPicker
import douzifly.list.widget.FontSizeBar
import java.util.*

/**
 * Created by douzifly on 12/14/15.
 */
class DetailActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        reminderDate = Date(year - 1900, monthOfYear, dayOfMonth)
        showTimePicker()
    }

    override fun onTimeSet(view: RadialPickerLayout?, hourOfDay: Int, minute: Int, second: Int) {
        "${hourOfDay} : ${minute}".logd("oooo")
        reminderDate?.hours = hourOfDay
        reminderDate?.minutes = minute
        initDate = reminderDate
        updateTimeUI(reminderDate)
    }

    fun updateTimeUI(date: Date?) {
        if (date == null) {
            txtReminder.text = ""
            delReminder.visibility = View.GONE
        } else {
            delReminder.visibility = View.VISIBLE
            txtReminder.text = formatDateTime(date)
            formatTextViewcolor(txtReminder, date)
        }
    }


    companion object {
        val EXTRA_THING_ID = "thing_id"
        private val TAG = "DetailActivity"
        val RESULT_DELETE = 2
        val RESULT_UPDATE = 3
        val REQ_CHANGE_GROUP = 100
    }

    var reminderDate: Date? = null
    var thing: Thing? = null
    var initDate: Date? = null

    val actionDone: FloatingActionButton by lazy {
        val v = findViewById(R.id.action_done) as FloatingActionButton
        v.setImageDrawable(
                GoogleMaterial.Icon.gmd_done.colorResOf(R.color.redPrimary)
        )
        v
    }

    val actionDelete: FloatingActionButton by lazy {
        val v = findViewById(R.id.action_delete) as FloatingActionButton
        v.setImageDrawable(
                GoogleMaterial.Icon.gmd_delete.colorOf(Color.WHITE)
        )
        v
    }

    val txtTitle: TextView by lazy {
        findViewById(R.id.txt_title) as TextView
    }

    val editTitle: EditText by lazy {
        findViewById(R.id.edit_title) as EditText
    }

    val editContent: EditText by lazy {
        findViewById(R.id.txt_content) as EditText
    }

    val addReminder: FloatingActionButton by lazy {
        findViewById(R.id.fab_add_reminder) as FloatingActionButton
    }

    val txtReminder: TextView by lazy {
        findViewById(R.id.txt_reminder) as TextView
    }

    val colorPicker: ColorPicker by lazy {
        findViewById(R.id.color_picker) as ColorPicker
    }

    val txtGroup : TextView by lazy {
        findViewById(R.id.txt_group) as TextView
    }

    val delReminder: View by lazy {
        val v = findViewById(R.id.reminder_del)
        v.setOnClickListener {
            cancelPickTime()
        }
        v
    }

    val focusChangeListener = View.OnFocusChangeListener {
        v, hasFocus ->
        if (!hasFocus) {
            when (v) {
                editTitle -> {
                    setTitleEditMode(false)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_activity)
        initView()
        parseIntent()

        val alphaAnim = ObjectAnimator.ofFloat(editContent, "alpha", 0.0f, 1.0f)
        alphaAnim.setDuration(500)
        alphaAnim.start()


        addReminder.setImageDrawable(
                GoogleMaterial.Icon.gmd_alarm.colorResOf(R.color.greyPrimary)
        )

        addReminder.setOnClickListener {
            showDatePicker()
        }

        txtReminder.typeface = fontRailway

        loadData()
    }

    fun finishCompact() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition()
        } else {
            finish()
        }
    }

    fun cancelPickTime() {
        reminderDate = null
        updateTimeUI(reminderDate)
    }

    fun showDatePicker() {
        val selectedDate = Calendar.getInstance();
        if (initDate != null) {
            selectedDate.time = initDate
        }
        val dpd = DatePickerDialog.newInstance(
                this,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        if (initDate != null) {
        }

        dpd.accentColor = colorPicker.selectedColor

        ui {
            editTitle.hideKeyboard()
            editContent.hideKeyboard()
        }
        dpd.show((this as AppCompatActivity).getFragmentManager(), "Datepickerdialog");
    }

    fun showTimePicker() {
        val selectTime = Calendar.getInstance();
        if (initDate != null) {
            selectTime.time = initDate
        }
        val dpd = TimePickerDialog.newInstance(
                this,
                selectTime.get(Calendar.HOUR_OF_DAY),
                selectTime.get(Calendar.MINUTE),
                true)
        dpd.accentColor = colorPicker.selectedColor

        dpd.show((this as AppCompatActivity).getFragmentManager(), "Timepickerdialog");
    }


    fun parseIntent() {
        val id = intent?.getLongExtra(EXTRA_THING_ID, 0) ?: 0
        if (id > 0) {
            thing = ThingsManager.getThingByIdAtCurrentGroup(id)
            initDate =  if (thing!!.reminderTime > 0) Date(thing!!.reminderTime) else null
            updateGroupText(thing!!.group!!)
        }
    }

    var selectedGroup: ThingGroup? = null

    fun updateGroupText(group: ThingGroup) {
        selectedGroup = group
        txtGroup.text = selectedGroup?.title ?: "Unknown"
    }

    fun loadData() {
        if (thing == null) return
        txtTitle.text = thing!!.title
        editTitle.setText(thing!!.title)
        editContent.setText(thing!!.content)
        editContent.setBackgroundColor(0x0000)

        txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, FontSizeBar.fontSizeToDp(Settings.fontSize) + 2)
        editTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, FontSizeBar.fontSizeToDp(Settings.fontSize) + 2)
        editContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, FontSizeBar.fontSizeToDp(Settings.fontSize))
        txtGroup.setTextSize(TypedValue.COMPLEX_UNIT_PX, FontSizeBar.fontSizeToDp(Settings.fontSize))

        ui(200) {
            colorPicker.setSelected(thing!!.color)
        }

        updateTimeUI(
                if (thing!!.reminderTime > 0) Date(thing!!.reminderTime)
                else null
        )

        if (thing!!.reminderTime > 0) {
            reminderDate = Date(thing!!.reminderTime)
        }
    }

    fun saveData() {

        editTitle.hideKeyboard()
        editContent.hideKeyboard()


        var changed = false
        val newTitle = editTitle.text.toString()
        val newContent = editContent.text.toString()
        val newColor = colorPicker.selectedColor
        val newReminderTime = reminderDate?.time ?: 0

        if (thing!!.title != newTitle) {
            thing!!.title = newTitle
            changed = true
        }
        if (thing!!.content != newContent) {
            thing!!.content = newContent
            changed = true
        }

        if (thing!!.color != newColor) {
            thing!!.color = newColor
            ThingsManager.getGroupByGroupId(Settings.selectedGroupId)?.clearAllDisplayColor()
            changed = true
        }

        if (thing!!.reminderTime != newReminderTime) {
            thing!!.reminderTime = newReminderTime
            changed = true
        }

        if (selectedGroup != null) {
            changed = true
        }

        if (changed) {
            bg {
                setResult(RESULT_UPDATE)
                ThingsManager.saveThing(thing!!, selectedGroup)
            }
        }
    }

    fun initView() {

        txtTitle.typeface = fontSourceSansPro
        txtGroup.typeface = fontSourceSansPro
        editContent.typeface = fontSourceSansPro
        editTitle.typeface = fontSourceSansPro

        editTitle.visibility = View.GONE


        actionDelete.setOnClickListener {
            val intent = Intent()
            intent.putExtra(EXTRA_THING_ID, thing!!.id)
            setResult(RESULT_DELETE, intent)
            finishCompact()
        }

        actionDone.setOnClickListener {
            bg {
                saveData()
                ui {
                    finishCompact()
                }
            }
        }

        txtTitle.setOnClickListener(onClickListener)

        editTitle.onFocusChangeListener = focusChangeListener
        editContent.isFocusable = false
        editContent.isFocusableInTouchMode = false
        editContent.setOnClickListener {
            v ->
            editContentRequestFocus()
        }

        editTitle.setOnEditorActionListener {
            textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                editContentRequestFocus()
                true
            }
            false
        };

        txtGroup.setOnClickListener {
            showGroupChoose()
        }
    }

    fun showGroupChoose() {
        ui {
            editTitle.hideKeyboard()
            editContent.hideKeyboard()
        }
        val intent = Intent(this, GroupEditorActivity::class.java)
        intent.putExtra(GroupEditorActivity.EXTRA_KEY_SHOW_ALL, false)
        startActivityForResult(intent, REQ_CHANGE_GROUP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CHANGE_GROUP && resultCode == RESULT_OK) {
            val id = data!!.getLongExtra("id", -1L)
            if (id != thing!!.group!!.id) {
                // groupId changed
                selectedGroup = ThingsManager.getGroupByGroupId(id)
                "group changed to : ${selectedGroup?.title ?: "not changed"}".logd(TAG)
                updateGroupText(selectedGroup!!)
            }
        }
    }

    private fun editContentRequestFocus() {
        editContent.isFocusable = true
        editContent.isFocusableInTouchMode = true
        editContent.requestFocus()
        ui(100) {
            editContent.showKeyboard()
        }
    }

    fun setTitleEditMode(editMode: Boolean) {
        if (editMode) {
            // show edittext
            txtTitle.visibility = View.GONE
            editTitle.setText(txtTitle.text)
            editTitle.visibility = View.VISIBLE
            editTitle.requestFocus()
            editTitle.showKeyboard()
        } else {
            txtTitle.visibility = View.VISIBLE
            editTitle.visibility = View.GONE
            txtTitle.setText(editTitle.text)
        }
    }


    val onClickListener: (v: View) -> Unit = {
        v ->
        if (v == txtTitle) {
            setTitleEditMode(true)
        }
    }

}