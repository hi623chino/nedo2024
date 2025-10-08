package nedo2024;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import gp2.CrossOver;
import gp2.Mutation;
import gp2.ValueGetter;

public class Main2024 {
	private static String folderName = "C://Users/Hiroki/Documents/nedo_futami/hexcore5/";
	private static String exeFileName = "hexcore5_2025.exe";
	private static String tempFolderName = "C://Users/Hiroki/Documents/nedo_futami/nedo/";
	// Interface files are automatically generated. Moreover,
	// target value vs. generations data is saved on
	// "all_1.csv" in "[tempFolderName]/csv" folder.
	// If "all_1.csv" exists, "all_2.csv"
	// is generated, and so on. Also, "best_1.csv" and "best_2.csv," which save only
	// (temporal) best values, are created.

	private static String preparedInterfaceFolder = "C://Users/Hiroki/Documents/nedo_futami/interface3";

	// const1,const2 should be true.
//	private static boolean constraints[] = { false, true, true, true, true, true, false, false, false, true, true };
	 private static boolean constraints[] = { true, true, true, true, true, true, true, true, true, true, true };

	public static int minLength = 8; // under construction
	public static int maxLengthForConst4 = 4;
	public static int max_numForConst9_merge = 1;// e.g., if this value is set to one, it means there are no merging.
	public static int max_numForConst9_split = 3;// e.g., if this value is set to one, it means there are no splitting.
	public static int min_distanceForConst0 = 2;// e.g., if this value is set to two, the physical length between any
												// inlets and any outlets will be larger than or equal to two. That is,
												// setting to one or less value is meaningless.
	public static double maxDiffLengthForConst10 = 1.5;// times

	public static int swappingTargetTreeNum = 3;// After the all generations, the swapping process is performed on the
												// specified number of trees with the best values.
	public static int swappingTime = 100;// The swapping process is performed the specified number of times on the
											// target set of trees.

	private static boolean isSaveInfeasibleFile = false;

