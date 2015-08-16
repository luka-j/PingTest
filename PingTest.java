import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Luka
 */
public class PingTest {

    static final long INTERVAL = 1000;

    static int totalRequests = 0;
    static int failedRequests = 0;
    static long lastFail = -1;

    static File logdir = new File("E:\\PingTest\\");
    static File log = new File(logdir.getAbsolutePath() + "\\log.txt");
    static File err = new File(logdir.getAbsolutePath() + "\\err.txt");
    static FileWriter logger, errer;
    static DateFormat df = new SimpleDateFormat("kk:mm:ss");

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            float successRate = 100 - ((float)failedRequests/totalRequests) * 100;
            System.out.println("Ukupno paketa: " + totalRequests);
            System.out.println("Neuspelih: " + failedRequests);
            System.out.println("Uspesno: " + successRate + "%");
            try {
                logger.append("Uspesno: " + successRate + "%");
                logger.close();
                errer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        try {
            logdir.mkdir();
            log.delete();
            log.createNewFile();
            System.out.println("logfile: " + log.getCanonicalPath());
            err.delete();
            err.createNewFile();
            logger = new FileWriter(log, true);
            errer = new FileWriter(err);
            while (true) {
                try {
                    totalRequests++;
                    if (ping()) {
                        logFine();
                        Thread.sleep(INTERVAL);
                    } else {
                        logFail();
                        //Thread.sleep(2000);
                    }
                } catch (IOException ex) {
                    logError(ex);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static boolean ping() throws IOException, InterruptedException {
        Process proc;
        if(System.getProperty("os.name").contains("nix")) 
            proc = Runtime.getRuntime().exec("ping -c 2 google.com");
        else proc = Runtime.getRuntime().exec("ping -n 2 google.com");
        int returnVal = proc.waitFor();
        return returnVal == 0;
    }

    private static void logFine() throws IOException {
        System.out.println(df.format(new Date()) + " FINE");
        logger.append(df.format(new Date()) + " FINE\n");
        logger.flush();
    }

    private static void logFail() throws IOException, InterruptedException {
        Date time = new Date();
        System.out.println(df.format(time) + " FAIL");
        logger.append(df.format(time) + " FAIL\n");
        logger.flush();
        errer.append(df.format(time) + " FAIL\n");
        errer.flush();
        failedRequests++;
        if(time.getTime() - lastFail <= INTERVAL)
            Thread.sleep(INTERVAL - (time.getTime()-lastFail));
        lastFail = new Date().getTime();
    }

    private static void logError(Exception ex) {
        try {
            System.out.println("ERROR: " + ex.getMessage() + "");
            errer.append("ERROR: " + ex.getMessage() + "\n");
        } catch (IOException crit) {
            System.out.println("panic");
            System.exit(1);
        }
    }
}
