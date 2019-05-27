package abc;

import java.io.*;
import java.util.Arrays;

public class test {

    //使用自定义参数
    //static ServiceBeeColony serBee = new ServiceBeeColony(time_want_spent,n,lb,ub);

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
        File f = new File("混合运行结果.txt");
        try {
            FileOutputStream result_file = new FileOutputStream(f);
            OutputStreamWriter writer = new OutputStreamWriter(result_file, "UTF-8");
            String dataset_path;
            String folder = "./file/";
            for (int i = 0; i < fileNames.length; i++) {
                dataset_path = fileNames[i];
                // 初始化参数
                double time_urgency;  //时间紧迫度
                for (time_urgency = 0.045; time_urgency <= 0.08; time_urgency += 0.05) {
                    fun_without_fault(writer, folder, dataset_path, time_urgency);
                    fun_with_fault(writer, folder, dataset_path, time_urgency);
                    writer.append("\n");
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

    static double run_ABC_without_fault(BeeColony_without_fault serBee) {
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

    static double run_ABC_fault(ServiceBeeColony serBee) {
        int aaaaaaaaa = 0;
        long start_time = System.currentTimeMillis();
        aaaaaaaaa = serBee.initial();
        if (aaaaaaaaa == -1)
            return -1;
        serBee.MemorizeBestSource();
        for (int iter = 0; iter < serBee.maxCycle; iter++) {
            serBee.SendEmployedBees();
            serBee.CalculateProbabilities();
            serBee.re_init();
            serBee.SendOnlookerBees();
            serBee.re_init();
            serBee.MemorizeBestSource();
            serBee.SendScoutBees();
            serBee.re_init();
        }
        long end_time = System.currentTimeMillis();
        return (double) (end_time - start_time) / 1000;
    }

    static void fun_with_fault(OutputStreamWriter writer, String folder, String dataset_path, double time_urgency) {
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
            //writer.append("控制参数为：\n");
            System.out.println("\n\n带故障节点");
            System.out.println("控制参数为：");
            System.out.println("n: " + n + "\t备选服务数量为：" + number_of_service + "\t紧迫度：" + time_urgency + "\t时间要求：" + time_want_spent);
            //writer.append("n: " + n + "\t备选服务数量为：" + number_of_service + "\t紧迫度：" + time_urgency + "\t时间要求：" + time_want_spent + "\n");

            //lb = 0;  // 随机数的上下界，此处为每个服务的数量，暂定为每个服务数量都为20
            //ub = 19;

            int runtime = 3;  /*算法在test里面重复运行的次数，重复多次可以查看算法的稳健性*/
            double[] result = new double[runtime];

            int j = 0;
            for (int run = 0; run < runtime; run++) {
                ServiceBeeColony serBee = new ServiceBeeColony(time_want_spent, n, lb, ub, folder + dataset_path, maxCycle);
                double current_run_time;

                current_run_time = run_ABC_fault(serBee);
                if (current_run_time == -1) {
                    System.out.println("无解");
                    writer.append("无解    无解    无解    无解    无解    ");
                    continue;
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
                double[] time = new double[serBee.n + 1];
                double totalTime = 0;
                double[] price = new double[serBee.n + 1];
                double totalPrice = 0;
                for (int i = 0; i < serBee.path_matrix.length; i++) {
                    //writer.append("第"+(i+1)+"条路径: \n");
                    for (int k = 0; k < serBee.path_matrix[i].length; k++) {
                        if (serBee.path_matrix[i][k] == -1)
                            break;
                        time[i] += serBee.GlobalParams[serBee.path_matrix[i][k]].time;
                        price[i] += serBee.GlobalParams[serBee.path_matrix[i][k]].price;
                    }
                    totalPrice += price[i];
                    totalTime += time[i];
                }
                // 有错误节点的最优解的第一条路径的钱
                writer.append(price[0]+"    ");
                //有错误节点的最优解的钱最低的路径的钱
                writer.append(Arrays.stream(price).min().getAsDouble()+"    ");
                //有错误节点的最优解的最差的路径的钱
                writer.append(Arrays.stream(price).max().getAsDouble()+"    ");
                //有错误节点的最优解的n+1条路径的平均的钱
                writer.append(String.format("%.2f", totalPrice / (serBee.n + 1))+"    ");
                //有错误节点的运行时间
                writer.append(current_run_time+"    ");




                //writer.append("费用分别为：" + Arrays.toString(price) + "\n");
                //writer.append("时间分别为：" + Arrays.toString(time) + "\n");
                String avgPrice = String.format("%.2f", totalPrice / (serBee.n + 1));
                String avgTime = String.format("%.2f", totalTime / (serBee.n + 1));
                //writer.append("Cost：Min ：" + Arrays.stream(price).min().getAsDouble() + "    Avg:" + avgPrice + "    Max：" + Arrays.stream(price).max().getAsDouble() + "\n");
                //writer.append("Time：Min ：" + Arrays.stream(time).min().getAsDouble() + "    Avg:" + avgTime + "    Max：" + Arrays.stream(time).max().getAsDouble() + "\n");

                //writer.append("\n");
                //writer.append((run + 1) + " run: Lowest Cost: " + serBee.GlobalMin + "\n");
                //writer.append("用时：" + current_run_time + "秒" + "\n");
                System.out.println("用时：" + current_run_time + "秒");
                //writer.append("总试探次数：" + serBee.repeat_count + "\n");
                System.out.println("总试探次数：" + serBee.repeat_count);
                //writer.append("\n");
                result[run] = serBee.GlobalMin;
            }
            //writer.append(Arrays.toString(result) + "\n");
            double total_result = 0;
            for (double i : result) {
                total_result += i;
            }
            //writer.append("平均代价为：" + (total_result / result.length) + "\n\n\n\n\n\n\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void fun_without_fault(OutputStreamWriter writer, String folder, String dataset_path, double time_urgency) {
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
            //writer.append("控制参数为：\n");
            writer.append(n+"_"+number_of_service+"_"+time_urgency+"    ");
            System.out.println("\n\n没有故障节点");
            System.out.println("控制参数为：");
            System.out.println("n: " + n + "\t备选服务数量为：" + number_of_service + "\t紧迫度：" + time_urgency + "\t时间要求：" + time_want_spent);
            //writer.append("n: " + n + "\t备选服务数量为：" + number_of_service + "\t紧迫度：" + time_urgency + "\t时间要求：" + time_want_spent + "\n");
            //lb = 0;  // 随机数的上下界，此处为每个服务的数量，暂定为每个服务数量都为20
            //ub = 19;

            int runtime = 3;  /*算法在test里面重复运行的次数，重复多次可以查看算法的稳健性*/
            double[] result = new double[runtime];

            int j = 0;
            for (int run = 0; run < runtime; run++) {
                BeeColony_without_fault serBee2 = new BeeColony_without_fault(time_want_spent, n, lb, ub, folder + dataset_path, maxCycle);
                double current_run_time;

                current_run_time = run_ABC_without_fault(serBee2);
                if (current_run_time == -1) {
                    System.out.println("无解");
                    writer.append("无解    无解    ");
                    continue;
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
                for (int i = 0; i < serBee2.GlobalParams.length; i++) {
                    time+=serBee2.GlobalParams[i].time;
                    price+=serBee2.GlobalParams[i].price;
                }
                writer.append(price+"    ");
                //writer.append("费用为：" + price + "\n");
                //writer.append("时间为：" + time + "\n");

                //writer.append("\n");
                //writer.append((run + 1) + " run: Lowest Cost: " + serBee2.GlobalMin + "\n");
                writer.append(current_run_time+"    ");
                //writer.append("用时：" + current_run_time + "秒" + "\n");
                System.out.println("用时：" + current_run_time + "秒");
                //writer.append("总试探次数：" + serBee2.repeat_count + "\n");
                System.out.println("总试探次数：" + serBee2.repeat_count);
                //writer.append("\n");
                result[run] = serBee2.GlobalMin;
            }
            //writer.append(Arrays.toString(result) + "\n");
            double total_result = 0;
            for (double i : result) {
                total_result += i;
            }
            //writer.append("平均代价为：" + (total_result / result.length) + "\n\n\n\n\n\n\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
