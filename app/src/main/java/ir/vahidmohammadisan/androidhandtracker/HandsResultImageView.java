package ir.vahidmohammadisan.androidhandtracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import androidx.appcompat.widget.AppCompatImageView;

import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutions.hands.HandsResult;

import java.util.List;

public class HandsResultImageView extends AppCompatImageView {

    private static final int LANDMARK_COLOR = Color.RED;
    private static final int LANDMARK_RADIUS = 15;
    private static final int CONNECTION_COLOR = Color.GREEN;
    private static final int CONNECTION_THICKNESS = 10;
    private Bitmap latest;

    public HandsResultImageView(Context context) {
        super(context);
        setScaleType(ScaleType.FIT_CENTER);
    }

    public void setHandsResult(HandsResult result) {
        if (result == null) {
            return;
        }
        Bitmap bmInput = result.inputBitmap();
        int width = bmInput.getWidth();
        int height = bmInput.getHeight();
        latest = Bitmap.createBitmap(width, height, bmInput.getConfig());
        Canvas canvas = new Canvas(latest);

        canvas.drawBitmap(bmInput, new Matrix(), null);
        int numHands = result.multiHandLandmarks().size();
        for (int i = 0; i < numHands; ++i) {
            drawLandmarksOnCanvas(
                    result.multiHandLandmarks().get(i).getLandmarkList(), canvas, width, height);
        }
    }

    public void update() {
        postInvalidate();
        if (latest != null) {
            setImageBitmap(latest);
        }
    }

    private void drawLandmarksOnCanvas(
            List<NormalizedLandmark> handLandmarkList, Canvas canvas, int width, int height) {
        // Draw connections.
        for (Hands.Connection c : Hands.HAND_CONNECTIONS) {
            Paint connectionPaint = new Paint();
            connectionPaint.setColor(CONNECTION_COLOR);
            connectionPaint.setStrokeWidth(CONNECTION_THICKNESS);
            NormalizedLandmark start = handLandmarkList.get(c.start());
            NormalizedLandmark end = handLandmarkList.get(c.end());
            canvas.drawLine(
                    start.getX() * width,
                    start.getY() * height,
                    end.getX() * width,
                    end.getY() * height,
                    connectionPaint);
        }
        Paint landmarkPaint = new Paint();
        landmarkPaint.setColor(LANDMARK_COLOR);
        // Draw landmarks.
        for (NormalizedLandmark landmark : handLandmarkList) {
            canvas.drawCircle(
                    landmark.getX() * width, landmark.getY() * height, LANDMARK_RADIUS, landmarkPaint);
        }
    }
}
