import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;

public abstract class BotMotor {

	protected NXTMotor motor;

	public BotMotor(int port) {
		if (port == 1)
			motor = new NXTMotor(MotorPort.A);
		else if (port == 2)
			motor = new NXTMotor(MotorPort.B);
		else
			// if(port == 3)
			motor = new NXTMotor(MotorPort.C);
	}

	public NXTMotor getMotor() {
		return motor;
	}

	// direction - 1 is forward, 0 is backward
	public void rotateDegrees(int power, int degrees, int direction) {
		motor.stop();
		motor.setPower(power);
		motor.resetTachoCount();

		if (direction == 0) {
			motor.backward();
			while (motor.getTachoCount() > -degrees) {
			}
			motor.stop();
		} else {
			motor.forward();
			while (motor.getTachoCount() < degrees) {
			}
			motor.stop();
		}
	}
}
