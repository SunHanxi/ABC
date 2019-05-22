package abc;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class readfile {
    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();

        int n = 4;
        double lb = 0;
        double ub = 9;
        Service[][] services = new Service[n][(int) (ub - lb + 1)];
        String encoding = "UTF-8";
        File file = new File("4_10.txt");
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

        List<String> serviceList;
        serviceList = list;
        int group = 0;
        for (int i = 0; i < n * (ub - lb + 1); i++) {
            String[] tmpList = serviceList.get(i).split("  ");
            group = (int) (i / (ub - lb + 1) + 1);
            System.out.println("tmpList: " + Arrays.toString(tmpList));
            System.out.println("group: " + group);
            int[] gg = new int[2];
            gg[0] = group - 1;
            gg[1] = i - (group - 1) * (int) (ub - lb + 1);
            System.out.println("gg:" + Arrays.toString(gg));
            services[group - 1][gg[1]] = new Service(gg, Double.parseDouble(tmpList[0]), Double.parseDouble(tmpList[1]));
        }//完成初始化服务商
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 10; j++) {
                System.out.println(services[i][j].group[0] + " " + services[i][j].group[1] + "\t" + services[i][j].time
                        + "\t" + services[i][j].price);
            }
        }

    }
}
