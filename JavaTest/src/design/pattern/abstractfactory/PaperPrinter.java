package design.pattern.abstractfactory;

class PaperPrinter implements Printer {

	@Override
	public void print() {
		System.out.println("paper");
	}
}