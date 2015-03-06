import java.io.File;
import java.util.ArrayList;

import lejos.nxt.LCD;
import lejos.nxt.Sound;

public class SuperRobot {

	private final int MAX_ENERGY = 500;
	private final int INITIAL_LIFE = 750;
	private final int INITIAL_ENERGY = 500;
	private final int SOUND_DAMAGE = 50;
	private final int SOUND_ENERGY_CONSUMPTION = 50;
	private final int TANK = 0;
	private final int ARTILLERY = 1;
	private final int SOLDIER = 2;
	private final int SOUND = 0;
	private final int PUNCH = 1;
	private final int CRANE = 2;

	private int life;
	private int energy;
	private Platform platform;
	private Punch punch;
	private Crane crane;
	private ArrayList<Cure> cures = new ArrayList<Cure>();

	public SuperRobot() {
		life = INITIAL_LIFE;
		energy = INITIAL_ENERGY;

		platform = new Platform();
		punch = new Punch();
		crane = new Crane();

		cures.add(new Cure(1));
		cures.add(new Cure(2));
		cures.add(new Cure(3));
	}

	public int getLife() {
		return life;
	}

	public void removeLife(int life) {
		this.life -= life;
		if (this.life < 0)
			this.life = 0;
	}

	public int getEnergy() {
		return energy;
	}

	/**
	 * Função que regenera a energia do robot, o robot recupera 50% da sua energia 
	 * depois de cada turno, não podendo ter mais que o máximo(MAX_ENERGY)
	 */
	public void regen() {
		energy += (energy * 0.50);
		if (energy > MAX_ENERGY)
			energy = MAX_ENERGY;
	}

	public Platform getPlatform() {
		return platform;
	}

	public Punch getPunch() {
		return punch;
	}

	public Crane getCrane() {
		return crane;
	}

	public ArrayList<Cure> getCures() {
		return cures;
	}

	public void useCure(int cure) {
		life += cures.get(cure).getLifeRegen();
		if (life > INITIAL_LIFE)
			life = INITIAL_LIFE;

		energy -= cures.get(cure).getEnergyConsumption();
		cures.get(cure).setUsed(true);
	}

	public void resetCures() {
		for (int i = 0; i < cures.size(); i++)
			cures.get(i).setUsed(false);
	}

	public int getSoundEnergyConsumption() {
		return SOUND_ENERGY_CONSUMPTION;
	}

	public void detectEnemies(ArrayList<Enemy> enemies, boolean[] slots) throws InterruptedException {
		platform.detectEnemies(enemies, slots, this);
	}

	public void initialPosition() {
		platform.initialPosition(this);
	}

	private void punchAttack(Enemy enemy) throws InterruptedException {
		if (energy >= 100) {
			platform.preparePunchAttack(enemy.getSlot(), this);
			Thread.sleep(1500);
			punch.attack();

			energy -= punch.getEnergyConsumption();
			enemy.removeLife(punch.getDamage());
			enemy.setAttacked(true);

			if (enemy.getLife() <= 0)
				finishHim(enemy, 1);
		}
	}

	private void craneAttack(Enemy enemy) throws InterruptedException {
		if (energy >= 300) {
			platform.prepareCraneAttack(enemy.getSlot(), 0, this);
			Thread.sleep(1500);
			platform.rotateDegrees(40, 10, 0);
			crane.down();
			Thread.sleep(500);
			platform.rotateDegrees(40, 15, 1);
			Thread.sleep(1500);
			crane.up();

			energy -= crane.getEnergyConsumption();
			enemy.removeLife(crane.getDamage());
			enemy.setAttacked(true);
			enemy.setInGame(false);
		}
	}

	/**
	 * Função usada para retirar um inimigo do ambiente porque este
	 * já morreu
	 * @param enemy - inimigo para retirar
	 * @throws InterruptedException
	 */
	public void finishHim(Enemy enemy, int state) throws InterruptedException {
		Thread.sleep(1000);
		platform.prepareCraneAttack(enemy.getSlot(), state, this);
		Thread.sleep(1500);
		platform.rotateDegrees(40, 10, 0);
		crane.down();
		Thread.sleep(500);
		platform.rotateDegrees(40, 15, 1);
		Thread.sleep(1500);
		crane.up();

		enemy.setInGame(false);
	}

