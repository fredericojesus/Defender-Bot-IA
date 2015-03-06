import java.io.File;
import java.util.ArrayList;

import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;

public class Platform extends BotMotor {

	private final int POWER = 15;
	private ColorSensor colorSensor;
	private final int YELLOW = 3;
	private final int WHITE = 6;
	private final int RED = 0;
	private final int GREEN = 1;
	private final int BLUE = 2;

	public Platform() {
		super(1);
		colorSensor = new ColorSensor(SensorPort.S1);
	}

	public ColorSensor getColorSensor() {
		return colorSensor;
	}

	public void detectEnemies(ArrayList<Enemy> enemies, boolean[] slots, SuperRobot defenderBot) throws InterruptedException {
		motor.stop();
		motor.setPower(POWER);
		motor.resetTachoCount();
		// começamos com cor -1 porque é uma cor que não existe
		int color = -1;

		Sound.playSample(new File("Tracking Object.wav"));
		Thread.sleep(500);
		
		// roda 360 graus, sempre que encontra um inimigo pára e guarda a sua informação
		while (true) {
			LCD.clear();
			showRobotDetails(defenderBot);
			LCD.drawString("DETECTING ENEMY", 1, 3);
			motor.forward();
			
			// enquanto a cor recebida for igual à cor inicial, roda
			while (colorSensor.getColorID() == color) {
			}
			// enquanto não encontrar vermelho, verde ou azul, roda
			while (true) {
				color = colorSensor.getColorID();
				if (color == RED || color == GREEN || color == BLUE) {
					int i = 0;
					// se o sensor continuar a encontrar a mesma cor vai acreditar que a cor está mesmo lá
					while(i < 5) {
						if (colorSensor.getColorID() == color)
							i++;
						else
							break;
					}
					
					if (i == 5)
						break;
				}
				
				//se já tiver dado uma volta completa (~360 graus) pára
				if (motor.getTachoCount() > 355) {
					motor.stop();
					LCD.clear();
					return;
				}
			}

			// adiciona o inimigo encontrado, guardando o seu tipo e posição pos 1
			if (motor.getTachoCount() > 10 && motor.getTachoCount() < 50 && !slots[0]) {
				motor.stop();
				enemies.add(new Enemy(1, color));
				slots[0] = true;
			}
			//pos 2
			else if (motor.getTachoCount() > 70 && motor.getTachoCount() < 110 && !slots[1]) {
				motor.stop();
				enemies.add(new Enemy(2, color));
				slots[1] = true;
			}
			//pos 3
			else if (motor.getTachoCount() > 130 && motor.getTachoCount() < 170 && !slots[2]) {
				motor.stop();
				enemies.add(new Enemy(3, color));
				slots[2] = true;
			}
			//pos 4
			else if (motor.getTachoCount() > 190 && motor.getTachoCount() < 230 && !slots[3]) {
				motor.stop();
				enemies.add(new Enemy(4, color));
				slots[3] = true;
			}
			//pos 5
			else if (motor.getTachoCount() > 250 && motor.getTachoCount() < 290 && !slots[4]) {
				motor.stop();
				enemies.add(new Enemy(5, color));
				slots[4] = true;
			}
			//pos 6
			else if (motor.getTachoCount() > 310 && motor.getTachoCount() < 350 && !slots[5]) {
				motor.stop();
				enemies.add(new Enemy(6, color));
				slots[5] = true;
			} else
				continue;

			LCD.clear();
			showRobotDetails(defenderBot);
			switch (color) {
			case RED:
				LCD.drawString("TANK DETECTED", 2, 2);
				Sound.playSample(new File("Object Detected.wav"));
				break;
			case GREEN:
				LCD.drawString("ARTI DETECTED", 2, 2);
				Sound.playSample(new File("Object Detected.wav"));
				break;
			case BLUE:
				LCD.drawString("SOLD DETECTED", 2, 2);
				Sound.playSample(new File("Object Detected.wav"));
				break;
			default:
				break;
			}
			LCD.drawString("POSITION", 4, 4);
			LCD.drawInt(enemies.get(enemies.size() - 1).getSlot(), 13, 4);
			Thread.sleep(1500);
			Sound.playSample(new File("Tracking Object.wav"));
			Thread.sleep(500);
			LCD.clear();
		}
	}

