package edu.soic.ParseWrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

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
	String negPosRatio=null;
	
	public static final String databaseStr = "-dt";
	String DB;
	
	
	public static String bkPathForDB = null;
	
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
	
	public void createWillBk()
	{
		String postFix=null;
		StringBuilder predDefBuild = new StringBuilder();
		StringBuilder modeBuild = new StringBuilder();
		File dir = new File(this.trainPath);
		try 
		{
			if(dir.isDirectory())
			{
				postFix = dir.getName();
				System.out.println(postFix);
			}
			else
			{
				System.out.println("Error in train directory path!!");
				System.exit(1);
			}
			
			if (postFix.endsWith("/")) {
				postFix = postFix.substring(0, postFix.length() - 1);
	        }
			String bkFilePath = this.trainPath+"/"+postFix+"_bk.txt";
			
			File bkFile = new File(bkFilePath);
			BufferedReader bf = new BufferedReader(new FileReader(bkFile));
			String line="";
			if((line=bf.readLine())!=null)
			{
				if(line.startsWith("import:"))
				{
					String temp = line.split(":")[1].trim();
					temp = temp.substring(0, temp.length()-1);
					temp = temp.replace('"', ' ').trim();
					bkFilePath = this.trainPath+"/"+temp;
				}
			}
			bf.close();
			bkFile = new File(bkFilePath);
			bf = new BufferedReader(new FileReader(bkFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./DB_bk.txt")));
			
			while((line = bf.readLine())!=null)
			{
				if(line.startsWith("mode:"))
				{
					line = line.trim();
					String predDef = line.replaceAll("mode:", "predDef:");
					predDef.replaceAll("+", "").replaceAll("-", "");
					predDefBuild.append(predDef+"\n");	
					
					modeBuild.append(line+"\n");
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	
	
}
