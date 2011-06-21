package dvcs2lod;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static final int GIT_LOG=1;
    public static final int Mercurial_LOG=2;
    public static final int Bazaar_LOG=3;
    public static final String dateFormatLog="yyyy-MM-dd HH:mm:ss Z";
    public static final String dateFormatJena="yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static void main( String[] args )
    {
        if (args.length<3)
        {
                System.err.println("Usage: java -jar dvcs2lod.jar <TDB_folder> <log_type> <output_rdf_file>");
                System.exit(0);
        }

        long startTime = System.currentTimeMillis();
        String DBdirectory = args[0] ;
        LogParser G = new LogParser();
        Jena J= new Jena(DBdirectory);
        J.clean();
        System.out.print("Loading ChangeSets and adding PullFeeds ... ");
        String logtype= args[1];

        if (logtype.equalsIgnoreCase("git"))
        {
            G.Parse(J,GIT_LOG);
        }
        if (logtype.equalsIgnoreCase("mercurial"))
        {
            G.Parse(J,Mercurial_LOG);
        }
        if (logtype.equalsIgnoreCase("bazaar"))
        {
            G.Parse(J,Bazaar_LOG);
        }
        System.out.println("DONE");
        System.out.print("Adding PushFeeds ... ");
        J.addPushFeeds();
        System.out.println("DONE");
        long endTime = System.currentTimeMillis();
        System.out.println("Ontology population time (seconds):"+ (endTime-startTime)/1000);
        System.out.println("Number of triple(s) = "+J.getTripleCount());
        System.out.println("Number of site(s) = "+J.getSiteCount());
        System.out.println("Number of commit(s) = "+J.getCommitCount());
        System.out.println("Number of merge(s) = "+J.getMergeCount());
        System.out.println("Number of author(s) = "+J.getAuthorCount());

        FileOutputStream outFile;
        try {
            outFile = new FileOutputStream(args[2]);
            PrintStream out = new PrintStream(outFile);
            J.dump(out);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        J.close();

    }
}
