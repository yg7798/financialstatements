package com.tekion.accounting.fs.api;

import com.tekion.accounting.fs.service.onboarding.OnBoardingService;
import com.tekion.core.service.api.TResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/onboarding")
public class DealerOnboardingApi {

	private final OnBoardingService dealerOnBoardService;

	/**
	 * creating snapshots for two years when a new dealer onboards analytics
	 * */
	@PostMapping("/analytics")
	public ResponseEntity createSnapshotsToOnboardNewDealer(){
		dealerOnBoardService.createSnapshotsToOnboardNewDealer();
		return TResponseEntityBuilder.okResponseEntity("success");
	}

}
