package cloudStorage;

public final class YunHaiWeight {
	// 7-标度
	public static final double VL = 1.0;
	public static  final double L = 3.0;
	public static final double ML = 4.0;
	public static final double M = 5.0;
	public static final double MH = 6.0;
	public static final double H = 7.0;
	public static final double VH = 9.0;

	//指标个数
	public static final short k=6;
	// 正向指标
	public static double SPACE_Weight;			//节点存储空间权重
	public static double THROUGHPUT_Weight;		//节点吞吐量权重
	public static double IOPS_Weight;			//节点IOPS权重

	// 负向指标
	public static double SpaceUSAGE_Weight;		//节点存储空间使用率
	public static double CpuUSAGE_Weight;		//节点CPU利用率
	public static double MemUSAGE_Weight;		//节点内存利用率
	
	//指标权重数组 
	public static double[] weightMatrix;

	/**
	 * Construct the weight with YunHai OS default weight
	 */
	public YunHaiWeight() {
		init();
	}

	public static void init(){
		SPACE_Weight = H;
		THROUGHPUT_Weight = VL;
		IOPS_Weight = VL;

		SpaceUSAGE_Weight = VH;
		CpuUSAGE_Weight = ML;
		MemUSAGE_Weight = ML;
		
		weightMatrix = new double[k];
	}
	/**
	 * Construct the YunHai weight with given value
	 * 
	 * @param space_weight
	 * @param throughput_weight
	 * @param iops_weight
	 * @param spaceusage_weight
	 * @param cpuusage_weight
	 * @param memusage_weight
	 */
	public YunHaiWeight(double space_weight, double throughput_weight,
			double iops_weight, double spaceusage_weight,
			double cpuusage_weight, double memusage_weight) {
		
		SPACE_Weight = space_weight;
		THROUGHPUT_Weight = throughput_weight;
		IOPS_Weight = iops_weight;

		SpaceUSAGE_Weight = spaceusage_weight;
		CpuUSAGE_Weight = cpuusage_weight;
		MemUSAGE_Weight = memusage_weight;
		
		weightMatrix = new double[k];
	}

	/**
	 * normalize the weight to make the sum of them to be 1.0
	 */
	public static void normalize() {
		init();
		double sum = CpuUSAGE_Weight + THROUGHPUT_Weight + SPACE_Weight
				+ SpaceUSAGE_Weight + MemUSAGE_Weight + IOPS_Weight;
		weightMatrix[0] = SPACE_Weight / sum;
		weightMatrix[1] = THROUGHPUT_Weight / sum;
		weightMatrix[2] = IOPS_Weight / sum;

		weightMatrix[3] = SpaceUSAGE_Weight / sum;
		weightMatrix[4] = CpuUSAGE_Weight / sum;
		weightMatrix[5] = MemUSAGE_Weight / sum;
	}
}
