package abc;

import java.io.*;
import java.util.Arrays;

public class test_without_fault {

    public static void main(String[] args) {

        //获取所有数据集
        String folder_path = "./file";
        File file = new File(folder_path);
        File[] aaa = file.listFiles();
        String[] fileNames = new String[aaa.length];
        for (int i = 0; i < aaa.length; i++) {
            fileNames[i] = aaa[i].getName();
        }

        //新建文件流
        File f = new File("result_without_fault_时间紧迫度_0.2_0.8_0.1.txt");
        try {
            FileOutputStream result_file = new FileOutputStream(f);
            OutputStreamWriter writer = new OutputStreamWriter(result_file, "UTF-8");
            String dataset_path;
            String folder = "./file/";
            for (int i = 0; i < fileNames.length; i++) {
                dataset_path = fileNames[i];
                // 初始化参数
                double time_urgency;  //时间紧迫度
                for (time_urgency = 0.2; time_urgency <= 0.85; time_urgency += 0.1) {
                    fun(writer, folder, dataset_path, time_urgency);
                }
            }

            writer.close();
            result_file.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static double run_ABC(BeeColony_without_fault serBee) {
        int aaaaaaaaa = 0;
        long start_time = System.currentTimeMillis();
        aaaaaaaaa = serBee.initial();
        if (aaaaaaaaa == -1)
            return -1;
        serBee.MemorizeBestSource();
        for (int iter = 0; iter < serBee.maxCycle; iter++) {
            serBee.SendEmployedBees();
            serBee.CalculateProbabilities();
            serBee.SendOnlookerBees();
            serBee.MemorizeBestSource();
            serBee.SendScoutBees();
        }
        long end_time = System.currentTimeMillis();
        return (double) (end_time - start_time) / 1000;
    }

    static void fun(OutputStreamWriter writer, String folder, String dataset_path, double time_urgency) {
        int maxCycle = 50000;

        int n;  // 服务商的类别的数量
        int number_of_service;
        double time_want_spent; //约束条件,因为时间是5-20随机生成，取中位数12.5,共4个
        double lb;  // 随机数的上下界，此处为每个服务的数量，暂定为每个服务数量都为20
        double ub;
        double[] max_time = new double[2];  //随机出来的时间的范围

        //从文件名中提取参数
        String param[] = dataset_path.split("\\.")[0].split("_");

        n = Integer.parseInt(param[0]);
        number_of_service = Integer.parseInt(param[1]);
        lb = Integer.parseInt(param[2]);
        ub = Integer.parseInt(param[3]);
        max_time[0] = Integer.parseInt(param[4]);
        max_time[1] = Integer.parseInt(param[5]);


        //n = 4;  // 服务商的类别的数量
        /*
         *             期望用时-minTime
         * 时间紧迫度= -----------------
         *             maxTime-minTime
         * */
        time_want_spent = n * (time_urgency * (max_time[1] - max_time[0]) + max_time[0]); //约束条件,因为时间是5-20随机生成，取中位数12.5,共4个
        try {
            writer.append("控制参数为：\n");
            System.out.println("\n\n控制参数为：");
            System.out.println("n: " + n + "\t备选服务数量为：" + number_of_service + "\t紧迫度：" + time_urgency + "\t时间要求：" + time_want_spent);
            writer.append("n: " + n + "\t备选服务数量为：" + number_of_service + "\t紧迫度：" + time_urgency + "\t时间要求：" + time_want_spent + "\n");
            //lb = 0;  // 随机数的上下界，此处为每个服务的数量，暂定为每个服务数量都为20
            //ub = 19;

            int runtime = 3;  /*算法在test里面重复运行的次数，重复多次可以查看算法的稳健性*/
            double[] result = new double[runtime];

            int j = 0;
            for (int run = 0; run < runtime; run++) {
                BeeColony_without_fault serBee = new BeeColony_without_fault(time_want_spent, n, lb, ub, folder + dataset_path, maxCycle);
                double current_run_time;

                current_run_time = run_ABC(serBee);
                if (current_run_time == -1) {
                    System.out.println("无解");
                    writer.append("无解\n");
                    return;
                }

                //输出最佳解
                /*for (int i = 0; i < serBee.D; i++) {
                    j = i + 1;
                    writer.append(j + "\t");
                }
                writer.append("\n");
                for (int i = 0; i < serBee.D; i++) {
                    writer.append(serBee.GlobalParams[i].group[1] + "\t");
                }*/

                //按路径依次输出每条路径的解
                //求解每条路径的cost和price
                double time = 0;
                double price = 0;
                for (int i = 0; i < serBee.GlobalParams.length; i++) {
                    time+=serBee.GlobalParams[i].time;
                    price+=serBee.GlobalParams[i].price;
                }
                writer.append("费用为：" + price + "\n");
                writer.append("时间为：" + time + "\n");

                writer.append("\n");
                writer.append((run + 1) + " run: Lowest Cost: " + serBee.GlobalMin + "\n");
                writer.append("用时：" + current_run_time + "秒" + "\n");
                System.out.println("用时：" + current_run_time + "秒");
                writer.append("总试探次数：" + serBee.repeat_count + "\n");
                System.out.println("总试探次数：" + serBee.repeat_count);
                writer.append("\n");
                result[run] = serBee.GlobalMin;
            }
            writer.append(Arrays.toString(result) + "\n");
            double total_result = 0;
            for (double i : result) {
                total_result += i;
            }
            writer.append("平均代价为：" + (total_result / result.length) + "\n\n\n\n\n\n\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
