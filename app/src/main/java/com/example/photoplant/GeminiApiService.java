package com.example.photoplant;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiApiService {
    private static final String PROJECT_ID = "ID_DO_PROJETO";
    private static final String LOCATION = "us-central1";
    private static final String MODEL = "gemini-2.0-flash";
    private final Context context;
    private final OkHttpClient client;
    private GoogleCredentials credentials;

    public GeminiApiService(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        loadCredentials();
    }

    private void loadCredentials() {
        try {
            InputStream credentialsStream = context.getResources().openRawResource(R.raw.service_account);
            credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
        } catch (IOException e) {
            Log.e("GeminiApiService", "Erro ao carregar credenciais: " + e.getMessage(), e);
        }
    }

    public interface GeminiApiCallback {
        void onSuccess(String plantName);
        void onError(String errorMessage);
    }

    public void identifyPlant(Bitmap image, GeminiApiCallback callback) {
        new Thread(() -> {
            try {
                // Obter token de acesso
                credentials.refreshIfExpired();
                String accessToken = credentials.getAccessToken().getTokenValue();

                // Converter imagem para Base64
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                byte[] imageBytes = outputStream.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                // Criar URL da API
                String apiUrl = String.format(
                        "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:generateContent",
                        LOCATION, PROJECT_ID, LOCATION, MODEL);

                // Construir o corpo JSON da requisição
                JsonObject requestBody = new JsonObject();

                // Configurar conteúdo com a imagem e o prompt
                JsonArray contents = new JsonArray();
                JsonObject content = new JsonObject();
                content.addProperty("role", "user");

                // Adicionar partes (texto e imagem)
                JsonArray parts = new JsonArray();

                // Adicionar texto
                JsonObject textPart = new JsonObject();
                textPart.addProperty("text", "Identifique a planta da foto com apenas uma palavra em português.");
                parts.add(textPart);

                // Adicionar imagem
                JsonObject imagePart = new JsonObject();
                JsonObject inlineData = new JsonObject();
                inlineData.addProperty("mime_type", "image/jpeg");
                inlineData.addProperty("data", base64Image);
                imagePart.add("inline_data", inlineData);
                parts.add(imagePart);

                // Adicionar partes ao conteúdo
                content.add("parts", parts);
                contents.add(content);
                requestBody.add("contents", contents);

                // Configurações de geração
                JsonObject generationConfig = new JsonObject();
                generationConfig.addProperty("temperature", 0.4);
                generationConfig.addProperty("topK", 32);
                generationConfig.addProperty("topP", 1.0);
                generationConfig.addProperty("maxOutputTokens", 2048);
                requestBody.add("generationConfig", generationConfig);

                // Converter para string
                String jsonBody = new Gson().toJson(requestBody);

                // Criar e executar a requisição HTTP
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(jsonBody, JSON);

                Request request = new Request.Builder()
                        .url(apiUrl)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Sem corpo de resposta";
                        callback.onError("Erro na API: " + response.code() + " " + response.message());
                        return;
                    }

                    String responseBody = response.body().string();
                    String plantName = parseGeminiResponse(responseBody);
                    callback.onSuccess(plantName);
                }
            } catch (Exception e) {
                callback.onError("Erro: " + e.getMessage());
            }
        }).start();
    }

    private String parseGeminiResponse(String jsonResponse) {
        try {
            JsonObject responseObj = JsonParser.parseString(jsonResponse).getAsJsonObject();

            if (responseObj.has("candidates")) {
                JsonArray candidates = responseObj.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    JsonObject candidate = candidates.get(0).getAsJsonObject();
                    if (candidate.has("content")) {
                        JsonObject content = candidate.getAsJsonObject("content");
                        if (content.has("parts")) {
                            JsonArray parts = content.getAsJsonArray("parts");
                            if (parts.size() > 0) {
                                JsonObject part = parts.get(0).getAsJsonObject();
                                if (part.has("text")) {
                                    return part.get("text").getAsString();
                                }
                            }
                        }
                    }
                }
            }
            return "Não foi possível entender a resposta do Gemini.";
        } catch (Exception e) {
            return "Erro ao processar resposta: " + e.getMessage();
        }
    }
}