package com.tekion.accounting.fs.common.exceptions;

import com.tekion.core.exceptions.TekionError;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum FSError implements TekionError {
	somethingWentWrong("A100","something.went.input"),
	invalidPoInvoiceTypeForDelete("A101","invalid.poinvoice.type.for.delete"),
	invalidPoInvoiceStatusToPost("A102","invalid.poinvoice.status.to.post"),
	invalidPoInvoiceTransactionAmount("A103","invalid.poinvoice.transaction.amount"),
	invalidTemplateName("A104","invalid.template.name"),
	duplicateSalesChainAccount("A105","saleschain.account.already.exists"),
	duplicateSalesChainCosAccount("A106","saleschain.cos.account.already.exists"),
	duplicatePayTypeMapping("A107","paytype.mapping.already.exists"),
	duplicateExpenseAllocation("A108","duplicate.expense.allocation"),
	duplicateJournalNumber("A109","duplicate.journal.number"),
	journalAlreadyLinkedInTransaction("A110","journal.linked.with.transaction"),
	duplicateInvoiceNumber("A111","duplicate.invoice.number"),
	transactionNotInDraft("A112","trx.not.in.draft"),
	sameBalanceAndWriteoffAccounts("A113", "same.bal.and.writeoff.account"),
	payingAmountIsGreaterThanDueAmount("A114","to.be.paid.is.greater"),
	canNotCloseCurrentOrFutureMonth("A115","invalid.month.to.close"),
	canNotCloseMonthWhichIsAlreadyClosed("A116","cannot.close.closedmonth"),
	duplicateLiabilityAccount("A117","duplicate.liabilityAccount"),
	duplicateSalesAccount("A118","duplicate.salesAccount"),
	duplicateReceivableAccount("A119", "duplicate.receivableAccount"),
	duplicateCostOfSaleAccountIdInSales("A120", "duplicate.costOfSaleAccountIdInSales"),
	duplicateCostOfSaleAccountIdInLiability("A121", "duplicate.costOfSaleAccountIdInLiability"),
	noCheckNumberPresentForThisBankNumber("A122", "no.checknumber.present.for.this.banknumber"),
	onlyOpenInvoiceCanbeVoided("A123", "only.openInvoices.canbe.voided" ),
	onlyPostedCashReceiptCanBeVoided("A124", "only.postedCashReceipts.can.be.voided"),
	invoiceIsAlreadyClosed("A125", "invoice.is.already.closed"),
	powerPostingFailure("A126", "power.posting.failure"),
	invalidCreditInvoiceStatusToPost("A127","invalid.creditinvoice.status.to.post"),
	invalidCreditInvoiceTransactionAmount("A128","invalid.creditinvoice.transaction.amount"),
	transactionIsAlreadyPresent("A129","transaction.already.present"),
	closeActiveMonthFirst("A130","close.activeMonthFirst"),
	invalidStartEndTime("A131","invalid.start.end.time"),
	canNotPostInClosedMonth("A132","cannot.post.in.closedMonth"),
	missingMonthCloseSetup("A133","monthClose.setup.is.missing"),
	noPostingsToAdjust("A134","no.postings.to.adjust"),
	cashInBankAccountAlreadyExists("A135","cashInBankAccount.for.accountnumber.exists"),
	refIdIsMandatoryAsPerCOA("A136","mandatory.refId.coa"),
	postingRestrictedForUser("A137","posting.restricted.forUser"),
	batchNotFoundWithGivenId("A138", "invoiceBatch.not.found"),
	batchPrintFailedId("A139", "invoiceBatchCheques.print.failed"),
	mediaServiceRequestFailed("A140", "mediaService.request.failed"),
	accountAlreadyExists("A141","account.already.exists"),
	invalidScheduledTime("A142", "invalid.scheduled.time"),
	sameScheduledDay("A143", "same.scheduled.time"),
	equityAccountNotPresent("A144","equity.account.not.present"),
	monthCloseInProgress("A145","month.close.in.progress"),
	apSetupNotFound("A146", "apSetup.not.found"),
	draftsAlreadyPosted("A147","drafts.already.posted"),
	draftAlreadyPosted("A148","draft.already.posted"),
	bulkDraftAlreadyInProgress("A149","bulk.draft.in.progress"),
	powerPostingPayrollFailure("A150", "power.posting.payroll.failure"),
	powerPostingCraftsCreationFailure("A151", "power.posting.drafts.creation.failure"),
	bulkDraftPostingFailed("A152", "bulk.draft.posting.failed"),
	bulkDraftPostingHasNonDraftTransaction("A153", "bulk.posting.has.nonDraftTransaction"),
	transactionNotFound("A154", "trx.not.found"),
	selectedDraftsInvalid("A155", "selected.drafts.invalid"),
	glReportGeneratorNotFound("A156", "glReportGenerator.not.found"),
	sectionNotFound("A157", "section.not.found"),
	glReportSetupIsNotValid("A158", "glReportSetup.not.valid"),
	scheduleNotFound("A159", "schedule.not.found"),
	voidScheduledTimeNotFound("A160", "void.scheduledTime.not.found"),
	noChecksToGeneratePostivePay("A161", "no.checks.to.generate.positive.pay"),
	pleaseTryAgain("A162", "please.try.again"),
	inactiveGlAcccountPresent("A163", "inactive.glAccount.present"),
	scheduleCannotBeUpdated("A164","schedule.cannot.update"),
	anotherScheduleBeingComputed("A165","another.schedule.computing"),
	failedToInitializeSchedule("A166","schedule.initialization.failed"),
	scheduleNumberAlreadyExists("A167","schedule.number.already.exists"),
	controlBookStatusMissingPause("A168","control.book.status.pause.non.determined"),
	scheduleAlreadyInitializing("A169","schedule.already.computing"),
	controlBooksAlreadyPreset("A170","control.books.not.deleted"),
	duplicateGroupDistribution("A171", "duplicate.group.distribution"),
	noChequesToPrint("A172", "no.cheques.to.print"),
	canNotCreateBegBalOnceYouCompleteReconcileOnce("A173", "cannot.create.beginning.balance.once.complete.rec.is.done"),
	needBankRecBeginningBalance("A174", "need.bank.rec.beginning.balance"),
	invoiceAlreadyHolded("A175", "invoice.already.holded"),
	invoiceAlreadyUnHolded("A176", "invoice.already.unholded"),
	vendorAlreadyHolded("A177", "vendor.already.holded"),
	vendorAlreadyUnholded("A178", "vendor.already.unholded"),
	cannotBindToTheSameGlaccount("A179", "cannot.bind.to.same.glaccount"),
	journalIdNotValid("A180", "journal.not.found"),
	unableToUpdatePO("A181", "unable.to.update.po"),
	duplicateUserAccess("A182", "duplicate.useraccess.setup"),
	cantDeleteAutoPostedDraft("A183", "autoPostedDraft.cant.delete"),
	transactionAlreadyReversed("A184", "transaction.already.reversed"),
	refIdCantBeEmpty("A185", "refId.cant.be.empty"),
	refTextCannotBeEmpty("A186","refText.cant.be.empty"),
	invalidVendor("A187", "invalid.vendor"),
	invalidVendorSiteId("A188", "invalid.vendor.siteId"),
	vendorsCannotBeGreaterThanOne("A189", "vendor.cannot.be.greater.than.one"),
	errorGlReportXL("A190", "error.at.generating.glReportXl"),
	accessRestricted("A191", "user.access.restricted", "User does not have access to perform this action"),
	deleteDraftPermissionIsNotValid("A192", "deleteDraft.permission.not.valid"),
	fsSubmitError("A193","fs.submit.error"),
	payrollCompanyNotValid("A194", "payroll.company.not.valid"),
	entityNotFound("A195", "entity.not.found"),
	invalidMonth("A196", "invalid.month"),
	emptyFiscalYearStartDate("A197","empty.fiscal.year.start.date"),
	cannotHaveMultipleApGlAccountId("A198", "cannot.have.multiple.apGlAccountId"),
	invalidRefType("A199", "invalid.reftype"),
	duplicateInterDealerMapping("A200", "inter.dealer.mapping.already.exist"),
	insufficientInfoToCreateARInvoice("A201", "insufficient.info.to.create.ar.invoice"),
	incorrectInvoiceDate("A202", "incorrect.invoice.date"),
	incorrectInvoiceDueDate("A203", "incorrect.invoice.due.date"),
	noEftNumberPresentForThisBankNumber("A204", "no.eft.present.for.this.banknumber"),
	notSupported("A205", "not.supported"),
	incorrectReconciledGlPostingIds("A206","incorrect.reconciled.gl.posting.ids"),
	balancesDoNotMatchWhileBankRecCompletion("A207","balances.do.not.match.while.bank.rec.completion"),
	cannotUpdateARAccountControlType("A208", "cannot.update.ar.account.control.type"),
	controlTypeHasToBeCustomer("A209","control.type.has.to.be.customer"),
	accountBalanceNotZero("A210","account.balance.not.zero"),
	fileFormatNotSupported("A211","file.format.not.supported"),
	valuesExistForRequestedYear("A212","values.exist.for.year"),
	controlNumberMandatory("A213","control.number.mandatory"),
	cannotPostZeroDollarInvoice("A214", "cannot.post.zero.dollar.invoice"),
	cannotVoidInvoice("A215", "cannot.void.invoice"),
	journalOverrideNotAllowedForArGlAccount("A216", "journal.override.not.allowed.for.ar.glAccount"),
	mandatoryPostingFieldsNotFilledAsPerCOA("A217", "mandatory.posting.fields.not.filled"),
	control2MandatoryAsPerCOA("A218", "control2.mandatory.coa"),
	postingDescriptionMandatoryAsPerCOA("A219", "posting.description.mandatory.coa"),
	controlNumberMadatoryForArSetup("A220","control.number.mandatory.for.AR.Setup"),
	expenseAllocationNotSetup("A221","expense.allocation.setup"),
	beginningBalanceNotValid("A222","beginning.balance.not.valid"),
	bankRecEndingDateNotValid("A223","bank.rec.ending.date.not.valid"),
	circularDependencyExists("A224","circular.dependency.exists"),
	duplicateTaxCode("A225","duplicate.tax.code"),
	taxCodeNotFound("A226","tax.code.not.found"),
	invalidPayload("A227", "invalid.payload"),
	inactiveVendor("A228", "inactive.vendor"),
	hasToBeChargeCustomer("A229","has.to.be.charge.customer"),
	exceededCreditLimit("A230", "exceeded.credit.limit"),
	customerIdsNotFound("A231", "customerIds.not.found"),
	ioError("A232","ioError while reading file"),
	cannotPostApTransaction("A233", "cannot.post.ap.transaction"),
	controlNumberMandatoryForSchedules("A234","control.number.mandatory.for.schedule"),
	batchesAlreadyCreated("A235", "batch.already.created"),
	batchCreationInProgress("A236", "batch.creation.in.progress"),
	noInvoicesLeftToMigrate("A237", "no.invoices.left.to.migrate"),
	vendorPresentInMoreThanOneBatch("A238","vendor.present.in.more.than.one.batch"),
	accountTypeNotSupportedForSalesTaxReport("A239","account.type.not.supported.for.Sales.Tax.Report"),
	salesTaxAccountTypeNotSupported("A240","sales.tax.account.type.not.supported"),
	unableToDetermineColumnForReportGeneration("A241","unable.to.determine.column.for.report.generation"),
	accountTypeNotSupportedForGrossProfitCalculation("A242","account.type.not.supported.for.GrossProfit.calculation"),
	duplicateEntryFoundInGrossProfitSetup("A243", "duplicate.entry.in.gross.profit.setup"),
	migratedScheduleCannotBeDeleted("A244","migrated.schedule.cannot.be.deleted"),
	reconciledGlPostingCannotBeRemovedFromBegBal("A245","reconciled.gl.posting.cannot.be.removed.from.beg.bal"),
	glAccountInExpenseAllocationNotPermittedInArSetup("A246", "glAccount.in.expenseAllocation.not.permitted.in.arSetup"),
	glAccountInArSetupNotPermittedInExpenseAllocation("A247", "glAccount.in.arSetup.not.permitted.in.expenseAllocation"),
	accountNotFound("A248", "account.not.found"),
	noAdjustmentEntryFound("A249", "no.adjustment.entry.found"),
	invalidAdjustmentToUpdate("A250", "invalid.adjustment.to.update"),
	controlTypeInJournalOverrideForArGlAccount("A251","controlType.in.journal.override.for.arGlAccount"),
	duplicateSalesTaxReportName("A252","duplicate.sales.tax.report.name"),
	incorrectInvoiceStatus("A253","incorrect.invoice.status"),
	incorrectInvoiceId("A254","incorrect.invoice.id"),
	incorrectControlType("A255","incorrect.control.type"),
	wipReconciliationInProgress("A256","wip.rec.in.progress"),
	leaseCapReductionCannotExceedVehicleSales("A257", "leasecap.reduction.cannot.exceed.vehicleSales"),
	powerPostingTypeMissing("A258", "power.posting.type.missing"),
	memoWorksheetsAreMissing("A259", "memo.worksheets.not.present"),
	onlyDraftOrErrorCashReceiptCanDeleted("A260", "only.draftOrErrorCashReceipts.can.be.deleted"),
	wipRecFailed("A261", "wip.rec.failed"),
	bankDetailsAlreadyPresent("A262","bank.details.already.present"),
	journalRestrictedForUser("A263", "journalId.restricted"),
	vendorPayeeTypeNotPresent("A264","vendor.payee.type.not.present"),
	noChecksFoundForVendorPayeeTypeInGivenDateRange("A265","no.checks.found.for.vendor.payee.type.in.given.date.range"),
	form1099TypeNotPresentForVendor("A266","form.1099.type.not.present.for.vendor"),
	startDateAndEndDateNotSpecifiedCorrectly("A267","start.date.and.end.date.not.specified.correctly"),
	form1099GenerationInProgress("A268","form.1099.generation.in.progress"),
	incorrectYear("A269","incorrect.year"),
	form8300InvalidUpdateRequestForSuspiciousTransaction("A270","form8300.invalid.update.request.for.suspicious.transaction"),
	noPayerInfoFoundInApSetup("A271", "no.payer.info.found.in.apSetup"),
	cashReceiptIsAlreadyPosted("A272", "cannot.post.as.cashReceipt.is.already.posted"),
	leaseCapShouldBeCreatedAgainstOriginalDealEntry("A273", "leasecap.should.be.created.against.original.dealEntry"),
	cannotAdjustTheInvoice("A274", "cannot.adjust.the.invoice"),
	cannotAdjustTheInvoiceWithPO("A275", "cannot.adjust.invoice.with.PO"),
	M13ForYearAlreadyExist("A272","m13.for.year.exists"),
	M13ForYearAlreadyOpen("A273","m13.already.open"),
	M13SelectedYearGTCurrentYear("A274","m13.selected.year.gt.current.year"),
	M13NotOpen("A275","no.open.m13.found"),
	TrxYearNotOpenM13Year("A275","trx.year.not.open.m13.year"),
	M13CannotCloseIfyearNotClosed("A276","cannot.close.year.if.year.not.closed"),
	OwnersEquityAccountMissing("A276","owner.equity.account.missing"),
	ApOrArAccountUsed("A275","ap.or.ar.account.used"),
	EmailIdMissingForSendingMail("A276","email.id.missing.for.sending.mail"),
	EmailTemplateMissing("A277","email.template.missing"),
	EmailIdNotVerified("A278","email.id.not.verified"),
	EmailTemplateTypeMissing("A279","email.template.type.missing"),
	EmailPreferenceNotPresent("A280", "email.preference.not.present"),
	EmailIdNotPresentInCMS("A281", "email.id.not.present.in.cms"),
	NoCustomerDataFound("A282", "no.customer.data.found"),
	NoCustomersForStatementGeneration("A283", "no.customers.for.statement.generation"),
	InvalidCashReceiptStartNumber("A284", "cash.receipt.start.number.invalid"),
	ArGlAccountPresentInOtherArGlAccountInArSetup("A285","ar.glAccount.present.in.otherArGlAccount.in.arSetup"),
	customerPresentInMoreThanOneFCGroup("A286", "customer.is.present.in.more.than.one.group"),
	duplicateAssetsFoundForRestrictAndGrantAccess("A287", "duplicate.assets.found.for.restrict.and.grant.access"),
	batchNotInPendingStatus("A288", "batch.not.in.pending.status"),
	checkAlreadyVoided("A289", "check.already.voided"),
	invalidPoInvoiceStatus("A290", "invalid.poinvoice.status"),
	cannotPostCheckTransaction("A291","cannot.post.check.transaction"),
	cannotPostEFTTransaction("A292","cannot.post.EFT.transaction"),
	targetDealerMissing("A293","target.dealer.missing"),
	invalidOutboundMessageType("A294","invalid.outbound.message.type"),
	unauthorizedCAGLAccountSearchRequest("A295","unauthorized.ca.glAccount.search.request"),
	dealerNotExistInWorkSpace("A296","dealer.not.exist.in.workSpace"),
	invalidPaymentMethod("A297","invalid.payment.method"),
	chequeOutboundMissing("A298","outbound.for.cheque.is.missing"),
	moreThanOneChequeOutbound("A299","outbound.for.cheque.is.more.than.one"),
	iclOutboundMissing("A300","outbound.for.inter_company_link.is.missing"),
	moreThanOneICLOutbound("A301","outbound.for.inter_company_link.is.more.than.one"),
	payeeDealersMonthClosed("A302","payee.dealers.month.closed"),
	notAWorkspaceParent("A303","dealer.is.not.a.workspace.parent"),
	unauthorizedCARequest("A304","unauthorized.ca.request"),
	icNotAllowedForJournal("A305","inter.company.not.allowed.for.journal"),
	journalNotActive("A306","journal.not.active"),
	failedToCreateCACheck("A307", "failed.to.create.ca.check"),
	failedToCreateCAIFTxn("A308", "failed.to.create.ca.IF.transaction"),
	failedToCreateIFLink("A309", "failed.to.create.IFLink"),
	issuedCheckNotCreatedYet("A310", "issued.check.not.created.yet"),
	failedToUpdateTxnIdInCheck("A311", "failed.to.update.txnId.in.check"),
	txnIdAlreadyPresentInCheck("A312", "txnId.already.present.in.check"),
	childDealerAreMissing("A313","child.dealers.are.missing"),
	dealerDoesNotExistInWorkspace("A314","dealer.does.not.exist.in.workspace"),
	invalidTxnType("A315","invalid.transaction.type"),
	caPostingRestrictedForUser("A316","ca.posting.restricted.forUser"),
	noOutBoundForFoundForRequestId("A317","no.outbound.found.for.requestId"),
	noFailedOutBoundEntryFoundForRequestId("A318","no.failed.entry.outbound.for.requestId"),
	rollBackNotAllowedForEvent("A319","roll.back.not.allowed.for.event"),
	interDealerShipLinkMissing("A320","inter.dealership.link.missing"),
	countAdjustmentNotEnabled("A321", "countAdjustment.not.enabled.for.account"),
	unbalancedTransaction("A322", "unbalanced.transaction"),
	cannotCreatePOInvoice("A323", "cannot.create.PO.Invoice"),
	m13CloseDisabled("A324", "m13.close.disabled"),
	canOpenOnlyRecentClosedMonth("A325","cannot.open.older.month"),
	canNotOpenMonthWhichIsAlreadyOpen("A326","cannot.close.open.month"),
	cannotUpdateSetupDueToIdConflict("A327", "cannot.update.setup.due.to.id.conflict"),
	cannotUpdateInvoice("A328", "cannot.update.invoice"),
	cannotGenerateStatementForClosedMonth("A329","cannot.generate.statement.for.closed.month"),
	allocationSumNotEqualToHundred("A330","allocation.sum.not.equal.to.100"),
	franchiseFieldAllocationIsInvalid("A331","franchise.field.allocation.field.is.invalid"),
	depositedBankDepositCannotBeVoided("A332", "deposited.bank.deposit.cannot.be.voided"),
	alreadyInitiatedAnExport("A333", "already.initiated.an.export"),
	failedToInitiateBulkExport("A334", "failed.to.initiate.bulk.export"),
	invalidColumnNamesInTheUpdateRequest("A335", "invalid.columnnames.in.updateRequest"),
	castErrorWhileSqlUpdate("A336", "cast.error.while.updating.sql.data"),
	clearBalanceToDeactivate("A337","clear.balance.to.deactivate"),
	noTransactionFound("A338", "no.transaction.found" ),
	vendorPayableNotFound("A339","vendorPayableInfo.not.found"),
	chequeNotCreatedForBatch("A340","cheque.not.created.for.batch"),
	batchTransactionNotCreated("A341","batch.transaction.not.created"),
	franchiseIdIsMissing("A342","franchise.id.is.missing.in.request.payload"),
	cashReceiptNotFound("A343", "cash.receipt.not.found"),
	invalidCashReceiptId("A344", "invalid.cash.receipt.id"),
	draftAlreadyExists("A345", "draft.already.exists"),
	invalidExcelReportType("A346", "invalid.excel.report.type"),
	uploadValidPowerPostingFile("A347","upload.valid.powerPosting.file"),
	entryNumberCannotBeEmpty("A348","entry.number.cannot.empty"),
	noArSetupConfiguration("A349","no.ar.setup.configuration"),
	noDiscountAccountPresent("A350","no.discount.account.present"),
	noCashClearingAccountPresent("A351","no.cash.clearing.account.present"),
	noArCashReceiptJournalPresent("A352", "no.ar.cash.receipt.journal.present"),
	cannotDeleteCashInBankAccount("A353","cannot.delete.cash.in.bank.account"),
	outboundMessagePersistFailed("A354","invalid.outbound.message.persist.failed"),
	workSpaceDetailsResponsesNull("A355","workspace.detail.response.object.is.null"),
	ifExpenseAllocationSetupMissing("A356","expense.allocation.setup.missing.for.glAccount"),
	invalidRequest("A357","invalid.request"),
	invalidIFEAExpenseAllocationTransaction("A358","ifea.transaction.valid.failed"),
	invalidFSOemValueType("A359","invalid.oem.value.type"),
	invalidJournalsPresentInRequest("A360", "invalid.journals.present.in.request"),
	reconciledGlPostingMustBeSelectedInBegBal("A361", "reconciled.gl.posting.must.be.selected.in.beg.bal"),
	reconciledAmountDoesNotMatchReconciledCreditAndDebit("A362","reconciled.posting.amount.is.invalid.from.request"),
	eitherOemValueTypeOrValueTypeInCellCodeMustPresent("A363","eitherOemValueType.orValueTypeInCellCodeMust.present"),
	invalidOemId("A363","invalid.oem.id"),
	excelSheetNotRegistered("A364","excel.sheets.not.registered"),
	emptyExcelRequestDto("A365","empty.request.dto"),
	srcPostingsMissing("A366", "source.dealer.postings.missing"),
	duplicateUniqueKey("A367","duplicate.unique.key"),
	branchExistForBank("A368","branch.exist.for.bank"),
	failedToGenerateOutboundRequest("A369", "failed.to.generate.outbound.request"),
	duplicateInvoiceNumberInRequest("A370","duplicate.invoicenumber.in.request"),
	noApGlAccountFound("A371","no.ap.glAccount.found" ),
	dueDateCannotBeEmpty("A372", "due.date.cannot.be.empty"),
	invalidPO("A373", "invalid.PO"),
	noBankRecHistoryFound("A374","no.BankRecHistory.found"),
	nvoicePayRequestFailed("A375","nvoice.pay.request.failed"),
	thirdPartyPaymentProviderNotSpecified("A376","third.party.payment.provider.not.specified"),
	thirdPartyPaymentProviderSetupNotSpecified("A377","third.party.payment.provider.setup.not.specified"),
	invoiceNotInOpenStatus("A378", "invoice.not.in.open.status"),
	errorInPostingTransaction("A379","error.in.posting.transaction"),
	errorInCheckCreation("A380","error.in.check.creation"),
	maximumPostingsUpdateAllowedBreach("A381", "maximum.postings.update.allowed.breach"),
	ifTransactionFailed("A382","if.transaction.failed"),
	ifMappingMissing("A383","if.setup.mapping.missing"),
	dealerDoesNotBelongToTenant("A384", "dealer.doesnt.belong.to.tenant"),
	fsEntryNotFoundById("A385", "fsEntry.id.not.found"),
	internalSystemError("A386","internal.system.error"),
	vendorDetailsMissing("A387" , "vendor.details.missing"),
	alreadyReconciledInLastBankRec("A388", "reconciled.in.last.bank.rec"),
	fsEntryNotFoundForRequestedOemAndYear("A389","fs.entry.not.found"),
	noArSetupConfigurationForDealer("A390" , "no.ar.setup.configuration.for.dealer"),
	noApSetupConfigurationForDealer("A391" , "no.ap.setup.configuration.for.dealer"),
	missingGlAccount("A392","glAccount.details.missing.for.dealer"),
	poAndInvoiceAmountMismatch("A393", "po.and.invoiceAmount.misMatch"),
	inactiveCustomer("A394", "inactive.customer"),
	journalIdNotFoundInDefaultJournal("A395", "journal.not.found.in.default.journal"),
	checkAndDirectDepositJournalNotSet("A396", "check.and.direct.deposit.journal.not.set"),
	invalidCheckAndDirectDepositJournalNotSet("A397", "invalid.check.and.direct.deposit.journal.not.set"),
	noCashInBankAccountInGlamForCheckEmployee("A398", "no.cash.in.bank.account.in.glam.for.check.employee"),
	noCashClearingAccountInGlamForCheckEmployee("A399", "no.cash.clearing.account.in.glam.for.check.employee"),
	noCashInBankAccountInAPSetupForCheckEmployee("A400", "no.cash.in.bank.account.in.ap.setup.for.check.employee"),
	noCashInBankAccountInGlamForDirectDepositEmployees("A401", "no.cash.in.bank.account.in.glam.for.direct.deposit.employees"),
	noCashClearingAccountInGlamForDirectDepositEmployees("A402", "no.cash.clearing.account.in.glam.for.direct.deposit.employees"),
	noCashInBankAccountInAPSetupForDirectDepositEmployees("A403", "no.cash.in.bank.account.in.ap.setup.for.direct.deposit.employees"),
	transactionIsCheckPrintType("A404","transaction.is.check.print.type.not.allowed.for.reversal"),
	postingContainAPGLAccountVoidTheVendorInvoiceToReverseJournalEntry("A405","posting.has.ap.gl.accounts.not.allowed.for.reversal"),
	transactionTypeIsExpense("A406","transaction.type.expense.not.allowed.for.reversal"),
	referenceTypeNotAllowedForReversal("A407","ref.type.not.allowed.for.reversal"),
	forwardTransactionNotFound("A408","forward.transaction.not.found"),
	batchUpdateGlPostingsFailed("A409", "batch.update.glPostings.failed"),
	scheduleHistoryNotAllowedForSuchLongDurations("A410", "schedule.history.not.allowed.for.such.long.durations"),
	offsetAccountHasExpenseSetup("A411","ifsetup.offset.account.has.expense.allocation.setup"),
	glamExcelParseError("A412","glam.excel.parse.error"),
	glamExcelIncorrectFileLayout("A413","glam.excel.incorrect.file.layout"),
	glamExcelInvalidValues("A414","glam.excel.invalid.values"),
	payrollAdjustmentAlreadyCreated("A415", "payroll.adjustment.already.created"),
	invalidTransactionId("A416", "invalid.transaction.id"),
	inactiveGlAcccountPresentInVirtualTransaction("A417","inactive.glaccount.present.in.virtual.transaction"),
	invoiceinProcessingState("A418", "invoice.in.processing.state"),
	checkAndDirectDepositJournalInactive("A419", "check.and.direct.deposit.journal.inactive"),
	noEmployeeFound("A420", "no.employee.found"),
	failedToInitiateRecurringScheduleJob("A421", "failed.to.initiate.recurring.schedule.job"),
	endAfterOccurrencesShouldBeGreaterThanEnteredValue("A422", "end.after.occurrences.should.be.greater.than.entered.value"),
	payeeDealerIdNotFound("A423","payee.dealer.Id.not.found"),
	duplicateSiteOemCombinationPresentInRequest("A424","duplicate.site.oem.combination.present.in.request"),
	interFranchisePostingPermissionNotFound("A425","does.not.have.interFranchise.posting.permission"),
	timeZoneConversionIssue("A426","issue.in.converting.time.to.targetTimeZone"),
	transactionCreationFailedForLoggedInDealer("A427","transaction.creation.failed.for.logged.in.dealer"),
	noEmployeeFoundForDirectDeposit("A428", "no.employee.found.for.direct.deposit"),
	noEmployeeAddedFordd("A429", "no.employee.added.for.dd"),
	employeeAlreadyVoided("A430", "employee.already.voided"),
	cannotVoidDepositedCheck("A431", "cannot.void.deposited.check"),
	multipleDDNotAllowed("A432", "multiple.dd.not.allowed"),
	bulkRequestLimitExceeded("A433" , "bulk.request.limit.exceeded"),
	requestGotThrottled("A434" , "request.got.throttled"),
	checkVoidingInProgress("A435", "check.voiding.in.progress"),
	dailyDepositRequestInvalid("A436", "daily.deposit.request.invalid"),
	negativePaymentAmount("A437", "negative.payment.amount"),
	invoiceAmountPaymentAmountDoNotMatch("A438", "invoice.amount.payment.amount.do.not.match"),
	uploadValidPclCodesFile("A439","upload.valid.PclCodes.file")
	;

	FSError(String code, String key) {
		this.code = code;
		this.key = key;
	}


	FSError(String code, String key, String defaultMessage) {
		this.code = code;
		this.key = key;
		this.defaultMessage = defaultMessage;
	}

	private final String code;
	private final String key;
	private String defaultMessage = "unexpected error";
	private static final Map<String, FSError> map = new HashMap<>(values().length, 1);

	public String getCode() {
		return code;
	}

	@Override
	public String getErrorCode() {
		return code;
	}

	public String getKey() {
		return key;
	}

	@Override
	public String getOptionalDebugMessage() {
		return defaultMessage;
	}

	static {
		for (FSError c : values()) map.put(c.code, c);
	}

	public static FSError of(String code) {
		String invalidCode = "Invalid Error Code";

		if(Objects.isNull(code)) throw new IllegalArgumentException(invalidCode);

		FSError result = map.get(code);
		if (Objects.isNull(result)) {
			throw new IllegalArgumentException(invalidCode);
		}
		return result;
	}
}

