package com.tekion.accounting.fs.service.accountingInfo;

import com.tekion.accounting.fs.beans.common.FSEntry;
import com.tekion.accounting.fs.beans.accountingInfo.AccountingInfo;
import com.tekion.accounting.fs.dto.accountingInfo.AccountingInfoDto;
import com.tekion.accounting.fs.enums.OEM;
import com.tekion.accounting.fs.repos.FSEntryRepo;
import com.tekion.accounting.fs.repos.accountingInfo.AccountingInfoRepo;
import com.tekion.accounting.fs.common.utils.JsonUtil;
import com.tekion.core.utils.TCollectionUtils;
import com.tekion.core.utils.UserContextProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class AccountingInfoServiceImpl implements AccountingInfoService {

	private final AccountingInfoRepo infoRepo;
	private final FSEntryRepo fsEntryRepo;

	@Override
	public AccountingInfo saveOrUpdate(AccountingInfoDto dto) {
		AccountingInfo infoFromDb = infoRepo.findByDealerIdNonDeleted(UserContextProvider.getCurrentDealerId());
		log.info("previous info from DB {}", JsonUtil.toJson(infoFromDb));
		AccountingInfo currentInfo = dto.toAccountingInfo();

		if(infoFromDb != null){
			currentInfo.setId(infoFromDb.getId());
			currentInfo.setCreatedTime(System.currentTimeMillis());
			currentInfo.setCreatedByUserId(UserContextProvider.getCurrentUserId());
		}

		return infoRepo.save(currentInfo);
	}

	@Override
	public AccountingInfo find(String dealerId) {

		AccountingInfo info = infoRepo.findByDealerIdNonDeleted(dealerId);
		if(Objects.isNull(info)){
			return populateOEMFields();
		}
		return info;
	}

	@Override
	public List<AccountingInfo> findList(Collection<String> dealerIds) {
		return infoRepo.findByDealerIdNonDeleted(TCollectionUtils.nullSafeCollection(dealerIds));
	}

	@Override
	public AccountingInfo delete(String dealerId) {
		AccountingInfo accountingInfo = infoRepo.findByDealerIdNonDeleted(dealerId);
		if(Objects.isNull(accountingInfo)) return null;
		accountingInfo.setDeleted(true);
		accountingInfo.setModifiedTime(System.currentTimeMillis());
		accountingInfo.setModifiedByUserId(UserContextProvider.getCurrentUserId());
		return infoRepo.save(accountingInfo);
	}

	@Override
	public AccountingInfo populateOEMFields() {
		String dealerId = UserContextProvider.getCurrentDealerId();
		log.info("populating OEM fields in AccountingInfo for {} ", UserContextProvider.getCurrentDealerId());
		AccountingInfo accountingInfo = infoRepo.findByDealerIdNonDeleted(dealerId);
		if(Objects.isNull(accountingInfo)){
			accountingInfo = new AccountingInfo();
		}
		List<String> oemIdsFromInfos = fsEntryRepo.fetchAllByDealerIdNonDeleted(dealerId).stream()
				.map(FSEntry::getOemId).distinct().sorted().collect(Collectors.toList());
		if(TCollectionUtils.isEmpty(oemIdsFromInfos)){
			return infoRepo.save(accountingInfo);
		}
		if(Objects.isNull(accountingInfo.getPrimaryOEM())) accountingInfo.setPrimaryOEM(oemIdsFromInfos.get(0));
		accountingInfo.setSupportedOEMs(new HashSet<>(oemIdsFromInfos));
		return infoRepo.save(accountingInfo);
	}

	@Override
	public AccountingInfo addOem(OEM oem){
		String oemId = oem.name();
		AccountingInfo info = infoRepo.findByDealerIdNonDeleted(UserContextProvider.getCurrentDealerId());
		if(Objects.isNull(info)){
			info = new AccountingInfo();
		}
		Set<String> supportedOems = info.getSupportedOEMs();
		if(TCollectionUtils.isEmpty(supportedOems)){
			supportedOems = new HashSet<>();
			supportedOems.add(oemId);
			info.setSupportedOEMs(supportedOems);
		}else{
			if(supportedOems.contains(oemId)) return info;
			else supportedOems.add(oemId);
		}

		if(Objects.isNull(info.getPrimaryOEM())) info.setPrimaryOEM(oemId);
		return infoRepo.save(info);
	}

	@Override
	public AccountingInfo removeOem(OEM oem) {
		String oemId = oem.name();
		AccountingInfo info = infoRepo.findByDealerIdNonDeleted(UserContextProvider.getCurrentDealerId());

		Set<String> supportedOems = info.getSupportedOEMs();
		if (TCollectionUtils.isEmpty(supportedOems) || !supportedOems.contains(oemId)) {
			return info;
		} else {
			supportedOems.remove(oemId);
			if(oemId.equals(info.getPrimaryOEM())){
				info.setPrimaryOEM(null);
				List<String> oemIds = new ArrayList<>(supportedOems).stream().sorted().collect(Collectors.toList());
				if(TCollectionUtils.isNotEmpty(oemIds)){
					log.info("new primary Oem {}", oemIds.get(0));
					info.setPrimaryOEM(oemIds.get(0));
				}
			}
		}

		return infoRepo.save(info);
	}

	@Override
	public AccountingInfo setPrimaryOem(OEM oem){
		String oemId = oem.name();
		AccountingInfo info = infoRepo.findByDealerIdNonDeleted(UserContextProvider.getCurrentDealerId());
		if(Objects.isNull(info)){
			info = new AccountingInfo();
		}
		if(oemId.equals(info.getPrimaryOEM())) return info;
		info.getSupportedOEMs().add(oemId);
		info.setPrimaryOEM(oemId);
		return infoRepo.save(info);
	}

	@Override
	public void migrateFsRoundOffOffset(){
		log.info("migrateFsRoundOffOffset dealerId {}", UserContextProvider.getCurrentDealerId());
		AccountingInfo info = infoRepo.findByDealerIdNonDeleted(UserContextProvider.getCurrentDealerId());
		if(Objects.nonNull(info) && Objects.nonNull(info.getFsRoundOffOffset()) && !info.getFsRoundOffOffset()){
			info.setFsRoundOffOffset(null);
			infoRepo.save(info);
		}
	}
}
