package cdef.grouping;

import java.util.ArrayList;
import java.util.List;

public class TestList {
	public static List<Code> testList = new ArrayList<Code>();
	
	public void add(Code code) {
		testList.add(code);
	}
	
	public static List<Code> getTestList() {
		return testList;
	}
}
