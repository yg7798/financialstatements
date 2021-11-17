package com.tekion.accounting.fs.client;

import com.tekion.accounting.fs.entities.AccountingInfo;
import com.tekion.accounting.fs.entities.OEMFsCellCodeSnapshotBulkResponseDto;
import com.tekion.core.beans.TResponse;
import com.tekion.core.feign.ClientBuilder;
import com.tekion.core.service.internalauth.AbstractServiceClientFactory;
import com.tekion.core.service.internalauth.TokenGenerator;
import com.tekion.core.utils.TGlobalConstants;
import com.tekion.core.utils.TRequestUtils;
import com.tekion.core.utils.UserContextProvider;
import com.tekion.core.validation.TValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class FSClientImpl implements FSClient{

	private final ClientBuilder clientBuilder;
	private final AbstractServiceClientFactory clientFactory;
	private final TokenGenerator tokenGenerator;
	private final TValidator validator;
	private final FSInternalClient fsInternalClient ;

	public FSClientImpl(ClientBuilder clientBuilder, AbstractServiceClientFactory clientFactory, TokenGenerator tokenGenerator,TValidator validator) {
		this.clientBuilder = clientBuilder;
		this.clientFactory = clientFactory;
		this.tokenGenerator = tokenGenerator;
		this.fsInternalClient = getFSClient(clientBuilder,clientFactory,tokenGenerator);
		this.validator=validator;
	}

	private Map<String, String> getHeaderMapWithPermissionsEncoded(){
		Map<String , String> userHeaderOptions = new HashMap<>();
		userHeaderOptions.put(TGlobalConstants.PERMISSIONS_ENCODED_KEY , UserContextProvider.getPermissionEncoded());
		return TRequestUtils.userCallHeaderMap(userHeaderOptions);
	}

	public static FSInternalClient getFSClient(ClientBuilder builder, AbstractServiceClientFactory clientFactory, TokenGenerator generator){
		return FSInternalClient.createClient(builder, clientFactory,generator );
	}

	@Override
	public TResponse<List<OEMFsCellCodeSnapshotBulkResponseDto>> getFSReportBulk(Set<String> codes,
																				 Long fromTimestamp,
																				 Long toTimestamp,
																				 boolean includeM13,
																				 String oemId,
																				 Integer oemFsVersion,
																				 Integer oemFsYear) {
		Map<String, String> queryMap = new HashMap<>();
		queryMap.put("fromTimestamp", fromTimestamp.toString());
		queryMap.put("toTimestamp", toTimestamp.toString());
		queryMap.put("includeM13", String.valueOf(includeM13));
		return fsInternalClient.getFSReportBulk(TRequestUtils.internalCallHeaderMap(),
				codes, oemId, oemFsVersion, oemFsYear, queryMap);
	}

	@Override
	public TResponse<AccountingInfo> getAccountingInfo() {
		return fsInternalClient.getAccountingInfo(TRequestUtils.internalCallHeaderMap());
	}
}
