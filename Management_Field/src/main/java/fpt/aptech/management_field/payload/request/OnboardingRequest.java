package fpt.aptech.management_field.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = OnboardingRequestDeserializer.class)
public class OnboardingRequest {
    private List<SportSelectionDto> sportProfiles;
    private Boolean isDiscoverable;
    
    // Alternative constructor for frontend format
    public static OnboardingRequest fromFrontendFormat(List<FrontendSportProfile> frontendSportProfiles, Boolean isDiscoverable) {
        List<SportSelectionDto> sportProfiles = frontendSportProfiles.stream()
            .map(fp -> {
                SportSelectionDto dto = new SportSelectionDto();
                dto.setSportName(fp.getSportName());
                dto.setSkill(fp.getSkill());
                dto.setTags(fp.getTags().stream()
                    .map(tag -> {
                        TagSelectionDto tagDto = new TagSelectionDto();
                        tagDto.setTagName(tag.getTagName());
                        return tagDto;
                    })
                    .collect(Collectors.toList()));
                return dto;
            })
            .collect(Collectors.toList());
        
        return new OnboardingRequest(sportProfiles, isDiscoverable);
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrontendSportProfile {
        private String sportName;
        private Integer skill;
        private List<FrontendTag> tags;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FrontendTag {
        private String tagName;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SportSelectionDto {
        private Long sportId; // For existing sports
        private String sportName; // For new sports to be created
        private String sportIcon; // For new sports (optional)
        private Integer skill; // 1-5 skill level
        private List<TagSelectionDto> tags;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagSelectionDto {
        private Long tagId; // For existing tags
        private String tagName; // For new tags to be created
    }
}