	public static void main(String args[]) throws IOException {

		if (isSaveInfeasibleFile) {
			System.out.println(
					"In this mode, even infeasible files will be saved. If you don't require infeasible files, set the isSaveInfeasibleFile to false.");
		}

//		System.setProperty("DEBUG_MODE", "debug");
		String mode = System.getProperty("DEBUG_MODE");
		// if (mode != null && mode.equals("debug")) {
		// System.err.println(
		// "!!!!!THIS IS DEBUG_MODE!!!!! If you want to execute non-dubug mode, please
		// comment out [System.setProperty(\"DEBUG_MODE\", \"debug\");]");
		// }

		ConstraintChecker checker = new ConstraintChecker(maxLengthForConst4, max_numForConst9_merge,
				max_numForConst9_split, maxDiffLengthForConst10, min_distanceForConst0, constraints);

		int generations = 2;// # of generations (e.g.,1000)
		int nums = 10;// # of sample populations. (e.g., 500)
		int r = 12; // rows
		int c = 3; // columns

		double mutateProbability = 0.5;
		double crossProbability = 0.2;
		double eliteProbability = 0.1;

		// If we want to maximize values, set true.
		boolean isBigBetter = true;

		double best = 0;
		if (isBigBetter) {
			best = 0;
		} else {
			best = Double.MAX_VALUE;
		}

		Tree bestTree = null;

		System.out.println("generations = 0");
		List<Tree> trees = new ArrayList<Tree>();
		tree2vals = new HashMap<Tree, Double>();
		tree2fileIds = new HashMap<Tree, String>();

		// List<Tree> sameTrees = new ArrayList<Tree>();

		if (preparedInterfaceFolder != null) {
			List<Tree> preparedTrees = Util.getPreparedTrees(preparedInterfaceFolder, checker); // Structure check
			for (Tree tree : preparedTrees) {
				int r2 = tree.getYNum();
				int c2 = tree.getXNum();
				if (r != r2 || c != c2) {
					System.err.println(
							"The number of rows or the number of columns are not consistent between the setting and the prepared xml files.");
					System.exit(-1);
				}
			}
			trees.addAll(preparedTrees);

			for (int i = 0; i < nums - preparedTrees.size(); i++) {
				Tree tree = Util.initialTree(r, c, checker);

				trees.add(tree);

				mode = System.getProperty("DEBUG_MODE");
				if (mode != null && mode.equals("debug")) {
					System.out.println("Tree generation: " + i);
				}
			}
			System.out.println("Trees of the first generation are created.");
		} else {
			for (int i = 0; i < nums; i++) {

				mode = System.getProperty("DEBUG_MODE");
				if (mode != null && mode.equals("debug")) {
					System.out.println("Tree generation: " + i);
				}

				Tree tree = Util.initialTree(r, c, checker);
				trees.add(tree);
			}
			System.out.println("Trees of the first generation are created.");
		}

		for (int i = 0; i < trees.size(); i++) {
			Tree tree = trees.get(i);
			String fileId = "g0_" + tree.hashCode();

			double val = 0;
			String isNewString = "";

			if (tree2vals.containsKey(tree)) {
				val = tree2vals.get(tree);
				tree.setVal(val);
				isNewString = "[Existing] ";
			} else {
				isNewString = "[New] ";
				val = ValueGetter.CalcExec(r, c, tree, fileId, folderName, exeFileName, tempFolderName);
				tree2vals.put(tree, val);
				tree2fileIds.put(tree, fileId);
			}

			if (val != -1.0) {
				outputAllProgress(0, val, fileId);
			}
			if (val == -1.0) {
				if (isSaveInfeasibleFile) {
					System.err.println(
							isNewString + "Infeasible. Best is " + best + ". [" + tree2fileIds.get(tree) + "]");
				} else {
					System.err.println(isNewString + "Infeasible. Best is " + best + ".");
				}
				if (!isSaveInfeasibleFile) {
					ValueGetter.deleteFile(fileId, tempFolderName);
				}
			} else if ((isBigBetter && (val > best)) || (!isBigBetter && (val < best))) {
				best = val;
				System.err.println(
						isNewString + "Update the best! Best is " + best + ". [" + tree2fileIds.get(tree) + "]");
				outputBestProgress(0, best, fileId);
				bestTree = tree;
			} else {
				System.err.println(
						isNewString + "Feasible(" + val + "). Best is " + best + ". [" + tree2fileIds.get(tree) + "]");
				// ValueGetter.deleteFile(fileId, tempFolderName);
			}
		}

		System.out.println(getResult(trees));

		for (int gene = 1; gene < generations; gene++) {
			System.out.println("generations = " + gene);
			removeDead(trees);

			Set<Tree> elites = null;
			if (trees.size() > 0) {
				elites = eliteSelect(trees, Math.max(swappingTargetTreeNum, (int) (nums * eliteProbability)),
						isBigBetter);

				// int targetCrossCount = (int) (crossProbability * trees.size() / 2.0);
				int targetCrossCount = Math.max((int) (crossProbability * nums / 2.0), 2);
				int successCrossCount = 0;

				boolean crossEnough = false;

				Set<Tree> passedTrees = new HashSet<Tree>();
				Set<Tree> alreadyCrossedTrees = new HashSet<Tree>();

				for (int count = 0; count < targetCrossCount * 2; count++) {
					Collections.shuffle(trees);

					List<Tree> targetTrees = new ArrayList<Tree>();
					targetTrees.addAll(trees);
					targetTrees.removeAll(passedTrees);
					if (targetTrees.size() <= 1) {
						break;
					}

					Iterator<Tree> ite = targetTrees.iterator();
					for (int i = 0; i < trees.size() - 1; i++) {
						if (!ite.hasNext()) {
							break;
						}
						Tree tree1 = ite.next();
						if (!ite.hasNext()) {
							break;
						}
						Tree tree2 = ite.next();
						Tree crossedTrees[] = CrossOver.cross2(tree1, tree2);
						if (!crossedTrees[0].equals(tree1) && !crossedTrees[1].equals(tree2)
								&& !alreadyCrossedTrees.contains(crossedTrees[0])
								&& !alreadyCrossedTrees.contains(crossedTrees[1])) {

							Tree c1 = Util.adjustLength(crossedTrees[0]);
							Tree c2 = Util.adjustLength(crossedTrees[1]);

							passedTrees.add(tree1);
							passedTrees.add(tree2);
							alreadyCrossedTrees.add(c1);
							alreadyCrossedTrees.add(c2);
							successCrossCount++;
							trees.add(crossedTrees[0]);
							trees.add(crossedTrees[1]);
							if (successCrossCount >= targetCrossCount) {
								crossEnough = true;
								break;
							}

						} else {

						}
					}
					if (crossEnough) {
						break;
					}
				}

				mode = System.getProperty("DEBUG_MODE");
				if (mode != null && mode.equals("debug")) {
					System.err.println("cross success num: " + successCrossCount);
				}
			}

			Set<Tree> tempTrees = new HashSet<Tree>();
			tempTrees.addAll(trees);
			for (Tree tree : tempTrees) {
				if (Math.random() < mutateProbability) {
					trees.remove(tree);
					Tree newTree = Mutation.mutate(tree);
					trees.add(newTree);
				}
			}

			trees.removeAll(tree2vals.keySet());

			if (elites != null) {
				trees.removeAll(elites);
				trees.addAll(elites);
			}

			int tempSize = trees.size();
			for (int i = 0; i < nums - tempSize; i++) {
				mode = System.getProperty("DEBUG_MODE");
				if (mode != null && mode.equals("debug")) {
					System.out.println("Tree generation: " + i);
				}
				Tree tree = Util.initialTree(r, c, checker, tree2vals.keySet());
				trees.add(tree);
			}

			for (Tree tree : trees) {
				// String fileId = Math.random() + "";

				String fileId = "g" + gene + "_" + tree.hashCode();

				double val = 0;
				String isNewString = "";

				if (tree2vals.containsKey(tree)) {
					val = tree2vals.get(tree);
					tree.setVal(val);
					// sameTrees.add(tree);
					isNewString = "[Existing] ";
				} else {
					isNewString = "[New] ";
					val = ValueGetter.CalcExec(r, c, tree, fileId, folderName, exeFileName, tempFolderName);
					tree2vals.put(tree, val);
					tree2fileIds.put(tree, fileId);
				}

				if (val != -1.0) {
					outputAllProgress(gene, val, fileId);
				}

				if (val == -1.0) {
					if (isSaveInfeasibleFile) {
						System.err.println(
								isNewString + "Infeasible. Best is " + best + ". [" + tree2fileIds.get(tree) + "]");
					} else {
						System.err.println(isNewString + "Infeasible. Best is " + best + ".");
					}
					if (!isSaveInfeasibleFile) {
						ValueGetter.deleteFile(fileId, tempFolderName);
					}
				} else if ((isBigBetter && (val > best)) || (!isBigBetter && (val < best))) {
					best = val;
					System.err.println(
							isNewString + "Update the best! Best is " + best + ". [" + tree2fileIds.get(tree) + "]");
					outputBestProgress(gene, best, fileId);
					bestTree = tree;
				} else {
					System.err.println(isNewString + "Feasible(" + val + "). Best is " + best + ". ["
							+ tree2fileIds.get(tree) + "]");
					// ValueGetter.deleteFile(fileId, tempFolderName);
				}

			}

			System.out.println(getResult(trees));
		}

		//// swapping
		System.out.println("Swap Positions of the top　" + swappingTargetTreeNum + "th trees.");

		if (bestTree == null) {
			System.out.println("There are no feasible trees.");
			return;
		}

		List<Tree> orderedTrees = null;

		if (isBigBetter) {// descending order
			orderedTrees = tree2vals.entrySet().stream()
					.sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue())).map(Map.Entry::getKey)
					.collect(Collectors.toList());

		} else {// ascending order
			orderedTrees = tree2vals.entrySet().stream().sorted(Comparator.comparingDouble(Map.Entry::getValue))
					.map(Map.Entry::getKey).collect(Collectors.toList());
		}

		List<Tree> topTrees = new LinkedList<Tree>();

		for (Tree tree : orderedTrees) {
			boolean isOk = true;
			for (Tree topTree : topTrees) {
				if (topTree.hashCodeOnlyStructure() == tree.hashCodeOnlyStructure()) {
					isOk = false;
					break;
				}
			}
			if (isOk && tree.getVal() != -1) {
				topTrees.add(tree);
				if (topTrees.size() == swappingTargetTreeNum) {
					break;
				}
			}
		}

		int topCount = 1;
		for (Tree topTree : topTrees) {
			System.out.println("Swapping for the top " + topCount + "th tree.");
			topCount++;

			for (int i = 0; i < swappingTime; i++) {

				Tree tree = topTree.copy();
				Tree treeTemp = Mutation.changePosition(tree, true);
				if (treeTemp == null) {
					treeTemp = Mutation.changePosition(tree, tree2vals.keySet());
				}
				if (treeTemp != null) {
					tree = treeTemp;
				}

				String fileId = "g" + generations + "_" + tree.hashCode();

				double val = 0;
				String isNewString = "";

				if (treeTemp == null) {
					isNewString = "[Swapping failed] ";
				} else {
					isNewString = "[New] ";
					val = ValueGetter.CalcExec(r, c, tree, fileId, folderName, exeFileName, tempFolderName);
					tree2vals.put(tree, val);
					tree2fileIds.put(tree, fileId);
				}

				if (val != -1.0) {
					outputAllProgress(generations, val, fileId);
				}
				if (val == -1.0) {
					if (isSaveInfeasibleFile) {
						System.err.println(
								isNewString + "Infeasible. Best is " + best + ". [" + tree2fileIds.get(tree) + "]");
					} else {
						System.err.println(isNewString + "Infeasible. Best is " + best + ".");
					}
					if (!isSaveInfeasibleFile) {
						ValueGetter.deleteFile(fileId, tempFolderName);
					}
				} else if ((isBigBetter && (val > best)) || (!isBigBetter && (val < best))) {
					best = val;
					System.err.println(
							isNewString + "Update the best! Best is " + best + ". [" + tree2fileIds.get(tree) + "]");
					outputBestProgress(generations, best, fileId);
					bestTree = tree;
				} else {
					System.err.println(isNewString + "Feasible(" + val + "). Best is " + best + ". ["
							+ tree2fileIds.get(tree) + "]");
					// ValueGetter.deleteFile(fileId, tempFolderName);
				}
			}
		}
		ValueGetter.createXmlFile(tempFolderName + "best_" + bestTree.getVal() + ".xml", bestTree);
	}

	private static int getDiversity(List<Tree> trees) {
		Set<String> set = new HashSet<String>();
		for (Tree tree : trees) {
			set.add(tree.toString());
		}
		return set.size();
	}

	public static void removeDead(List<Tree> trees) {
		Set<Tree> removes = new HashSet<Tree>();
		for (Tree tree : trees) {
			if (tree.getVal() < 0) {
				removes.add(tree);
			}
		}

		trees.removeAll(removes);
	}

	public static void removeSameTrees(List<Tree> trees, List<Tree> sameTrees) {

		Set<Tree> removes = new HashSet<Tree>();
		for (Tree tree : trees) {
			if (sameTrees.contains(tree)) {
				removes.add(tree);
			}
		}

		trees.removeAll(removes);
		trees.addAll(removes);
	}

	public static Set<Tree> eliteSelect(List<Tree> trees, int num, boolean isBigBetter) throws IOException {
		TreeMap<Double, Tree> map = null;
		Set<Integer> structureHashCodes = new HashSet<Integer>();

		if (isBigBetter) {
			map = new TreeMap<Double, Tree>(Collections.reverseOrder());
		} else {
			map = new TreeMap<Double, Tree>();
		}

		for (Tree tree : trees) {
			if (tree.getVal() == -1.0) {
				continue;
			}
			map.put(tree.getVal() + 0.0000001 * Math.random(), tree);
		}

		Set<Tree> newTrees = new HashSet<Tree>();

		if (trees.size() == 0) {
			return newTrees;
		}

		for (Tree tree : map.values()) {
			int structureHashCode = tree.hashCodeOnlyStructure();
			if (!structureHashCodes.contains(structureHashCode)) {
				structureHashCodes.add(structureHashCode);
				newTrees.add(tree.copy());
				if (newTrees.size() == num) {
					break;
				}
			}
		}
		return newTrees;
	}

	public static Result getResult(List<Tree> trees) {
		int okNum = 0;
		double average = 0.0;
		double max = -100;
		double min = 10000;

		for (Tree tree : trees) {
			double val = tree.getVal();
			if (val != -1.0) {
				okNum++;
				average += val;
				if (val < min) {
					min = val;
				}
				if (max < val) {
					max = val;
				}
			}
		}
		average /= okNum;
		Result r = new Result(okNum, average, max, min, getDiversity(trees));
		return r;
	}

	private static void outputAllProgress(int generationNum, double value, String fileId) throws IOException {
		if (outputAllProgressFile == null) {
			String folderName = tempFolderName + "csv/";
			new File(folderName).mkdirs();
			File files[] = new File(folderName).listFiles();
			TreeSet<String> names = new TreeSet<String>();
			for (File file : files) {
				names.add(file.getName());
			}
			int count = 1;
			for (String name : names) {
				String jun = String.format("%07d", count);
				if (name.equals("all_" + jun + ".csv")) {
					count++;
				}
			}
			outputCsvCount = count;
			outputAllProgressFile = new File(folderName + "all_" + String.format("%07d", count) + ".csv");
		}
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(outputAllProgressFile, true)));
		bw.write(generationNum + "," + value + "," + fileId);
		bw.newLine();
		bw.close();
	}

	private static void outputBestProgress(int generationNum, double value, String fileId) throws IOException {
		if (outputBestProgressFile == null) {
			String folderName = tempFolderName + "csv/";
			outputBestProgressFile = new File(folderName + "best_" + String.format("%07d", outputCsvCount) + ".csv");
		}
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(outputBestProgressFile, true)));
		bw.write(generationNum + "," + value + "," + fileId);
		bw.newLine();
		bw.close();
	}

	private static File outputBestProgressFile = null;
	private static File outputAllProgressFile = null;
	private static int outputCsvCount = 0;
	private static HashMap<Tree, Double> tree2vals = null;
	private static HashMap<Tree, String> tree2fileIds = null;
}
