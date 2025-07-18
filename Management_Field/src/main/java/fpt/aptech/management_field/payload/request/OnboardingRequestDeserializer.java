package fpt.aptech.management_field.payload.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OnboardingRequestDeserializer extends JsonDeserializer<OnboardingRequest> {
    
    @Override
    public OnboardingRequest deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);
        System.out.println("DEBUG: OnboardingRequestDeserializer - Raw JSON: " + node.toString());
        
        Boolean isDiscoverable = node.has("isDiscoverable") ? node.get("isDiscoverable").asBoolean() : true;
        System.out.println("DEBUG: Deserializer - isDiscoverable: " + isDiscoverable);
        
        JsonNode sportProfilesNode = node.get("sportProfiles");
        System.out.println("DEBUG: Deserializer - sportProfiles node: " + (sportProfilesNode != null ? sportProfilesNode.toString() : "null"));
        List<OnboardingRequest.SportSelectionDto> sportProfiles = new ArrayList<>();
        
        if (sportProfilesNode != null && sportProfilesNode.isArray()) {
            for (JsonNode sportNode : sportProfilesNode) {
                System.out.println("DEBUG: Deserializer - processing sport node: " + sportNode.toString());
                OnboardingRequest.SportSelectionDto sportDto = new OnboardingRequest.SportSelectionDto();
                
                // Handle both frontend format (sportName) and backend format (sportId)
                if (sportNode.has("sportId") && !sportNode.get("sportId").isNull()) {
                    sportDto.setSportId(sportNode.get("sportId").asLong());
                    System.out.println("DEBUG: Deserializer - sportId: " + sportNode.get("sportId").asLong());
                } else if (sportNode.has("sportId")) {
                    sportDto.setSportId(null);
                    System.out.println("DEBUG: Deserializer - sportId is null");
                }
                if (sportNode.has("sportName")) {
                    sportDto.setSportName(sportNode.get("sportName").asText());
                    System.out.println("DEBUG: Deserializer - sportName: " + sportNode.get("sportName").asText());
                }
                if (sportNode.has("sportIcon")) {
                    sportDto.setSportIcon(sportNode.get("sportIcon").asText());
                }
                if (sportNode.has("skill")) {
                    sportDto.setSkill(sportNode.get("skill").asInt());
                    System.out.println("DEBUG: Deserializer - skill: " + sportNode.get("skill").asInt());
                }
                
                // Handle tags
                List<OnboardingRequest.TagSelectionDto> tags = new ArrayList<>();
                JsonNode tagsNode = sportNode.get("tags");
                System.out.println("DEBUG: Deserializer - tags node: " + (tagsNode != null ? tagsNode.toString() : "null"));
                if (tagsNode != null && tagsNode.isArray()) {
                    for (JsonNode tagNode : tagsNode) {
                        System.out.println("DEBUG: Deserializer - processing tag node: " + tagNode.toString());
                        OnboardingRequest.TagSelectionDto tagDto = new OnboardingRequest.TagSelectionDto();
                        
                        // Handle both frontend format (tagName) and backend format (tagId)
                        if (tagNode.has("tagId") && !tagNode.get("tagId").isNull()) {
                            tagDto.setTagId(tagNode.get("tagId").asLong());
                            System.out.println("DEBUG: Deserializer - tagId: " + tagNode.get("tagId").asLong());
                        } else {
                            tagDto.setTagId(null);
                            System.out.println("DEBUG: Deserializer - tagId is null");
                        }
                        if (tagNode.has("tagName")) {
                            tagDto.setTagName(tagNode.get("tagName").asText());
                            System.out.println("DEBUG: Deserializer - tagName: " + tagNode.get("tagName").asText());
                        } else if (tagNode.isTextual()) {
                            // Handle case where tags is array of strings
                            tagDto.setTagName(tagNode.asText());
                            System.out.println("DEBUG: Deserializer - tag name (string): " + tagNode.asText());
                        }
                        
                        tags.add(tagDto);
                    }
                }
                sportDto.setTags(tags);
                System.out.println("DEBUG: Deserializer - sport DTO created with " + tags.size() + " tags");
                
                sportProfiles.add(sportDto);
            }
        }
        
        System.out.println("DEBUG: Deserializer - total sport profiles: " + sportProfiles.size());
        System.out.println("DEBUG: Deserializer - OnboardingRequest created successfully");
        return new OnboardingRequest(sportProfiles, isDiscoverable);
    }
}