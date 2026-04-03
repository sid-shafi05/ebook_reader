package org.example.bookreader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatHandler {
    private VBox chatPanel;
    private VBox chatMessages;
    private TextField chatInput;
    private boolean chatPanelVisible = false;
    private List<Map<String, String>> chatHistory = new ArrayList<>();
    private Book currentBook;
    private PageNavigator navigator;

    public ChatHandler(VBox chatPanel, VBox chatMessages, TextField chatInput, PageNavigator navigator) {
        this.chatPanel = chatPanel;
        this.chatMessages = chatMessages;
        this.chatInput = chatInput;
        this.navigator = navigator;
    }

    public void setCurrentBook(Book book) { this.currentBook = book; }

    public void toggleChatPanel() {
        chatPanelVisible = !chatPanelVisible;
        chatPanel.setVisible(chatPanelVisible);
        chatPanel.setManaged(chatPanelVisible);
    }

    public void sendMessage() {
        if (chatInput == null) return;
        String userMessage = chatInput.getText().trim();
        if (userMessage.isEmpty()) return;

        addBubble("You", userMessage, true);
        chatInput.clear();

        String bookContext = "You are a helpful reading assistant. The user is reading '"
                + (currentBook != null ? currentBook.getTitle() : "a book")
                + "' and is currently on page " + (navigator.getCurrentPage() + 1)
                + ". Answer their questions helpfully and concisely.";

        Map<String, String> userMsg = new java.util.HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        chatHistory.add(userMsg);

        Label loading = new Label("AI is thinking...");
        loading.getStyleClass().add("chat-sender-label");
        chatMessages.getChildren().add(loading);

        new Thread(() -> {
            String reply = callGroqAPI(bookContext, userMessage);
            javafx.application.Platform.runLater(() -> {
                chatMessages.getChildren().remove(loading);
                addBubble("AI", reply, false);
                Map<String, String> assistantMsg = new java.util.HashMap<>();
                assistantMsg.put("role", "assistant");
                assistantMsg.put("content", reply);
                chatHistory.add(assistantMsg);
            });
        }).start();
    }


    private void addBubble(String sender, String text, boolean isUser) {
        VBox bubble = new VBox(3);
        bubble.getStyleClass().add(isUser ? "chat-bubble-user" : "chat-bubble-ai");
        bubble.setMaxWidth(240);

        Label senderLabel = new Label(sender);
        senderLabel.getStyleClass().add("chat-sender-label");

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(220);
        messageLabel.getStyleClass().add(isUser ? "chat-text-user" : "chat-text-ai");

        bubble.getChildren().addAll(senderLabel, messageLabel);
        chatMessages.getChildren().add(bubble);
    }

    private String callGroqAPI(String context, String userPrompt) {
        try {
            java.util.Properties props = new java.util.Properties();
            java.io.InputStream stream = getClass().getClassLoader()
                    .getResourceAsStream("config.properties");
            if (stream == null) {
                stream = new java.io.FileInputStream("src/config.properties");
            }
            props.load(stream);
            String apiKey = props.getProperty("api.key");

            String url = "https://api.groq.com/openai/v1/chat/completions";
            String fullPrompt = context + "\n\nUser Question: " + userPrompt;
            String escaped = fullPrompt.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");

            String jsonBody = "{"
                    + "\"model\": \"llama-3.1-8b-instant\","
                    + "\"max_tokens\": 512,"
                    + "\"messages\": [{\"role\": \"user\", \"content\": \"" + escaped + "\"}]"
                    + "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            if (root.has("error"))
                return "Error: " + root.path("error").path("message").asText();

            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty())
                return choices.get(0).path("message").path("content").asText();

            return "No reply from AI.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}