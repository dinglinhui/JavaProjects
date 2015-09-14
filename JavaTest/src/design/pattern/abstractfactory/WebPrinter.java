package design.pattern.abstractfactory;

class WebPrinter implements Printer {

	@Override
	public void print() {
		System.out.println("web");
	}
}