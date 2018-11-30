package za.co.riggaroo.motioncamera

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions

class MotionSensingActivity : AppCompatActivity(), MotionSensor.MotionListener {

    private lateinit var ledMotionIndicatorGpio: Gpio
    private lateinit var ledArmedIndicatorGpio: Gpio
    private lateinit var camera: CustomCamera
    private lateinit var motionImageView: ImageView
    private lateinit var buttonArmSystem: Button
    private lateinit var buttonTakePhoto: Button
    private lateinit var motionViewModel: MotionSensingViewModel
    private lateinit var motionSensor: MotionSensor
    private var armed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motion_sensing)
        setTitle(R.string.app_name)
        setupViewModel()
        setupCamera()
        setupActuators()
        setupSensors()

        setupUIElements()
    }

    private fun setupViewModel() {
        motionViewModel = ViewModelProviders.of(this).get(MotionSensingViewModel::class.java)
    }

    private fun setupSensors() {
        motionSensor = MotionSensor(this, MOTION_SENSOR_GPIO_PIN)
        lifecycle.addObserver(motionSensor)
    }


    private fun setupActuators() {
        val peripheralManagerService = PeripheralManager.getInstance()
        ledMotionIndicatorGpio = peripheralManagerService.openGpio(LED_GPIO_PIN)
        ledMotionIndicatorGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        ledArmedIndicatorGpio = peripheralManagerService.openGpio(LED_ARMED_INDICATOR_PIN)
        ledArmedIndicatorGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
    }

    override fun onDestroy() {
        super.onDestroy()
        ledArmedIndicatorGpio.close()
        ledMotionIndicatorGpio.close()
    }

    private fun setupUIElements() {
        motionImageView = findViewById(R.id.image_view_motion)

        buttonArmSystem = findViewById(R.id.button_arm_disarm)
        buttonArmSystem.setOnClickListener {
            motionViewModel.toggleSystemArmedStatus()
        }

        buttonTakePhoto = findViewById(R.id.button_take_photo)
        buttonTakePhoto.setOnClickListener {
            camera.takePicture()
        }

        motionViewModel.armed.observe(this, Observer { armed ->
            armed?.let {
                this.armed = armed
                buttonArmSystem.text = if (armed) {
                    getString(R.string.disarm_system)
                } else {
                    getString(R.string.arm_system)
                }
                ledArmedIndicatorGpio.value = armed
            }

        })
    }

    private fun setupCamera() {
        camera = CustomCamera.getInstance()
        camera.initializeCamera(this, Handler(), imageAvailableListener)
    }

    private val imageAvailableListener = object : CustomCamera.ImageCapturedListener {
        override fun onImageCaptured(bitmap: Bitmap) {
            motionImageView.setImageBitmap(bitmap)
            uploadIfFaceDetected(bitmap)
        }
    }

    private fun uploadIfFaceDetected(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val highAccuracyOpts = FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                .setContourMode(FirebaseVisionFaceDetectorOptions.NO_CONTOURS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                .build()
        FirebaseVision.getInstance()
                .getVisionFaceDetector(highAccuracyOpts)
                .detectInImage(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNullOrEmpty()) {
                        Log.v("FaceDetection", "No face detected in image")
                    } else {
                        Log.v("FaceDetection", "Face detected!")
                        // Face detected!
                        motionViewModel.uploadMotionImage(bitmap)
                    }
                }
                .addOnFailureListener {
                    Log.e("FaceDetection", "Error processing face detection: $it")
                }
    }

    override fun onMotionDetected() {
        Log.d(ACT_TAG, "onMotionDetected")

        ledMotionIndicatorGpio.value = true

        if (armed) {
            camera.takePicture()
        }
    }

    override fun onMotionStopped() {
        Log.d(ACT_TAG, "onMotionStopped")
        ledMotionIndicatorGpio.value = false
    }


    companion object {
        val LED_ARMED_INDICATOR_PIN: String = "GPIO6_IO15"
        val ACT_TAG: String = "MotionSensingActivity"
        val LED_GPIO_PIN = "GPIO6_IO14"
        val MOTION_SENSOR_GPIO_PIN = "GPIO2_IO03"
    }
}
