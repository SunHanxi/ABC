import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;


public class tt {
    public static void main(String[] args) {

        String folder_path = "./file";
        File file = new File(folder_path);
        File[] aaa = file.listFiles();
        String[] fileNames = new String[aaa.length];
        /*for (int i = 0; i < aaa.length; i++) {
            fileNames[i] = aaa[i].getName();
        }*/

        //使用循环来制造文件名
        for (int n = 5,count = 0; n <= 30 ; n+=5) {
            for (int ub = 50; ub <= 300 ; ub+=50,count++) {
                int uuub = ub-1;
                int length = ub;
                fileNames[count] = n+"_"+length+"_0_"+uuub+"_5_50.txt";
            }
        }


        for (int i = 0; i < fileNames.length; i++) {
            System.out.println(fileNames[i]);
        }
        System.out.println(fileNames.length);
    }
}
