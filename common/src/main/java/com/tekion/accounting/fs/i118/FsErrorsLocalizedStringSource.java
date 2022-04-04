package com.tekion.accounting.fs.i118;

import com.tekion.core.service.i118.AbstractResourceBasedLocalizedStringSource;
import com.tekion.core.service.i118.LocalizedMessageType;
import org.springframework.stereotype.Component;

@Component
public class FsErrorsLocalizedStringSource  extends AbstractResourceBasedLocalizedStringSource {

	public static String FIN_STATEMENTS = "financialstatements";
	public static Integer HIGH_PRIORITY_ZERO = 0;
	/**
	 * please note here.. the file name that it would search for us errors_common
	 */
	public FsErrorsLocalizedStringSource() {
		super(LocalizedMessageType.ERROR, FIN_STATEMENTS);
	}

	@Override
	public int order() {
		return HIGH_PRIORITY_ZERO;
	}
}
