package abc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//寻路算法测试版，不看

public class FindPath {

    public static void main(String[] args) {

        // 服务商的类别的数量
        int n = 4;

        //新建group_map，用于存放每个节点所属的组别
        HashMap<Integer,Integer> group_map = new HashMap<>();


        //初始化存放故障节点的矩阵
        int[][] fault_node = new int[n + 1][2];

        //初始化路径矩阵
        int[][] path_matrix = new int[n + 1][n + 1];

        FindPath fd = new FindPath();
        fd.findpath(path_matrix, fault_node, n, group_map);

        // 输出矩阵
        System.out.println("路径矩阵为：");
        for (int i = 0; i <= n; i++) {
            System.out.println(Arrays.toString(path_matrix[i]));
        }

        // 输出故障节点矩阵
        System.out.println("故障节点矩阵为：");
        for (int i = 0; i <= n; i++) {
            System.out.println(Arrays.toString(fault_node[i]));
        }

        //输出节点组别字典
        System.out.println("节点所属组别字典为：");
        System.out.println(group_map.entrySet());
        System.out.println(group_map.get(13));

    }

    //寻路算法
    public void findpath(int[][] path_matrix, int[][] fault_node, int n, HashMap<Integer,Integer> group_map) {

        List<List<Integer>> group_list = new ArrayList<>();

        // 所有节点的数量
        int total = n * (n + 3) / 2;

        // 初始化节点增量
        int delta = 0;


        //初始化存放故障节点的矩阵
        //int[][] fault_node = new int[n+1][2];

        //初始化路径矩阵
        //int[][] path_matrix = new int[n + 1][n + 1];

        //初始化所有的节点序号
        int[] node_number = new int[total];
        for (int i = 0; i < total; i++) {
            node_number[i] = i + 1;
        }

        //输出节点序号
        /*for (int i : node_number) {
            System.out.print(i + "\t");
        }
        System.out.println();*/

        //填充第一条路径
        for (int i = 0; i < n; i++) {
            path_matrix[0][i] = i + 1;
            List<Integer> tmp = new ArrayList<Integer>();
            tmp.add(i + 1);
            group_list.add(tmp);
        }

       /* //输出第一条路径
        for (int i = 0; i < n; i++) {

            System.out.println(group_list.get(i).get(0));
        }*/
        path_matrix[0][n] = 0;

        //循环，找剩下的路径
        // 从第二条路径开始，所以下标开始为1
        for (int i = 1; i <= n; i++) {

            //故障节点的序号
            int node = i;

            // 填充故障节点之前的路径
            for (int j = 0; j < node; j++) {
                path_matrix[i][j] = j + 1;
            }

            // 计算下一节点
            delta = n - node + 1 + delta;
            //System.out.println("delta=" + delta);
            // 实际初始化时，此处应设置条件，令next_node上的点不等于node这个点的值
            int next_node = delta + node;

            //存放故障节点
            fault_node[i][0] = node;
            fault_node[i][1] = next_node;

            // 循环填充剩下的节点
            int remaining_length = n - node;
            for (int j = 0; j <= remaining_length; j++) {
                path_matrix[i][node + j] = next_node + j;
            }
        }

        /*
         * 存放组别的list
         * 寻组算法：
         * 从第n个元素开始，一直到第total个元素结束，共n轮循环
         *
         * 1  2  3  4  ...  n
         *    2  3  4  ...  n
         *       3  4  ...  n
         *       ...
         *             n-1  n
         *                  n
         *
         * */
        int count = n+1;
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                group_list.get(j).add(count);
                count++;
            }
        }

        /*// 测试输出group_list
        for(Object oo:group_list)
        {
            System.out.println(oo.toString());
        }*/

        //故障节点和路径矩阵依次-1
        for (int i = 0; i < path_matrix.length; i++) {
            for (int j = 0; j < path_matrix[i].length; j++) {
                path_matrix[i][j]-=1;
            }
        }
        for (int i = 0; i < fault_node.length; i++) {
            for (int j = 0; j < fault_node[i].length; j++) {
                fault_node[i][j]-=1;
            }
        }

        for (int i = 0; i < group_list.size(); i++) {
            for (int j = 0; j < group_list.get(i).size(); j++) {
                group_map.put(group_list.get(i).get(j)-1,i);
            }
        }



    }
}

