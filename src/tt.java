import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import abc.Service;
import abc.Service.*;

//测试函数，不看。
public class tt {
    public static void main(String[] args) {
        double r;
        Integer[] a = new Integer[3];
        a[0] = 1;
        a[1] = 2;
        a[2] = 3;

        System.out.println(Arrays.asList(a).contains(2));
        System.out.println(Arrays.toString(a));
        double aaa = 1.0;
        int bbb = 1;
        System.out.println(aaa==bbb);

        aaa();

    }

    static void aaa()
    {
        for (int i = 0; i < 100; i++) {
            System.out.println(i);
            for (int j = 0; j < 2; j++) {
                System.out.println(j);
                if (i == 2)
                    return;
            }
        }
    }
}
