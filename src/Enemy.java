public class Enemy {

	private int slot;
	private int type; // 0 - tank, red ; 1 - artillery, green ; 2 - soldier, blue
	private String typeName;
	private int strength;
	private int attacks;
	private int initialLife;
	private int life;
	private boolean inGame;
	
	// variável que é true se o inimigo já foi atacado na ronda, false se não
	private boolean attacked;

	public Enemy(int slot, int type) {
		this.slot = slot;
		this.type = type;

		switch (type) {
		// tank
		case 0:
			typeName = "TANK";
			strength = 200;
			attacks = 2;
			initialLife = 200;
			life = 200;
			inGame = true;
			break;
		// artillery
		case 1:
			typeName = "ARTI";
			strength = 500;
			attacks = 1;
			initialLife = 50;
			life = 50;
			inGame = true;
			break;
		// soldier
		case 2:
			typeName = "SOLD";
			strength = 100;
			attacks = 3;
			initialLife = 100;
			life = 100;
			inGame = true;
			break;
		}
	}

	public int getSlot() {
		return slot;
	}

	public int getType() {
		return type;
	}
	
	public String getTypeName() {
		return typeName;
	}

	public int getStrength() {
		return strength;
	}

	public int getAttacks() {
		return attacks;
	}

	public void removeLife(int life) {
		this.life -= life;
		if (this.life < 0) {
			this.life = 0;
			attacks = 0;
		}
	}

	public int getLife() {
		return life;
	}

	public boolean isAttacked() {
		return attacked;
	}

	public void setAttacked(boolean attacked) {
		this.attacked = attacked;
	}
	
	public int getDamage() {
		return (strength * ((life * 100) / initialLife)) / 100;
	}
	
	public boolean isInGame() {
		return inGame;
	}

	public void setInGame(boolean inGame) {
		this.inGame = inGame;
	}
	
	/**
	 * Função que retira vida ao robot dependendo da vida e força do inimigo
	 * Força x %Vida
	 * 
	 * @param defenderBot
	 * @return - retorna o dano dado pelo inimigo
	 */
	public int attack(SuperRobot defenderBot) {
		int damageDealt = 0;
		if (attacks > 0) {
			damageDealt = (strength * ((life * 100) / initialLife)) / 100;
			defenderBot.removeLife(damageDealt);
			attacks--;
		}
		return damageDealt;
	}

	/**
	 * 
	 * @param round
	 * @return
	 */
	public int getTotalDamage(int round) {
		int damageDealt = 0, auxAttacks = attacks;
		damageDealt = (strength * ((life * 100) / initialLife)) / 100;

		if (round < 10)
			return damageDealt * auxAttacks;

		if (round == 10) {
			if (auxAttacks == 3)
				auxAttacks = 2;
		} else if (round == 12) {
			if (auxAttacks > 1)
				auxAttacks = 1;
		}

		return damageDealt * auxAttacks;
	}
}
