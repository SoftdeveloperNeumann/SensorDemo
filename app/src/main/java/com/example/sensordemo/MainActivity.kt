package com.example.sensordemo

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewGroupCompat.setLayoutMode
import com.example.sensordemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var manager: SensorManager
    lateinit var listener: SensorEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        manager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val sensores = manager.getSensorList(Sensor.TYPE_ALL)

        sensores.forEach { sensor ->
            var isDynamic = false

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                isDynamic = sensor.isDynamicSensor
            }
            binding.tvOutput
                .append("Name: ${sensor.name}, Hersteller: ${sensor.vendor}, Version: ${sensor.version}, ist dynamisch: $isDynamic\n")
        }
    }

    override fun onResume() {
        super.onResume()
        val sensor_prox = manager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        val sensor_light = manager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if(sensor_prox == null){
            Toast.makeText(this, "Sensor nicht vorhanden", Toast.LENGTH_SHORT).show()
        }
        if (sensor_light == null) {
            Toast.makeText(this, "Helligkeitssensor nicht vorhanden", Toast.LENGTH_SHORT).show()
        }

        listener = object : SensorEventListener{
            override fun onSensorChanged(event: SensorEvent?) {
               when(event!!.sensor.type){
                   Sensor.TYPE_PROXIMITY ->{
                       if(event.values[0] < sensor_prox.maximumRange){
                           binding.tvOutput.setBackgroundColor(Color.RED)
                       }else{
                           binding.tvOutput.setBackgroundColor(Color.WHITE)
                       }
                   }
                   Sensor.TYPE_LIGHT ->{
                       val light = event.values[0]
                       if(light < 5000){
                           Toast.makeText(
                               this@MainActivity, "es soll in den Nachtmodus geschaltet werden", Toast.LENGTH_SHORT)
                               .show()
                           setMyLayoutMode(true)
                       }else{
                           setMyLayoutMode(false)
                       }
                   }
               }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }
        }

        manager.registerListener(listener,sensor_prox,SensorManager.SENSOR_DELAY_NORMAL)
        manager.registerListener(listener,sensor_light,SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun setMyLayoutMode(isNight: Boolean) {
        if(isNight){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    override fun onPause() {
        super.onPause()
        manager.unregisterListener(listener)
    }
}