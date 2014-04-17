package cloudStorage;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Hua Hero(huahero.long@gmail.com)
 * Class that implements the TOPSIS(Technique for Order Preference by Similarity to Ideal
 *  Solution) algorithm
 *  In this class,the rankedDCindex will be very useful for it will be used by the caller 
 */
public final class TOPSIS {
	public static int dataCenterSize;
	public static int nodeSize;

	/** The storage node list */
	// private List<? extends StorageNode> nodeList;
	public static List<? extends StorageNode> nodeList = null;

	/** The datacenter list */
	// private List<StorageDatacenter> datacenterList;
	public static List<StorageDatacenter> datacenterList = null;

	// ����TOPSIS�㷨�Խڵ���������Ľڵ㰴TOPSIS����Ľڵ������
	// private int[] rankedTOPSISnodeIndex;
	public static int[] rankedTOPSISnodeIndex = null;

	/**
	 * Constructor:get the rankedTOPSISnodeIndex
	 * @param dcList the whole Data Center in the multi-data center Cloud Storage System
	 *        nodeLst the all nodes in the Multi-Data center  Cloud Storage System
	 * */
	public static void buildTOPSIS(List<StorageDatacenter> dcList,List<StorageNode> nodeLst) {
		nodeList = nodeLst;
		datacenterList = dcList;
		nodeSize = nodeList.size();
		dataCenterSize = datacenterList.size();
		double totalCpu = 0.0;
		double totalMem = 0.0;
		for (int i = 0; i < dataCenterSize; i++) {
			totalCpu += datacenterList.get(i).totalMIPS;
			totalMem += datacenterList.get(i).totalMem;
		}
		// construct the TOPSIS decision matrix
		double[][] Decision = new double[YunHaiWeight.k][nodeSize];
		// initialize the Decsion matrix with each nodes'
		// capacity,IOPS,Throughput,SpaceUsage,CpuUsage and MemUsage
		for (int c = 0; c < nodeSize; c++) {
			// for(int r=0;r<YunHaiWeight.k;r++){
			StorageNode node = nodeList.get(c);
			Decision[0][c] = node.getCapacity();
			Decision[1][c] = node.getIOPS();
			Decision[2][c] = node.getThroughput();
			double currentUsage = node.getCurrentSize() / node.getCapacity();

			Decision[3][c] = currentUsage;
			Decision[4][c] = node.getPeList().get(0).getMips() / totalCpu; // this
																			// should
																			// be
																			// the
																			// cpu
																			// usage
																			// when
																			// executing
																			// ...
			Decision[5][c] = node.getRamProvisioner().getAvailableRam()
					/ totalMem;
			// }
		}
		// normalize the DecisionMatrix with Dij=Dij/(sum(power(Dij,2))),which i
		// increasing
		for (int c = 0; c < nodeSize; c++) {
			double colSum = Math.sqrt((Decision[0][c] * Decision[0][c])
					+ (Decision[1][c] * Decision[1][c])
					+ (Decision[2][c] * Decision[2][c])
					+ (Decision[3][c] * Decision[3][c])
					+ (Decision[4][c] * Decision[4][c])
					+ (Decision[5][c] * Decision[5][c]));
			for (int r = 0; r < 6; r++) {
				Decision[r][c] /= colSum;
			}

		}

		YunHaiWeight.normalize();
		// the weight matrix
		double[][] weightMatrix = new double[YunHaiWeight.k][YunHaiWeight.k];
		for (int i = 0; i < YunHaiWeight.k; i++) {
			for (int j = 0; j < YunHaiWeight.k; j++) {
				if (i == j)
					weightMatrix[i][j] = YunHaiWeight.weightMatrix[i];
				else
					weightMatrix[i][j] = 0;
			}
		}

		// compute the weighted decision matrix
		for (int j = 0; j < nodeSize; j++) {
			for (int i = 0; i < YunHaiWeight.k; i++) {
				double wD = 0.0;
				for (int m = 0; m < YunHaiWeight.k; m++) {
					wD += weightMatrix[i][m] * Decision[m][j];
				}
				Decision[i][j] = wD;
			}
		}

		/*
		 * get the positive ideal solution which means that in the positive
		 * ideal solution, the negative property is smllest,and the positive
		 * property is biggest
		 */
		double[] posIdealSolution = new double[YunHaiWeight.k];
		double[] negIdealSolution = new double[YunHaiWeight.k];
		negIdealSolution[5] = posIdealSolution[0] = Double.MIN_VALUE;
		negIdealSolution[4] = posIdealSolution[1] = Double.MIN_VALUE;
		negIdealSolution[3] = posIdealSolution[2] = Double.MIN_VALUE;
		negIdealSolution[2] = posIdealSolution[3] = Double.MAX_VALUE;
		negIdealSolution[1] = posIdealSolution[4] = Double.MAX_VALUE;
		negIdealSolution[0] = posIdealSolution[5] = Double.MAX_VALUE;

		for (int c = 0; c < nodeSize; c++) {
			// ������ָ�꣬�����Ҫȡ�������ֵ�������Ҫȡ������Сֵ
			for (int r = 0; r < 3; r++) {
				if (Decision[r][c] > posIdealSolution[r]) {
					posIdealSolution[r] = Decision[r][c];
				}
				if (Decision[r][c] < negIdealSolution[r]) {
					negIdealSolution[r] = Decision[r][c];
				}
			}
			// ���ڸ�ָ�꣬�����Ҫȡ������Сֵ�������Ҫȡ�������ֵ
			for (int r = 3; r < 6; r++) {
				if (Decision[r][c] < posIdealSolution[r]) {
					posIdealSolution[r] = Decision[r][c];
				}
				if (Decision[r][c] > negIdealSolution[r]) {
					negIdealSolution[r] = Decision[r][c];
				}
			}
		}

		// �����ѡ�⵽������븺���ľ���
		double[] posDis = new double[nodeSize];
		double[] negDis = new double[nodeSize];
		for (int m = 0; m < nodeSize; m++) {
			posDis[m] = 0;
			negDis[m] = 0;
			for (int n = 0; n < YunHaiWeight.k; n++) {
				// �����ѡ�⵽�����ľ���
				posDis[m] += (Decision[n][m] - posIdealSolution[n])
						* (Decision[n][m] - posIdealSolution[n]);
				// �����ѡ�⵽�����ľ���
				negDis[m] += (Decision[n][m] - negIdealSolution[n])
						* (Decision[n][m] - negIdealSolution[n]);
			}
			posDis[m] = Math.sqrt(posDis[m]);
			negDis[m] = Math.sqrt(negDis[m]);

		}

		// �������ѡ��node j���������posIdealSolution����Ծ��룬rc j = negDis[j]/(neg)
		double[] Rc = new double[nodeSize];
		double[] originRc = new double[nodeSize];// originRc��¼ԭ����Rc��ֵ
		for (int j = 0; j < nodeSize; j++) {
			originRc[j] = Rc[j] = negDis[j] / (negDis[j] + posDis[j]);
		}

		// ���ݸ�node j��Rc[j]ֵ�Խڵ��������,��Rc[j]��ֵ��������
		rankedTOPSISnodeIndex = new int[nodeSize];
		Arrays.sort(Rc);
		for (int j = 0; j < nodeSize; j++) {
			for (int j1 = 0; j1 < nodeSize; j1++) {
				if (Rc[j] == originRc[j1]) {
					rankedTOPSISnodeIndex[j] = j1;
					originRc[j1] = Double.MIN_NORMAL;// ��ԭoriginRc[]���ҳ�������ΪDouble.MIN_NORMAL,��ֹ�Ժ�����ֵ�и���
					break;
				}
			}
		}

	}
}
