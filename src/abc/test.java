package abc;

import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.List;

public class test {

    //使用自定义参数
    //static ServiceBeeColony serBee = new ServiceBeeColony(time_want_spent,n,lb,ub);

    public static void main(String[] args) {

        //String dataset_path = "15_20_0_19_5_20.txt";
        String dataset_path = "4_20_0_19_5_20.txt";
        //String dataset_path = "10_20_0_19_5_20.txt";
        // 初始化参数
        double time_urgency = 0.52;  //时间紧迫度
        int maxCycle = 5000;


        int n;  // 服务商的类别的数量
        double time_want_spent; //约束条件,因为时间是5-20随机生成，取中位数12.5,共4个
        double lb;  // 随机数的上下界，此处为每个服务的数量，暂定为每个服务数量都为20
        double ub;
        double[] max_time = new double[2];  //随机出来的时间的范围

        //从文件名中提取参数
        String param[] = dataset_path.split("\\.")[0].split("_");

        n = Integer.parseInt(param[0]);
        lb = Integer.parseInt(param[2]);
        ub = Integer.parseInt(param[3]);
        max_time[0] = Integer.parseInt(param[4]);
        max_time[1] = Integer.parseInt(param[5]);



        //n = 4;  // 服务商的类别的数量
        time_want_spent = (max_time[1] + max_time[0]) * n * time_urgency; //约束条件,因为时间是5-20随机生成，取中位数12.5,共4个
        System.out.println("控制参数为：");
        System.out.println("n: "+n+"\t紧迫度："+time_urgency+"\t时间要求："+time_want_spent);
        //lb = 0;  // 随机数的上下界，此处为每个服务的数量，暂定为每个服务数量都为20
        //ub = 19;

        int runtime = 5;  /*算法在test里面重复运行的次数，重复多次可以查看算法的稳健性*/
        double[] result = new double[runtime];

        int j = 0;
        for (int run = 0; run < runtime; run++) {
            ServiceBeeColony serBee = new ServiceBeeColony(time_want_spent, n, lb, ub, dataset_path,maxCycle);
            long start_time = System.currentTimeMillis();
            serBee.initial();
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

            //输出最佳解
            for (int i = 0; i < serBee.D; i++) {
                j = i + 1;
                System.out.print(j + "\t");
            }
            System.out.println();
            for (int i = 0; i < serBee.D; i++) {
                System.out.print(serBee.GlobalParams[i].group[1] + "\t");
            }
            System.out.println();
            System.out.println((run + 1) + " run: Lowest Cost: " + serBee.GlobalMin);
            System.out.println("用时：" + (double) (end_time - start_time) / 1000 + "秒");
            System.out.println("总试探次数：" + serBee.repeat_count);
            System.out.println();
            result[run] = serBee.GlobalMin;
        }

        System.out.println(Arrays.toString(result));
        double total_result = 0;
        for (double i:result)
        {
            total_result+=i;
        }
        System.out.println("平均代价为："+(total_result/result.length));

    }
}
