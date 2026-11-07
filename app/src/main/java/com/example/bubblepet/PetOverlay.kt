package com.example.bubblepet
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.animation.ObjectAnimator

object PetOverlay {
 private val handler = Handler(Looper.getMainLooper())
 private var root: LinearLayout? = null
 private var manager: WindowManager? = null
 private var expiry: Runnable? = null
 private var generation = 0
 private fun prefs(c: Context) = c.getSharedPreferences("pet", Context.MODE_PRIVATE)
 private fun dp(c: Context, n: Int) = (n * c.resources.displayMetrics.density).toInt()
 private fun remove() {
  expiry?.let(handler::removeCallbacks); expiry = null
  root?.let { try { manager?.removeView(it) } catch (_: Exception) {} }
  root = null; manager = null
 }
 fun stop() { handler.post { generation++; remove() } }
 fun sync(context: Context) { val app = context.applicationContext; handler.post {
  generation++
  if (!Settings.canDrawOverlays(app)) { remove(); return@post }
  if (prefs(app).getBoolean("always", false)) render(app, null) else remove()
 } }
 fun show(context: Context, message: String) { val app = context.applicationContext; handler.post {
  if (Settings.canDrawOverlays(app)) { generation++; render(app, message.take(160)) }
 } }
 private fun render(c: Context, message: String?) {
  remove()
  val settings = prefs(c)
  val scale = settings.getInt("size", 100).coerceIn(60, 160) / 100f
  val petSize = dp(c, (65 * scale).toInt())
  val sideRight = settings.getBoolean("right", true)
  val panel = LinearLayout(c).apply { orientation = LinearLayout.VERTICAL; gravity = if (sideRight) Gravity.END else Gravity.START }
  if (message != null) {
   panel.addView(TextView(c).apply {
    text = message; textSize = 15f; setTextColor(Color.rgb(52, 42, 55))
    setPadding(dp(c, 14), dp(c, 10), dp(c, 14), dp(c, 10))
    background = GradientDrawable().apply { setColor(Color.rgb(255, 245, 250)); cornerRadius = dp(c, 18).toFloat(); setStroke(dp(c, 1), Color.rgb(255, 182, 207)) }
    maxWidth = (c.resources.displayMetrics.widthPixels * .72f).toInt()
   })
  }
  panel.addView(TextView(c).apply {
   text = "🐱"; textSize = 42f * scale; gravity = Gravity.CENTER
   layoutParams = LinearLayout.LayoutParams(petSize, petSize)
   contentDescription = "Bubble Pet 고양이: 드래그하여 이동"
  })
  val window = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
  val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
  val lp = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
   WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, flags, PixelFormat.TRANSLUCENT).apply {
   gravity = (if (sideRight) Gravity.RIGHT else Gravity.LEFT) or Gravity.CENTER_VERTICAL
   x = settings.getInt("offsetX", 0); y = settings.getInt("offsetY", 0)
   alpha = settings.getInt("opacity", 100).coerceIn(30, 100) / 100f
  }
  var downRawX = 0f; var downRawY = 0f; var originX = 0; var originY = 0
  panel.setOnTouchListener { _: View, event: MotionEvent ->
   when (event.actionMasked) {
    MotionEvent.ACTION_DOWN -> { downRawX = event.rawX; downRawY = event.rawY; originX = lp.x; originY = lp.y; true }
    MotionEvent.ACTION_MOVE -> {
     val dx = (event.rawX - downRawX).toInt()
     lp.x = (originX + if (sideRight) -dx else dx).coerceAtLeast(0)
     lp.y = originY + (event.rawY - downRawY).toInt()
     try { window.updateViewLayout(panel, lp) } catch (_: Exception) {}
     true
    }
    MotionEvent.ACTION_UP -> { settings.edit().putInt("offsetX", lp.x).putInt("offsetY", lp.y).apply(); true }
    else -> true
   }
  }
  try { window.addView(panel, lp) } catch (_: Exception) { return }
  root = panel; manager = window
  panel.translationX = if (sideRight) dp(c, 100).toFloat() else -dp(c, 100).toFloat()
  ObjectAnimator.ofFloat(panel, "translationX", panel.translationX, 0f).apply { duration = 330; start() }
  if (message != null) {
   val current = generation
   val task = Runnable {
    if (current == generation) {
     if (prefs(c).getBoolean("always", false)) render(c, null) else remove()
    }
   }
   expiry = task
   handler.postDelayed(task, settings.getInt("duration", 4500).toLong().coerceIn(1500, 15000))
  }
 }
}