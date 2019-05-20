package abc;

import sun.security.util.Length;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class readfile {
    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        String encoding = "UTF-8";
        File file = new File("./src/abc/service.txt");
        InputStreamReader read;
        {
            try {
                read = new InputStreamReader(
                        new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                while ((lineTxt = bufferedReader.readLine()) != null) {
                    list.add(lineTxt);
                }
                bufferedReader.close();
                read.close();
                //System.out.println(list);
                for (int i = 0; i < list.size(); i++) {
                    //System.out.println(list.get(i));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Service[][] services = new Service[4][10];
        int group = 0;
        for (int i = 1; i <= 40; i++) {
            String[] tmpList = list.get(i - 1).split("  ");
            if (i <= 10 && i >= 1)
                group = 1;
            if (i <= 20 && i >= 11)
                group = 2;
            if (i <= 30 && i >= 21)
                group = 3;
            if (i <= 40 && i >= 31)
                group = 4;
            int[] gg = new int[2];
            gg[0] = group;
            gg[1] = i - (group - 1) * 10 - 1;
            services[group - 1][i - (group - 1) * 10 - 1] = new Service(gg, Double.parseDouble(tmpList[0]),
                    Double.parseDouble(tmpList[1]));
        }

        /*for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 10; j++) {
                System.out.println(services[i][j].group[0] + " " + services[i][j].group[1] + "\t" + services[i][j].time
                        + "\t" + services[i][j].price);
            }
        }*/

    }
}
