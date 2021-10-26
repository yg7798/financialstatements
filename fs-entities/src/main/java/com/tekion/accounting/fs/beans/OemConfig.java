package com.tekion.accounting.fs.beans;

import com.tekion.core.beans.TBaseMongoBean;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OemConfig extends TBaseMongoBean {

	public static final String OEM_ID = "oemId";
	public static final String FIRST_CODE = "minuendCellCode";
	public static final String SECOND_CODE = "subtrahendCellCode";
	public static final String MEMO_KEY = "memoKeyToOffset";

	private String oemId;
	private String country;
	private boolean xmlEnabled;
	private boolean submissionEnabled;
	private String oemLogoURL;
	private String defaultPrecision;
	private boolean useDealerLogo;
	private List<String> supportedFileFormats = new ArrayList<>();
	private boolean downloadFileFromIntegration;
	private boolean enableRoundOff;
	private boolean enableRoundOffOffset;

	private String createdByUserId;
	private String modifiedByUserId;

	private Map<String, String> additionalInfo;
	private FSPreferences fsPreferences;
	public enum SupportedFileFormats{
		PDF,
		XML,
		STAR,
		EXCEL,
		FIN,
		CSV,
		TXT
	}
}
