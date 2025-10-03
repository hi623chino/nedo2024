package nedo2024;

public class Result {
	int okNum;
	double average;
	double max;
	double min;
	int diversity;

	Result(int okNum_, double average_, double max_, double min_, int diversity_) {
		okNum = okNum_;
		average = average_;
		max = max_;
		min = min_;
		diversity = diversity_;
	}

	public int getDiversity() {
		return diversity;
	}

	public int getOkNum() {
		return okNum;
	}

	public double getAverage() {
		return average;
	}

	public double getMax() {
		return max;
	}

	public double getMin() {
		return min;
	}

	public String toString() {
		String s = "[\t" + okNum + "\t" + average + "\t" + max + "\t" + min + "\t" + diversity + "\t]";
		return s;
	}

}