	private void soundAttack(Enemy enemy) throws InterruptedException {
		if (energy >= 50) {
			platform.prepareSoundAttack(enemy.getSlot(), enemy.getType(), this);
			Thread.sleep(400);
			Sound.playSample(new File("! Laser.wav"));
			Thread.sleep(400);
			Sound.playSample(new File("! Laser.wav"));
			Thread.sleep(400);
			Sound.playSample(new File("! Laser.wav"));
			Thread.sleep(400);

			energy -= SOUND_ENERGY_CONSUMPTION;
			enemy.removeLife(SOUND_DAMAGE);
			enemy.setAttacked(true);

			if (enemy.getLife() <= 0)
				finishHim(enemy, 1);
		}
	}

	/**
	 * Inteligência Artifical !
	 * 
	 * @param enemies
	 * @throws InterruptedException 
	 */
	public void robotPlay(ArrayList<Enemy> enemies, int round) throws InterruptedException {
		while (true) {
			ArrayList<Enemy> availableEnemies = availableEnemies(enemies);
			int artilleryAttacked = 0;

			if (energy >= 50) {
				// se não existem inimigos disponíveis para atacar sai da função
				if (availableEnemies.isEmpty())
					return;

				// se existirem artilharias ele ataca-as sempre
				for (int i = 0; i < availableEnemies.size(); i++) {
					if (availableEnemies.get(i).getType() == ARTILLERY && energy >= 50) {
						soundAttack(availableEnemies.get(i));
						artilleryAttacked = 1;
					}
				}

				if (artilleryAttacked == 1)
					continue;

				if (enemies.size() <= 4) {
					attack4EnemyStrategy(availableEnemies, round);
				} else if (enemies.size() == 5) {
					attack5EnemyStrategy(availableEnemies, round);
				} else if (enemies.size() == 6) {
					attack6EnemyStrategy(availableEnemies, round);
				}
			}

			if (robotDiesUsingNothing(availableEnemies))
				killThemAll();

			break;
		}
	}

	/**
	 * @param enemies
	 * @return - retorna todos os disponíveis que o robot pode atacar, isto é,
	 * 			 o robot só pode atacar inimigos que tenham ataques disponíveis,
	 * 			 que tenham vida e ainda não tenham sido atacados na ronda
	 */
	private ArrayList<Enemy> availableEnemies(ArrayList<Enemy> enemies) {
		ArrayList<Enemy> availableEnemies = new ArrayList<Enemy>();

		for (int i = 0; i < enemies.size(); i++) {
			if (enemies.get(i).getLife() > 0 && enemies.get(i).getAttacks() > 0 && !enemies.get(i).isAttacked())
				availableEnemies.add(enemies.get(i));
		}

		return availableEnemies;
	}

