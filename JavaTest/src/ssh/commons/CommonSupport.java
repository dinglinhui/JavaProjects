package ssh.commons;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import ssh.db.ConnectionFactory;
import ssh.model.Dog;
import ssh.model.Person;

/***
 * 反射自动查询和封装的类
 *
 * @author dinglinhui
 * 
 * */
public class CommonSupport {

	/**
	 * @param obj需要保存的对象
	 * @param string
	 *            保存对象的sql语句
	 * */
	public static String createSqlByObject(Object obj) {

		StringBuffer sb = new StringBuffer("insert into ");

		// 得到对象的类
		Class c = obj.getClass();
		// 得到对象中的所有方法
		Method[] ms = c.getMethods();

		// 得到对象中所有的属性,虽然在这个里面就能获取所有的字段名，但不建议这么用，破坏类的封装性
		Field[] fs = c.getDeclaredFields();
		// 得到对象类的名字
		String cname = c.getName();
		System.out.println("类名字： " + cname);
		// 表名字
		String tableName = cname.split("\\.")[cname.split("\\.").length - 1];
		System.out.println("表名字： " + tableName);
		// 追加表名和（左边的符号
		sb.append(tableName).append(" (");
		// 存放列名的集合
		List<String> columns = new ArrayList<String>();
		// 存放值的集合
		List values = new ArrayList();
		// 遍历方法
		for (Method m : ms) {
			String methodName = m.getName();// 获取每一个方法名
			// 只得到具有get方法的属性，getClass除外
			if (methodName.startsWith("get")
					&& !methodName.startsWith("getClass")) {
				System.out.println("属性名：" + methodName);
				String fieldName = methodName.substring(3, methodName.length());
				System.out.println("字段名：" + fieldName);
				columns.add(fieldName);// 将列名添加到列名的集合里
				try {
					Object value = m.invoke(obj, null);
					System.out.println("执行方法返回的值：" + value);
					if (value instanceof String) {
						System.out.println("字符串类型字段值：" + value);
						values.add("'" + value + "'");// 加上两个单引号，代表是字符串类型的
					} else {
						System.out.println("数值类型字段值：" + value);
						values.add(value);// 数值类型的则直接添加
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}

		for (int i = 0; i < columns.size(); i++) {
			String column = columns.get(i);
			Object value = values.get(i);
			System.out.println("列名：" + column + " 值：  " + value);
		}

		// 拼接列名
		for (int i = 0; i < columns.size(); i++) {
			if (i == columns.size() - 1) {
				sb.append(columns.get(i)).append(" ) ");
			} else {
				sb.append(columns.get(i)).append(" , ");
			}
		}
		System.out.println(" 拼接列名后的sql：" + sb.toString());
		sb.append(" values ( ");
		// 拼接值
		for (int i = 0; i < values.size(); i++) {
			if (i == values.size() - 1) {
				sb.append(values.get(i)).append(" ) ");
			} else {
				sb.append(values.get(i)).append(" , ");
			}
		}

		System.out.println(" 拼接值后的sql：" + sb.toString());

		// 返回组装的sql语句
		return sb.toString();
	}

	/**
	 * 将对象保存在数据库中
	 * 
	 * @param obj
	 *            保存的对象
	 * **/
	public static void addOne(Object obj) {
		try {
			Connection con = ConnectionFactory.getCon();
			String sql = createSqlByObject(obj);
			PreparedStatement ps = con.prepareStatement(sql);
			int result = ps.executeUpdate();
			if (result == 1) {
				System.out.println("保存成功！");
			} else {
				System.out.println("保存失败！");
			}
			ps.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 根据类名字和一个查询条件 自动封装一个Bean对象
	 * 
	 * @param columnName
	 *            列名
	 * @param value
	 *            列值
	 * @return {@link Object}
	 * 
	 * */
	public static Object getOneObject(String className, String columnName,
			String value) {

		String tableName = className.split("\\.")[className.split("\\.").length - 1];
		System.out.println("表名字： " + tableName);

		// 根据类名来创建对象
		Class c = null;
		try {
			c = Class.forName(className);// 反射生成一个类实例
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 拼接sql语句
		StringBuffer sb = new StringBuffer();
		sb.append("select * from ").append(tableName).append(" where ")
				.append(columnName).append(" = ").append("'").append(value)
				.append("'");

		String querySql = sb.toString();
		System.out.println("查询的sql语句为:" + querySql);

		Object obj = null;
		try {
			Connection con = ConnectionFactory.getCon();// 得到一个数据库连接
			PreparedStatement ps = con.prepareStatement(querySql);// 预编译语句
			ResultSet rs = ps.executeQuery();// 执行查询
			// 得到对象的所有的方法
			Method ms[] = c.getMethods();

			if (rs.next()) {
				// 生成一个实例
				obj = c.newInstance();

				for (Method m : ms) {
					String mName = m.getName();
					if (mName.startsWith("set")) {
						// 根据方法名字自动提取表中对应的列名
						String cname = mName.substring(3, mName.length());
						// 打印set的方法名
						System.out.println(cname);
						// 得到方法的参数类型
						Class[] params = m.getParameterTypes();
						for (Class cp : params) {
							System.out.println(cp.toString());
						}
						// 如果参数是String类型，则从结果集中，按照列名取到的值，进行set
						// 从params[0]的第一个值，能得到该数的参数类型
						if (params[0] == String.class) {//
							m.invoke(obj, rs.getString(cname));
							// 如果判断出来是int形，则使用int
						} else if (params[0] == int.class) {
							m.invoke(obj, rs.getInt(cname));
						}
					}
				}

			} else {
				System.out.println("请注意：" + columnName + "=" + value
						+ "的条件，没有查询到数据!!");
			}
			rs.close();
			ps.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return obj;
	}

	public static void main(String[] args) throws Exception {

		Dog d = new Dog(21, "小不点", "藏獒", "灰色", 25);
		Person p = new Person(6, "大象hadoop", 10, "家住Apache基金组织");
		createSqlByObject(d);
//		addOne(d);// 给dog表添加一条数据
//		addOne(p);// 给person表添加一条数据

		// 强制转换为原始类
		Dog d1 = (Dog) getOneObject("ssh.model.Dog", "id", "1");
		System.out.println(d1);

		Person p1 = (Person) getOneObject("ssh.model.Person", "id", "1");
		// Person p1=(Person)getOneObject("ssh.model.Person", "name", "王婷");
		System.out.println(p1);

	}

}
