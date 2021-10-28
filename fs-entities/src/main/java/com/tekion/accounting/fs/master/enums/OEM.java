package com.tekion.accounting.fs.master.enums;

import java.util.Arrays;

import static com.tekion.core.utils.TStringUtils.isBlank;

public enum OEM {
	GM("GM", "gm", "GMC"),
	GMInternal("GM", "gm", "GMC"),
	Toyota("Toyota", "toyota", "Toyota"),
	FCA("FCA", "fca", "FCA"),
	Nissan("Nissan", "renault_nissan_mitsubishi_alliance", "Nissan"),
	Acura("HONDA","honda", "Acura"),
	Volkswagen("Volkswagen","volkswagen", "Volkswagen"),
	BMW("BMW","bmw", "BMW"),
	Hyundai("Hyundai","hyundai", "Hyundai"),
	HyundaiInternal("Hyundai","hyundai", "Hyundai"),
	Kia("Kia","Kia", "Kia"),
	Audi("Audi","volkswagen", "Audi"),
	Lexus("Lexus","toyota", "Lexus"),
	Genesis("Hyundai","hyundai", "Genesis"),
	Mitsubishi("Reno","renault_nissan_mitsubishi_alliance", "Mitsubishi"),
	Infiniti("Infiniti","renault_nissan_mitsubishi_alliance", "Infiniti"),
	MB("Benz","benz", "Others"),
	Honda("HONDA","honda", "Honda"),
	Ford("Ford","ford", "Ford"),
	Chrysler("FCA", "fca", "Chrysler"),
	Porsche("Porsche", "porsche", "Porsche"),
	Lamborghini("Lamborghini", "lamborghini", "Lamborghini"),
	Maserati("FCA", "fca", "Maserati"),
	Mazda("Mazda", "mazda", "Mazda"),
	RollsRoyce("bmw", "bmw", "rollsroyce"),
	Bentley("volkswagen", "volkswagen", "bentley"),
	@Deprecated
	VW("Volkswagen","volkswagen", "Volkswagen");

	private String brand;
	private String oem;
	private String make;

	OEM(String brand, String oem, String make){
		this.brand = brand;
		this.oem = oem;
		this.make = make;
	}

	public String getBrand(){
		return brand;
	}

	public String getMake() {
		return make;
	}

	public String getOem(){
		return oem;
	}

	public static OEM fromOem(String oem){
		if (isBlank(oem)) {
			return null;
		}
		return Arrays.stream(values())
				.filter(tempOem -> tempOem.name().equalsIgnoreCase(oem))
				.findFirst()
				.orElse(null);
	}
}
