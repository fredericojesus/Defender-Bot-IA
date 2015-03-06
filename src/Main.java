import java.io.File;
import java.util.ArrayList;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Sound;

public class Main {

	private static SuperRobot defenderBot = new SuperRobot();
	private static ArrayList<Enemy> enemies = new ArrayList<Enemy>();
	
	//está variável representa todos os 6 slots, se true está ocupado, se false não está
	private static boolean[] slots = new boolean[6]; 
	private static int rounds = 13;

	public static void main(String[] args) throws InterruptedException {
		//preenche todos os slots a 0, ou seja, o ambiente está vazio
		for (int i = 0; i < 6; i++)
			slots[i] = false;

		LCD.drawString("PRESS", 5, 1);
		LCD.drawString("ENTER", 5, 3);
		LCD.drawString("TO START", 4, 5);
		Button.ENTER.waitForPress();
		LCD.clear();
		Thread.sleep(2000);

		//ciclo de jogo, jogadas ímpares é o inimigo, jogares pares o robot
		for (int i = 1; i <= rounds; i++) {
			if (isWon())
				break;
			
			//jogadas pares, robot
			if (i % 2 == 0) {
				if (enemies.size() < 6) {
					defenderBot.initialPosition();
					Thread.sleep(1000);
					defenderBot.detectEnemies(enemies, slots);
				}
				defenderBot.regen();
				defenderBot.resetCures();
				resetAttackedEnemies();
				showRound(i, 1);
				Sound.playSample(new File("Robot.wav"));
				Thread.sleep(1000);
				Sound.playSample(new File("Play.wav"));
				defenderBot.robotPlay(enemies, i);

			} 
			//jogadas ímpares, inimigos
			else {
				int damageDealt = 0;
				for (int j = 0; j < enemies.size(); j++)
					damageDealt += enemies.get(j).attack(defenderBot);

				if (defenderBot.getLife() > 0) {
					showEnemiesRound(i, damageDealt);
					Button.ENTER.waitForPress();
				} else {
					showEndGame(0);
					Sound.playSample(new File("Game Over.wav"));
					Button.ENTER.waitForPress();
					return;
				}
				Thread.sleep(1000);
				
				int clean = 0;
				// limpar a mesa, retira do jogo os inimigos que já não têm ataques
				for (int k = 0; k < enemies.size(); k++) {
					if (enemies.get(k).getAttacks() == 0 && enemies.get(k).isInGame()) {
						defenderBot.finishHim(enemies.get(k), 2);
						clean = 1;
					}
				}
				    
				if (clean == 0) {
					LCD.clear();
					LCD.drawString("THERE", 6, 2);
					LCD.drawString("IS NOTHING", 3, 3);
					LCD.drawString("TO CLEAN", 4, 4);
					Thread.sleep(1500);
				}
					
				showRound(i, 1);
			}
		}

		Thread.sleep(1000);
		showEndGame(1);
		defenderBot.winningDance();
		Button.ENTER.waitForPress();
		showEnemies();
		Button.ENTER.waitForPress();
	}

	/**
	 * Função que imprime no display os detalhes do robot, a ronda em que está jogo e
	 * dano dado pelos inimigos na ronda
	 * 
	 * @param round - ronda em que está o jogo
	 * @param damageDealt - dano dado pelos inimigos na ronda
	 */
	private static void showEnemiesRound(int round, int damageDealt) {
		LCD.clear();
		showRobotDetails();
		showRound(round);
		LCD.drawString("E DAMAGE --> ", 0, 4);
		LCD.drawInt(damageDealt, 13, 4);
		LCD.drawString("   CLEAN <-- ", 1, 6);
	}

