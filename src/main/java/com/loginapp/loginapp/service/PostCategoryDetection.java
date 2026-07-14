package com.loginapp.loginapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginapp.loginapp.DTO.PostCategoryDTO;
import com.loginapp.loginapp.entity.PostsEntity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostCategoryDetection {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    // Inject Other Files thorugh constructor

    private final PostCategoryService postCategoryService;

    PostCategoryDetection(PostCategoryService postCategoryService) {
        this.postCategoryService = postCategoryService;
    }

    public void detectAndSaveCategory(PostsEntity post, String contentType) {
        Long postId = post.getPostId(); 
        String fileName = post.getFileName();
        String caption = post.getPostCaption();
        String location = post.getPostLocation();

        CompletableFuture.runAsync(() -> {
            try {
                String response = callGroq(postId, fileName, caption, location, contentType);
                PostCategoryDTO dto = parseResponse(response);
                postCategoryService.saveCategory(postId, dto);  
            } catch (Exception e) {
                System.out.println("Category detection error: " + e.getMessage());
            }
        });
    }

    private String callGroq(Long postId, String fileName, String caption, String location, String contentType) throws Exception {

        // Create an HTTP client
        HttpClient client = HttpClient.newHttpClient();

        // check valdation for caption and location
        String captionText = (caption != null && !caption.isEmpty())
            ? caption : "no caption";

        String locationText = (location != null && !location.isEmpty())
            ? location : "";

        // Read and Get image data as base64
        String imageInfo = "";
        if (contentType.startsWith("image/")) {
            try {
                String imagePath = uploadDir + fileName;
                byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                imageInfo = "Image data (base64): " + base64Image.substring(0, Math.min(100, base64Image.length())) + "...";
            } catch (Exception e) {
                imageInfo = "Image could not be read";
            }
        }

        // Prepare the prompt for Groq API
        String prompt = """
            Analyze this social media post and return ONLY a JSON object, no markdown, no extra text.
            
            Post type: %s
            Caption: %s
            Location: %s
            
            Return ONLY this JSON:
            {
              "primary_category": "Technology/Fitness/Food/Travel/Fashion/Art/Music/Sports/Education/Entertainment/Other",
              "sub_categories": ["max 2 sub categories"],
              "topics": ["max 3 specific topics"],
              "sentiment": "POSITIVE or NEGATIVE or NEUTRAL",
              "content_type": "EDUCATIONAL or ENTERTAINMENT or NEWS or OPINION or MEME or OTHER",
              "confidence_score": 0.0 to 1.0
            }
        """.formatted(
            contentType.startsWith("video/") ? "VIDEO" : "IMAGE",
            captionText,
            locationText
        );

        String requestBody = """
            {
                "model": "llama-3.3-70b-versatile",
                "messages": [
                    {
                        "role": "user",
                        "content": "%s"
                    }
                ],
                "max_tokens": 300
            }
        """.formatted(
            prompt.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "")
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + groqApiKey)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpResponse<String> response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("Groq raw response: " + response.body());
        return extractTextFromResponse(response.body());
    }

    private String extractTextFromResponse(String responseBody) {

        if (responseBody.contains("\"error\"")) {
            System.out.println("Groq API error: " + responseBody);
            throw new RuntimeException("Groq API error");
        }

        String contentKey = "\"content\":\"";
        int start = responseBody.indexOf(contentKey);
        if (start == -1) {
            throw new RuntimeException("No content in response");
        }

        start = start + contentKey.length();
        String remaining = responseBody.substring(start);

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < remaining.length()) {
            char c = remaining.charAt(i);
            if (c == '\\' && i + 1 < remaining.length()) {
                char next = remaining.charAt(i + 1);
                if (next == 'n') { result.append('\n'); i += 2; continue; }
                if (next == '"') { result.append('"'); i += 2; continue; }
                if (next == '\\') { result.append('\\'); i += 2; continue; }
            }
            if (c == '"') break;
            result.append(c);
            i++;
        }

        String extracted = result.toString().trim();
        System.out.println("Extracted content: " + extracted);
        return extracted;
    }

    private PostCategoryDTO parseResponse(String jsonResponse) {
        PostCategoryDTO dto = new PostCategoryDTO();
        dto.setPrimaryCategory(extractJsonValue(jsonResponse, "primary_category"));
        dto.setSentiment(extractJsonValue(jsonResponse, "sentiment"));
        dto.setContentType(extractJsonValue(jsonResponse, "content_type"));
        dto.setConfidenceScore(Float.parseFloat(
            extractJsonValue(jsonResponse, "confidence_score")
        ));
        dto.setSubCategories(parseJsonArray(jsonResponse, "sub_categories"));
        dto.setTopics(parseJsonArray(jsonResponse, "topics"));
        return dto;
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search) + search.length();
        String rest = json.substring(start).trim();
        if (rest.startsWith("\"")) {
            int end = rest.indexOf("\"", 1);
            return rest.substring(1, end);
        } else {
            int end = rest.indexOf(",");
            if (end == -1) end = rest.indexOf("}");
            return rest.substring(0, end).trim();
        }
    }

    private List<String> parseJsonArray(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search) + search.length();
        String rest = json.substring(start).trim();
        int end = rest.indexOf("]") + 1;
        String arrayStr = rest.substring(1, end - 1);
        return Arrays.stream(arrayStr.split(","))
            .map(s -> s.trim().replace("\"", ""))
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

}