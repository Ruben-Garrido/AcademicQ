package com.academiq.academiq.service;

import com.academiq.academiq.domain.entity.HistorialSolicitud;
import com.academiq.academiq.domain.entity.Solicitud;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAiService {

    @Value("${gemini.api.key:AQUI_TU_API_KEY_CUANDO_LA_TENGAS}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String apiUrl;

    private final RestClient restClient = RestClient.create();

    public String generarResumen(Solicitud solicitud, List<HistorialSolicitud> historial) {
        
        // 1. Construir el prompt estructurado
        String prompt = construirPrompt(solicitud, historial);

        // 2. Construir el body del request según la documentación de Gemini
        GeminiRequest requestBody = new GeminiRequest(prompt);

        try {
            // 3. Hacer la llamada a la API
            GeminiResponse response = restClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(GeminiResponse.class);

            // 4. Extraer el texto de la respuesta
            if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                return response.getCandidates().get(0).getContent().getParts().get(0).getText();
            }
            return "No se pudo generar el resumen. La respuesta de Gemini vino vacía.";

        } catch (Exception e) {
            log.error("Error comunicándose con Gemini API", e);
            return "El servicio de IA no está disponible en este momento. Puede continuar con el proceso manual.";
        }
    }

    private String construirPrompt(Solicitud solicitud, List<HistorialSolicitud> historial) {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres un asistente administrativo universitario. ");
        sb.append("Genera un resumen muy breve y profesional (máximo 3 líneas) del siguiente caso académico para que un coordinador lo lea rápidamente.\\n\\n");
        sb.append("DATOS DE LA SOLICITUD:\\n");
        sb.append("- Tipo: ").append(solicitud.getTipo()).append("\\n");
        sb.append("- Estado Actual: ").append(solicitud.getEstado()).append("\\n");
        sb.append("- Descripción del Estudiante: ").append(solicitud.getDescripcion()).append("\\n\\n");
        
        sb.append("HISTORIAL DE GESTIÓN:\\n");
        for (HistorialSolicitud h : historial) {
            sb.append("- ").append(h.getFechaAccion().toLocalDate()).append(" [").append(h.getAccion()).append("]: ").append(h.getObservacion()).append("\\n");
        }
        
        sb.append("\\nResumen:");
        return sb.toString();
    }

    // --- Clases DTO Internas para mapear el JSON de Gemini ---

    @Data
    static class GeminiRequest {
        private List<Content> contents;

        public GeminiRequest(String text) {
            this.contents = List.of(new Content(List.of(new Part(text))));
        }
    }

    @Data
    static class Content {
        private List<Part> parts;
        public Content() {}
        public Content(List<Part> parts) { this.parts = parts; }
    }

    @Data
    static class Part {
        private String text;
        public Part() {}
        public Part(String text) { this.text = text; }
    }

    @Data
    static class GeminiResponse {
        private List<Candidate> candidates;
    }

    @Data
    static class Candidate {
        private Content content;
    }
}
