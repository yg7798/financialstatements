package com.tekion.accounting.fs.common;

import com.google.common.collect.Lists;
import com.tekion.accounting.fs.common.pod.BasePodLevelRunRequestDto;
import com.tekion.accounting.fs.common.pod.DealerInfo;
import com.tekion.accounting.fs.common.pod.PodUtils;
import com.tekion.accounting.fs.common.utils.MDCUtils;
import com.tekion.accounting.fs.common.utils.UserContextUtils;
import com.tekion.admin.beans.beansdto.DealerMasterBulkRequest;
import com.tekion.client.globalsettings.GlobalSettingsClient;
import com.tekion.client.globalsettings.beans.Status;
import com.tekion.client.globalsettings.beans.TenantInfo;
import com.tekion.client.globalsettings.beans.dto.DealerInfoWithOEMDetails;
import com.tekion.clients.preference.client.PreferenceClient;
import com.tekion.core.beans.TResponse;
import com.tekion.core.exceptions.TBaseRuntimeException;
import com.tekion.core.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import static com.tekion.accounting.fs.common.pod.Validator.validateRequest;

/**
 * GlobalService used to run tasks for all available tenants/dealers.
 *
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GlobalService {
	private final GlobalSettingsClient globalSettingsClient;
	@Qualifier(AsyncContextDecorator.ASYNC_THREAD_POOL)
	@Autowired
	private AsyncTaskExecutor asyncTaskExecutor;
	private final PreferenceClient preferenceClient;

	public interface GlobalTask{
		void execute();
	}

	static private class RunnableTask implements Runnable{
		private UserContext userContext;
		private GlobalTask globalTask;
		private Semaphore semaphore;

		public RunnableTask(UserContext userContext, GlobalTask globalTask, Semaphore semaphore){
			this.userContext = userContext;
			this.globalTask = globalTask;
			this.semaphore = semaphore;
		}

		@Override
		public final void run(){
			try{
				UserContextProvider.setContext(userContext);
				MDCUtils.setMDCParamsFromUserContext(userContext);
				semaphore.acquire();
				log.info("Task started for tId: {}, dId: {}", userContext.getTenantId(),userContext.getDealerId());
				globalTask.execute();
				log.info("Task finished for tId: {}, dId: {}", userContext.getTenantId(),userContext.getDealerId());
			}catch (Exception e){
				log.error("Task failed for tId: {}, dId: {}", userContext.getTenantId(),userContext.getDealerId());
			}finally{
				UserContextProvider.unsetContext();
				MDCUtils.clearMDC();
				semaphore.release();
			}
		}
	}

	public void executeTaskForAllDealers(GlobalTask globalTask){
		executeTaskForAllDealers(globalTask,1);
	}

	public void executeTaskForAllDealers(GlobalTask globalTask, int concurrency){
		executeTaskForAllDealersExcept(globalTask, concurrency, new HashSet<>());
	}

	public void executeTaskForAllDealersExcept(GlobalTask globalTask, int concurrency,
											   Collection<String> tenantIdUnderscoreDealerIdSet){
		if(concurrency<=0){
			throw new TBaseRuntimeException("Invalid configuration.");
		}
		Semaphore semaphore = new Semaphore(concurrency);
		List<Future> tasks = Lists.newArrayList();
		List<TenantInfo> tenantInfos = fetchActiveTenants();
		if(TCollectionUtils.isNotEmpty(tenantInfos)){
			for(TenantInfo tenantInfo: tenantInfos){
				List<DealerInfoWithOEMDetails> dealers = fetchAllDealersForATenant(tenantInfo.getTenantId());
				for(DealerInfoWithOEMDetails dealerInfoWithOEMDetails: dealers){
					if (tenantIdUnderscoreDealerIdSet.contains(dealerInfoWithOEMDetails.getTenantId() + "_" + dealerInfoWithOEMDetails.getDealerId())){
						continue;
					}
					try{
						semaphore.acquire();
						tasks.add(asyncTaskExecutor.submit(new RunnableTask(getContext(dealerInfoWithOEMDetails), globalTask, semaphore)));
					}catch (InterruptedException e){
						log.error("Thread interrupted ",e);
					}finally{
						semaphore.release();
					}
				}
			}
		}
		waitForTasksToComplete(tasks);
	}
	public void executeTaskForAllDealers(GlobalTask globalTask, int concurrency, BasePodLevelRunRequestDto basePodLevelRunRequestDto){
		validateRequest(basePodLevelRunRequestDto);
		if(concurrency<=0){
			throw new TBaseRuntimeException("Invalid configuration.");
		}
		Semaphore semaphore = new Semaphore(concurrency);
		List<Future> tasks = Lists.newArrayList();
		List<DealerInfo> dealerInfos = PodUtils.extractDealerInfoDetailsToRunFor(basePodLevelRunRequestDto);
		if(TCollectionUtils.isNotEmpty(dealerInfos)){
			for(DealerInfo dealerInfo : dealerInfos){
				try{
					semaphore.acquire();
					tasks.add(asyncTaskExecutor.submit(new RunnableTask(UserContextUtils.getContextFromDealerAndTenant(dealerInfo.getDealerId(), dealerInfo.getTenantId()), globalTask, semaphore)));
				}catch (InterruptedException e){
					log.error("Thread interrupted ",e);
				}finally{
					semaphore.release();
				}
			}
		}
		waitForTasksToComplete(tasks);
	}
	public void executeTaskForAllDealersOfCurrentTenant(GlobalTask globalTask, int concurrency){
		if(concurrency<=0){
			throw new TBaseRuntimeException("Invalid configuration.");
		}
		Semaphore semaphore = new Semaphore(concurrency);
		List<Future> tasks = Lists.newArrayList();
		List<DealerInfoWithOEMDetails> dealers = fetchAllDealersForATenant(UserContextProvider.getCurrentTenantId());
		for(DealerInfoWithOEMDetails dealerInfoWithOEMDetails: dealers){
			try{
				semaphore.acquire();
				tasks.add(asyncTaskExecutor.submit(new RunnableTask(getContext(dealerInfoWithOEMDetails), globalTask, semaphore)));
			}catch (InterruptedException e){
				log.error("Thread interrupted ",e);
			}finally{
				semaphore.release();
			}
		}
		waitForTasksToComplete(tasks);
	}

	public void executeTaskInParallel(GlobalTask globalTask, int parallelism) throws ExecutionException, InterruptedException {
		parallelism = Math.max(parallelism, 1);
		ForkJoinPool forkJoinPool = null;
		try {
			forkJoinPool = new ForkJoinPool(parallelism);
			forkJoinPool.submit(globalTask::execute).get();
		}finally {
			if (forkJoinPool != null) {
				forkJoinPool.shutdown();
			}
		}
	}

	private void waitForTasksToComplete(List<Future> tasks) {
		for(Future task : tasks){
			try {
				task.get();
			} catch (Exception e) {
				log.error("Exception ",e);
			}
		}
	}

	public UserContext getContext(DealerInfoWithOEMDetails dealerInfoWithOEMDetails) {
		UserContext userContext = UserContextProvider.createCopy();
		userContext.setDealerId(dealerInfoWithOEMDetails.getDealerId());
		userContext.setTenantId(dealerInfoWithOEMDetails.getTenantId());
		userContext.setDseUserContext(DSEUserContext.builder().tenantName(dealerInfoWithOEMDetails.getTenantId()).dealerId(dealerInfoWithOEMDetails.getDealerId())
				.roleId("1").tekionApiToken("").build());
		return userContext;
	}

	public List<TenantInfo> fetchActiveTenants() {
		TResponse<List<TenantInfo>> tResponse = globalSettingsClient.findAllActiveTenants(TRequestUtils.internalCallHeaderMap(),null);
		if( Objects.nonNull(tResponse)){
			return tResponse.getData();
		}
		log.error("Could not fetch tenants");
		return null;
	}

	public List<DealerInfoWithOEMDetails> fetchAllDealersForATenant(String tenantId){
		TResponse<List<DealerInfoWithOEMDetails>> tResponse = globalSettingsClient.fetchAllDealersForATenant(TRequestUtils.internalCallHeaderMap(),null,tenantId);
		if(Objects.nonNull(tResponse)){
			return TCollectionUtils.nullSafeList(tResponse.getData());
		}
		return Collections.emptyList();
	}

	public List<DealerInfoWithOEMDetails> fetchAllDealersForATenantByStatus(String tenantId, List<Status> allowedStatus){
		List<DealerInfoWithOEMDetails> dealerInfoWithOEMDetailsList = fetchAllDealersForATenant(tenantId);
		return dealerInfoWithOEMDetailsList.stream().filter( dealerInfoWithOEMDetails -> allowedStatus.contains(dealerInfoWithOEMDetails.getStatus())).collect(Collectors.toList());
	}

	public List<com.tekion.admin.beans.dealersetting.DealerMaster> getAllDealerDetailsForTenant(String tenantId){
		TResponse<List<DealerInfoWithOEMDetails>> tResponse = globalSettingsClient.fetchAllDealersForATenant(TRequestUtils.internalCallHeaderMap(),null,tenantId);
		if(Objects.nonNull(tResponse)){
			List<DealerInfoWithOEMDetails> dealerInfoWithOEMDetails = TCollectionUtils.nullSafeList(tResponse.getData());
			List<String> dealerIds = dealerInfoWithOEMDetails.stream().map(x -> x.getDealerId()).collect(Collectors.toList());

			DealerMasterBulkRequest dealerMasterBulkRequest = new DealerMasterBulkRequest();
			dealerMasterBulkRequest.setDealerIds(dealerIds);
			dealerMasterBulkRequest.setSelectedFields(Arrays.asList("id","dealerName","tenantId","bankDetails",
					"dealerDoingBusinessAsName","timeZone"));
			return preferenceClient.getAllDealerMastersWithSelectedFields(dealerMasterBulkRequest).getData();
		}
		return Collections.emptyList();
	}

}
