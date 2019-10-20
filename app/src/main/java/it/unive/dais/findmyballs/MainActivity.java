package it.unive.dais.findmyballs;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {

    private String TAG = "AndroidIngSwOpenCV";

    private CameraBridgeViewBase mOpenCvCameraView;

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
        mOpenCvCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                Log.e(TAG, "Camera Started");
            }

            @Override
            public void onCameraViewStopped() {
                Log.e(TAG, "Camera Stopped");
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
                Imgproc.medianBlur(median, median, 5);

                // Ritorna il frame da visualizzare a schermo
                return median;
            }
        });

        // Abilita la visualizzazione dell'immagine sullo schermo
        mOpenCvCameraView.enableView();
    }
}
