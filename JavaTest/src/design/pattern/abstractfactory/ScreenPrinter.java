package design.pattern.abstractfactory;

class ScreenPrinter implements Printer {

	@Override
	public void print() {
		System.out.println("screen");
	}
}