	private void attack4EnemyStrategy(ArrayList<Enemy> enemies, int round) throws InterruptedException {
		if (enemies.size() == 1) {
			if (energy >= 450) {
				if (robotDiesUsingPunchSound(enemies, round, 1))
					attackMostDangerous(enemies, round, CRANE);
			} else if (energy >= 350) {
				simplify4EnemyStrategyEnemies1(enemies, round);
			} else {
				if (robotDiesUsingNothing(enemies))
					simplify4EnemyStrategyEnemies1(enemies, round);
			}

		} else if (enemies.size() == 2) {
			if (energy == 500) {
				if (robotDiesUsingPunchSound(enemies, round, 1))
					if (robotDiesUsingPunchSound(enemies, round, 2)) {
						attackMostDangerous(enemies, round, CRANE);
						attackMostDangerous(enemies, round, PUNCH);
					}
			} else if (energy >= 400) {
				if (robotDiesUsingSound(enemies, round, 2))
					if (robotDiesUsingPunchSound(enemies, round, 1))
						if (robotDiesUsingPunchSound(enemies, round, 2)) {
							attackMostDangerous(enemies, round, CRANE);
							attackMostDangerous(enemies, round, PUNCH);
						}
			} else if (energy >= 350) {
				simplify4EnemyStrategyEnemies2(enemies, round);
			} else {
				if (robotDiesUsingNothing(enemies))
					simplify4EnemyStrategyEnemies2(enemies, round);
			}

		} else if (enemies.size() == 3) {
			if (energy >= 450) {
				simplify4EnemyStrategyEnemies3(enemies, round);
			} else if (energy >= 400) {
				if (robotDiesUsingSound(enemies, round, 2))
					simplify4EnemyStrategyEnemies3(enemies, round);
			} else if (energy >= 350) {
				if (robotDiesUsingSound(enemies, round, 1))
					if (robotDiesUsingSound(enemies, round, 2))
						simplify4EnemyStrategyEnemies3(enemies, round);
			} else {
				if (robotDiesUsingNothing(enemies))
					if (robotDiesUsingSound(enemies, round, 1))
						if (robotDiesUsingSound(enemies, round, 2))
							simplify4EnemyStrategyEnemies3(enemies, round);
			}
		} else if (enemies.size() == 4) {
			if (energy == 500) {
				simplify4EnemyStrategyEnemies4(enemies, round);
			} else if (energy >= 450) {
				if (robotDiesUsingSound(enemies, round, 3))
					simplify4EnemyStrategyEnemies4(enemies, round);
			} else if (energy >= 400) {
				if (robotDiesUsingSound(enemies, round, 2))
					if (robotDiesUsingSound(enemies, round, 3))
						simplify4EnemyStrategyEnemies4(enemies, round);
			} else if (energy >= 350) {
				if (robotDiesUsingSound(enemies, round, 1))
					if (robotDiesUsingSound(enemies, round, 2))
						if (robotDiesUsingSound(enemies, round, 3))
							simplify4EnemyStrategyEnemies4(enemies, round);
			} else {
				if (robotDiesUsingNothing(enemies))
					if (robotDiesUsingSound(enemies, round, 1))
						if (robotDiesUsingSound(enemies, round, 2))
							if (robotDiesUsingSound(enemies, round, 3))
								simplify4EnemyStrategyEnemies4(enemies, round);
			}
		}
	}

	private void simplify4EnemyStrategyEnemies1(ArrayList<Enemy> enemies, int round) throws InterruptedException {
		if (robotDiesUsingSound(enemies, round, 1))
			if (robotDiesUsingPunchSound(enemies, round, 1))
				attackMostDangerous(enemies, round, CRANE);
	}

	private void simplify4EnemyStrategyEnemies2(ArrayList<Enemy> enemies, int round) throws InterruptedException {
		if (robotDiesUsingSound(enemies, round, 1))
			if (robotDiesUsingSound(enemies, round, 2))
				if (robotDiesUsingPunchSound(enemies, round, 1))
					if (robotDiesUsingPunchSound(enemies, round, 2)) {
						attackMostDangerous(enemies, round, CRANE);
						attackMostDangerous(enemies, round, PUNCH);
					}
	}

	private void simplify4EnemyStrategyEnemies3(ArrayList<Enemy> enemies, int round) throws InterruptedException {
		if (robotDiesUsingSound(enemies, round, 3))
			if (robotDiesUsingPunchSound(enemies, round, 1))
				if (robotDiesUsingPunchSound(enemies, round, 2))
					if (robotDiesUsingPunchSound(enemies, round, 3)) {
						attackMostDangerous(enemies, round, CRANE);
						attackMostDangerous(enemies, round, PUNCH);
						attackMostDangerous(enemies, round, SOUND);
					}
	}

	private void simplify4EnemyStrategyEnemies4(ArrayList<Enemy> enemies, int round) throws InterruptedException {
		if (robotDiesUsingSound(enemies, round, 4))
			if (robotDiesUsingPunchSound(enemies, round, 1))
				if (robotDiesUsingPunchSound(enemies, round, 2))
					if (robotDiesUsingPunchSound(enemies, round, 3)) {
						attackMostDangerous(enemies, round, CRANE);
						attackMostDangerous(enemies, round, PUNCH);
						attackMostDangerous(enemies, round, SOUND);
					}
	}

