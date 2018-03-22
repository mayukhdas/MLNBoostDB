package edu.soic.Parse;
import org.kohsuke.args4j.Option;

/**
 * Parser for command line arguments that reads:
 * (1) the background file descriptor
 * (2) the facts file descriptor
 * (3)the output file descriptor
 * 
 * Last Modified: November 4, 2015
 */

public class CmdOpts {
    @Option(name="-i", aliases="-bk", required=true, usage="REQUIRED. Input BK file.")
    public String fBK;

    @Option(name="-f", aliases="-facts", required=true, usage="REQUIRED. Input facts file.")
    public String ffacts;
    
    @Option(name="-posNegRatio", required=false, usage="Limit of negative examples used in trainig.")
    public String posNegRatio;
    
    @Option(name="-caching", required=false, usage="Select default caching scheme (Default F).")
    public String caching;
    
    @Option(name="-dt", aliases="-database", required=true, usage="REQUIRED. Input database type.")
    public String dt;
    
    @Option(name="-allowRecursion", required=true, usage="REQUIRED. Specify recursion preference.")
    public String recursion;
    
    @Option(name="-o", aliases="-output", required=true, usage="REQUIRED. Output directory.")
    public String fOut;
    
}