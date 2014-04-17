package cloudStorage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author HuaHero(gmail.com)
 * @date:2014/4/4
 * Class that implements the AHP-BackwardCloud algorithm
 * In this class,the rankedDCindex will be very useful for it will be used by the caller 
 */
public final class AHP_BackwardCloud {
	/**the MAX number of Data Center each data can be replicated or stored*/
	public static final short MAX_DC = 5;
	
	/**经过AHP-逆向云算法排序的数据中心索引号*/
	public static int[] rankedDCindex = null;
	
	/**AHP中用到的平均随机一致性指标*/
	public static final double[] RI = { 0.00, 0.00, 0.58, 0.90, 1.12, 1.21,
			1.32, 1.41, 1.45, 1.49 };
	
	//以下这些数据是经过逆向云生成器生成的云模型数字特征
	/**broker to the destinated datacenter's net delay,Expect and variance.These
	 * results come from samples and the backward cloud
	 * */
	/*public static final double[][] netDelay = { { 144.0, 404.262857143 },
			{ 89.0, 98.0 }, { 120.0, 20.0 }, { 89.25, 329.990873016 },
			{ 123.0, 136.0 } };*/
	public static final double[][] netDelay = { { 144.0, 71.9283248857},
		{ 89.0, 15.0336190441 }, { 120.0, 20.0 }, { 90.0, 26.1621749638},
		{ 123.0, 15.3396360499} };
	
	/**evaluated data center's MTBF ,which is pair of Expect and variance*/
	/*public static final double[][] mtbf = { { 49.3566, 26.9147912 },
			{ 33.68886, 289.452373043 }, { 103.47384, 211.773047828 },
			{ 119.67175, 125.303758819 }, {98.80432,90.426677127}};*/
	
	public static final double[][] mtbf = { { 49.3566,  9.54234577959886 },
			{ 33.68886, 21.6261833560376 }, { 103.47384, 19.0195776548 },
			{ 119.67175, 14.2249332572 }, {98.44685,18.9430691338}};
	
	/**according to the task and the data-centers' evaluation,rank the index(not the id) of 
	 * the data-centers*/
	public static void AHP_BackwardCloud_Init(List<StorageDatacenter> datacenterList){
		int numDc = datacenterList.size();
		/**The average load of the whole system */
		double CMSSS_Load = 0;
		double[] usage = new double[numDc];
		/**the datacenter that can be selected*/
		for(int i = 0;i < numDc;i++){
			usage[i] = 0;
			double totalUsed = 0.0001,totalCapacity = 0.0001;//in case divide by zero
			StorageDatacenter dc = datacenterList.get(i);
			for(int j = 0;j<dc.nodeList.size();j++){
				StorageNode node = dc.nodeList.get(j);
				totalUsed += node.getCurrentSize();
				totalCapacity += node.getCapacity();
			}
			usage[i] = totalUsed / totalCapacity;
			CMSSS_Load += usage[i];
		}
		CMSSS_Load /= numDc;
		
		//关键是selDcIndex或其在被调函数中seIndex、selIndex都须一致
		List<Integer> selDcIndex = new ArrayList<Integer>();
		for(int i=0;i<numDc;i++){
			/*this is for the initial state,when the whole system load is smaller 
			 * than 0.001,we can think it stored nothing
			 */
			if(CMSSS_Load <= 0.001){
				selDcIndex.add(i);
				continue;
			}
			if(usage[i]<=CMSSS_Load){
				selDcIndex.add(i);
			}
		}
		rankedDCindex = new int[selDcIndex.size()];
		if(selDcIndex.size()==1){
			rankedDCindex[0] = selDcIndex.get(0);
			return ;
		}
		ahp_backwardCloud(datacenterList,selDcIndex,usage);
	}
	