	private void attack5EnemyStrategy(ArrayList<Enemy> enemies, int round) throws InterruptedException {
		if (enemies.size() == 1) {
			if (energy == 500) {
				attackMostDangerous(enemies, round, CRANE);
			} else if (energy >= 350) {
				if (robotDiesUsingPunchSound(enemies, round, 1))
					attackMostDangerous(enemies, round, CRANE);
			} else if (energy >= 250) {
				simplify5EnemyStrategyEnemies1(enemies, round);
			} else {
				if (robotDiesUsingNothing(enemies))
					simplify5EnemyStrategyEnemies1(enemies, round);
			}
		} else if (enemies.size() == 2) {
			if (energy == 500) {
				if (robotDiesUsingPunchSound(enemies, round, 2)) {
					attackMostDangerous(enemies, round, CRANE);
					attackMostDangerous(enemies, round, PUNCH);
				}
			} else if (energy >= 400) {
				if (robotDiesUsingPunchSound(enemies, round, 1))
					if (robotDiesUsingPunchSound(enemies, round, 2)) {
						attackMostDangerous(enemies, round, CRANE);
						attackMostDangerous(enemies, round, PUNCH);
					}
			} else if (energy >= 300) {
				if (robotDiesUsingSound(enemies, round, 2))
					if (robotDiesUsingPunchSound(enemies, round, 1))
						if (robotDiesUsingPunchSound(enemies, round, 2)) {
							attackMostDangerous(enemies, round, CRANE);
							attackMostDangerous(enemies, round, PUNCH);
						}
			} else if (energy >= 250) {
				simplify5EnemyStrategyEnemies2(enemies, round);
			} else {
				if (robotDiesUsingNothing(enemies))
					simplify5EnemyStrategyEnemies2(enemies, round);
			}

		} else if (enemies.size() == 3) {
			if (energy >= 450) {
				simplify5EnemyStrategyEnemies3(enemies, round);
			} else if (energy >= 350) {
				if (robotDiesUsingSound(enemies, round, 3))
					simplify5EnemyStrategyEnemies3(enemies, round);
			} else if (energy >= 300) {
				if (robotDiesUsingSound(enemies, round, 2))
					if (robotDiesUsingSound(enemies, round, 3))
						simplify5EnemyStrategyEnemies3(enemies, round);
			} else if (energy >= 250) {
				if (robotDiesUsingSound(enemies, round, 1))
					if (robotDiesUsingSound(enemies, round, 2))
						if (robotDiesUsingSound(enemies, round, 3))
							simplify5EnemyStrategyEnemies3(enemies, round);
			} else {
				if (robotDiesUsingNothing(enemies))
					if (robotDiesUsingSound(enemies, round, 1))
						if (robotDiesUsingSound(enemies, round, 2))
							if (robotDiesUsingSound(enemies, round, 3))
								simplify5EnemyStrategyEnemies3(enemies, round);
			}
		} else if (enemies.size() == 4) {
			if (energy == 500) {
				simplify5EnemyStrategyEnemies4(enemies, round);
			} else if (energy >= 400) {
				if (robotDiesUsingSound(enemies, round, 4))
					simplify5EnemyStrategyEnemies4(enemies, round);
			} else if (energy >= 350) {
				if (robotDiesUsingSound(enemies, round, 3))
					if (robotDiesUsingSound(enemies, round, 4))
						simplify5EnemyStrategyEnemies4(enemies, round);
			} else if (energy >= 300) {
				if (robotDiesUsingSound(enemies, round, 2))
					if (robotDiesUsingSound(enemies, round, 3))
						if (robotDiesUsingSound(enemies, round, 4))
							simplify5EnemyStrategyEnemies4(enemies, round);
			} else if (energy >= 250) {
				if (robotDiesUsingSound(enemies, round, 1))
					if (robotDiesUsingSound(enemies, round, 2))
						if (robotDiesUsingSound(enemies, round, 3))
							if (robotDiesUsingSound(enemies, round, 4))
								simplify5EnemyStrategyEnemies4(enemies, round);
			} else {
				if (robotDiesUsingNothing(enemies))
					if (robotDiesUsingSound(enemies, round, 1))
						if (robotDiesUsingSound(enemies, round, 2))
							if (robotDiesUsingSound(enemies, round, 3))
								if (robotDiesUsingSound(enemies, round, 4))
									simplify5EnemyStrategyEnemies4(enemies, round);
			}

		} else if (enemies.size() == 5) {
			if (energy >= 450) {
				simplify5EnemyStrategyEnemies5(enemies, round);
			} else if (energy >= 400) {
				if (robotDiesUsingSound(enemies, round, 4))
					simplify5EnemyStrategyEnemies5(enemies, round);
			} else if (energy >= 350) {
				if (robotDiesUsingSound(enemies, round, 3))
					if (robotDiesUsingSound(enemies, round, 4))
						simplify5EnemyStrategyEnemies5(enemies, round);
			} else if (energy >= 300) {
				if (robotDiesUsingSound(enemies, round, 2))
					if (robotDiesUsingSound(enemies, round, 3))
						if (robotDiesUsingSound(enemies, round, 4))
							simplify5EnemyStrategyEnemies5(enemies, round);
			} else if (energy >= 250) {
				if (robotDiesUsingSound(enemies, round, 1))
					if (robotDiesUsingSound(enemies, round, 2))
						if (robotDiesUsingSound(enemies, round, 3))
							if (robotDiesUsingSound(enemies, round, 4))
								simplify5EnemyStrategyEnemies5(enemies, round);
			} else {
				if (robotDiesUsingNothing(enemies))
					if (robotDiesUsingSound(enemies, round, 1))
						if (robotDiesUsingSound(enemies, round, 2))
							if (robotDiesUsingSound(enemies, round, 3))
								if (robotDiesUsingSound(enemies, round, 4))
									simplify5EnemyStrategyEnemies5(enemies, round);
			}
		}
	}

