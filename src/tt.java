import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;


public class tt {
    public static void main(String[] args) {
        double[] a = {6,3,7,3,9};
        Arrays.sort(a);
        System.out.println(Arrays.toString(a));
        double max = Arrays.stream(a).max().getAsDouble();
        System.out.println(max);
    }
}
