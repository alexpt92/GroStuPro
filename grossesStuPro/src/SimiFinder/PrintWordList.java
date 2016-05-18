package SimiFinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
import java.util.Map.Entry;

import SimiFinder.Counter;
import SimiFinder.OrderOutput;

public class PrintWordList {

	static void printCountedList(Map<String, Counter> m) {
		Map<String, Integer> resultMap = new HashMap<String, Integer>();
		try {
			for (Entry<String, Counter> entry : m.entrySet()) {
				if (entry.getValue().getVal() > 1) {
					resultMap.put(entry.getKey(), entry.getValue().getVal());
				}
			}
			System.out.println("Start sorting");
			resultMap = OrderOutput.sortMapByValues(resultMap);
			System.out.println("Done sorting");
			PrintStream ps = new PrintStream(new File("wordCount.txt"));
			for (Entry<String, Integer> entry : resultMap.entrySet()) {

				ps.println(entry.getValue() + " " + entry.getKey());

			}
			System.out.println("File printed.");

			ps.close();
			m.clear();
			resultMap.clear();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