	/**
	 * 根据AHP_逆向云算法，对数据中心排序，从而将排序后的索引保存在ranked
	 * @param datacenterLst
	 * @param selIndex
	 * @param usage
	 * @param n
	 */
	public static void ahp_backwardCloud(List<StorageDatacenter> datacenterLst,List<Integer> selIndex,double[] usage){
		/**根据1-9标度法和一致性检验后得到的标准权重矢量，分别对应数据中心的网络时延、数据中心的存储空间使用率与数据中心的无故障可靠性*/
		double[] U = {0.1884,0.0810,0.7306};	//figure out by matlab
		int k = selIndex.size();	//可选数据中心的个数
		rankedDCindex = new int[k];
		//可选数据中心在网络时延属性方面的成对比矩阵
		double[][] B1=new double[k][k];
		//可选数据中心在数据中心存储空间使用率属性方面的成对比矩阵，注意，存储空间使用率越小，权重越大
		double[][] B2=new double[k][k];
		//可选数据中心在无故障可靠性属性方面的成对比矩阵
		double[][] B3=new double[k][k];
		for(int i=0;i<k;i++){
			for(int j=0;j<i;j++){
				int uA = selIndex.get(i);
				int uB = selIndex.get(j);
				double comp = usage[uA] /usage[uB];
				if(1.000000001 == comp){
					B2[i][j] = 1.0;
				}else if(0.80000000001 <=comp && comp <=1.000000001){
					B2[i][j] = 3 / 1.0;
				}else if(0.54645245001 <=comp && comp <= 0.80000000001){
					B2[i][j] = 5 / 1.0;
				}else if(0.20001 <=comp && comp<= 0.54645245001){
					B2[i][j] = 7 / 1.0;
				}else{
					B2[i][j] = 9 / 1.0;
				}
				B2[j][i] = 1 / B2[i][j];
			}
			B2[i][i] = 1.0;
		}
		short NET_DELAY= 0;
		short MTBF = 1;
		backward_cloud(B1,selIndex,datacenterLst,NET_DELAY);
		backward_cloud(B3,selIndex,datacenterLst,MTBF);
		double[] w1 = new double[k];
		double[] w2 = new double[k];
		double[] w3 = new double[k];
		AHP_Vec.character_vec(w1,B1,k);
		AHP_Vec.character_vec(w2,B2,k);
		AHP_Vec.character_vec(w3,B3,k);
		
		/**k个数据中心每个的最终权重*/
		double[] final_weight = new double[k];
		double[] flex_weight = new double[k];
		for(int i = 0;i<k;i++){
			final_weight[i] = w1[i] * U[0]+w2[i] * U[1] +w3[i]*U[2];
			flex_weight[i] = 1/final_weight[i];
		}
		Arrays.sort(flex_weight);
		for(int i=0;i<k;i++){
			for(int j=0;j<k;j++){
				if(final_weight[j] == 1/flex_weight[i]){
					rankedDCindex[i] = selIndex.get(j);
					final_weight[j] = Double.MIN_NORMAL;
					break;
				}
			}
		}
	}
	
	public static void backward_cloud(double[][] B,List<Integer> seIndex,List<StorageDatacenter> dcLst,short Type){
		short NET_DELAY= 0;
		short MTBF = 1;
		int selSize = seIndex.size();
		if(Type == NET_DELAY){
			for(int i=0;i<selSize;i++){
				for(int j=0;j<=i;j++){
					if(i==j){
						B[i][j] = 1;
						continue;
					}
					double var = netDelay[seIndex.get(i)][0]-netDelay[seIndex.get(j)][0];
					double Mx = Math.max(netDelay[seIndex.get(i)][1], netDelay[seIndex.get(j)][1]);
					if(var >0){
						if(var >= 3*Mx){
							B[i][j] = 9.0/1.0;
							B[j][i] = 1.0/9;
						}else if(2.5 * Mx <=var && var < 3*Mx){
							B[i][j] = 7/1.0;
							B[j][i] = 1/7.0;
						}else if(2 * Mx <= var && var< 2.5 * Mx){
							B[i][j] = 5/1.0;
							B[j][i] = 1/5.0;
						}else if(Mx <= var && var <1.5 * Mx){
							B[i][j] = 3/1.0;
							B[j][i] = 1/3.0;
						}else if(Math.abs(var)<=Mx){
							B[i][j] = B[j][i] = 1.0;
						}
					}else if(var < 0){
						if(-var >= 3*Mx){
							B[j][i] = 9.0/1.0;
							B[i][j] = 1.0/9;
						}else if(2.5 * Mx <=-var && -var < 3*Mx){
							B[j][i] = 7/1.0;
							B[i][j] = 1/7.0;
						}else if(2 * Mx <= var && var< 2.5 * Mx){
							B[j][i] = 5/1.0;
							B[i][j] = 1/5.0;
						}else if(Mx <= var && var <1.5 * Mx){
							B[j][i] = 3/1.0;
							B[i][j] = 1/3.0;
						}else if(Math.abs(var)<=Mx){
							B[i][j] = B[j][i] = 1.0;
						}
					}else{
						B[i][j] =B[i][j] = 0;
					}
					
				}
			}
			
		}else if(MTBF == Type){
			for(int i=0;i<selSize;i++){
				for(int j=0;j<selSize;j++){
					if(i==j){
						B[i][j] = 1;
						continue;
					}
					double var = mtbf[seIndex.get(i)][0] - mtbf[seIndex.get(j)][0];
					double Mx = Math.max(mtbf[seIndex.get(i)][1],mtbf[seIndex.get(j)][1] );
					if(var>0){
						if(var >= 3*Mx){
							B[i][j] = 9.0/1.0;
							B[j][i] = 1.0/9;
						}else if(2.5 * Mx <=var && var < 3*Mx){
							B[i][j] = 7/1.0;
							B[j][i] = 1/7.0;
						}else if(2 * Mx <= var && var< 2.5 * Mx){
							B[i][j] = 5/1.0;
							B[j][i] = 1/5.0;
						}else if(Mx <= var && var <1.5 * Mx){
							B[i][j] = 3/1.0;
							B[j][i] = 1/3.0;
						}else if(Math.abs(var)<=Mx){
							B[i][j] = B[j][i] = 1.0;
						}
					}else if(var<0){
						if(-var >= 3*Mx){
							B[j][i] = 9.0/1.0;
							B[i][j] = 1.0/9;
						}else if(2.5 * Mx <=-var && -var < 3*Mx){
							B[j][i] = 7/1.0;
							B[i][j] = 1/7.0;
						}else if(2 * Mx <= var && var< 2.5 * Mx){
							B[i][j] = 5/1.0;
							B[j][i] = 1/5.0;
						}else if(Mx <= -var && -var <1.5 * Mx){
							B[j][i] = 3/1.0;
							B[i][j] = 1/3.0;
						}else if(Math.abs(var)<=Mx){
							B[i][j] = B[j][i] = 1.0;
						}
					}
					else{
						B[i][j] = B[j][i] = 1.0;
					}
				}
			}
		}
	}
	
