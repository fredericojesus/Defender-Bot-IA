import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;

public class Punch extends BotMotor {

	private final int damage = 100;
	private final int energyConsumption = 150;
	private TouchSensor touchSensor;

	public Punch() {
		super(2);
		touchSensor = new TouchSensor(SensorPort.S4);
	}

	public int getDamage() {
		return damage;
	}

	public int getEnergyConsumption() {
		return energyConsumption;
	}

	public void attack() throws InterruptedException {
		motor.stop();
		motor.setPower(100);
		motor.resetTachoCount();
		motor.forward();
		while (true) {
			if (touchSensor.isPressed()) {
				motor.stop();
				break;
			}
		}
		Thread.sleep(1000);
		rotateDegrees(100, motor.getTachoCount(), 0);
	}
}
