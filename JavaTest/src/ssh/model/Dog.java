package ssh.model;

public class Dog {

	private int id;
	private String name;
	private String type;
	private String color;
	private int weight;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public Dog() {
	}

	public Dog(int id, String name, String type, String color, int weight) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.color = color;
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "Dog [id=" + id + ", name=" + name + ", type=" + type
				+ ", color=" + color + ", weight=" + weight + "]";
	}

}
