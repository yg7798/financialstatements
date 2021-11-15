package com.tekion.accounting.fs.common.pod;

import com.tekion.core.excelGeneration.models.utils.TCollectionUtils;
import com.tekion.core.exceptions.TBaseRuntimeException;
import lombok.experimental.UtilityClass;

import java.util.Objects;

import static com.tekion.accounting.fs.common.pod.PodUtils.isBoolSetToTrue;

@UtilityClass
public class Validator {
	public static void validateRequest(BasePodLevelRunRequestDto requestDto){
		if(!isValidRequestInternal(requestDto)){
			throw new TBaseRuntimeException("Invalid Request");
		}

	}

	private static boolean isValidRequestInternal(BasePodLevelRunRequestDto requestDto){
		if(Objects.isNull(requestDto)){
			return true;
		}
		// you can only provide 1 list right. doesnt make sense to provide both
		if(TCollectionUtils.isNotEmpty(requestDto.getDealersToRunFor()) &&
				TCollectionUtils.isNotEmpty(requestDto.getRunExceptFollowingDealers())){
			return false;
		}
		boolean oneOfListNotEmpty = false;
		if(TCollectionUtils.isNotEmpty(requestDto.getDealersToRunFor()) ||
				TCollectionUtils.isNotEmpty(requestDto.getRunExceptFollowingDealers())){
			oneOfListNotEmpty=true;
		}

		// you can only provide 1 right. either include or exclude.
		if((Objects.nonNull(requestDto.getExcludeQADealershipsOnRun()) && requestDto.getExcludeQADealershipsOnRun()) &&
				(Objects.nonNull(requestDto.getOnlyQADealershipsOnRun()) && requestDto.getOnlyQADealershipsOnRun()) ){
			return false;
		}

		// only 1 of flag is permitted. as of now. code the rest of functionality if you want more freedom.
		if(oneOfListNotEmpty &&
				(
						isBoolSetToTrue(requestDto.getExcludeQADealershipsOnRun()) ||
								isBoolSetToTrue(requestDto.getOnlyQADealershipsOnRun())
				)
		){
			return false;
		}


		return true;
	}


}