	private void simplify5EnemyStrategyEnemies1(ArrayList<Enemy> enemies, int round) throws InterruptedException {
		if (robotDiesUsingSound(enemies, round, 1))
			if (robotDiesUsingPunchSound(enemies, round, 1))
				attackMostDangerous(enemies, round, CRANE);
	}

	private void simplify5EnemyStrategyEnemies2(ArrayList<Enemy> enemies, int round) throws InterruptedException {
		if (robotDiesUsingSound(enemies, round, 1))
			if (robotDiesUsingSound(enemies, round, 2))
				if (robotDiesUsingPunchSound(enemies, round, 1))
					if (robotDiesUsingPunchSound(enemies, round, 2)) {
						attackMostDangerous(enemies, round, CRANE);
						attackMostDangerous(enemies, round, PUNCH);
					}
	}

	private void simplify5EnemyStrategyEnemies3(ArrayList<Enemy> enemies, int round) throws InterruptedException {
		if (robotDiesUsingPunchSound(enemies, round, 1))
			if (robotDiesUsingPunchSound(enemies, round, 2))
				if (robotDiesUsingPunchSound(enemies, round, 3)) {
					attackMostDangerous(enemies, round, CRANE);
					attackMostDangerous(enemies, round, PUNCH);
					attackMostDangerous(enemies, round, SOUND);
				}
	}

	private void simplify5EnemyStrategyEnemies4(ArrayList<Enemy> enemies, int round) throws InterruptedException {
		if (robotDiesUsingPunchSound(enemies, round, 1))
			if (robotDiesUsingPunchSound(enemies, round, 2))
				if (robotDiesUsingPunchSound(enemies, round, 3)) {
					attackMostDangerous(enemies, round, CRANE);
					attackMostDangerous(enemies, round, PUNCH);
					attackMostDangerous(enemies, round, SOUND);
				}
	}

	private void simplify5EnemyStrategyEnemies5(ArrayList<Enemy> enemies, int round) throws InterruptedException {
		if (robotDiesUsingSound(enemies, round, 5))
			if (robotDiesUsingPunchSound(enemies, round, 1)) {
				attackMostDangerous(enemies, round, PUNCH);
				attackMostDangerous(enemies, round, PUNCH);
				attackMostDangerous(enemies, round, SOUND);
				attackMostDangerous(enemies, round, SOUND);
				attackMostDangerous(enemies, round, SOUND);
			}
	}

