package gi.soloviev.tendis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * ПРОГРАММА расчета параметров напряженно-деформированного состояния пород
 * кровли в окрестности очистного забоя
 */

public class TenDis {

	/**
	 * Pасчет параметров напряженно-деформированного состояния пород кровли
	 * 
	 * @param x массив горизонтальных координат
	 * @param z массив вертикальных координат
	 * 
	 * @throws IOException
	 */
	public static String solve(double[] x, double[] z) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream out = new PrintStream(baos)) {
			double ro = 0;
			double sZ = 0;
			double sX = 0;
			double tXZ = 0;
			double u = 0;
			double w = 0;
			double fi = 0;
			out.printf("%5s %5s %10s %10s %10s %10s %10s%n", "X", "Z", "SIGZ", "SIGX", "TAUXZ", "U", "W");
			for (int j = 0; j < z.length; j++) {
				for (int i = 0; i < x.length; i++) {
					ro = calcRo(x[i], z[j]);
					fi = calcFi(x[i], z[j], ro);
					sZ = calcSigmaZ(x[i], z[j], ro, fi);
					sX = calcSigmaX(x[i], z[j], ro, fi);
					tXZ = calcTauXZ(x[i], z[j], ro, fi);
					u = calcU(x[i], ro, fi);
					w = calcW(z[j], ro, fi);
					out.printf("%5.1f %5.1f %10.3f %10.3f %10.3f %10.3f %10.3f%n", x[i], z[j], sZ, sX, tXZ, u, w);
				}
			}
			return baos.toString();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	/**
	 * 
	 * @param x горизонтальная координата
	 * @param z вертикальная координата
	 * @return модуль комплексного числа
	 */
	static double calcRo(double x, double z) {
		return Math.sqrt(Math.pow(z * z - x * x - 1, 2) + 4 * x * x * z * z);
	}

	/**
	 * 
	 * @param x  горизонтальная координата
	 * @param z  вертикальная координата
	 * @param ro модуль комплексного числа
	 * @return аргумент комплексного числа
	 */
	static double calcFi(double x, double z, double ro) {
		double dum = Double.MAX_VALUE;
		double result = 0;
		for (int v = 0; v < 157; v++) {
			double fit = 0.01 * v;
			double du = Math.abs(2 * x * z / ro - Math.sin(fit));
			if (du <= dum) {
				dum = du;
				result = fit;
			}
		}
		return result;
	}

	/**
	 * 
	 * @param x  горизонтальная координата
	 * @param z  вертикальная координата
	 * @param ro модуль комплексного числа
	 * @param fi аргумент комплексного числа
	 * @return вертикальное напряжение
	 */
    static double calcSigmaZ(double x, double z, double ro, double fi) {
        return -z / Math.pow(ro, 1.5) * Math.sin(1.5 * fi) - (x * Math.cos(0.5 * fi) + z * Math.sin(0.5 * fi)) / Math.sqrt(ro);
    }

	/**
	 * Calculates the horizontal stress component (σₓ)
	 * 
	 * @param x  horizontal coordinate
	 * @param z  vertical coordinate
	 * @param ro modulus of complex number
	 * @param fi argument of complex number
	 * @return horizontal stress component
	 */
    static double calcSigmaX(double x, double z, double ro, double fi) {
        return -z / Math.pow(ro, 1.5) * Math.sin(1.5 * fi) - (x * Math.cos(0.5 * fi) - z * Math.sin(0.5 * fi)) / Math.sqrt(ro);
    }

	/**
	 * 
	 * @param z  вертикальная координата
	 * @param ro модуль комплексного числа
	 * @param fi аргумент комплексного числа
	 * @return касательное напряжение
	 */
    static double calcTauXZ(double x, double z, double ro, double fi) {
        return -z / Math.pow(ro, 1.5) * Math.cos(1.5 * fi);
    }


	/**
	 * 
	 * @param x  горизонтальная координата
	 * @param ro модуль комплексного числа
	 * @param fi аргумент комплексного числа
	 * @return горизонталное смещение
	 */
	static double calcU(double x, double ro, double fi) {
		return x - Math.sqrt(ro) * Math.cos(fi / 2);
	}

	/**
	 * 
	 * @param z  вертикальная координата
	 * @param ro модуль комплексного числа
	 * @param fi аргумент комплексного числа
	 * @return вертикальное смещение
	 */
	static double calcW(double z, double ro, double fi) {
		return z - Math.sqrt(ro) * Math.sin(fi / 2);
	}
}
