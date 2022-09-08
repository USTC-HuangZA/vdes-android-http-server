package configs;

import android.content.Context;
import android.util.Log;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.ConsolePrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;

import java.io.File;
import java.util.Arrays;

public class LogConfig{
    public static final int MAX_PRINT_LINES = 100;
    public static boolean LoggerInitDone = false;
//    static final String LOGGERPATH = "/storage/emulated/0/AIS/log";
    public static void LoggerInit(Context context){
        Log.i("LOGGER PATH:", (context.getExternalFilesDir("log").getAbsolutePath()));
        if(!LoggerInitDone){
            try {
//                File file = new File(LOGGERPATH);
//                if(!file.exists()){
//                    file.mkdirs();
//                }
                Printer filePrinter = new FilePrinter                      // 打印日志到文件的打印器
                        .Builder(context.getExternalFilesDir("log").getAbsolutePath())                             // 指定保存日志文件的路径
                        .fileNameGenerator(new DateFileNameGenerator())        // 指定日志文件名生成器，默认为 ChangelessFileNameGenerator("log")
                        .build();
                XLog.init(LogLevel.ALL, filePrinter);
                XLog.i("Logger init done");
                LoggerInitDone = true;
            }catch (Exception e){
                Log.e("XLog", "initFail");
                Log.e("Reason", Arrays.toString(e.getStackTrace()));
            }
        }
    }

}