	public static class AHP_Vec{
		/**
		 * 求对称矩阵B的特征向量w2
		 * @param w2 保存得到的特征向量
		 * @param B 对称阵
		 * @param k 对称阵及向量的维度
		 */
		public  static void character_vec(double[] w2,double[][] B,int k){
			//随机一致性比率
					double CR = 0.0;
					//最大特征值
					double lambda = 0.0;
					//初始向量
					double[] w0 = new double[k];
					for(int i=0;i<k;i++){
						w0[i] = 1.0/k;
					}
					//一般向量
					double[] w1 = new double[k];
					//归一化向量 w2
					
					double sum = 1.0;
					double d = 1.0;
					//误差
					double delta = 0.0001;
					while(d > delta){
						d = 0.0;
						sum = 0.0;
						//获取向量
						for(int j=0;j<k;j++){
							double t = 0.0;
							for(int l=0;l<k;l++){
								t += B[j][l] * w0[l];
							}
							w1[j] = t;
							sum += w1[j];
						}
							//向量归一化
							for(int m=0;m<k;m++){
								w2[m] = w1[m]/sum;
								
								//最大差值
								d = Math.max(Math.abs(w2[m]-w0[m]),d);
								
								//用于下次迭代
								w0[m] = w2[m];
							}
						}
						//计算矩阵最大特征值lambda,CI,RI
						lambda = 0.0;
						for(int i=0;i<k;i++){
							lambda += w1[i]/(k * w0[i]);
						}
						double CI = (lambda - k)/(k-1);
						if(RI[k-1]!=0){
							CR = CI/RI[k-1];
							if(CR>0.1)
								return ;
						}
						
						//四舍五入处理
						lambda = round(lambda,3);
						CI = Math.abs(round(CI,3));
						CR = Math.abs(round(CR,3));
						
						for(int i=0;i<k;i++){
							w0[i] = round(w0[i],4);
							w1[i] = round(w1[i],4);
							w2[i] = round(w2[i],4);
						}
		}
		/**
		 * 四舍五入处理
		 * @param v the value needed to be manupulated
		 * @param scale the scale of float point
		 * @return
		 */
		public static double round(double v, int scale) {
			if (scale < 0) {
				throw new IllegalArgumentException(
						"The scale must be a positive integer or zero");
			}
			BigDecimal b = new BigDecimal(Double.toString(v));
			BigDecimal one = new BigDecimal("1");
			return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
		}
	}
}
