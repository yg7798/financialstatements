package com.tekion.accounting.fs.common.slackAlert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FsSlackMessageDto {

	private String oemId ;
	private String dealerId ;
	private String tenantId ;
	private String status;
}
