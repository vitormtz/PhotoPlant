package com.example.photoplant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private Button captureButton;
    private ImageView imageView;
    private TextView resultTextView;
    private ProgressBar progressBar;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar componentes da interface
        captureButton = findViewById(R.id.captureButton);
        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.resultTextView);
        progressBar = findViewById(R.id.progressBar);

        inicializarCredenciais();
    }

    private void inicializarCredenciais() {
        try {
            // Carregar credenciais do arquivo JSON da conta de serviço
            InputStream credentialsStream = getResources().openRawResource(R.raw.service_account);
            ServiceAccountCredentials.fromStream(credentialsStream)
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao carregar credenciais: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        // Configurar o botão de captura
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();
            }
        });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Permissão da câmera necessária", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Garantir que exista um aplicativo de câmera para lidar com o intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Criar o arquivo onde a foto deve ir
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Erro ao criar arquivo de imagem", Toast.LENGTH_SHORT).show();
            }

            // Continue somente se o arquivo foi criado com sucesso
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.photoplant.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Criar um nome de arquivo de imagem
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Salvar um caminho de arquivo para uso com intents ACTION_VIEW
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                // Mostrar a imagem capturada
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                        Uri.fromFile(new File(currentPhotoPath)));
                imageView.setImageBitmap(bitmap);

                // Mostrar carregamento
                progressBar.setVisibility(View.VISIBLE);
                resultTextView.setText("Identificando a planta...");

                // Identificar a planta
                GeminiApiService geminiService = new GeminiApiService(this);
                geminiService.identifyPlant(bitmap, new GeminiApiService.GeminiApiCallback() {
                    @Override
                    public void onSuccess(String plantName) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            resultTextView.setText("Planta identificada: " + plantName);
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            resultTextView.setText("Erro: " + errorMessage);
                        });
                    }
                });
            } catch (IOException e) {
                Toast.makeText(this, "Erro ao carregar a imagem", Toast.LENGTH_SHORT).show();
            }
        }
    }
}