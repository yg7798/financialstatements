package com.tekion.accounting.fs.master.dto;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FsGroupCodeDetailsResponseDto {

    private Map<String, FsGroupCodeDetail> details = Maps.newHashMap();
    private String date;

}