package com.raju.pratilipi_fm.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.*
import java.util.concurrent.TimeUnit

class BitLevelView : View {
    val ctx: Context
    private var amplitudes: MutableList<Float> = ArrayList() // amplitudes for line lengths
    private var isRecording = false
    private var startTime = 0
    private var start = 0
    private var secsPerMark = 0
    private var longMarksPerScreen = 0
    var widths = 0
    var heights = 0
    private var midWidth = 0f
    private var startHeight = 0f
    private var centralHeight = 0f
    private var lineWidth = 0f
    private var timeIntervalWidth = 0f
    private var startGrid = 0f
    private var timeScaleWidth = 0f
    private var boxRect: Rect? = null
    private var linePaint: Paint? = null
    private var lineRedPaint: Paint? = null
    private var boxPaint: Paint? = null
    private var shortMarkPaint: Paint? = null
    private var longMarkPaint: Paint? = null
    private var horLinePaint: Paint? = null
    private var horLinePath: Path? = null
    private var numPaint: Paint? = null

    // constructor
    constructor(context: Context) : super(context) // call superclass constructor
    {
        this.ctx = context
    }

    // constructor
    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs
    ) // call superclass constructor
    {
        this.ctx = context
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setup()
    }

    // Initial setup of the view.
    private fun setup() {
        val scale = resources.displayMetrics.density

        // Lines of amplitudes.
        lineWidth = convertDpToPixel(LINE_WIDTH_DP.toFloat()).toFloat()
        linePaint = Paint()
        linePaint!!.color = Color.parseColor("#2DA9E0")
        linePaint!!.strokeWidth = lineWidth

        // Red line indicating the current position.
        lineRedPaint = Paint()
        lineRedPaint!!.color = Color.parseColor("#E00707")
        lineRedPaint!!.strokeWidth = lineWidth

        // Surrounding box.
        boxPaint = Paint()
        boxPaint!!.color = Color.parseColor("#F8F8F8")
        boxPaint!!.style = Paint.Style.FILL

        // Grid.
        startGrid = 0f // the grid is drawn starting from x = 0
        timeIntervalWidth = convertDpToPixel(TIME_INTERVAL_WIDTH_DP.toFloat()).toFloat()

        // Short time marks.
        shortMarkPaint = Paint()
        shortMarkPaint!!.color = Color.parseColor("#999999")
        shortMarkPaint!!.strokeWidth = lineWidth

        // Long time marks.
        longMarkPaint = Paint()
        longMarkPaint!!.color = Color.parseColor("#666666")
        longMarkPaint!!.strokeWidth = lineWidth

        // Horizontal dotted line at the center of the view.
        horLinePath = Path()
        horLinePaint = Paint()
        horLinePaint!!.color = Color.parseColor("#2DA9E0")
        horLinePaint!!.strokeWidth = lineWidth
        horLinePaint!!.style = Paint.Style.STROKE
        horLinePaint!!.pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0f)

        // Numbers of the grid.
        secsPerMark =
            (TIME_INTERVAL_WIDTH_DP * 4 / (LINE_WIDTH_DP * LINES_SPACE) * UPDATE_FREQ).toInt() // seconds for each mark in the time scale (small marks)
        numPaint = Paint()
        numPaint!!.textSize = 8 * scale
        numPaint!!.color = Color.parseColor("#2DA9E0")
        numPaint!!.isAntiAlias = true
        numPaint!!.textAlign = Paint.Align.CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldWidth: Int, oldHeight: Int) {
        amplitudes = ArrayList((width / lineWidth * LINES_SPACE).toInt())
        widths = w
        heights = h
        startHeight =
            convertDpToPixel(TOP_PADDING.toFloat()).toFloat() // padding at the top to show times
        midWidth = (width / 2).toFloat()
        centralHeight = height * 0.6f // amplitudes are centered at this height
        longMarksPerScreen = (width / (timeIntervalWidth * 4)).toInt()
        timeScaleWidth =
            timeIntervalWidth * 4 * longMarksPerScreen * 2 // we draw a time scale 2 times the width of the screen and then move it to the left as times goes by
        boxRect = Rect(0, startHeight.toInt(), width, height)
    }

    // Start and stop recording.
    fun startRecording(secondsElapsed: Int) {
        setup()
        amplitudes.clear()
        startTime = secondsElapsed
        start = startTime
        isRecording = true
        invalidate()
    }

    fun stopRecording() {
        setup()
        amplitudes.clear()
        startTime = 0
        start = startTime
        isRecording = false
        invalidate()
    }

    // Add the given amplitude to the amplitudes ArrayList.
    fun addAmplitude(amplitude: Float) {
        var amplitude = amplitude
        amplitude =
            (amplitude * 1.2).toFloat()
                .coerceAtMost(MAX_AMPLITUDE.toFloat()) // increase sensitivity
        amplitude =
            if (amplitude < MIN_AMPLITUDE) MIN_AMPLITUDE.toFloat() else amplitude + MIN_AMPLITUDE
        amplitudes.add(amplitude)
        updateAmplitudesAndGrid()
        invalidate()
    }

    private fun updateAmplitudesAndGrid() {
        if (amplitudes.size * (lineWidth * LINES_SPACE) >= midWidth) { // red lines reaches half view
            amplitudes.removeAt(0) // remove oldest amplitude
            // Start moving the grid left by 1 line.
            startGrid -= lineWidth * LINES_SPACE
            if (startGrid <= -timeScaleWidth) {
                startGrid = 0f
                start += longMarksPerScreen * 2 * secsPerMark
            }
        }
    }

    // Draw the visualizer with scaled lines representing the amplitudes.
    public override fun onDraw(canvas: Canvas) {
        drawBox(canvas)
        if (isRecording) drawAmplitudes(canvas)
    }

    // Draw the the box.
    private fun drawBox(canvas: Canvas) {
        // Draw the surrounding box.
        canvas.drawRect(boxRect!!, boxPaint!!)

        // Draw the time scale.
        var count = 0
        var time: Int
        var x = startGrid
        while (x < timeScaleWidth) {
            if (count % 5 == 0) { // long mark
                canvas.drawLine(x, startHeight, x, (height / 5).toFloat(), shortMarkPaint!!)
                time = start + count / 5 * secsPerMark
                if (count > 0 || start > startTime) canvas.drawText(
                    formatDuration(time.toLong()),
                    x,
                    startHeight - convertDpToPixel(4f),
                    numPaint!!
                )
            } else { // short mark
                canvas.drawLine(x, startHeight, x, (height / 7).toFloat(), longMarkPaint!!)
            }
            x += timeIntervalWidth
            count++
        }

        // Draw dashed line at the middle.
        horLinePath!!.moveTo(0f, centralHeight)
        horLinePath!!.quadTo((width / 2).toFloat(), centralHeight, width.toFloat(), centralHeight)
        canvas.drawPath(horLinePath!!, horLinePaint!!)
    }

    private fun drawAmplitudes(canvas: Canvas) {
        // Draw the amplitudes.
        var curX = 0f // the x position where to draw the lines
        for (amplitude in amplitudes) {
            val scaledHeight = (amplitude / MAX_AMPLITUDE * centralHeight * 0.55).toFloat()
            curX += lineWidth * LINES_SPACE
            canvas.drawLine(
                curX, centralHeight + scaledHeight, curX, centralHeight
                        - scaledHeight, linePaint!!
            )
        }

        // Draw the red line at the end marking the current time.
        if (amplitudes.size > 0) {
            curX += lineWidth * LINES_SPACE
            canvas.drawLine(curX, startHeight, curX, height.toFloat(), lineRedPaint!!)
        }
    }

    // Utility functions.
    private fun convertDpToPixel(dp: Float): Int {
        return (dp * context.resources.displayMetrics.density + 0.5f).toInt()
    }

    companion object {
        private const val TAG = "AUDIO_RECORDER"
        private const val LINE_WIDTH_DP = 1
        private const val TIME_INTERVAL_WIDTH_DP = 25
        private const val LINES_SPACE = 2 // space between 2 consecutive lines
        private const val UPDATE_FREQ = 0.1f
        private const val MAX_AMPLITUDE = 32767
        private const val MIN_AMPLITUDE = 1500
        private const val TOP_PADDING = 12

        // Format duration (hh:mm:ss).
        private fun formatDuration(duration: Long): String {
            return if (TimeUnit.SECONDS.toHours(duration) > 0) String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(duration),
                TimeUnit.SECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(
                    1
                ),
                TimeUnit.SECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(
                    1
                )
            ) else String.format(
                Locale.getDefault(),
                "%02d:%02d",
                TimeUnit.SECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(
                    1
                ),
                TimeUnit.SECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(
                    1
                )
            )
        }
    }
}