package edu.soic.ParseWrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

public class CmdOptsWrapper {
	public static final String trainOptionStr = "-l";
	public boolean trainIndicator=false;
	
	public static final String trainDirStr = "-train";
	public String trainPath;
	
	public static final String testOptionStr = "-i";
	public boolean testIndicator;
	
	public static final String testDirStr = "-test";
	public String testPath;
    
	public static final String modelDirStr = "-model";
	public String modelPath;
	
	public static final String treesStr = "-trees";
	public int trees=5;
	
	public static final String targetStr = "-target";
	public String target;
	
	public static final String aucDirStr = "-aucJarPath";
	public String aucJarDir;
	
	public static final String posNegStr = "-negPosRatio";
	public String negPosRatio=null;
	
	public static final String databaseStr = "-dt";
	public String DB;
	
	
	
	
	public String bkPathForDB = null;
	public String evidencePathForDB = null;
	public CmdOptsWrapper()
	{
		
	}
	
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
		createDBBk();
		createEv();
		
	}
	
	public void createDBBk()
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
				//System.out.println(postFix);
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
			while((line=bf.readLine())!=null)
			{
				System.out.println(line);
				if(line.startsWith("//"))
					continue;
				if(line.startsWith("import:"))
				{
					String temp = line.split(":")[1].trim();
					temp = temp.substring(0, temp.length()-1);
					temp = temp.replace('"', ' ').trim();
					bkFilePath = this.trainPath+"/"+temp;
					System.out.println("TEMP-----"+temp);
					break;
				}
			}
			bf.close();
			//System.out.println(bkFilePath);
			bkFile = new File(bkFilePath);
			bf = new BufferedReader(new FileReader(bkFile));
			this.bkPathForDB = this.trainPath+"/"+"DB_bk.txt";
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(this.bkPathForDB)));
			//bw.close();
			ArrayList<String> pDefExists = new ArrayList<String>();
			while((line = bf.readLine())!=null)
			{
				//System.out.println(line);
				if(line.startsWith("mode:"))
				{
					line = line.trim();
					String predDef = line.replaceAll("mode:", "predDef:");
					predDef = predDef.replaceAll("\\+", "").replaceAll("-", "");
					if(predDef.contains(this.target))
						predDef = predDef.replaceAll("\\)", ")?");
					if(!pDefExists.contains(predDef))
					{
						predDefBuild.append(predDef+"\n");	
						pDefExists.add(predDef);
					}
					
					String mode = line;
					mode = mode.replaceAll("\\+([A-Z]*|[a-z]*|[0-9]*),", "+@@,");
					mode = mode.replaceAll("\\+([A-Z]*|[a-z]*|[0-9]*)\\)", "+@@)");
					mode = mode.replaceAll("-([A-Z]*|[a-z]*|[0-9]*),", "-@@,");
					mode = mode.replaceAll("-([A-Z]*|[a-z]*|[0-9]*)\\)", "-@@)");
					mode = mode.replaceAll("#([A-Z]*|[a-z]*|[0-9]*),", "#@@,");
					mode = mode.replaceAll("#([A-Z]*|[a-z]*|[0-9]*)\\)", "#@@)");
					mode = mode.replaceAll("@@", "");
					modeBuild.append(mode+"\n");
				}
			}
			System.out.println(predDefBuild.toString());
			System.out.println(modeBuild.toString());
			bf.close();
			
			String topMatter = "// BK file follows assumption that each predicate name is unique\r\n" + 
					"// Options for the ILP search:\r\n" + 
					"option(M) = "+this.trees+". 	// Number of gradient steps\r\n" + 
					"option(CM) = 1. 	// Number of clauses learned for each target per gradient step\r\n" + 
					"option(N) = 4.  	// Maximum length of a clause\r\n" + 
					"option(B) = 10.		// Beam size\r\n";
		
			bw.write(topMatter);
			bw.write(predDefBuild.toString());
			bw.write(modeBuild.toString());
		
			bw.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void createEv()
	{
		
		try {
			String postFix=null;
			File dir = new File(this.trainPath);
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
			String sourceStr = this.trainPath+"/"+postFix+"_facts.txt";
			String destStr = this.trainPath+"/"+postFix+"_factsDB.txt";
			File source = new File(sourceStr);
			File dest = new File(destStr);
			FileUtils.copyFile(source, dest);
			
			String posFileStr = this.trainPath+"/"+postFix+"_pos.txt";
			BufferedReader bf = new BufferedReader(new FileReader(new File(posFileStr)));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(destStr),true));
			String line;
			while((line=bf.readLine())!=null)
			{
				bw.write(line+"\n");
			}
			bw.close();
			bf.close();
			this.evidencePathForDB = destStr;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		CmdOptsWrapper cmd = new CmdOptsWrapper();
		cmd.trainPath = "./Datasets/Toy-Cancer/train";
		cmd.target = "cancer";
		cmd.createDBBk();
		cmd.createEv();
	}
	
	
}
