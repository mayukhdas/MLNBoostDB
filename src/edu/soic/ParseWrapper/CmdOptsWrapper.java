package edu.soic.ParseWrapper;


public class CmdOptsWrapper {
	public static final String trainOptionStr = "-l";
	boolean trainIndicator=false;
	
	public static final String trainDirStr = "-train";
	String trainPath;
	
	public static final String testOptionStr = "-i";
	boolean testIndicator;
	
	public static final String testDirStr = "-test";
	String testPath;
    
	public static final String modelDirStr = "-model";
	String modelPath;
	
	public static final String treesStr = "-trees";
	int trees;
	
	public static final String targetStr = "-target";
	String target;
	
	public static final String aucDirStr = "-aucJarPath";
	String aucJarDir;
	
	public static final String posNegStr = "-negPosRatio";
	String negPosRatio;
	
	public static final String databaseStr = "-dt";
	String DB;
	
	public CmdOptsWrapper(String[] args)
	{
		for(int i =0;i<args.length;i++)
		{
			if(args[i].equals(trainOptionStr)) {
				this.trainIndicator=true;
			}
			if(args[i].equals(trainDirStr)) {
				this.trainPath = args[i+1];
			}
			if(args[i].equals(testOptionStr)) {
				this.testIndicator = true;
			}
			if(args[i].equals(testDirStr)) {
				this.testPath = args[i+1];
			}
			if(args[i].equals(modelDirStr)) {
				this.modelPath = args[i+1];
			}
			if(args[i].equals(targetStr)) {
				this.target = args[i+1];
			}
			if(args[i].equals(treesStr)) {
				this.trees = Integer.parseInt(args[i+1]);
			}
			if(args[i].equals(aucDirStr)) {
				this.aucJarDir = args[i+1];
			}
			if(args[i].equals(databaseStr))	{
				this.DB = args[i+1];
			}
		}
	}
	
}
