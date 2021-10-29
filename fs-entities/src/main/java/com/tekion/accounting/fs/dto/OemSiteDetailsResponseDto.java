package com.tekion.accounting.fs.dto;

import lombok.Data;

import java.util.List;

@Data
public class OemSiteDetailsResponseDto {
    List<OemSiteDetailsDto> listOfOemSiteDetails;
}
