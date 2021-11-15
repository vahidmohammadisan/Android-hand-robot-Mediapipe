package ir.vahidmohammadisan.androidhandtracker

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.ahmedabdelmeged.bluetoothmc.BluetoothMC
import com.google.mediapipe.framework.TextureFrame
import com.google.mediapipe.solutioncore.CameraInput
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView
import com.google.mediapipe.solutions.hands.HandLandmark
import com.google.mediapipe.solutions.hands.Hands
import com.google.mediapipe.solutions.hands.HandsOptions
import com.google.mediapipe.solutions.hands.HandsResult
import com.ahmedabdelmeged.bluetoothmc.util.BluetoothStates

import androidx.core.app.ActivityCompat.startActivityForResult

import com.ahmedabdelmeged.bluetoothmc.ui.BluetoothDevices

import android.content.Intent
import android.app.Activity

class MainActivity : AppCompatActivity() {

    private var hands: Hands? = null
    lateinit var bluetoothmc:BluetoothMC

    // Live camera demo UI and camera components.
    private var cameraInput: CameraInput? = null
    private var glSurfaceView: SolutionGlSurfaceView<HandsResult>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothmc = BluetoothMC()

        val intent = Intent(this@MainActivity, BluetoothDevices::class.java)
        startActivityForResult(intent, BluetoothStates.REQUEST_CONNECT_DEVICE)

        setupStreamingModePipeline()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BluetoothStates.REQUEST_CONNECT_DEVICE) {
            if (resultCode == RESULT_OK) {
                bluetoothmc.connect(data)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Restarts the camera and the opengl surface rendering.
        cameraInput = CameraInput(this)
        cameraInput!!.setNewFrameListener { textureFrame: TextureFrame? ->
            hands!!.send(
                textureFrame
            )
        }
        glSurfaceView!!.post { startCamera() }
        glSurfaceView!!.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView!!.visibility = View.GONE
        cameraInput!!.close()
    }

    private fun setupStreamingModePipeline() {
        // Initializes a new MediaPipe Hands solution instance in the streaming mode.
        hands = Hands(
            this,
            HandsOptions.builder()
                .setStaticImageMode(false)
                .setMaxNumHands(1)
                .setRunOnGpu(RUN_ON_GPU)
                .build()
        )
        hands!!.setErrorListener { message: String, e: RuntimeException? ->
            Log.e(
                TAG,
                "MediaPipe Hands error:$message"
            )
        }

        cameraInput = CameraInput(this)
        cameraInput!!.setNewFrameListener { textureFrame: TextureFrame? ->
            hands!!.send(
                textureFrame
            )
        }

        // Initializes a new Gl surface view with a user-defined HandsResultGlRenderer.
        glSurfaceView = SolutionGlSurfaceView(this, hands!!.glContext, hands!!.glMajorVersion)
        glSurfaceView!!.setSolutionResultRenderer(HandsResultGlRenderer())
        glSurfaceView!!.setRenderInputImage(true)
        hands!!.setResultListener { handsResult: HandsResult ->
            logWristLandmark(handsResult,  /*showPixelValues=*/false)
            glSurfaceView!!.setRenderData(handsResult)
            glSurfaceView!!.requestRender()
        }

        // The runnable to start camera after the gl surface view is attached.
        // For video input source, videoInput.start() will be called when the video uri is available.
        glSurfaceView!!.post { startCamera() }

        // Updates the preview layout.


        // Updates the preview layout.
        val frameLayout = findViewById<FrameLayout>(R.id.preview_display_layout)
        frameLayout.removeAllViewsInLayout()
        frameLayout.addView(glSurfaceView)
        glSurfaceView!!.visibility = View.VISIBLE
        frameLayout.requestLayout()

    }

    private fun startCamera() {
        cameraInput!!.start(
            this,
            hands!!.glContext,
            CameraInput.CameraFacing.BACK,
            glSurfaceView!!.width,
            glSurfaceView!!.height
        )
    }

    private fun logWristLandmark(result: HandsResult, showPixelValues: Boolean) {

        if (result.multiHandLandmarks().isEmpty()) {
            return
        }

        val THUMB_TIP = result.multiHandLandmarks()[0].landmarkList[HandLandmark.THUMB_TIP]
        val THUMB_IP = result.multiHandLandmarks()[0].landmarkList[HandLandmark.THUMB_IP]
        val INDEX_FINGER_TIP =
            result.multiHandLandmarks()[0].landmarkList[HandLandmark.INDEX_FINGER_TIP]
        val INDEX_FINGER_PIP =
            result.multiHandLandmarks()[0].landmarkList[HandLandmark.INDEX_FINGER_PIP]

        val MIDDLE_FINGER_TIP =
            result.multiHandLandmarks()[0].landmarkList[HandLandmark.MIDDLE_FINGER_TIP]
        val MIDDLE_FINGER_PIP =
            result.multiHandLandmarks()[0].landmarkList[HandLandmark.MIDDLE_FINGER_PIP]
        val RING_FINGER_TIP =
            result.multiHandLandmarks()[0].landmarkList[HandLandmark.RING_FINGER_TIP]
        val RING_FINGER_PIP =
            result.multiHandLandmarks()[0].landmarkList[HandLandmark.RING_FINGER_PIP]
        val PINKY_TIP = result.multiHandLandmarks()[0].landmarkList[HandLandmark.PINKY_TIP]
        val PINKY_PIP = result.multiHandLandmarks()[0].landmarkList[HandLandmark.PINKY_PIP]

        if (showPixelValues) {
            val width = result.inputBitmap().width
            val height = result.inputBitmap().height
        } else {

            if (THUMB_IP.y > THUMB_TIP.y) {
                runOnUiThread { Log.w("THUMB is: ", "open") }
                bluetoothmc.send("A");
            } else {
                runOnUiThread { Log.w("THUMB is: ", "close") }
                bluetoothmc.send("a");
            }

            if (INDEX_FINGER_PIP.y > INDEX_FINGER_TIP.y) {
                runOnUiThread { Log.w("INDEX_FINGER is: ", "open") }
                bluetoothmc.send("V");
            } else {
                runOnUiThread { Log.w("INDEX_FINGER is: ", "close") }
                bluetoothmc.send("v");
            }

            if (MIDDLE_FINGER_PIP.y > MIDDLE_FINGER_TIP.y) {
                runOnUiThread { Log.w("MIDDLE_FINGER is: ", "open") }
                bluetoothmc.send("C");
            } else {
                runOnUiThread { Log.w("MIDDLE_FINGER is: ", "close") }
                bluetoothmc.send("c");
            }

            if (RING_FINGER_PIP.y > RING_FINGER_TIP.y) {
                runOnUiThread { Log.w("RING_FINGER is: ", "open") }
                bluetoothmc.send("D");
            } else {
                runOnUiThread { Log.w("RING_FINGER is: ", "close") }
                bluetoothmc.send("d");
            }

            if (PINKY_PIP.y > PINKY_TIP.y) {
                runOnUiThread { Log.w("PINKY is: ", "open") }
                bluetoothmc.send("F");
            } else {
                runOnUiThread { Log.w("PINKY is: ", "close") }
                bluetoothmc.send("f");
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        // Run the pipeline and the model inference on GPU or CPU.
        private const val RUN_ON_GPU = true
    }
}