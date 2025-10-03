package nedo2024;

import java.io.IOException;

import gp2.Mutation;

public class Test {
	public static void main(String args[]) throws IOException {
		String tempFileName = "G:\\nedo\\temp3/g0_763643283.xml";

		boolean constraints[] = { false, true, true, true, true, true, true, true, true, true, true };

		int maxLengthForConst4 = 4;
		int max_numForConst9_merge = 1;// # of parents <=1
		int max_numForConst9_split = 2;// # of children <= 3
		int min_distanceForConst0 = 1;
		double maxDiffLengthForConst10 = 2;// times
		ConstraintChecker checker = new ConstraintChecker(maxLengthForConst4, max_numForConst9_merge,
				max_numForConst9_split, maxDiffLengthForConst10, min_distanceForConst0, constraints);

		Tree tree = Tree.getTreeFromInterface(tempFileName, checker);

		// tree.changeNodePosition(tree.getNode(13), tree.getNode(14));
		tree = Mutation.changePosition(tree, null);

		boolean b = checker.allCheck(tree, false);
		System.out.println(b);

		// VisualMain.visualize_logical(tree);
	}
}
