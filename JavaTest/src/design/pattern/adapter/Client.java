package design.pattern.adapter;

public class Client {

	public static void main(String[] args) {
		AbstractComputerPower computerPower = new ComputerPowerAdapter();
		computerPower.provideDirectCurrent();
	}
}