	private void attack6EnemyStrategy(ArrayList<Enemy> enemies, int round) throws InterruptedException {
		if (enemies.size() == 1) {
			attackMostDangerous(enemies, round, CRANE);
		} else if (enemies.size() == 2) {
			if (energy == 500) {
				if (robotDiesUsingPunchSound(enemies, round, 2)) {
					attackMostDangerous(enemies, round, CRANE);
					attackMostDangerous(enemies, round, PUNCH);
				}
			} else if (energy >= 400) {
				if (robotDiesUsingPunchSound(enemies, round, 1))
					if (robotDiesUsingPunchSound(enemies, round, 2)) {
						attackMostDangerous(enemies, round, CRANE);
						attackMostDangerous(enemies, round, PUNCH);
					}
			} else if (energy >= 300) {
				if (robotDiesUsingSound(enemies, round, 2))
					if (robotDiesUsingPunchSound(enemies, round, 1))
						if (robotDiesUsingPunchSound(enemies, round, 2)) {
							attackMostDangerous(enemies, round, CRANE);
							attackMostDangerous(enemies, round, PUNCH);
						}
			} else if (energy >= 250) {
				if (robotDiesUsingSound(enemies, round, 1))
					if (robotDiesUsingSound(enemies, round, 2))
						if (robotDiesUsingPunchSound(enemies, round, 1)) {
							attackMostDangerous(enemies, round, PUNCH);
							attackMostDangerous(enemies, round, PUNCH);
						}
			} else {
				if (robotDiesUsingSound(enemies, round, 1))
					if (robotDiesUsingSound(enemies, round, 2)) {
						attackMostDangerous(enemies, round, PUNCH);
						attackMostDangerous(enemies, round, SOUND);
					}
			}
		} else if (enemies.size() == 3) {
			if (energy >= 450) {
				simplify5EnemyStrategyEnemies3(enemies, round);
			} else if (energy >= 350) {
				if (robotDiesUsingSound(enemies, round, 3))
					simplify5EnemyStrategyEnemies3(enemies, round);
			} else if (energy >= 300) {
				if (robotDiesUsingSound(enemies, round, 2))
					if (robotDiesUsingSound(enemies, round, 3))
						simplify5EnemyStrategyEnemies3(enemies, round);
			} else if (energy >= 250) {
				if (robotDiesUsingSound(enemies, round, 1))
					if (robotDiesUsingSound(enemies, round, 2))
						if (robotDiesUsingSound(enemies, round, 3))
							simplify5EnemyStrategyEnemies3(enemies, round);
			} else {
				if (robotDiesUsingNothing(enemies))
					if (robotDiesUsingSound(enemies, round, 1))
						if (robotDiesUsingSound(enemies, round, 2))
							if (robotDiesUsingSound(enemies, round, 3))
								simplify5EnemyStrategyEnemies3(enemies, round);
			}
		} else if (enemies.size() == 4) {
			if (energy == 500) {
				simplify5EnemyStrategyEnemies4(enemies, round);
			} else if (energy >= 400) {
				if (robotDiesUsingSound(enemies, round, 4))
					simplify5EnemyStrategyEnemies4(enemies, round);
			} else if (energy >= 350) {
				if (robotDiesUsingSound(enemies, round, 3))
					if (robotDiesUsingSound(enemies, round, 4))
						simplify5EnemyStrategyEnemies4(enemies, round);
			} else if (energy >= 300) {
				if (robotDiesUsingSound(enemies, round, 2))
					if (robotDiesUsingSound(enemies, round, 3))
						if (robotDiesUsingSound(enemies, round, 4))
							simplify5EnemyStrategyEnemies4(enemies, round);
			} else if (energy >= 250) {
				if (robotDiesUsingSound(enemies, round, 1))
					if (robotDiesUsingSound(enemies, round, 2))
						if (robotDiesUsingSound(enemies, round, 3))
							if (robotDiesUsingSound(enemies, round, 4))
								simplify5EnemyStrategyEnemies4(enemies, round);
			} else {
				if (robotDiesUsingNothing(enemies))
					if (robotDiesUsingSound(enemies, round, 1))
						if (robotDiesUsingSound(enemies, round, 2))
							if (robotDiesUsingSound(enemies, round, 3))
								if (robotDiesUsingSound(enemies, round, 4))
									simplify5EnemyStrategyEnemies4(enemies, round);
			}

		} else if (enemies.size() == 5) {
			if (energy >= 450) {
				simplify5EnemyStrategyEnemies5(enemies, round);
			} else if (energy >= 400) {
				if (robotDiesUsingSound(enemies, round, 4))
					simplify5EnemyStrategyEnemies5(enemies, round);
			} else if (energy >= 350) {
				if (robotDiesUsingSound(enemies, round, 3))
					if (robotDiesUsingSound(enemies, round, 4))
						simplify5EnemyStrategyEnemies5(enemies, round);
			} else if (energy >= 300) {
				if (robotDiesUsingSound(enemies, round, 2))
					if (robotDiesUsingSound(enemies, round, 3))
						if (robotDiesUsingSound(enemies, round, 4))
							simplify5EnemyStrategyEnemies5(enemies, round);
			} else if (energy >= 250) {
				if (robotDiesUsingSound(enemies, round, 1))
					if (robotDiesUsingSound(enemies, round, 2))
						if (robotDiesUsingSound(enemies, round, 3))
							if (robotDiesUsingSound(enemies, round, 4))
								simplify5EnemyStrategyEnemies5(enemies, round);
			} else {
				if (robotDiesUsingNothing(enemies))
					if (robotDiesUsingSound(enemies, round, 1))
						if (robotDiesUsingSound(enemies, round, 2))
							if (robotDiesUsingSound(enemies, round, 3))
								if (robotDiesUsingSound(enemies, round, 4))
									simplify5EnemyStrategyEnemies5(enemies, round);
			}
		} else if (enemies.size() == 6) {
			if (energy == 500) {
				if (robotDiesUsingSound(enemies, round, 6))
					if (robotDiesUsingPunchSound(enemies, round, 1)) {
						attackMostDangerous(enemies, round, PUNCH);
						attackMostDangerous(enemies, round, PUNCH);
						attackMostDangerous(enemies, round, SOUND);
						attackMostDangerous(enemies, round, SOUND);
						attackMostDangerous(enemies, round, SOUND);
						attackMostDangerous(enemies, round, SOUND);
					}
			} else if (energy >= 450) {
				if (robotDiesUsingSound(enemies, round, 5))
					if (robotDiesUsingSound(enemies, round, 6)) {
						attackMostDangerous(enemies, round, PUNCH);
						attackMostDangerous(enemies, round, SOUND);
						attackMostDangerous(enemies, round, SOUND);
						attackMostDangerous(enemies, round, SOUND);
						attackMostDangerous(enemies, round, SOUND);
						attackMostDangerous(enemies, round, SOUND);
					}
			} else if (energy >= 400) {
				if (robotDiesUsingSound(enemies, round, 4))
					if (robotDiesUsingSound(enemies, round, 5)) {
						attackMostDangerous(enemies, round, PUNCH);
						attackMostDangerous(enemies, round, SOUND);
						attackMostDangerous(enemies, round, SOUND);
						attackMostDangerous(enemies, round, SOUND);
						attackMostDangerous(enemies, round, SOUND);
						attackMostDangerous(enemies, round, SOUND);
					}
			} else if (energy >= 350) {
				if (robotDiesUsingSound(enemies, round, 3))
					if (robotDiesUsingSound(enemies, round, 4))
						if (robotDiesUsingSound(enemies, round, 5)) {
							attackMostDangerous(enemies, round, SOUND);
							attackMostDangerous(enemies, round, SOUND);
							attackMostDangerous(enemies, round, SOUND);
							attackMostDangerous(enemies, round, SOUND);
							attackMostDangerous(enemies, round, SOUND);
							attackMostDangerous(enemies, round, SOUND);
						}
			} else if (energy >= 300) {
				if (robotDiesUsingSound(enemies, round, 2))
					if (robotDiesUsingSound(enemies, round, 3))
						if (robotDiesUsingSound(enemies, round, 4))
							if (robotDiesUsingSound(enemies, round, 5)) {
								attackMostDangerous(enemies, round, SOUND);
								attackMostDangerous(enemies, round, SOUND);
								attackMostDangerous(enemies, round, SOUND);
								attackMostDangerous(enemies, round, SOUND);
								attackMostDangerous(enemies, round, SOUND);
								attackMostDangerous(enemies, round, SOUND);
							}
			} else if (energy >= 250) {
				if (robotDiesUsingSound(enemies, round, 1))
					if (robotDiesUsingSound(enemies, round, 2))
						if (robotDiesUsingSound(enemies, round, 3))
							if (robotDiesUsingSound(enemies, round, 4)) {
								attackMostDangerous(enemies, round, SOUND);
								attackMostDangerous(enemies, round, SOUND);
								attackMostDangerous(enemies, round, SOUND);
								attackMostDangerous(enemies, round, SOUND);
								attackMostDangerous(enemies, round, SOUND);
							}
			} else {
				if (robotDiesUsingNothing(enemies))
					if (robotDiesUsingSound(enemies, round, 1))
						if (robotDiesUsingSound(enemies, round, 2))
							if (robotDiesUsingSound(enemies, round, 3)) {
								attackMostDangerous(enemies, round, SOUND);
								attackMostDangerous(enemies, round, SOUND);
								attackMostDangerous(enemies, round, SOUND);
								attackMostDangerous(enemies, round, SOUND);
							}
			}
		}
	}

