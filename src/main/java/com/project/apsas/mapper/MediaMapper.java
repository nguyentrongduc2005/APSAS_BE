package com.project.apsas.mapper;

import com.project.apsas.dto.response.tutorial.DetailContentResponse;
import com.project.apsas.entity.Media;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MediaMapper {
    List<DetailContentResponse.Media> toMediaList(List<Media> mediaList);
}
