package com.tekion.accounting.fs.i118;

import com.tekion.core.service.i118.AbstractResourceBasedLocalizedStringSource;
import com.tekion.core.service.i118.LocalizedMessageType;
import org.springframework.stereotype.Component;

import static com.tekion.accounting.fs.i118.FsErrorsLocalizedStringSource.FIN_STATEMENTS;
import static com.tekion.accounting.fs.i118.FsErrorsLocalizedStringSource.HIGH_PRIORITY_ZERO;

@Component
public class FsLabelsLocalizedStringSource extends AbstractResourceBasedLocalizedStringSource {

	/**
	 * please note here.. the file name that it would search for us errors_common
	 */
	public FsLabelsLocalizedStringSource() {
		super(LocalizedMessageType.LABEL, FIN_STATEMENTS);
	}

	@Override
	public int order() {
		return HIGH_PRIORITY_ZERO;
	}
}
