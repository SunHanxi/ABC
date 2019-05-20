package abc;

// 服务的类，包含了组别，价格，时间。
// group[0] 代表服务的组别，group[1]代表组里的第几号
public class Service {

    public double price;
    public double time;
    public int[] group = new int[2];

    public Service(int[] group, double price, double time) {
        this.group = group;
        this.price = price;
        this.time = time;
    }

    Service() {
        this.group = new int[2];
        this.price = 0;
        this.time = 0;
    }

    @Override
    public String toString() {
        return "group: [" + this.group[0]+" "+this.group[1]+"] time: " + this.time + " " + "price: " + this.price;
    }
}
