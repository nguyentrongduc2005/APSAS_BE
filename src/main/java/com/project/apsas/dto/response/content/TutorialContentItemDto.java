package com.project.apsas.dto.response.content;

import com.project.apsas.dto.response.tutorial.TutorialItemDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TutorialContentItemDto implements TutorialItemDto {
    private Long id;
    private String title;
    private long imageCount;
    private long videoCount;
    private Integer orderNo;
    @Override
    public String getItemType() {
        return "CONTENT"; // Sẽ được ưu tiên (sắp xếp A-Z)
    }
}
