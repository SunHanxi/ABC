package abc;

import java.lang.Math;

public class beeColony {

    //原始ABC，不看

    /* ABC的控制参数*/
    int NP = 20; /* 种群的规模*/
    int FoodNumber = NP / 2; /*蜜源数量*/
    int limit = 100;  /*通过“limit”试验无法改善的食物来源被其使用的蜜蜂所放弃*/
    int maxCycle = 2500; /*实验的轮数*/

    /* 问题特定变量*/
    int D = 100; /*要优化的问题的参数数量*/
    double lb = -5.12;
    double ub = 5.12;

    int runtime = 1;  /*算法在test里面重复运行的次数，重复多次可以查看算法的稳健性*/

    int[] dizi1 = new int[10];
    double[][] Foods = new double[FoodNumber][D];        /*Foods is the population of food sources. Each row of Foods matrix is a vector holding D parameters to be optimized. The number of rows of Foods matrix equals to the FoodNumber*/
    double[] f = new double[FoodNumber];        /*f is a vector holding objective function values associated with food sources */
    double[] fitness = new double[FoodNumber];      /*fitness is a vector holding fitness (quality) values associated with food sources*/
    double[] trial = new double[FoodNumber];         /*trial is a vector holding trial numbers through which solutions can not be improved*/
    double[] prob = new double[FoodNumber];          /*prob is a vector holding probabilities of food sources (solutions) to be chosen*/
    double[] solution = new double[D];            /*New solution (neighbour) produced by v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) j is a randomly chosen parameter and k is a randomlu chosen solution different from i*/


    double ObjValSol;              /*Objective function value of new solution*/
    double FitnessSol;              /*Fitness value of new solution*/
    int neighbour, param2change;                   /*param2change corrresponds to j, neighbour corresponds to k in equation v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij})*/

    double GlobalMin;                       /*Optimum solution obtained by ABC algorithm*/
    double[] GlobalParams = new double[D];                   /*Parameters of the optimum solution*/
    double[] GlobalMins = new double[runtime];
    /*GlobalMins holds the GlobalMin of each run in multiple runs*/
    double r; /*a random number in the range [0,1)*/

    /*函数指针返回double并将D维数组作为参数 */
    /*If your function takes additional arguments then change function pointer definition and lines calling "...=function(solution);" in the code*/
    /*如果你的函数需要额外的参数，那么改变函数指针定义和调用“... = function（solution）;”的行。 在代码中*/

//	typedef double (*FunctionCallback)(double sol[D]);  

    /*benchmark functions */

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
        int j;
        for (j = 0; j < D; j++) {
            r = (Math.random() * 32767 / ((double) 32767 + (double) (1)));
            Foods[index][j] = r * (ub - lb) + lb;
            solution[j] = Foods[index][j];
        }
        f[index] = calculateFunction(solution);
        fitness[index] = CalculateFitness(f[index]);
        trial[index] = 0;
    }


    /*All food sources are initialized */
    void initial() {
        int i;
        for (i = 0; i < FoodNumber; i++) {
            init(i);
        }
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

            /*随机选择的解决方案必须与解决方案i不同*/
            // while(neighbour==i)
            // {
            // r = (   (double)Math.random()*32767 / ((double)(32767)+(double)(1)) );
            // neighbour=(int)(r*FoodNumber);
            // }
            for (j = 0; j < D; j++)
                solution[j] = Foods[i][j];

            /*v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
            r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
            solution[param2change] = Foods[i][param2change] + (Foods[i][param2change] - Foods[neighbour][param2change]) * (r - 0.5) * 2;

            /*如果生成的参数值超出边界，则将其移动到边界上*/
            if (solution[param2change] < lb)
                solution[param2change] = lb;
            if (solution[param2change] > ub)
                solution[param2change] = ub;
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
        double maxfit;
        maxfit = fitness[0];
        for (i = 1; i < FoodNumber; i++) {
            if (fitness[i] > maxfit)
                maxfit = fitness[i];
        }

        for (i = 0; i < FoodNumber; i++) {
            prob[i] = (0.9 * (fitness[i] / maxfit)) + 0.1;
        }

    }

    void SendOnlookerBees() {

        int i, j, t;
        i = 0;
        t = 0;
        /*onlooker Bee Phase*/
        while (t < FoodNumber) {

            r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
            if (r < prob[i]) /*choose a food source depending on its probability to be chosen*/ {
                t++;

                /*The parameter to be changed is determined randomly*/
                r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
                param2change = (int) (r * D);

                /*A randomly chosen solution is used in producing a mutant solution of the solution i*/
                r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
                neighbour = (int) (r * FoodNumber);

                /*Randomly selected solution must be different from the solution i*/
                while (neighbour == i) {
                    //System.out.println(Math.random()*32767+"  "+32767);
                    r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
                    neighbour = (int) (r * FoodNumber);
                }
                for (j = 0; j < D; j++)
                    solution[j] = Foods[i][j];

                /*v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
                r = (Math.random() * 32767 / ((double) (32767) + (double) (1)));
                solution[param2change] = Foods[i][param2change] + (Foods[i][param2change] - Foods[neighbour][param2change]) * (r - 0.5) * 2;

                /*if generated parameter value is out of boundaries, it is shifted onto the boundaries*/
                if (solution[param2change] < lb)
                    solution[param2change] = lb;
                if (solution[param2change] > ub)
                    solution[param2change] = ub;
                ObjValSol = calculateFunction(solution);
                FitnessSol = CalculateFitness(ObjValSol);

                /*a greedy selection is applied between the current solution i and its mutant*/
                if (FitnessSol > fitness[i]) {
                    /*If the mutant solution is better than the current solution i, replace the solution with the mutant and reset the trial counter of solution i*/
                    trial[i] = 0;
                    for (j = 0; j < D; j++)
                        Foods[i][j] = solution[j];
                    f[i] = ObjValSol;
                    fitness[i] = FitnessSol;
                } else {   /*if the solution i can not be improved, increase its trial counter*/
                    trial[i] = trial[i] + 1;
                }
            } /*if */
            i++;
            if (i == FoodNumber)
                i = 0;
        }/*while*/

        /*end of onlooker bee phase     */
    }

    /*determine the food sources whose trial counter exceeds the "limit" value. In Basic ABC, only one scout is allowed to occur in each cycle*/
    void SendScoutBees() {
        int maxtrialindex, i;
        maxtrialindex = 0;
        for (i = 1; i < FoodNumber; i++) {
            if (trial[i] > trial[maxtrialindex])
                maxtrialindex = i;
        }
        if (trial[maxtrialindex] >= limit) {
            init(maxtrialindex);
        }
    }


    // 此处设置需要进行计算的函数
    double calculateFunction(double[] sol) {
        return Rastrigin(sol);
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
    double MyFun(double[] sol){
        int j;
        double top = 0;
        for(j = 0; j<D;j++)
        {
            top = 1;
        }
        return top;
    }
}
