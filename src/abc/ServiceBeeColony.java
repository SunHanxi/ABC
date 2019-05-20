package abc;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ForkJoinPool;


public class ServiceBeeColony {

    /* ABC的控制参数*/

    //约束条件,因为时间是5-15随机生成，取中位数10,共4个，所以为40
    double time_want_spent = 60;

    //全局重复检测器
    int repeat_count = 0;

    //雇佣蜂计数器
    int employ_count = 0;

    //跟随蜂计数器
    int on_look_count = 0;

    // 服务商的类别的数量
    int n = 4;
    // 所有节点的数量
    int total = n * (n + 3) / 2;

    int NP = 20; /* 种群的规模*/

    int FoodNumber = NP / 2; /*蜜源数量*/

    int limit = 20;  /*通过“limit”试验无法改善的食物来源被其使用的蜜蜂所放弃*/

    int maxCycle = 2500; /*实验的轮数*/

    /* 问题特定变量*/

    /*
     * 初始化所有服务商，假设有4类服务，每类服务有10个备选项。
     * filepath = "./src/abc/service.txt"
     */
    Service[][] services = new Service[4][20];

    //新建group_map，使用字典来保存每个蜜源对应位置的服务所属的类别
    HashMap<Integer, Integer> group_map = new HashMap<>();

    int D = total; /*要优化的问题的参数数量*/

    // 定义路径矩阵
    int[][] path_matrix = new int[n + 1][n + 1];

    //定义存放故障节点的矩阵
    int[][] fault_node = new int[n + 1][2];

    // 随机数的上下界，此处为每个服务的数量，暂定为每个服务数量都为20
    double lb = 0;
    double ub = 19;

    int runtime = 1;  /*算法在test里面重复运行的次数，重复多次可以查看算法的稳健性*/


    /*Foods是蜜源。 Foods矩阵的每一行都是一个包含要优化的D参数的向量。
    Foods矩阵的行数等于FoodNumber*/
    //修改，直接让Foods存储service对象
    Service[][] Foods = new Service[FoodNumber][D];
    //double[][] Foods = new double[FoodNumber][D];

    //f是保持与蜜源相关的目标函数值的向量
    /*f is a vector holding objective function values associated with food sources */
    double[] f = new double[FoodNumber];

    //"fitness"是一种保持与食物来源相关的适应度的向量
    double[] fitness = new double[FoodNumber];

    //trail是每个蜜源的试验次数
    double[] trial = new double[FoodNumber];

    //prob是一个保持蜜源(解决方案)概率的载体,也即轮盘赌的概率
    double[] prob = new double[FoodNumber];

    /*由v_{ij} = x_{ij} + \ phi_{ij} *（x_{kj} - x_{ij}）生成的新解（邻居）j是随机选择的参数，
    k是随机选择的解决方案，与i不同*/
    Service[] solution = new Service[D];

    //新解决方案的目标函数值
    double ObjValSol;

    //新解决方案的适应度
    double FitnessSol;

    /*param2change对应于j，
    neighbour对应于等式v中的v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij})*/
    int neighbour, param2change;

    //ABC算法获得的最优解
    double GlobalMin;

    //最优解的参数 修改为直接存储service对象
    Service[] GlobalParams = new Service[D];
    //double[] GlobalParams = new double[D];

    //GlobalMins在多次运行中保存每次运行的GlobalMin
    double[] GlobalMins = new double[runtime];

    /*在[0,1)范围内的随机数*/
    double r;

    /*函数指针返回double并将D维数组作为参数 */
    /*If your function takes additional arguments then change function pointer definition and lines calling "...=function(solution);" in the code*/
    /*如果你的函数需要额外的参数，那么改变函数指针定义和调用“... = function（solution）;”的行。 在代码中*/

//	typedef double (*FunctionCallback)(double sol[D]);  

    /*下面是四个测试函数*/

//	double sphere(double sol[D]);
//	double Rosenbrock(double sol[D]);
//	double Griewank(double sol[D]);
//	double Rastrigin(double sol[D]);

    /*编写自己的目标函数名称来取代sphere*/
//	FunctionCallback function = &sphere;

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
    /*Variables are initialized in the range [lb,ub]. If each parameter has different range, use arrays lb[j], ub[j] instead of lb and ub */
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

