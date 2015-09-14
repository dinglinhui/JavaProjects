package design.pattern.abstractfactory;

public class Main {
	public static void main(String[] args) {

		// get shape factory
		AbstractFactory shapeFactory = FactoryProducer.getFactory("SHAPE");

		// get an object of Shape Circle
		Shape shape1 = shapeFactory.getShape("CIRCLE");

		// call draw method of Shape Circle
		shape1.draw();

		// get an object of Shape Rectangle
		Shape shape2 = shapeFactory.getShape("RECTANGLE");

		// call draw method of Shape Rectangle
		shape2.draw();

		// get an object of Shape Square
		Shape shape3 = shapeFactory.getShape("SQUARE");

		// call draw method of Shape Square
		shape3.draw();

		// get printer factory
		AbstractFactory printerFactory = FactoryProducer.getFactory("printer");

		Printer printer1 = printerFactory.getPrinter("Paper");
		printer1.print();
		Printer printer2 = printerFactory.getPrinter("Web");
		printer2.print();
		Printer printer3 = printerFactory.getPrinter("Screen");
		printer3.print();
	}
}