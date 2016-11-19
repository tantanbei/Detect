package tantanbei.detect;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.HOGDescriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    Button button;
    JavaCameraView camera;

    private Mat grayscaleImage;
    private int absoluteFaceSize;

    private CascadeClassifier bodyDetector;
    private HOGDescriptor hogDescriptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        camera = (JavaCameraView) findViewById(R.id.camera);

        camera.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);

        camera.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                grayscaleImage = new Mat(height, width, CvType.CV_8UC4);

                // The faces will be a 20% of the height of the screen
                absoluteFaceSize = (int) (height * 0.2);
            }

            @Override
            public void onCameraViewStopped() {
                grayscaleImage = null;
            }

            @Override
            public Mat onCameraFrame(Mat inputFrame) {

                // Create a grayscale image
                Imgproc.cvtColor(inputFrame, grayscaleImage, Imgproc.COLOR_RGB2GRAY);

                MatOfRect bodies = new MatOfRect();

                // Use the classifier to detect faces
                if (hogDescriptor != null) {
                    hogDescriptor.detectMultiScale(grayscaleImage, bodies, new MatOfDouble(1));
                }

                // If there are any faces found, draw a rectangle around it
                Rect[] facesArray = bodies.toArray();
                for (int i = 0; i < facesArray.length; i++) {
                    Core.rectangle(inputFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
                }

                return inputFrame;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.enableView();
            }
        });
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.d("tan", "Load success");
                    init();
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.d("tan", "Load fail");
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //load OpenCV engine and init OpenCV library
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        Log.d("tan", "onResume sucess load OpenCV...");
    }

    private void init() {
        InputStream is = null;
        try {
            is = getAssets().open("opencv/haarcascade_mcs_upperbody.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        File f = createFileFromInputStream(is);
        Log.d("tan", "xml file is exist: " + f.exists());

        bodyDetector = new CascadeClassifier(f.getAbsolutePath());

        if (bodyDetector.empty()) {
            Log.d("tan", "bodyDetector is empty ");
            bodyDetector = null;
            throw new RuntimeException("bodyDetector create failed");
        }

        hogDescriptor = new HOGDescriptor();
        hogDescriptor.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());


    }

    private File createFileFromInputStream(InputStream inputStream) {

        try {
            File dir = new File(getExternalCacheDir().getPath() + "/cache/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File f = new File(getExternalCacheDir().getPath() + "/cache/tmp.file");
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();

            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
