package za.co.riggaroo.motioncamera

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManager

class MotionSensor(private val motionListener: MotionListener,
                   motionSensorPinNumber: String) : LifecycleObserver {

    private val motionSensorGpioPin: Gpio = PeripheralManager.getInstance().openGpio(motionSensorPinNumber)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun start() {
        val portList: List<String> = PeripheralManager.getInstance().gpioList
        if (portList.isEmpty()) {
            Log.v("MotionSensor", "No GPIO port available on this device.")
        } else {
            Log.v("MotionSensor", "List of available ports: $portList")
        }
        //Receive data from the sensor - DIRECTION_IN
        motionSensorGpioPin.setDirection(Gpio.DIRECTION_IN)
        //High voltage means movement has been detected
        motionSensorGpioPin.setActiveType(Gpio.ACTIVE_HIGH)
        //The trigger we want to receive both low and high triggers so EDGE_BOTH
        motionSensorGpioPin.setEdgeTriggerType(Gpio.EDGE_RISING)
        motionSensorGpioPin.registerGpioCallback(gpioCallback)
    }

    private val gpioCallback = object : GpioCallback {
        override fun onGpioEdge(gpio: Gpio): Boolean {
            if (gpio.value) {
                motionListener.onMotionDetected()
            } else {
                motionListener.onMotionStopped()
            }

            // Continue listening for more interrupts
            return true
        }

        override fun onGpioError(gpio: Gpio, error: Int) {
            Log.w("MotionSensor", "$gpio: Error event $error")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun stop() {
        motionSensorGpioPin.unregisterGpioCallback(gpioCallback)
        motionSensorGpioPin.close()
    }

    interface MotionListener {
        fun onMotionDetected()
        fun onMotionStopped()
    }
}