	public void preparePunchAttack(int slot, SuperRobot defenderBot) throws InterruptedException {
		initialPosition(defenderBot);
		Thread.sleep(1000);
		LCD.clear();
		showRobotDetails(defenderBot);
		LCD.drawString("PUNCH ATTACK", 2, 3);

		switch (slot) {
		case 1:
			detectColor(250, WHITE);
			break;
		case 2:
			return;
		case 3:
			detectColor(10, WHITE);
			break;
		case 4:
			detectColor(70, WHITE);
			break;
		case 5:
			detectColor(130, WHITE);
			break;
		case 6:
			detectColor(190, WHITE);
			break;
		}
	}

	public void prepareCraneAttack(int slot, int state, SuperRobot defenderBot) throws InterruptedException {
		initialPosition(defenderBot);
		Thread.sleep(1000);
		LCD.clear();
		showRobotDetails(defenderBot);
		if (state == 0)
			LCD.drawString("CRANE ATTACK", 2, 3);
		else if (state == 1)
			LCD.drawString("FINISH HIM", 3, 3);
		else
			LCD.drawString("CLEANING", 4, 3);
			
		switch (slot) {
		case 1:
			detectColor(70, WHITE);
			break;
		case 2:
			detectColor(130, WHITE);
			break;
		case 3:
			detectColor(190, WHITE);
			break;
		case 4:
			detectColor(250, WHITE);
			break;
		case 5:
			return;
		case 6:
			detectColor(10, WHITE);
			break;
		}
	}

	public void prepareSoundAttack(int slot, int color, SuperRobot defenderBot) throws InterruptedException {
		initialPosition(defenderBot);
		Thread.sleep(1000);
		LCD.clear();
		showRobotDetails(defenderBot);
		LCD.drawString("SOUND ATTACK", 2, 3);

		int back = 0;
		switch (slot) {
		case 1:
			detectColor(0, color);
			back = -2;
			break;
		case 2:
			detectColor(70, color);
			back = -4;
			break;
		case 3:
			detectColor(130, color);
			back = -5;
			break;
		case 4:
			detectColor(190, color);
			back = -6;
			break;
		case 5:
			detectColor(250, color);
			back = -6;
			break;
		case 6:
			detectColor(310, color);
			back = -6;
			break;
		}

		Thread.sleep(1000);
		motor.resetTachoCount();
		motor.setPower(20);
		motor.backward();
		while (true) {
			if (motor.getTachoCount() < back)
				break;
		}
		motor.stop();
		motor.setPower(15);
	}

	/**
	 * Função que roda o motor até encontrar a cor desejada depois do ângulo desejado
	 * 
	 * @param stop - ângulo
	 * @param color - cor
	 */
	public void detectColor(int stop, int color) {
		motor.stop();
		motor.setPower(POWER);
		motor.resetTachoCount();
		motor.forward();

		for (int i = 0; i < 3;) {
			if (colorSensor.getColorID() == color && motor.getTachoCount() > stop)
				i++;
		}

		motor.stop();
	}

	/**
	 * Função que roda até encontrar a cor a amarela (posição inicial)
	 * Enquanto roda, imprime no ecrã INITIAL POSITION
	 */
	public void initialPosition(SuperRobot defenderBot) {
		LCD.clear();
		showRobotDetails(defenderBot);
		LCD.drawString("INITIAL", 5, 3);
		LCD.drawString("POSITION", 4, 4);
		motor.stop();
		motor.setPower(POWER);
		motor.resetTachoCount();
		motor.forward();

		for (int i = 0; i < 5;) {
			if (motor.getTachoCount() < 180)
				continue;
			if (colorSensor.getColorID() == YELLOW)
				i++;
		}

		motor.stop();
	}
	
	/**
	 * Função que imprime na primeira linha do display a vida, energia e curas
	 * do robot -750-500--1-2-3-
	 * 
	 * Nas curas imprime um X se a cura já foi usada -750-500--X-2-3-
	 */
	private static void showRobotDetails(SuperRobot defenderBot) {
		//limpa a primeira linha
		LCD.clear(0);

		//imprime a vida e a energia
		LCD.drawInt(defenderBot.getLife(), 1, 0);
		LCD.drawInt(defenderBot.getEnergy(), 5, 0);

		//imprime as curas
		for (int i = 0, j = 10; i < defenderBot.getCures().size(); i++, j += 2) {
			if (defenderBot.getCures().get(i).isUsed())
				LCD.drawString("X", j, 0);
			else
				LCD.drawInt(defenderBot.getCures().get(i).getNumber(), j, 0);
		}
	}
}
