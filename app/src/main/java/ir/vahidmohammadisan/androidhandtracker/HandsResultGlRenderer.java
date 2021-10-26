package ir.vahidmohammadisan.androidhandtracker;

import android.opengl.GLES20;

import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutioncore.ResultGlRenderer;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutions.hands.HandsResult;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

public class HandsResultGlRenderer implements ResultGlRenderer<HandsResult> {

    private static final float CONNECTION_THICKNESS = 20.0f;
    private static final String VERTEX_SHADER =
            "uniform mat4 uProjectionMatrix;\n"
                    + "attribute vec4 vPosition;\n"
                    + "void main() {\n"
                    + "  gl_Position = uProjectionMatrix * vPosition;\n"
                    + "}";
    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n"
                    + "void main() {\n"
                    + "  gl_FragColor = vec4(0, 1, 0, 1);\n"
                    + "}";
    private int program;
    private int positionHandle;
    private int projectionMatrixHandle;

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    @Override
    public void setupRendering() {
        program = GLES20.glCreateProgram();
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        projectionMatrixHandle = GLES20.glGetUniformLocation(program, "uProjectionMatrix");
    }

    @Override
    public void renderResult(HandsResult result, float[] projectionMatrix) {
        if (result == null) {
            return;
        }
        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);
        GLES20.glLineWidth(CONNECTION_THICKNESS);

        int numHands = result.multiHandLandmarks().size();
        for (int i = 0; i < numHands; ++i) {
            drawLandmarks(result.multiHandLandmarks().get(i).getLandmarkList());
        }
    }

    public void release() {
        GLES20.glDeleteProgram(program);
    }

    private void drawLandmarks(List<LandmarkProto.NormalizedLandmark> handLandmarkList) {
        for (Hands.Connection c : Hands.HAND_CONNECTIONS) {
            LandmarkProto.NormalizedLandmark start = handLandmarkList.get(c.start());
            LandmarkProto.NormalizedLandmark end = handLandmarkList.get(c.end());
            float[] vertex = {start.getX(), start.getY(), end.getX(), end.getY()};
            FloatBuffer vertexBuffer =
                    ByteBuffer.allocateDirect(vertex.length * 4)
                            .order(ByteOrder.nativeOrder())
                            .asFloatBuffer()
                            .put(vertex);
            vertexBuffer.position(0);
            GLES20.glEnableVertexAttribArray(positionHandle);
            GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
        }
    }
}
