package com.example.proyectofinaluem;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class ImageClassifier {

    private Interpreter interpreter;
    private List<String> labels;
    private static final int IMAGE_SIZE = 224;

    public ImageClassifier(Context context) throws IOException {
        ByteBuffer model = FileUtil.loadMappedFile(context, "model.tflite");
        interpreter = new Interpreter(model);
        labels = FileUtil.loadLabels(context, "labels.txt");
    }

    public String classify(Bitmap bitmap) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);

        ByteBuffer input = convertBitmapToByteBuffer(scaledBitmap);
        byte[][] output = new byte[1][labels.size()];
        interpreter.run(input, output);

        int maxIndex = 0;
        float maxConfidence = 0f;
        for (int i = 0; i < output[0].length; i++) {
            float confidence = (output[0][i] & 0xFF) / 255.0f;
            if (confidence > maxConfidence) {
                maxConfidence = confidence;
                maxIndex = i;
            }
        }

        return (maxConfidence > 0.3f) ? labels.get(maxIndex) : null;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(IMAGE_SIZE * IMAGE_SIZE * 3);
        buffer.order(ByteOrder.nativeOrder());
        int[] pixels = new int[IMAGE_SIZE * IMAGE_SIZE];
        bitmap.getPixels(pixels, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE);
        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            buffer.put((byte) r);
            buffer.put((byte) g);
            buffer.put((byte) b);
        }
        return buffer;
    }
}
