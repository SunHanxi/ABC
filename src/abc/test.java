package abc;

import java.util.Arrays;

public class test {
    static beeColony bee = new beeColony();
    static ServiceBeeColony serBee = new ServiceBeeColony();

    public static void main(String[] args) {

        //迭代次数
        int iter = 0;

        //算法运行次数
        int run = 0;

        int j = 0;

        //算法多次运行的均值
        double mean = 0;
        //srand(time(NULL));
        for (run = 0; run < bee.runtime; run++) {
            serBee.initial();
            serBee.MemorizeBestSource();
            for (iter = 0; iter < 10000; iter++) {
                serBee.SendEmployedBees();
                serBee.CalculateProbabilities();
                serBee.SendOnlookerBees();
                serBee.MemorizeBestSource();
                serBee.SendScoutBees();
                if (iter < 500) {
                    System.out.println("\n第" + iter + "轮");
                    System.out.println("最小花费为：" + serBee.GlobalMin);
                    System.out.println("最佳解为：");
                    for (int iiii = 0; iiii < serBee.D; iiii++) {
                        System.out.print(serBee.GlobalParams[iiii].group[1] + "\t");
                    }
                    System.out.println();
                    System.out.println("所有的解为：");
                    for (int iiii = 0; iiii < serBee.Foods.length; iiii++) {
                        for (int jjjj = 0; jjjj < serBee.Foods[iiii].length; jjjj++) {
                            System.out.print(serBee.Foods[iiii][jjjj].group[1] + "\t");
                        }
                        System.out.println();
                    }
                    for (int i = 0; i < serBee.FoodNumber; i++) {
                        System.out.print(serBee.f[i] + "\t");
                    }
                    System.out.println();
                }
            }
            for (j = 0; j < serBee.D; j++) {
                //System.out.println("GlobalParam[%d]: %f\n",j+1,GlobalParams[j]);
                System.out.println("GlobalParam[" + (j + 1) + "]:" + serBee.GlobalParams[j]);
            }

            for (int i = 0; i < serBee.D; i++) {
                j = i + 1;
                System.out.print(j + "\t");
            }
            System.out.println();
            for (int i = 0; i < serBee.D; i++) {
                System.out.print(serBee.GlobalParams[i].group[1] + "\t");
            }
            System.out.println();
            //System.out.println("%d. run: %e \n",run+1,GlobalMin);
            System.out.println((run + 1) + ".run:" + serBee.GlobalMin);
            serBee.GlobalMins[run] = serBee.GlobalMin;
            //mean = mean + serBee.GlobalMin;
        }
        for (int iiii = 0; iiii < serBee.Foods.length; iiii++) {
            for (int jjjj = 0; jjjj < serBee.Foods[iiii].length; jjjj++) {
                System.out.print(serBee.Foods[iiii][jjjj].group[1] + "\t");
            }
            System.out.println();

        }
        for (int i = 0; i < serBee.FoodNumber; i++) {
            System.out.println(serBee.f[i]+"  " +serBee.fitness[i]);
        }
        System.out.println();

    }

}
