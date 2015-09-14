package design.pattern.adapter;

public class ChinesePower implements AbstractNationalPower {

	@Override
	public String provideAlternatableCurrent() {
		return "220V 交流电。";
	}
}

