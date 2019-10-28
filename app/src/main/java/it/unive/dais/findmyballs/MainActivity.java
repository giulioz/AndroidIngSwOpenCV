package it.unive.dais.findmyballs;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class MainActivity extends AppCompatActivity {

    private String TAG = "AndroidIngSwOpenCV";

    private CameraBridgeViewBase mOpenCvCameraView;
    private ZBarScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Imposta lo schermo a sempre acceso
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Carica le librerie di OpenCV in maniera sincrona
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "Unable to load OpenCV");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }

        // Configura l'elemento della camera
        mOpenCvCameraView = findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setMaxFrameSize(320, 240);
        mOpenCvCameraView.disableFpsMeter();
        mOpenCvCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                Log.d(TAG, "Camera Started");
            }

            @Override
            public void onCameraViewStopped() {
                Log.d(TAG, "Camera Stopped");
            }

            // Viene eseguito ad ogni frame, con inputFrame l'immagine corrente
            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                // Salva il frame corrente su un oggetto Mat, ossia una matrice bitmap
                Mat frame = inputFrame.rgba();
                // Crea una nuova Mat per effettuare elaborazioni
                Mat median = new Mat();

                // Converte il formato colore da BGR a RGB
                Imgproc.cvtColor(frame, median, Imgproc.COLOR_BGR2RGB);

                // Effettua un filtro mediana di dimensione 5 sull'immagine
                Imgproc.medianBlur(frame, median, 5);

                // Disegna una linea in mezzo allo schermo
                Imgproc.line(median, new Point(0, 120), new Point(320, 120), new Scalar(0, 255, 0), 1);

                ImageScanner mScanner = new ImageScanner();
                mScanner.setConfig(0, Config.X_DENSITY, 3);
                mScanner.setConfig(0, Config.Y_DENSITY, 3);
                mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
                for(BarcodeFormat format : BarcodeFormat.ALL_FORMATS) {
                    mScanner.setConfig(format.getId(), Config.ENABLE, 1);
                }

                Image imageToScan = new Image(frame.cols(), frame.rows(), "Y800");
                byte[] return_buff = new byte[(int) (frame.total() *
                        frame.channels())];
                frame.get(0, 0, return_buff);
                imageToScan.setData(return_buff);
                int qrResult = mScanner.scanImage(imageToScan);
                if (qrResult != 0) {
                    SymbolSet sym = mScanner.getResults();
                    for (Symbol s : sym) {
                        Log.d(TAG, "Found QR: " + s.getData());
                    }
                }

                // Ritorna il frame da visualizzare a schermo
                return frame;
            }
        });

        // Abilita la visualizzazione dell'immagine sullo schermo
        mOpenCvCameraView.enableView();

        mScannerView = new ZBarScannerView(this);
//        mScannerView.setVisibility(View.INVISIBLE);
//        LinearLayout layout = findViewById(R.id.layout);
//        layout.addView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();

        mScannerView.setResultHandler(new ZBarScannerView.ResultHandler() {
            private final ZBarScannerView.ResultHandler _this = this;

            @Override
            public void handleResult(Result rawResult) {
                Log.d(TAG, "Found QR: " + rawResult.getContents());

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScannerView.resumeCameraPreview(_this);
                    }
                }, 2000);
            }
        });

        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }
}
