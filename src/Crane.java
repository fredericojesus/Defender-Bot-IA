public class Crane extends BotMotor {

	private final int damage = 200;
	private final int energyConsumption = 300;

	public Crane() {
		super(3);
	}

	public int getDamage() {
		return damage;
	}

	public int getEnergyConsumption() {
		return energyConsumption;
	}

	public void down() {
		rotateDegrees(80, 450, 1);
	}

	public void up() {
		rotateDegrees(80, 455, 0);
	}

}
