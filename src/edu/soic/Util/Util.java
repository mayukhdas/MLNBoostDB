package edu.soic.Util;

public class Util {
	public static String intArrayToString(int[] array){
		StringBuilder sb = new StringBuilder();
		sb.append(array[0]);

		for(int i = 1; i < array.length; ++i){
			sb.append("," + array[i]);
		}

		return sb.toString();
	}
	public static String[] splitCommaSepStrings(String input){
		return input.split(",");
	}
}
