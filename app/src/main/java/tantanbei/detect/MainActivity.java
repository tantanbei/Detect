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
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    Button button;
    Button phone;
    ImageView imageView;

    Bitmap srcBitmap;
    Bitmap grayBitmap;
    Bitmap MaskBitmap;

    private Bitmap img = null;

    private static boolean flag = true;
    private static boolean isFirst = true;                      // Grey

    final private int PICTURE_CHOOSE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        imageView = (ImageView) findViewById(R.id.image);
        phone = (Button) findViewById(R.id.phone);

        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get a picture form your phone
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICTURE_CHOOSE);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
//                if(isFirst)
//                {
//                    procSrc2Gray();
//                    isFirst = false;
//                }
//                if(flag){
//                    imageView.setImageBitmap(grayBitmap);
//                    button.setText("Origin");
//                    flag = false;
//                }
//                else{
//                    imageView.setImageBitmap(srcBitmap);
//                    button.setText("Grey");
//                    flag = true;
//                }
                img = BitmapFactory.decodeResource(getResources(), R.drawable.d);
                detectBody();
            }
        });
    }

    public void procSrc2Gray() {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.a);
        grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.bitmapToMat(srcBitmap, rgbMat);//convert original bitmap to Mat, R G B.
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat
        Utils.matToBitmap(grayMat, grayBitmap); //convert mat to bitmap
        Log.d("tan", "procSrc2Gray sucess...");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Log.d("tan", "Load success");
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
        isFirst = true;
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        Log.i("tan", "onResume sucess load OpenCV...");

    }

    private void detectBody() {
        InputStream is = null;
        try {
            is = getAssets().open("opencv/haarcascade_frontalface_alt2.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        File f = createFileFromInputStream(is);
        Log.d("tan", "xml file is exist: " + f.exists());

        CascadeClassifier bodyDetector = new CascadeClassifier(f.getAbsolutePath());

        if (bodyDetector.empty()) {
            Log.d("tan", "bodyDetector is empty ");
            return;
        }

        // Bitmap bmptest = BitmapFactory.decodeResource(getResources(),
        // R.drawable.lena);
        Mat testMat = new Mat();
        Utils.bitmapToMat(img, testMat);

        // Detect faces in the image.
        // MatOfRect is a special container class for Rect.
        MatOfRect bodyDetections = new MatOfRect();
        bodyDetector.detectMultiScale(testMat, bodyDetections);

        Log.d("tan", String.format("Detected %s faces", bodyDetections.toArray().length));

        int bodyNum = 0;
        // Draw a bounding box around each face.
        for (Rect rect : bodyDetections.toArray()) {
            Core.rectangle(
                    testMat,
                    new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(255, 0, 0));
            ++bodyNum;
        }

        // Save the visualized detection.
        // Bitmap bmpdone = Bitmap.createBitmap(bmptest.getWidth(),
        // bmptest.getHeight(), Config.RGB_565);
        Utils.matToBitmap(testMat, img);
        imageView.setImageBitmap(img);

                /*Staticdetection2Activity.this.runOnUiThread(new Runnable() {

                    public void run() {
                        // show the image
                        imageView.setImageBitmap(img);
                        // textView.setText("Finished, "+ count + " faces.");
                        textView.setText("Finished, " + " faces.");
                        // set edit text
                        // editText.setText(str);
                    }
                });*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // the image picker callback
        if (requestCode == PICTURE_CHOOSE) {
            if (intent != null) {

                Cursor cursor = getContentResolver().query(intent.getData(),
                        null, null, null, null);
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                String fileSrc = cursor.getString(idx);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                img = BitmapFactory.decodeFile(fileSrc, options);

                options.inSampleSize = Math.max(1, (int) Math.ceil(Math.max(
                        (double) options.outWidth / 1024f,
                        (double) options.outHeight / 1024f)));
                options.inJustDecodeBounds = false;
                img = BitmapFactory.decodeFile(fileSrc, options);

                imageView.setImageBitmap(img);
            } else {
                Log.d("tan", "idButSelPic Photopicker canceled");
            }
        }
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
