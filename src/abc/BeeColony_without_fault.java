package abc;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BeeColony_without_fault {

    /* ABC的控制参数*/

    //不依赖于初始化的变量
    int maxCycle; /*实验的轮数*/
    int limit = 20;  /*通过“limit”试验无法改善的食物来源被其使用的蜜蜂所放弃*/
    int repeat_count = 0;   //全局重复检测器
    int NP = 20; /* 种群的规模*/
    int FoodNumber = NP / 2; /*蜜源数量*/
    double ObjValSol; //新解决方案的目标函数值
    double FitnessSol; //新解决方案的适应度
    /*param2change对应于j，
    neighbour对应于等式v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) 中的i*/
    int neighbour, param2change;
    double GlobalMin; //ABC算法获得的最优解
    double r; /*在[0,1)范围内的随机数*/


    //需要初始化的参数
    double time_want_spent; //约束条件,因为时间是5-20随机生成，取中位数12.5,共4个，所以为50
    int n;  // 服务商的类别的数量
    double lb;  // 随机数的上下界，此处为每个服务的数量，暂定为每个服务数量都为20
    double ub;
    String dataset_path; //数据集路径

    //依赖于初始化的参数
    int total;// 所有节点的数量
    int D; /*要优化的问题的参数数量*/
    Service[][] services; // 所有服务商
    /*Foods是蜜源。 Foods矩阵的每一行都是一个包含要优化的D参数的向量。
    Foods矩阵的行数等于FoodNumber*/
    Service[][] Foods; //直接让Foods存储service对象
    double[] f; //f是目标函数值
    double[] fitness; //"fitness适应度
    double[] trial;  //trail是每个蜜源的试验次数
    double[] prob; //prob是一个保持蜜源(解决方案)概率的载体,也即轮盘赌的概率
    Service[] solution;
    Service[] GlobalParams; //最优解的参数,直接存储service对象

    long start_this_time = System.currentTimeMillis();
    int max_run_time = 600;

    //构造函数中对变量进行初始化
    public BeeColony_without_fault(double time_want_spent, int n, double lb, double ub, String dataset_path, int maxCycle) {
        this.time_want_spent = time_want_spent;
        this.n = n;
        this.lb = lb;
        this.ub = ub;
        this.maxCycle = maxCycle;
        this.total = n;
        this.D = this.total;
        this.services = new Service[n][(int) (ub - lb + 1)];
        this.Foods = new Service[FoodNumber][D];
        this.f = new double[FoodNumber];
        this.fitness = new double[FoodNumber];
        this.trial = new double[FoodNumber];
        this.prob = new double[FoodNumber];
        this.solution = new Service[D];
        this.GlobalParams = new Service[D];
        this.dataset_path = dataset_path;
    }

    /*存储最佳蜜源*/
    void MemorizeBestSource() {
        int i, j;
        for (i = 0; i < FoodNumber; i++) {
            if (f[i] < GlobalMin) {
                GlobalMin = f[i];
                for (j = 0; j < D; j++)
                    GlobalParams[j] = Foods[i][j];
            }
        }
    }

    /*变量在[lb，ub]范围内初始化。 如果每个参数具有不同的范围，则使用数组lb [j]，ub [j]而不是lb和ub*/
    /* 蜜源的计数器也在此功能中初始化*/

    void init(int index) {
        /*
         * 初始化一个蜜源，一个蜜源有D个参数
         *  Math.random() 生成一个[0, 1)的随机数
         *  一个蜜源的结构为：
         * [1,2,3, ... ,total]
         * 每个维度所属的类别在group_map中可以查到
         * 故障节点在fault_node中可以查到
         * 路径矩阵存储在path_matrix中
         *服务商列表存在services[][]里面
         *
         * */
        int pos;
        repeat_count++;
        //开始初始化total个节点
        for (pos = 0; pos < total; pos++) {

            r = (Math.random() * 32767 / ((double) 32767 + (double) (1)));

            //本问题中需要是整数，所以强制类型转换,进行四舍五入后转换为int
            r = r * (ub - lb) + lb;
            BigDecimal b = new BigDecimal(r);
            b = b.setScale(0, RoundingMode.HALF_UP);
            int random_pos = b.intValue();
            //random_pos是实际需要选取的服务序号
            //临时变量tmp_group用来存储当前节点归属的组别0~(n-1),就是下标
            int tmp_group = pos;

            // 赋值给蜜源中对应的位置
            Foods[index][pos] = services[tmp_group][random_pos];
            //此处solution是一条蜜源
            solution[pos] = Foods[index][pos];
        }
        //计算函数值，本实验中计算函数值需要加载路径二维矩阵
        f[index] = calculateFunction(solution);
        //计算适应度
        fitness[index] = CalculateFitness(f[index]);
        //将所有蜜源的trail置零
        trial[index] = 0;
    }

    /*所有蜜源都已初始化 */
    int initial() {

        //初始化路径矩阵
        init_services();

        int i;

        // 初始化FoodNumber个蜜源
        for (i = 0; i < FoodNumber; i++) {
            init(i);
            boolean flag = calc_time_spent(i);
            /*
             * 初始化一个蜜源后
             * 计算约束条件，如果不符合约束条件，直接舍弃，重新生成。
             * 约束条件是时间，要求每条路径的时间都得符合要求
             * 路径信息在path_matrix中
             * */
            while (flag) {
                init(i);
                if((System.currentTimeMillis()-start_this_time)/1000 > max_run_time)
                    return -1;
                flag = calc_time_spent(i);
            }
        }
        //现在第i个蜜源是符合要求的

        // 接下来将第一个蜜源设为最佳蜜源
        GlobalMin = f[0];
        for (i = 0; i < D; i++)
            GlobalParams[i] = Foods[0][i];
        return 0;
    }

    void SendEmployedBees() {
        int i, j;
        boolean break_flag = false;
        /*雇佣峰阶段*/
        for (i = 0; i < FoodNumber; i++) {
            /*要改变的参数是随机确定的*/
            r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
            param2change = (int) (r * D);

            /*随机选择的方案用于产生方案i的突变方案*/
            r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
            neighbour = (int) (r * FoodNumber);

            //随机选择的解决方案必须与解决方案i不同
            while (neighbour == i) {
                r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
                neighbour = (int) (r * FoodNumber);
            }

            for (j = 0; j < D; j++)
                solution[j] = Foods[i][j];
            //貌似没法进行运算，所以直接替换被随机的一个服务商即可
            solution[param2change] = Foods[neighbour][param2change];
            /*v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
            //r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
            //solution[param2change] = Foods[i][param2change] + (Foods[i][param2change] - Foods[neighbour][param2change]) * (r - 0.5) * 2;

            ObjValSol = calculateFunction(solution);
            FitnessSol = CalculateFitness(ObjValSol);

            /*在当前解决方案i和其突变体之间应用贪婪选择*/
            if (FitnessSol > fitness[i]) {
                /*If the mutant solution is better than the current solution i, replace the solution with the mutant and reset the trial counter of solution i*/
                trial[i] = 0;
                for (j = 0; j < D; j++)
                    Foods[i][j] = solution[j];
                f[i] = ObjValSol;
                fitness[i] = FitnessSol;
            } else {   /*如果解决方案无法改进，增加计数器*/
                trial[i] = trial[i] + 1;
            }
        }
        /*雇佣蜂阶段结束*/
    }

    /*计算适应度*/
    double CalculateFitness(double fun) {
        double result = 0;
        if (fun >= 0) {
            result = 1 / (fun + 1);
        } else {

            result = 1 + Math.abs(fun);
        }
        return result;
    }

    /* 选择食物来源的概率与其质量成比例*/
    /*可以使用不同的方案来计算概率值，最经典的就是轮盘赌↓↓↓*/
    /* prob(i)=fitness(i)/sum(fitness)*/
    void CalculateProbabilities() {
        int i;
        //计算总的适应值
        double total_fit = 0;
        for (i = 0; i < FoodNumber; ++i) {
            total_fit += fitness[i];
        }
        //计算轮盘赌概率
        for (int j = 0; j < FoodNumber; j++) {
            prob[j] = fitness[j] / total_fit;
        }
    }

    void SendOnlookerBees() {
        //跟随蜂根据轮盘赌概率进行选择
        int i, j, t;
        i = 0;
        t = 0;
        /*onlooker Bee Phase 跟随蜂*/
        while (t < FoodNumber) {
            r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
            if (r < prob[i]) /*根据其选择的概率选择蜜源*/ {
                t++;
                // 需要生成新的蜜源进行贪婪选择
                /*要改变的参数是随机确定的*/
                r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
                param2change = (int) (r * D);

                /*一个随机选择的 solution is used in producing a mutant solution of the solution i*/
                r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
                neighbour = (int) (r * FoodNumber);

                //随机选择的解决方案必须与解决方案i不同
                while (neighbour == i) {
                    r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
                    neighbour = (int) (r * FoodNumber);
                }//循环结束

                for (j = 0; j < D; j++)
                    solution[j] = Foods[i][j];

                /*v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
                //r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
                //solution[param2change] = Foods[i][param2change] + (Foods[i][param2change] - Foods[neighbour][param2change]) * (r - 0.5) * 2;
                solution[param2change] = Foods[neighbour][param2change];
                ObjValSol = calculateFunction(solution);
                FitnessSol = CalculateFitness(ObjValSol);

                /*在当前解决方案i和其突变体之间应用贪婪选择*/
                if (FitnessSol > fitness[i]) {
                    /*如果突变体优于当前个体i，则用突变体替换当前个体并重置个体i的试验计数器*/
                    trial[i] = 0;
                    for (j = 0; j < D; j++)
                        Foods[i][j] = solution[j];
                    f[i] = ObjValSol;
                    fitness[i] = FitnessSol;
                } else {   /*如果个体i未被增强，则试验计数器+1*/
                    trial[i] = trial[i] + 1;
                }

            } /*if */
            i++;
            if (i == FoodNumber)
                i = 0;
        }/*while*/
        /*跟随蜂阶段结束*/
    }

    /*确定试验计数器超过“limit”值的蜜源。 在基本ABC中，每个循环中只允许一个侦察蜂*/
    void SendScoutBees() {
        int max_trial_index = 0;
        for (int i = 1; i < FoodNumber; i++) {
            if (trial[i] > trial[max_trial_index])
                max_trial_index = i;
        }
        if (trial[max_trial_index] >= limit) {
            init(max_trial_index);
        }
    }

    // 此处设置需要进行计算的函数
    double calculateFunction(Service[] sol) {
        return MyFun(sol);
    }

    //此处自定义自己的目标函数
    //本实验中计算函数值需要加载路径二维矩阵
    double MyFun(Service[] sol) {
        double total_price = 0;
        for (int i = 0; i < total; i++) {
            total_price += sol[i].price;
        }
        return total_price;
    }

    List<String> readFile() {
        List<String> list = new ArrayList<String>();
        String encoding = "UTF-8";
        File file = new File(dataset_path);
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
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    //计算随机生成的蜜源是否符合条件
    boolean calc_time_spent(int i) {
        boolean flag = false;
        double time_spent = 0;
        for (int j = 0; j < Foods[i].length; j++) {
            time_spent += Foods[i][j].time;
        }
        if (time_spent>time_want_spent)
            flag = true;
        return flag;
    }


    void init_services() {
        List<String> serviceList;
        serviceList = readFile();
        int group = 0;
        for (int i = 0; i < n * (ub - lb + 1); i++) {
            String[] tmpList = serviceList.get(i).split("  ");
            group = (int) (i / (ub - lb + 1) + 1);
            int[] gg = new int[2];
            gg[0] = group - 1;
            gg[1] = i - (group - 1) * (int) (ub - lb + 1);
            services[group - 1][gg[1]] = new Service(gg, Double.parseDouble(tmpList[0]), Double.parseDouble(tmpList[1]));
        }//完成初始化服务商
    }
}