package com.tekion.accounting.fs.master.dto;

import lombok.Data;

import java.util.List;

@Data
public class OemSiteDetailsResponseDto {
    List<OemSiteDetailsDto> listOfOemSiteDetails;
}
