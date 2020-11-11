package net.sourceforge.opencamera.sensorlogging;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import net.sourceforge.opencamera.ExtendedAppInterface;
import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.MyDebug;
import net.sourceforge.opencamera.StorageUtils;
import net.sourceforge.opencamera.StorageUtilsWrapper;
import net.sourceforge.opencamera.cameracontroller.ImageUtils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.jvm.Throws;

/**
 * Handles frame images and timestamps saving during video recording,
 * sequential Executor is used to queue saving tasks in the background thread.
 * Images get saved every EVERY_N_FRAME-th time.
 */
public class VideoFrameInfo implements Closeable {
    private final static String TAG = "FrameInfo";
    private final static String TIMESTAMP_FILE_SUFFIX = "_timestamps";
    private final static int NV21_TO_JPEG_QUALITY = 100;
    // TODO: move this value to preferences
    private final static int EVERY_N_FRAME = 20;

    //Sequential executor for frame and timestamps saving queue
    private final ExecutorService frameProcessor = Executors.newSingleThreadExecutor();
    private final Date mVideoDate;
    private final StorageUtilsWrapper mStorageUtils;
    private final ExtendedAppInterface mAppInterface;
    private final BufferedWriter mFrameBufferedWriter;

    private int mFrameNumber = 0;

    public VideoFrameInfo(Date videoDate, MainActivity context) throws IOException {
        mVideoDate = videoDate;
        mStorageUtils = context.getStorageUtils();
        mAppInterface = context.getApplicationInterface();

        File frameTimestampFile = mStorageUtils.createOutputCaptureInfo(
            StorageUtils.MEDIA_TYPE_RAW_SENSOR_INFO, ".csv", TIMESTAMP_FILE_SUFFIX, mVideoDate
        );
        mFrameBufferedWriter = new BufferedWriter(
            new PrintWriter(frameTimestampFile)
        );
    }

    public void submitProcessFrame(long timestamp, byte[] nv21, int width, int height, int rotation) {
        if (!frameProcessor.isShutdown()) {
            frameProcessor.execute(
                () -> {
                    try {
                        mFrameBufferedWriter
                                .append(Long.toString(timestamp))
                                .append("\n");
                        if (mFrameNumber % EVERY_N_FRAME == 0) {
                            if (MyDebug.LOG) {
                                Log.d(TAG, "Should save frame, timestamp: " + timestamp);
                            }
                            byte[] jpegResult = ImageUtils.NV21toJPEG(nv21, width, height, NV21_TO_JPEG_QUALITY);
                            File frameFile = mStorageUtils.createOutputCaptureInfo(
                                    StorageUtils.MEDIA_TYPE_VIDEO_FRAME, "jpg", String.valueOf(timestamp), mVideoDate
                            );
                            writeFrameJpeg(jpegResult, frameFile, rotation);
                        }
                        mFrameNumber++;
                    } catch (IOException e) {
                        mAppInterface.onFrameInfoRecordingFailed();
                        Log.e(TAG, "Failed to write frame info, timestamp: " + timestamp);
                        e.printStackTrace();
                        this.close();
                    }
                }
            );
        } else {
            Log.e(TAG, "Received new frame after frameProcessor executor shutdown");
        }
    }

    private void writeFrameJpeg(byte[] jpegResult, File frameFile, int rotation) throws IOException {
        FileOutputStream fos = new FileOutputStream(frameFile);

        // Apply rotation
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpegResult, 0, jpegResult.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.close();
    }

    @Override
    public void close() {
        if (frameProcessor != null) {
            if (MyDebug.LOG) {
                Log.d(TAG, "Attempting to shutdown frame processor");
            }
            // should let all assigned tasks finish execution
            // TODO: this could make UI thread block, we may need to specify handler for this class
            frameProcessor.shutdown();
        }
        if (MyDebug.LOG) {
            Log.d(TAG, "Closing frame info, frame number: " + mFrameNumber);
        }
        try {
            if (mFrameBufferedWriter != null) {
                mFrameBufferedWriter.flush();
                mFrameBufferedWriter.close();
            }
        } catch (IOException e) {
            Log.d(TAG, "Exception occured when attempting to close mFrameBufferedWriter");
            e.printStackTrace();
        }
    }
}