	private void attackMostDangerous(ArrayList<Enemy> enemies, int round, int weapon) throws InterruptedException {
		Enemy mostDangerous = null;
		for (int i = 0; i < enemies.size(); i++) {
			if (mostDangerous == null && !enemies.get(i).isAttacked())
				mostDangerous = enemies.get(i);
			else if (!enemies.get(i).isAttacked()
					&& enemies.get(i).getTotalDamage(round) > mostDangerous.getTotalDamage(round))
				mostDangerous = enemies.get(i);
		}

		if (mostDangerous != null) {
			switch (weapon) {
			case SOUND:
				soundAttack(mostDangerous);
				break;
			case PUNCH:
				if (mostDangerous.getLife() >= 100 && energy >= 100)
					punchAttack(mostDangerous);
				else
					soundAttack(mostDangerous);
				break;
			case CRANE:
				if (mostDangerous.getLife() > 100 && energy >= 300)
					craneAttack(mostDangerous);
				else if (mostDangerous.getLife() == 100 && energy >= 100)
					punchAttack(mostDangerous);
				else
					soundAttack(mostDangerous);
				break;
			}
		}
	}

	/**
	 * Função que verifica se o robot morre usando só o ataque de som
	 * em todos os inimigos
	 * 
	 * @param enemies
	 * @return
	 * @throws InterruptedException 
	 */
	private boolean robotDiesUsingSound(ArrayList<Enemy> enemies, int round, int sounds) throws InterruptedException {
		int auxLife = life;

		for (int i = 0; i < enemies.size(); i++)
			auxLife -= enemies.get(i).getDamage();

		auxLife += 50 * sounds;

		if (auxLife <= 0)
			return true;

		for (int i = 0; i < enemies.size(); i++) {
			attackMostDangerous(enemies, round, SOUND);
		}

		return false;
	}