        //开始初始化total个节点
        for (pos = 0; pos < total; pos++) {

            /*
             *         [0,1)*32767
             *  r =   -------------
             *         32767 + 1
             * 为什么选这样的随机数？
             *
             * */

            r = (Math.random() * 32767 / ((double) 32767 + (double) (1)));

            //本问题中需要是整数，所以强制类型转换,进行四舍五入后转换为int
            r = r * (ub - lb) + lb;
            BigDecimal b = new BigDecimal(r);
            b = b.setScale(0, RoundingMode.HALF_UP);
            int random_pos = b.intValue();
            //random_pos是实际需要选取的服务序号，根据pos来索引map中的服务类别，然后进行选取
            //临时变量tmp_group用来存储当前节点归属的组别0-3
            int tmp_group = group_map.get(pos+1);
            //得到组别后，去对应的组随机选取元素，而且注意如果此节点是fault节点，则不能不能重复
            //检查是否重复可以根据每个节点的group[][1]的值，这是每一组的每个服务的唯一编号
            if (pos+1 == fault_node[tmp_group + 1][1]) {
                //System.out.println(services[tmp_group][random_pos]+"~~"+Foods[index][fault_node[tmp_group+1][0]]);
                while (services[tmp_group][random_pos].group[1] == Foods[index][fault_node[tmp_group+1][0]-1].group[1]) {
                    //如果真的相同，则重新随机。
                    r = (Math.random() * 32767 / ((double) 32767 + (double) (1)));
                    r = r * (ub - lb) + lb;
                    b = new BigDecimal(r);
                    b = b.setScale(0, RoundingMode.HALF_UP);
                    random_pos = b.intValue();
                    tmp_group = group_map.get(pos);
                }
            }

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
    void initial() {


        String filepath = "./src/abc/service.txt";
        List<String> serviceList;
        serviceList = readFile(filepath);
        int group = 0;
        for (int i = 1; i <= 80; i++) {
            String[] tmpList = serviceList.get(i - 1).split("  ");
            if (i <= 20 && i >= 1)
                group = 1;
            if (i <= 40 && i >= 21)
                group = 2;
            if (i <= 60 && i >= 41)
                group = 3;
            if (i <= 80 && i >= 61)
                group = 4;
            int[] gg = new int[2];
            gg[0] = group-1;
            gg[1] = i - (group - 1) * 20 - 1;
            services[group - 1][gg[1]] = new Service(gg, Double.parseDouble(tmpList[0]),
                    Double.parseDouble(tmpList[1]));
        }//完成初始化服务商


        //初始化路径矩阵和故障节点
        FindPath fd = new FindPath();
        fd.findpath(path_matrix, fault_node, n, group_map);
        System.out.println("group_map: " + group_map.entrySet());
        for (int i = 0; i < path_matrix.length; i++) {
            for (int j = 0; j < path_matrix[0].length; j++) {
                System.out.print(path_matrix[i][j]+" ");
            }
            System.out.println();
        }
        int i;

        // 初始化FoodNumber个蜜源
        for (i = 0; i < FoodNumber; i++) {
            init(i);

            /*
             * 初始化一个蜜源后
             * 计算约束条件，如果不符合约束条件，直接舍弃，重新生成。
             * 约束条件是时间，要求每条路径的时间都得符合要求
             * 路径信息在path_matrix中
             * */
            //默认符合要求
            boolean flag = false;
            double time_spent[] = new double[n + 1];
            for (int j = 0; j < n + 1; j++) {
                //第一行
                if (j == 0) {
                    for (int k = 0; k < n; k++) {
                        double tmp_time = 0;
                        int node_number = path_matrix[j][k] - 1;
                        tmp_time = Foods[i][node_number].time;
                        time_spent[0] += tmp_time;
                    }
                } else {
                    for (int k = 0; k < n + 1; k++) {
                        double tmp_time = 0;
                        int node_number = path_matrix[j][k] - 1;
                        tmp_time = Foods[i][node_number].time;
                        time_spent[j] += tmp_time;
                    }
                }
            }

            for (int m = 0; m < time_spent.length; m++) {
                if (time_spent[m] > time_want_spent) {
                    //System.out.println("time = " + time_spent[m]);
                    flag = true;
                    break;
                }
            }

            while (flag) {
                init(i);
                //默认符合要求
                //再次计算，如果不符合要求就接着循环
                //清除time_spent[n]
                for (int j = 0; j < n + 1; j++) {
                    time_spent[j] = 0;
                }
                flag = false;
                for (int j = 0; j < n + 1; j++) {
                    //第一行
                    if (j == 0) {
                        for (int k = 0; k < n; k++) {
                            double tmp_time = 0;
                            int node_number = path_matrix[j][k] - 1;
                            tmp_time = Foods[i][node_number].time;
                            time_spent[0] += tmp_time;
                        }
                    } else {
                        for (int k = 0; k < n + 1; k++) {
                            double tmp_time = 0;
                            int node_number = path_matrix[j][k] - 1;
                            tmp_time = Foods[i][node_number].time;
                            time_spent[j] += tmp_time;
                        }
                    }
                }

                for (int m = 0; m < time_spent.length; m++) {
                    if (time_spent[m] > time_want_spent) {
                        //System.out.println("long time = " + time_spent[m]);
                        repeat_count++;
                        if (repeat_count % 1000 == 0)
                            System.out.println("探测第" + repeat_count + "次");
                        flag = true;
                        break;
                    }
                }
            }
            //System.out.println("short time = " + time_spent[0] + "\t" + time_spent[1] + "\t" + time_spent[2] + "\t" + time_spent[3] + "\t" + time_spent[4]);
        }
        //现在第i个蜜源是符合要求的

        // 接下来将第一个蜜源设为最佳蜜源
        GlobalMin = f[0];
        for (i = 0; i < D; i++)
            GlobalParams[i] = Foods[0][i];
    }

    void SendEmployedBees() {
        int i, j;
        /*雇佣峰阶段*/
        for (i = 0; i < FoodNumber; i++) {
            /*要改变的参数是随机确定的*/
            r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
            param2change = (int) (r * D);

            /*随机选择的方案用于产生方案i的突变方案*/
            r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
            neighbour = (int) (r * FoodNumber);


            //随机选择的解决方案必须与解决方案i不同,并且如果是故障节点冲突了，则不予采用，并重新随机邻居

            //待交换的节点所属的组别
            int node2change_group;
            //index是故障节点的索引
            //another_index是另一个节点的索引
            int index = 0;
            int another_index = 1;
            Integer[] tmp_fault_node;
            employ_count = 0;
            int flag = 1;
            while (flag == 1) {
                flag = 0;
                employ_count++;
                //随机选取的邻居是第几组的,得到的组号是以0开始的，到3结束
                node2change_group = group_map.get(param2change+1);
                //System.out.println("雇佣蜂：节点是第"+node2change_group+"组的");
                //提取故障矩阵对应行的数据，比如是第一组的故障[1,5],在蜜源中需要-1
                tmp_fault_node = new Integer[2];
                tmp_fault_node[0] = fault_node[node2change_group+1][0];
                tmp_fault_node[1] = fault_node[node2change_group+1][1];
                //System.out.println("这一组的故障节点是"+Arrays.toString(tmp_fault_node));
                //如果当前蜜源的当前点是某个故障节点

                if (Arrays.asList(tmp_fault_node).contains(param2change + 1)) {
                    if (tmp_fault_node[0] == param2change + 1) {
                        another_index = 1;
                        index = 0;
                    }
                    else {
                        index = 1;
                        another_index = 0;
                    }
                    //System.out.println("这一组的故障节点是"+Arrays.toString(tmp_fault_node));
                    //System.out.println("这一组的故障节点是"+tmp_fault_node[index]+"\t"+tmp_fault_node[another_index]);
                    if (Foods[i][tmp_fault_node[another_index] - 1].group[1] == Foods[neighbour][tmp_fault_node[index] - 1].group[1]) {
                        flag = 1;
                        //System.out.println("冲突了");
                    }
                }
                //System.out.println("雇佣蜂故障节点冲突");
                //System.out.println("故障节点：" +  );
                if (neighbour == i)
                    flag = 1;
                r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
                neighbour = (int) (r * FoodNumber);
            }
            //System.out.println("跳出循环");

            for (j = 0; j < D; j++)
                solution[j] = Foods[i][j];

            //貌似没法进行运算，所以直接替换被随机的一个服务商即可
            solution[param2change] = Foods[neighbour][param2change];
            /*v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
            //r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
            //solution[param2change] = Foods[i][param2change] + (Foods[i][param2change] - Foods[neighbour][param2change]) * (r - 0.5) * 2;


            /*如果生成的参数值超出边界，则将其移动到边界上*/
            // 此实验中不可能越界
            /*if (solution[param2change] < lb)
                solution[param2change] = lb;
            if (solution[param2change] > ub)
                solution[param2change] = ub;*/
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

    /* 选择食物来源的概率与其质量成比例*/
    /*可以使用不同的方案来计算概率值*/
    /* prob(i)=fitness(i)/sum(fitness)*/
    /*或以下面的metot中使用的方式 prob(i)=a*fitness(i)/max(fitness)+b*/
    /*通过使用适应度值计算概率值，并通过除以最大适应度值来归一化概率值*/

    // 这个采用的不是轮盘赌，需要稍微修改
    void CalculateProbabilities() {
        int i;
        /*double maxfit;
        maxfit = fitness[0];
        for (i = 1; i < FoodNumber; i++) {
            if (fitness[i] > maxfit)
                maxfit = fitness[i];
        }
        for (i = 0; i < FoodNumber; i++) {
            prob[i] = (0.9 * (fitness[i] / maxfit)) + 0.1;
        }*/

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


                //随机选择的解决方案必须与解决方案i不同,并且如果是故障节点冲突了，则不予采用，并重新随机邻居

                //待交换的节点所属的组别
                int node2change_group;
                //index是故障节点的索引
                //another_index是另一个节点的索引
                int index = 0;
                int another_index = 1;
                Integer[] tmp_fault_node;
                on_look_count = 0;
                int flag = 1;
                while (flag == 1) {
                    flag = 0;
                    on_look_count++;
                    //随机选取的邻居是第几组的,得到的组号是以0开始的，到3结束
                    node2change_group = group_map.get(param2change+1);
                    //System.out.println("节点是第"+node2change_group+"组的");
                    //提取故障矩阵对应行的数据，比如是第一组的故障[1,5],在蜜源中需要-1
                    tmp_fault_node = new Integer[2];
                    tmp_fault_node[0] = fault_node[node2change_group+1][0];
                    tmp_fault_node[1] = fault_node[node2change_group+1][1];
                    //System.out.println("这一组的故障节点是"+Arrays.toString(tmp_fault_node));
                    //如果当前蜜源的当前点是某个故障节点

                    if (Arrays.asList(tmp_fault_node).contains(param2change + 1)) {
                        if (tmp_fault_node[0] == param2change + 1) {
                            another_index = 1;
                            index = 0;
                        }
                        else {
                            index = 1;
                            another_index = 0;
                        }
                        //System.out.println("这一组的故障节点是"+Arrays.toString(tmp_fault_node));
                        //System.out.println("这一组的故障节点是"+tmp_fault_node[index]+"\t"+tmp_fault_node[another_index]);
                        if (Foods[i][tmp_fault_node[another_index] - 1].group[1] == Foods[neighbour][tmp_fault_node[index] - 1].group[1]) {
                            flag = 1;
                            //System.out.println("冲突了");
                        }
                    }
                    //System.out.println("雇佣蜂故障节点冲突");
                    //System.out.println("故障节点：" +  );
                    if (neighbour == i)
                        flag = 1;
                    r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
                    neighbour = (int) (r * FoodNumber);
                }
                //System.out.println("跳出循环");


                /* *//*随机选择的解决方案必须与解决方案i不同*//*
                while (neighbour == i) {
                    //System.out.println(Math.random()*32767+"  "+32767);
                    r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
                    neighbour = (int) (r * FoodNumber);
                }*/
                for (j = 0; j < D; j++)
                    solution[j] = Foods[i][j];

                /*v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
                r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
                //solution[param2change] = Foods[i][param2change] + (Foods[i][param2change] - Foods[neighbour][param2change]) * (r - 0.5) * 2;
                solution[param2change] = Foods[neighbour][param2change];

                /*如果生成的参数值超出边界，则将其移动到边界上*/
                /*if (solution[param2change] < lb)
                    solution[param2change] = lb;
                if (solution[param2change] > ub)
                    solution[param2change] = ub;*/
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

    double sphere(double[] sol) {
        int j;
        double top = 0;
        for (j = 0; j < D; j++) {
            top = top + sol[j] * sol[j];
        }
        return top;
    }

    double Rosenbrock(double[] sol) {
        int j;
        double top = 0;
        for (j = 0; j < D - 1; j++) {
            top = top + 100 * Math.pow((sol[j + 1] - Math.pow((sol[j]), (double) 2)), (double) 2) + Math.pow((sol[j] - 1), (double) 2);
        }
        return top;
    }

    double Griewank(double[] sol) {
        int j;
        double top1, top2, top;
        top = 0;
        top1 = 0;
        top2 = 1;
        for (j = 0; j < D; j++) {
            top1 = top1 + Math.pow((sol[j]), (double) 2);
            top2 = top2 * Math.cos((((sol[j]) / Math.sqrt((double) (j + 1))) * Math.PI) / 180);

        }
        top = (1 / (double) 4000) * top1 - top2 + 1;
        return top;
    }

    double Rastrigin(double[] sol) {
        int j;
        double top = 0;

        for (j = 0; j < D; j++) {
            top = top + (Math.pow(sol[j], (double) 2) - 10 * Math.cos(2 * Math.PI * sol[j]) + 10);
        }
        return top;
    }

    //此处自定义自己的目标函数
    //本实验中计算函数值需要加载路径二维矩阵
    double MyFun(Service[] sol) {
        double total_price = 0;
        double total_time = 0;
        for (int i = 0; i < sol.length; i++) {
            total_price += sol[i].price;
            //total_time += sol[i].time;
        }
        return total_price;
    }


    List<String> readFile(String filePath) {
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
                /*System.out.println(list);
                for (int i = 0; i < list.size(); i++) {
                    System.out.println(list.get(i));
                }*/
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

}




