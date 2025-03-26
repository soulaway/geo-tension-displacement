package gi.soloviev.tendis;

import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		double[] x = { 0.001, 0.2, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0, 2.5, 3.0, 3.5, 4.0 };
		double[] z = { 0.001, 0.2, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0 };
		System.out.println(TenDis.solve(x, z));
	}
}