	/**
	 * Função que imprime no display os detalhes do robot, a ronda em que está o jogo e
	 * duas opções:
	 * 		SHOW ENEMIES - para mostrar todos os inimigos
	 * 		PLAY ROBOT   - para o robot jogar a sua ronda
	 * 
	 * @param round - ronda em que está o jogo
	 * @param player - 1 se for o round do robot, 2 se for o round dos inimigos
	 * @throws InterruptedException
	 */
	private static void showRound(int round, int player) throws InterruptedException {
		// position = 4 se tem focus(<--) na opção SHOW ENEMIES
		// position = 5 se tem focus(<--) na opção ROBOT PLAY
		int position = 4;
		while (true) {
			LCD.clear();
			showRobotDetails();
			showRound(round);
			LCD.drawString("SHOW ENEMIES", 0, 4);
			if (round % 2 == 0)
				LCD.drawString("ROBOT PLAY", 2, 5);
			else
				LCD.drawString("NEXT ROUND", 2, 5);

			LCD.drawString("<--", 12, position);
			while (true) {
				if (Button.LEFT.isDown() || Button.RIGHT.isDown()) {
					if (position == 4) {
						LCD.clear(12, 4, 3);
						LCD.drawString("<--", 12, 5);
						position = 5;
					} else {
						LCD.clear(12, 5, 3);
						LCD.drawString("<--", 12, 4);
						position = 4;
					}
					Thread.sleep(500);
				} else if (Button.ENTER.isDown()) {
					if (position == 4)
						showEnemies();
					else
						return;
					Thread.sleep(500);
					break;
				}
			}
		}
	}

	/**
	 * Função que imprime no display os detalhes do robot e todos os inimigos
	 * 1- TANK   200  2
	 * 2- EMPTY
	 * 3- DEAD
	 * ..
	 * @throws InterruptedException 
	 */
	private static void showEnemies() throws InterruptedException {
		LCD.clear();
		showRobotDetails();
		
		for(int i = 1; i < 7; i++) {
			LCD.drawString(i + "-", 0, i);
			if (getEnemy(i) == null)
				LCD.drawString("EMPTY", 3, i);
			else if (getEnemy(i).getLife() == 0)
				LCD.drawString("DEAD", 3, i);
			else {
				LCD.drawString(getEnemy(i).getTypeName(), 3, i);
				LCD.drawInt(getEnemy(i).getLife(), 10, i);
				LCD.drawInt(getEnemy(i).getAttacks(), 14, i);
			}
		}
		
		Thread.sleep(500);
		Button.waitForAnyPress();
	}
	
	/**
	 * @param slot - slot do inimigo pretendido 
	 * @return - retorna o inimigo que está num determinado slot
	 * 			 ou null se não existir nenhum nesse slot
	 */
	private static Enemy getEnemy(int slot) {
		for (int i = 0; i < enemies.size(); i++) {
			if (enemies.get(i).getSlot() == slot)
				return enemies.get(i);
		}
		return null;
	}

	/**
	 * Função que imprime na primeira linha do display a vida, energia e curas
	 * do robot -750-500--1-2-3-
	 * 
	 * Nas curas imprime um X se a cura já foi usada -750-500--X-2-3-
	 */
	private static void showRobotDetails() {
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

	/**
	 * Função que imprime na terceira a linha a ronda em que estamos
	 * ----ROUND-XX----
	 */
	private static void showRound(int round) {
		//limpa a terceira linha
		LCD.clear(2);

		//imprime a ronda
		LCD.drawString("ROUND", 4, 2);
		LCD.drawInt(round, 10, 2);
	}

	/**
	 * Função que imprime no display se o robot ganhou ou perdeu o jogo
	 * 
	 * @param win - se 1 o robot saiu vencedor, se 0 o robot perdeu
	 */
	private static void showEndGame(int win) {
		LCD.clear();
		LCD.drawString("DEFENDER-BOT", 2, 2);

		if (win == 1)
			LCD.drawString("WINS", 6, 4);
		else
			LCD.drawString("LOSE", 6, 4);
	}
	
	/**
	 * Função que mete o atributo attacked de todos os inimigos a false, ou seja,
	 * vai ser usada depois do robot decidir que não quer atacar mais na ronda 
	 */
	private static void resetAttackedEnemies() {
		for(int i = 0; i < enemies.size(); i++) {
			enemies.get(i).setAttacked(false);
		}
	}
	
	/**
	 * Função que verifica se o robot já ganhou o jogo
	 * 
	 * @return - true se já ganhou, false se ainda não
	 */
	private static boolean isWon() {
		if (enemies.size() < 6)
			return false;
		for (int i = 0; i < enemies.size(); i++) {
			if (enemies.get(i).getLife() > 0 && enemies.get(i).getAttacks() > 0)
				return false;
		}
		return true;
	}
}