	private boolean robotDiesUsingPunchSound(ArrayList<Enemy> enemies, int round, int punches)
			throws InterruptedException {
		int auxLife = life;

		for (int i = 0; i < enemies.size(); i++)
			auxLife -= enemies.get(i).getDamage();

		auxLife += 100 * punches;
		auxLife += 50 * (enemies.size() - punches);

		if (auxLife <= 0)
			return true;

		//realiza o nº de ataques de soco pretendidos
		for (int i = 0; i < punches; i++)
			attackMostDangerous(enemies, round, PUNCH);

		//realiza ataque de som nos restantes
		for (int i = 0; i < enemies.size(); i++)
			attackMostDangerous(enemies, round, SOUND);

		return false;
	}

	private boolean robotDiesUsingNothing(ArrayList<Enemy> enemies) {
		int auxLife = life;

		for (int i = 0; i < enemies.size(); i++)
			auxLife -= enemies.get(i).getDamage();

		if (auxLife <= 0)
			return true;

		return false;
	}

	/**
	 * KILL THEM ALL
	 * @return - DEATHHHHHHHHHHHHHH
	 * @throws InterruptedException 
	 */
	private void killThemAll() throws InterruptedException {
		LCD.clear();
		LCD.drawString("KILL", 7, 2);
		LCD.drawString("THEM ALL", 3, 3);

		crane.down();
		Thread.sleep(500);
		platform.getMotor().resetTachoCount();
		platform.getMotor().setPower(20);
		platform.getMotor().forward();

		while (true) {
			Sound.playSample(new File("Laughing 02.wav"));
			Thread.sleep(1400);
			if (platform.getMotor().getTachoCount() > 730)
				break;
		}

		platform.getMotor().stop();
		crane.up();
	}
	
	public void winningDance() {
		platform.rotateDegrees(30, 45, 1);
		platform.rotateDegrees(30, 15, 0);
		Sound.playSample(new File("Champion.wav"));
		platform.rotateDegrees(30, 45, 1);
		platform.rotateDegrees(30, 15, 0);
		Sound.playSample(new File("Champion.wav"));
		platform.rotateDegrees(30, 45, 1);
		platform.rotateDegrees(30, 15, 0);
		Sound.playSample(new File("Champion.wav"));
		platform.rotateDegrees(30, 45, 1);
		platform.rotateDegrees(30, 15, 0);
		Sound.playSample(new File("Champion.wav"));
		platform.rotateDegrees(30, 45, 1);
		platform.rotateDegrees(30, 15, 0);
	}
}