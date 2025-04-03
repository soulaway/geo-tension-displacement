package gi.soloviev.tendis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 
 */

public class TenDis {

	// ROUND_CALC = false, - мат. исчисления выполняются без округления с 18 знаками
	// полсле зарятой (по умолчанию типа double)
	private static final boolean ROUND_CALC = false;

	// не менее 4 не более 17
	private static final int PRECISION = 6;

	private static final boolean ROUND_UP = true;

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
			out.printf("%1s %5s %1s %5s %1s %10s %1s %10s %1s %10s %1s %10s %1s %10s %1s%n", "|", "X", "|", "Z", "|",
					"SIGZ", "|", "SIGX", "|", "TAUXZ", "|", "U", "|", "W", "|");
			for (int j = 0; j < z.length; j++) {
				for (int i = 0; i < x.length; i++) {
					ro = calcRo(x[i], z[j]);
					fi = calcFi(x[i], z[j], ro);
					sZ = calcSigmaZ(x[i], z[j], ro, fi);
					sX = calcSigmaX(x[i], z[j], ro, fi);
					tXZ = calcTauXZ(x[i], z[j], ro, fi);
					u = calcU(x[i], ro, fi);
					w = calcW(z[j], ro, fi);
					out.printf("%1s %5.1f %1s %5.1f %1s %10.3f %1s %10.3f %1s %10.3f %1s %10.3f %1s %10.3f %1s%n", "|",
							x[i], "|", z[j], "|", sZ, "|", sX, "|", tXZ, "|", u, "|", w, "|");
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
		return round(Math.sqrt(round(Math.pow(z * z - x * x - 1, 2)) + 4 * x * x * z * z));
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
			double du = round(Math.abs(round(2 * x * z / ro) - round(Math.sin(fit))));
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
		return - (z * Math.sin(1.5 * fi)) / Math.pow(ro, 1.5)
				- (x * Math.cos(fi * 0.5) + z * Math.sin(fi * 0.5)) / Math.pow(ro, 0.5);
	}
	/**
	 * 
	 * @param x  горизонтальная координата
	 * @param z  вертикальная координата
	 * @param ro модуль комплексного числа
	 * @param fi аргумент комплексного числа
	 * @return горизонтальное напряжение
	 */
	static double calcSigmaX(double x, double z, double ro, double fi) {
		return (z * Math.sin(1.5 * fi)) / Math.pow(ro, 1.5)
				- (x * Math.cos(fi * 0.5) + z * Math.sin(fi * 0.5)) / Math.pow(ro, 0.5);
	}

	/**
	 * 
	 * @param z  вертикальная координата
	 * @param ro модуль комплексного числа
	 * @param fi аргумент комплексного числа
	 * @return касательное напряжение
	 */
	static double calcTauXZ(double x, double z, double ro, double fi) {
		return round(-z / round(Math.pow(ro, 1.5)) * round(Math.cos(1.5 * fi)));
	}

	/**
	 * 
	 * @param x  горизонтальная координата
	 * @param ro модуль комплексного числа
	 * @param fi аргумент комплексного числа
	 * @return горизонталное смещение
	 */
	static double calcU(double x, double ro, double fi) {
		return x - round(Math.sqrt(ro)) * round(Math.cos(fi / 2));
	}

	/**
	 * 
	 * @param z  вертикальная координата
	 * @param ro модуль комплексного числа
	 * @param fi аргумент комплексного числа
	 * @return вертикальное смещение
	 */
	static double calcW(double z, double ro, double fi) {
		return z - round(Math.sqrt(ro)) * round(Math.sin(fi / 2));
	}

	/**
	 * Oкругление чисел большой и бесконечной размерности которая может возникнуть
	 * при округлении с малой точностью (3 знака и менее)
	 * 
	 * @param val
	 * @return конечное число округленное с заданной точностью и направлением
	 */
	static double round(double val) {
		if (ROUND_CALC) {
			if (Double.isInfinite(val)) {
				val = (Double.POSITIVE_INFINITY == val) ? Double.MAX_VALUE : Double.MIN_NORMAL;
			}
			return ROUND_UP ? roundUp(val, PRECISION) : roundDown(val, PRECISION);
		}
		return val;
	}

	static double roundDown(double value, int precision) {
		return BigDecimal.valueOf(value).setScale(precision, RoundingMode.HALF_DOWN).doubleValue();
	}

	static double roundUp(double value, int precision) {
		return BigDecimal.valueOf(value).setScale(precision, RoundingMode.HALF_UP).doubleValue();
	}
}
