package design.pattern.singleton;

public class TestSingleton {
	String name = null;

	private TestSingleton() {
	}

	private static TestSingleton ts = null;

	public static TestSingleton getInstance() {
		if (ts == null) {
			ts = new TestSingleton();
		}
		return ts;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void printInfo() {
		System.out.println("the name is " + name);
	}

}