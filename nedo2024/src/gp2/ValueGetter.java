package gp2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import nedo2024.Node;
import nedo2024.Tree;

public class ValueGetter {

	public static void createXmlFile(String fileName, Tree tree) throws IOException {
		int rowNum = tree.getYNum();
		int columnNum = tree.getXNum();
		String xmlOther = Tree.getOther();

		String xml = "<hex>\n";
		xml += "<conf r=\"" + rowNum + "\" c=\"" + columnNum + "\"/>\n";
		for (Node node : tree) {
			for (Node child : node.getChildren()) {
				xml += "<seg in=\"" + node.getId() + "\" out=\"" + child.getId() + "\"/>\n";

			}
		}

		if (xmlOther != null && !xmlOther.equals("")) {
			xml += xmlOther;
		} else {
			xml += "	<ref mode=\"CSV\" name=\"R32.CSV\"/>\r\n" + "	<air mode=\"CSV\" name=\"AIR.CSV\"/>\r\n"
					+ "	<hx_type type=\"2\" />\r\n" + "	<correlation type=\"1\" />\r\n"
					+ "	<param type=\"double\" name=\"tube_D_o\" val=\"0.00635\"/>\r\n"
					+ "	<param type=\"double\" name=\"tube_D_i\" val=\"0.00535\"/>\r\n"
					+ "	<param type=\"double\" name=\"tube_L\" val=\"0.5\"/>\r\n"
					+ "	<param type=\"double\" name=\"tube_T\" val=\"0.0005\"/>\r\n"
					+ "	<param type=\"double\" name=\"tube_Hspace\" val=\"0.016\"/>\r\n"
					+ "	<param type=\"double\" name=\"tube_Vspace\" val=\"0.01905\"/>\r\n"
					+ "	<param type=\"double\" name=\"tube_beta\" val=\"0.0\"/>\r\n"
					+ "	<param type=\"double\" name=\"tube_k\" val=\"0.205\"/>\r\n"
					+ "	<param type=\"double\" name=\"fin_FPM\" val=\"0.0\"/>\r\n"
					+ "	<param type=\"double\" name=\"fin_P\" val=\"0.0\"/>\r\n"
					+ "	<param type=\"double\" name=\"fin_S\" val=\"0.0012\"/>\r\n"
					+ "	<param type=\"double\" name=\"fin_T\" val=\"0.000115\"/>\r\n"
					+ "	<param type=\"double\" name=\"fin_k\" val=\"0.205\"/>\r\n"
					+ "	<param type=\"double\" name=\"T_a_inlet\" val=\"26.0\"/>\r\n"
					+ "	<param type=\"double\" name=\"P_a_inlet\" val=\"101.325\"/>\r\n"
					+ "	<param type=\"double\" name=\"T_a_outlet\" val=\"18.0\"/>\r\n"
					+ "	<param type=\"double\" name=\"superheat\" val=\"5.0\"/>\r\n"
					+ "	<param type=\"double\" name=\"T_r_cond\" val=\"35.0\"/>\r\n"
					+ "	<param type=\"double\" name=\"subcool\" val=\"5.0\"/>\r\n"
					+ "	<param type=\"double\" name=\"evap_duty\" val=\"3.0\"/>";
			xml += "</hex>";

		}
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
		bw.write(xml);
		bw.close();
	}

	public static double CalcExec(String fileName, String folderName, String exeFileName) throws IOException {

		String command[] = { "cmd", "/c", folderName + exeFileName, fileName };
		Runtime runtime = Runtime.getRuntime();
		Process p = null;
		try {
			p = runtime.exec(command, null, new File(folderName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		InputStream in = p.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String line = br.readLine();
		line = br.readLine();

		double val = 0.0;

		// System.out.println(fileName);
		try {
			val = Double.parseDouble(line);
			// System.out.println(val);
		} catch (Exception e) {
			// System.out.println(line);
			if (line != null && line.contains("INFEASIBLE")) {
				System.err.println("INFEASIBLE: " + line);
			} else if (line != null && line.contains("Invalid")) {
				System.err.println("Invalid: " + line);
			} else if (line != null) {
				System.out.println(line);
			}
			val = -1;
		}

		br.close();
		p.destroy();

		return val;
	}

	public static void deleteFile(String fileId, String tempFolderName) {
		// System.out.println("debug: deleteFile");
		String fileName = tempFolderName + fileId + ".xml";
		File file = new File(fileName);
		file.delete();
	}

	public static double CalcExec(int rowNum, int columnNum, Tree net, String fileId, String folderName,
			String exeFileName, String tempFolderName) throws IOException {

		String mode = System.getProperty("DEBUG_MODE");
		if (mode != null && mode.equals("debug")) {
			boolean b = net.getConstraintChecker().allCheck(net, false);
			if (!b) {
				System.out.println("warning code: fk3053");
			}
		}

		String fileName = tempFolderName + fileId + ".xml";
		createXmlFile(fileName, net);

		double val = CalcExec(fileName, folderName, exeFileName);
		net.setVal(val);

		return val;

	}
}
