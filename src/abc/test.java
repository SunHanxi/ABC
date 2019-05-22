package abc;

public class test {

    //使用自定义参数
    //static ServiceBeeColony serBee = new ServiceBeeColony(time_want_spent,n,lb,ub,maxCycle,runtime);

    public static void main(String[] args) {
        // 初始化参数
        double time_want_spent = 50; //约束条件,因为时间是5-20随机生成，取中位数12.5,共4个，所以为50
        int n = 4;  // 服务商的类别的数量
        double lb = 0;  // 随机数的上下界，此处为每个服务的数量，暂定为每个服务数量都为20
        double ub = 19;
        int maxCycle = 2500; /*实验的轮数*/
        int runtime = 1;  /*算法在test里面重复运行的次数，重复多次可以查看算法的稳健性*/

        int j = 0;

        for (int run = 0; run < runtime; run++) {
            // 使用默认参数
            //ServiceBeeColony serBee = new ServiceBeeColony();
            ServiceBeeColony serBee = new ServiceBeeColony();
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
            System.out.println((run + 1) + " run: Best: " + serBee.GlobalMin);
            System.out.println("用时：" + (double) (end_time - start_time) / 1000 + "秒");
            System.out.println("总试探次数：" + serBee.repeat_count);
            System.out.println();
        }

        System.out.println();

    }

}
