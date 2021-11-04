package com.tekion.accounting.fs;

import com.tekion.accounting.fs.dpProvider.DpProvider;
import com.tekion.accounting.fs.dpProvider.DpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class StaticContextInitializer {

	@Autowired
	private DpProvider dpProvider;

	@PostConstruct
	public void init() {
		DpUtils.autowireStaticElem(dpProvider);
	}

}
