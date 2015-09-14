package design.pattern.singleton;

//懒汉式单例类.在第一次调用的时候实例化自己   
public class Singleton {
	// 私有的默认构造子
	private Singleton() {
	}

	// 注意，这里没有final
	private static Singleton single = null;

	// 静态工厂方法
	public static Singleton getInstance() {
		if (single == null) {
			single = new Singleton();
		}
		return single;
	}
}