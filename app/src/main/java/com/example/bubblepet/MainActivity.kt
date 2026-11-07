package com.example.bubblepet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Switch

class MainActivity : Activity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val prefs = getSharedPreferences("pet", MODE_PRIVATE)
  val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 44, 32, 24) }
  fun heading(t: String) { root.addView(TextView(this).apply { text = t; textSize = 19f }) }
  heading("🐱 Bubble Pet 0.3")
  fun toggle(label: String, key: String, initial: Boolean = false) {
   root.addView(Switch(this).apply {
    text = label; isChecked = prefs.getBoolean(key, initial)
    setOnCheckedChangeListener { _, value -> prefs.edit().putBoolean(key, value).apply(); PetOverlay.sync(this@MainActivity) }
   })
  }
  toggle("상시 동반형 (끄면 알림 등장형)", "always")
  toggle("오른쪽에서 등장 (끄면 왼쪽)", "right", true)
  fun slider(label: String, key: String, min: Int, max: Int, default: Int, step: Int) {
   val caption = TextView(this).apply { text = "$label: ${prefs.getInt(key, default)}" }
   root.addView(caption)
   root.addView(SeekBar(this).apply {
    this.max = (max - min) / step; progress = (prefs.getInt(key, default) - min) / step
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
     override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
      if (!fromUser) return
      val actual = min + value * step
      caption.text = "$label: $actual"
      prefs.edit().putInt(key, actual).apply(); PetOverlay.sync(this@MainActivity)
     }
     override fun onStartTrackingTouch(bar: SeekBar?) {}
     override fun onStopTrackingTouch(bar: SeekBar?) {}
    })
   })
  }
  slider("캐릭터 크기 (%)", "size", 60, 160, 100, 10)
  slider("투명도 (%)", "opacity", 30, 100, 100, 10)
  slider("말풍선 시간 (ms)", "duration", 1500, 10500, 4500, 1000)
  root.addView(Button(this).apply { text = "1. 다른 앱 위에 표시 허용"; setOnClickListener { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) } })
  root.addView(Button(this).apply { text = "2. 알림 접근 허용"; setOnClickListener { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) } })
  root.addView(Button(this).apply { text = "미리보기 테스트"; setOnClickListener { PetOverlay.show(this@MainActivity, "안녕하세요! 카톡이 왔어요 🐾") } })
  root.addView(Button(this).apply { text = "캐릭터 숨기기"; setOnClickListener { PetOverlay.stop() } })
  root.addView(TextView(this).apply { text = "알림 내용은 기기에만 표시됩니다. 다른 사람이 화면을 볼 때 메시지가 노출될 수 있습니다." })
  setContentView(root)
 }
 override fun onResume() { super.onResume(); PetOverlay.sync(this) }
}