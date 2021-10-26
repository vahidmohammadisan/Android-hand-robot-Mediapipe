package ir.vahidmohammadisan.androidhandtracker

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.mediapipe.framework.TextureFrame
import com.google.mediapipe.solutioncore.CameraInput
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView
import com.google.mediapipe.solutions.hands.HandLandmark
import com.google.mediapipe.solutions.hands.Hands
import com.google.mediapipe.solutions.hands.HandsOptions
import com.google.mediapipe.solutions.hands.HandsResult

class MainActivity : AppCompatActivity() {

    private var hands: Hands? = null

    // Live camera demo UI and camera components.
    private var cameraInput: CameraInput? = null
    private var glSurfaceView: SolutionGlSurfaceView<HandsResult>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupStreamingModePipeline()
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
            CameraInput.CameraFacing.FRONT,
            glSurfaceView!!.width,
            glSurfaceView!!.height
        )
    }

    private fun logWristLandmark(result: HandsResult, showPixelValues: Boolean) {
        val wristLandmark = Hands.getHandLandmark(result, 0, HandLandmark.WRIST)
        val THUMB_TIP = Hands.getHandLandmark(result, 0, HandLandmark.THUMB_TIP)
        val THUMB_IP = Hands.getHandLandmark(result, 0, HandLandmark.THUMB_IP)
        val INDEX_FINGER_TIP = Hands.getHandLandmark(result, 0, HandLandmark.INDEX_FINGER_TIP)
        val INDEX_FINGER_PIP = Hands.getHandLandmark(result, 0, HandLandmark.INDEX_FINGER_PIP)
        val MIDDLE_FINGER_TIP = Hands.getHandLandmark(result, 0, HandLandmark.MIDDLE_FINGER_TIP)
        val MIDDLE_FINGER_PIP = Hands.getHandLandmark(result, 0, HandLandmark.MIDDLE_FINGER_PIP)
        val RING_FINGER_TIP = Hands.getHandLandmark(result, 0, HandLandmark.RING_FINGER_TIP)
        val RING_FINGER_PIP = Hands.getHandLandmark(result, 0, HandLandmark.RING_FINGER_PIP)
        val PINKY_TIP = Hands.getHandLandmark(result, 0, HandLandmark.PINKY_TIP)
        val PINKY_PIP = Hands.getHandLandmark(result, 0, HandLandmark.PINKY_PIP)

        if (showPixelValues) {
            val width = result.inputBitmap().width
            val height = result.inputBitmap().height
            Log.i(
                TAG, String.format(
                    "MediaPipe Hand wrist coordinates (pixel values): x=%f, y=%f",
                    wristLandmark.x * width, wristLandmark.y * height
                )
            )
        } else {
            Log.i(
                TAG, String.format(
                    "MediaPipe Hand wrist normalized coordinates (value range: [0, 1]): x=%f, y=%f",
                    wristLandmark.x, wristLandmark.y
                )
            )
            if (INDEX_FINGER_PIP.y > INDEX_FINGER_TIP.y) {
                runOnUiThread { Log.w("helloooooow", "---------->") }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        // Run the pipeline and the model inference on GPU or CPU.
        private const val RUN_ON_GPU = true
    }
}