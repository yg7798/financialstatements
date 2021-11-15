package com.tekion.accounting.fs.service.oemPayload;

import com.tekion.accounting.fs.beans.common.AccountingOemFsCellCode;
import com.tekion.accounting.fs.integration.Detail;
import com.tekion.accounting.fs.enums.AccountingError;
import com.tekion.accounting.fs.enums.OemCellValueType;
import com.tekion.accounting.fs.enums.OemValueType;
import com.tekion.accounting.fs.service.integration.IntegrationClient;
import com.tekion.accounting.fs.service.oemPayload.beans.FillDetailContext;
import com.tekion.accounting.fs.common.utils.DealerConfig;
import com.tekion.core.exceptions.TBaseRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class MazdaFSService extends AbstractFinancialStatementService  {

	public MazdaFSService(DealerConfig dc, IntegrationClient ic, FsXMLServiceImpl fs) {
		super(dc, ic, fs);
	}

	@Override
	protected void setValueInDetail(FillDetailContext fdc) {
		String valueString = fdc.getValueString();
		AccountingOemFsCellCode cellCode = fdc.getCellCode();
		Detail detail = fdc.getDetail();

		if(OemCellValueType.DATE.name().equals(cellCode.getValueType())){
			addDateInDetail(fdc);
			return;
		}

		if(Objects.isNull(fdc.getCellCode().getOemValueType())){
			if(OemCellValueType.BALANCE.name().equals(cellCode.getValueType())){
				detail.setBalance1(valueString);
				return;
			}else if(OemCellValueType.COUNT.name().equals(cellCode.getValueType())){
				detail.setUnit1(valueString);
				return;
			}else{
				log.error("Either Oem value Type as BALANCE1, BALANCE2, UNIT1, UNIT2 or ValueType as BALANCE, COUNT must present for cell {}", cellCode.getCode());
				throw new TBaseRuntimeException(AccountingError.eitherOemValueTypeOrValueTypeInCellCodeMustPresent, cellCode.getCode());
			}
		}

		try{
			switch (OemValueType.valueOf(cellCode.getOemValueType().toUpperCase())){
				case UNIT1:
					detail.setUnit1(valueString);
					break;
				case UNIT2:
					detail.setUnit2(valueString);
					break;
				case BALANCE1:
					detail.setBalance1(valueString);
					break;
				case BALANCE2:
					detail.setBalance2(valueString);
					break;
				case VALUE:
					detail.setAccountValue(valueString);
					break;
				default:
					break;
			}

		}catch (IllegalArgumentException e){
			log.warn("invalid FS OemValueType {}", cellCode.getOemValueType());
		}
	}

	private void addDateInDetail(FillDetailContext fdc) {
		String unit1 = "00" + fdc.getCellDetail().getStringValue().substring(0,4);
		String unit2 = fdc.getCellDetail().getStringValue().substring(4,8) + "00";
		fdc.getDetail().setUnit1(unit1);
		fdc.getDetail().setUnit2(unit2);
	}
}
