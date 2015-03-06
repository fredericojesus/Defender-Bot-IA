public class Cure {

	private int lifeRegen;
	private int energyConsumption;
	private int number;
	private boolean used;

	public Cure(int cure) {
		switch (cure) {
		case 1:
			lifeRegen = 100;
			energyConsumption = 200;
			number = 1;
			break;
		case 2:
			lifeRegen = 200;
			energyConsumption = 300;
			number = 2;
			break;
		case 3:
			lifeRegen = 400;
			energyConsumption = 400;
			number = 3;
			break;
		}
		used = false;
	}

	public int getLifeRegen() {
		return lifeRegen;
	}

	public void setLifeRegen(int lifeRegen) {
		this.lifeRegen = lifeRegen;
	}

	public int getEnergyConsumption() {
		return energyConsumption;
	}

	public void setEnergyConsumption(int energyConsumption) {
		this.energyConsumption = energyConsumption;
	}

	public int getNumber() {
		return number;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}
}
