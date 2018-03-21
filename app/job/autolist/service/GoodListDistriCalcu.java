package job.autolist.service;


import java.math.BigDecimal;
import java.util.Random;

import models.autolist.AutoListRecord.DelistDistriType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.utils.NumberUtil;

public class GoodListDistriCalcu {
	private static final Logger log = LoggerFactory.getLogger(GoodListDistriCalcu.class);
		
	//7*24，一周中每小时上架商品的比例，从周日开始
	private double[] hourRateArray;
	
	private double[] averageTypeArray = new double[] {
		0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 12, 11, 0, 0, 11, 11, 11, 0, 0, 11, 12, 12, 1, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 12, 11, 0, 0, 11, 11, 11, 0, 0, 11, 12, 12, 1, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 12, 11, 0, 0, 11, 11, 11, 0, 0, 11, 12, 12, 1, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 12, 11, 0, 0, 11, 11, 11, 0, 0, 11, 12, 12, 1, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 12, 11, 0, 0, 11, 11, 11, 0, 0, 11, 12, 12, 1, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 12, 11, 0, 0, 11, 11, 11, 0, 0, 11, 12, 12, 1, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 12, 11, 0, 0, 11, 11, 11, 0, 0, 11, 12, 12, 1, 0
	};
	
	private double[] dayTypeArray = new double[] {
		0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 13, 0, 0, 13, 14, 14, 0, 0, 0, 0, 0, 0, 0,	
		0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 13, 0, 0, 13, 14, 14, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 13, 0, 0, 13, 14, 14, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 13, 0, 0, 13, 14, 14, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 13, 0, 0, 13, 14, 14, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 13, 0, 0, 13, 14, 14, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 13, 0, 0, 13, 14, 14, 0, 0, 0, 0, 0, 0, 0
	};
	
	private double[] morningArray = new double[] {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	
			0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	
			0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	
			0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	
			0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	
			0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	
			0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
	};
	
	private double[] afternoonArray = new double[] {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 14, 0, 0, 0, 0, 0, 0, 0,	
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 14, 0, 0, 0, 0, 0, 0, 0,	
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 14, 0, 0, 0, 0, 0, 0, 0,	
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 14, 0, 0, 0, 0, 0, 0, 0,	
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 14, 0, 0, 0, 0, 0, 0, 0,	
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 14, 0, 0, 0, 0, 0, 0, 0,	
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 14, 14, 0, 0, 0, 0, 0, 0, 0
	};
	
	private double[] nightTypeArray = new double[] {
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 16, 16, 3, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 16, 16, 3, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 16, 16, 3, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 16, 16, 3, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 16, 16, 3, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 16, 16, 3, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, 16, 16, 3, 0,
	};
	
	private double[] morningAndNightArray = new double[] {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 1, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 1, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 1, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 1, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 1, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 1, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 0, 0, 0, 0, 0, 0, 0, 16, 16, 16, 1, 0
		};
	
	private double[] afternoonAndNightArray = new double[] {
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 14, 14, 0, 0, 14, 14, 14, 1, 0,	
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 14, 14, 0, 0, 14, 14, 14, 1, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 14, 14, 0, 0, 14, 14, 14, 1, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 14, 14, 0, 0, 14, 14, 14, 1, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 14, 14, 0, 0, 14, 14, 14, 1, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 14, 14, 0, 0, 14, 14, 14, 1, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 14, 14, 0, 0, 14, 14, 14, 1, 0
	};
	
	
	
	//上午0点到11点，下午是12点到6点，晚上7点到11点
	public static final int[] dailyPartTime = new int[] {11, 17, 23};
	
	public GoodListDistriCalcu(double[] hourRateArray) {
	    this.hourRateArray = hourRateArray;
	}

	public GoodListDistriCalcu(int distriType, String distriTime, String distriHours) {
		boolean[] dayTimeArray = new boolean[7];
		String[] distriTimeArray = null;
		//注意，这里是从周一开始的，而hourRateArray是从周日开始的！！！！！！！！！！！！！！！！！
		if (!StringUtils.isEmpty(distriTime))
			distriTimeArray = StringUtils.split(distriTime, ",");
		for (int i = 0; i < dayTimeArray.length; i++) {
			if (distriTimeArray == null) {
				dayTimeArray[i] = true;
				continue;
			}
			int isDistri = 1;
			int index = i - 1;
			if (index < 0)
				index = 6;
			if (index < distriTimeArray.length)
				isDistri = NumberUtil.parserInt(distriTimeArray[index], 0);
			if (isDistri > 0)
				dayTimeArray[i] = true;
			else
				dayTimeArray[i] = false;
		}
		if (distriType == DelistDistriType.Average) {
			hourRateArray = averageTypeArray;
		} else if (distriType == DelistDistriType.Day) {
			hourRateArray = dayTypeArray;
		} else if (distriType == DelistDistriType.Night) {
			hourRateArray = nightTypeArray;
		} else if (distriType == DelistDistriType.Morning) {
			hourRateArray = morningArray;
		} else if (distriType == DelistDistriType.Afternoon) {
			hourRateArray = afternoonArray;
		} else if (distriType == DelistDistriType.MorningAndNight) {
			hourRateArray = morningAndNightArray;
		} else if (distriType == DelistDistriType.AfternoonAndNight) {
			hourRateArray = afternoonAndNightArray;
		} else if (distriType == DelistDistriType.UseSelfDefine){
			if (StringUtils.isEmpty(distriHours)) {
			    hourRateArray = averageTypeArray;
			} else {
			    String[] hourArray = distriHours.split(",");
			    hourRateArray = new double[7 * 24];
			    for (int i = 0; i < hourRateArray.length; i++) {
			        hourRateArray[i] = 0;
			    }
			    for (String hourStr : hourArray) {
			        try {
			            int hour = Integer.parseInt(hourStr);
			            for (int i = 0; i < 7; i++) {
			                hourRateArray[i * 24 + hour] = 1;
			            }
			            
			        } catch (Exception ex) {
			            log.error(ex.getMessage(), ex);
			        }
			    }
			}
		} else {
		    hourRateArray = averageTypeArray;
		}
		
		if (hourRateArray == null) {
		    hourRateArray = averageTypeArray;
		}
		
		for (int i = 0; i < dayTimeArray.length; i++) {
			if (dayTimeArray[i] == false) {
				for (int j = 0; j < 24; j++) {
					hourRateArray[i * 24 + j] = 0;
				}
			}
		}
	}
	
	/**
	 * 根据宝贝总数和分布比例，计算出每小时上架多少宝贝
	 * @return
	 */
	/*public int[] getGoodDistribute() {
		//先得出每天应该上架多少宝贝
		int[] dailyNumArray = new int[7];
		int allDailyNum = 0;
		for (int i = 0; i < dailyNumArray.length; i++) {
			double dailyRate = 0;
			for (int j = 0; j < 24; j++) {
				dailyRate += hourRateArray[i * 24 + j];
			}
			dailyNumArray[i] = (int)Math.ceil(dailyRate / 100 * totalSize);
			allDailyNum += dailyNumArray[i];
		}
		//因为Math.ceil，可能hourNumArray的总和大于totalSize的
		int exceedNum = allDailyNum - totalSize;//超出的数目
		while (exceedNum > 0) {
			//那么每天减少一个，直到结束
			for (int i = 0; i < dailyNumArray.length; i++) {
				if (dailyNumArray[i] > 0) {
					dailyNumArray[i] = dailyNumArray[i] - 1;
					exceedNum--;
				}
				if (exceedNum <= 0)
					break;
			}
		}
		
		//再计算每小时上架的数目
		int[] hourNumArray = new int[7 * 24];
		
		for (int i = 0; i < 7; i++) {
			int allHourNum = 0;
			for (int j = 0; j < 24; j++) {
				hourNumArray[i * 24 + j] = (int)Math.ceil(hourRateArray[i * 24 + j] / 100 * totalSize);
				allHourNum += hourNumArray[i * 24 + j];
			}
			int diff = allHourNum - dailyNumArray[i];
			while (diff > 0) {
				for (int j = 0; j < 24; j++) {
					if (hourNumArray[i * 24 + j] > 0) {
						hourNumArray[i * 24 + j] = hourNumArray[i * 24 + j] - 1;
						diff--;
					}
					if (diff <= 0)
						break;
				}
			}
		}
		
		return hourNumArray;
	}*/
	
	private int[] getDailyNumArray(int totalSize) {
		double[] dailyRateArray = new double[7];
		for (int i = 0; i < dailyRateArray.length; i++) {
			double dailyRate = 0;
			for (int j = 0; j < 24; j++) {
				dailyRate += hourRateArray[i * 24 + j];
			}
			dailyRateArray[i] = dailyRate;
		}
		
		return getDistriByRate(dailyRateArray, totalSize);
	}
	
	private int[] getDailyPartNumArray(int dayIndex, int dailyNum) {
		double[] dailyPartRate = new double[3];
		for (int i = 0; i < 24; i++) {
			if (i <= dailyPartTime[0])
				dailyPartRate[0] += hourRateArray[dayIndex * 24 + i];
			else if (i <= dailyPartTime[1])
				dailyPartRate[1] += hourRateArray[dayIndex * 24 + i];
			else
				dailyPartRate[2] += hourRateArray[dayIndex * 24 + i];
		}
		return getDistriByRate(dailyPartRate, dailyNum);
	}
	
	//得到上午、下午或者晚上每小时的宝贝分布
	private int[] getPartHourNumArray(int partIndex, int partNum) {
		int remainder = partIndex % 3;
		int quotient = partIndex / 3;
		int startHour = quotient * 24;
		int endHour = dailyPartTime[remainder] + quotient * 24;
		if (remainder > 0) {
			startHour += dailyPartTime[remainder - 1] + 1;
		}
		int length = endHour - startHour + 1;

		double[] reteArray = new double[length];
		for (int i = 0; i < length; i++) {
			reteArray[i] = hourRateArray[startHour + i];
		}
		int[] numArray = getDistriByRate(reteArray, partNum);
		
		return numArray;
		
	}
	
	//根据比例，根据总数，获取分布
	/*private int[] getDistriByRate(double[] rateArray, int allNum) {
		int[] numArray = new int[rateArray.length];
		double allRate = 0;
		for (int i = 0; i < rateArray.length; i++) {
			allRate += rateArray[i];
		}

		for (int i = 0; i < numArray.length; i++) {
			numArray[i] = 0;
		}
		double everyRate = ((double)allNum) / allRate;//一个宝贝所占的比例
		for (int i = 0; i < allNum; i++) {
			int maxIndex = 0;//找到最大的
			double maxRate = 0;
			for (int j = 0; j < rateArray.length; j++) {
				if (rateArray[j] > maxRate) {
					maxIndex = j;
					maxRate = rateArray[j];
				}
			}
			numArray[maxIndex]++;
			rateArray[maxIndex] -= everyRate;
		}
		
		return numArray;
	}*/
	
	//根据比例，根据总数，获取分布
	private int[] getDistriByRate(double[] rateArray, int allNum) {
		int length = rateArray.length;
		int[] numArray = new int[length];
		if (length <= 0)
			return new int[0];
		if (length == 1) {
			numArray[0] = allNum;
			return numArray;
		}
		if (length == 2) {
			AverageDistri ad = new AverageDistri();
			ad.calcuAverageDistri(rateArray[0], rateArray[1], allNum);
			numArray[0] = ad.leftNum;
			numArray[1] = ad.rightNum;
			return numArray;
		}
		
		int middle = length / 2 - 1;
		double[] leftRateArray = new double[middle + 1];
		double[] rightRateArray = new double[length - middle - 1];
		double leftRate = 0;
		for (int i = 0; i < middle + 1; i++) {
			leftRateArray[i] = rateArray[i];
			leftRate += rateArray[i];
		}
		double rightRate = 0;
		for (int i = middle + 1; i < length; i++) {
			rightRateArray[i - middle - 1] = rateArray[i];
			rightRate += rateArray[i];
		}
		AverageDistri ad = new AverageDistri();
		ad.calcuAverageDistri(leftRate, rightRate, allNum);
		int leftNum = ad.leftNum;
		int rightNum = ad.rightNum;
		int[] leftNumArray = getDistriByRate(leftRateArray, leftNum);
		int[] rightNumArray = getDistriByRate(rightRateArray, rightNum);
		for (int i = 0; i < leftNumArray.length; i++) {
			numArray[i] = leftNumArray[i];
		}
		for (int i = 0; i < rightNumArray.length; i++) {
			numArray[middle + 1 + i] = rightNumArray[i];
		}
		
		return numArray;
	}
	
	
	public static int[] calcuGoodDistribute(int[] hourRateArray, int totalSize) {
	    int arrLength = 7 * 24;
	    
	    double[] doubleRateArray = new double[arrLength];
	    if (hourRateArray == null || hourRateArray.length != arrLength) {
	        for (int i = 0; i < doubleRateArray.length; i++) {
	            doubleRateArray[i] = 0;
	        }
	    } else {
	        for (int i = 0; i < doubleRateArray.length; i++) {
                doubleRateArray[i] = hourRateArray[i];
            }
	    }
	    
	    GoodListDistriCalcu calcu = new GoodListDistriCalcu(doubleRateArray);
	    int[] distriNumArray = calcu.newGetGoodDistribute(totalSize);
	    
	    return distriNumArray;
	}
	
	
	
	public int[] newGetGoodDistribute(int totalSize) {
		boolean isNull = true;
		int[] hourNumArray = new int[7 * 24];
		for (int i = 0; i < hourRateArray.length; i++) {
			if (hourRateArray[i] > 0) {
				isNull = false;
				break;
			}
		}
		
		if (isNull == true) {
			for (int i = 0; i < hourNumArray.length; i++) {
				hourNumArray[i] = 0;
			}
			return hourNumArray;
		}
		
		
		//先得出每天应该上架多少宝贝
		int[] dailyNumArray = getDailyNumArray(totalSize);
		//再一天分成上午，下午，晚上三个部分
		int[] dailyPartNumArray = new int[3 * 7];
		for (int i = 0; i < 7; i++) {
			int[] tempArray = getDailyPartNumArray(i, dailyNumArray[i]);
			dailyPartNumArray[i * 3] = tempArray[0];
			dailyPartNumArray[i * 3 + 1] = tempArray[1];
			dailyPartNumArray[i * 3 + 2] = tempArray[2];
		}
		
		//再计算每小时上架的数目
		
		int curIndex = 0;
		for (int i = 0; i < 21; i++) {
			int partNum = dailyPartNumArray[i];
			int[] numArray = getPartHourNumArray(i, partNum);
			for (int j = 0; j < numArray.length; j++) {
				hourNumArray[curIndex] = numArray[j];
				curIndex++;
			}
		}
		
		return hourNumArray;
	} 
	
	/**
	 * 根据左右比例，将总数平均分配到左右两边
	 * @author Administrator
	 *
	 */
	private static class AverageDistri {
		private int leftNum;
		private int rightNum;
		
		public void calcuAverageDistri(double leftRate, double rightRate, int allNum) {
			if (allNum <= 0) 
				return;
			if (leftRate == 0 && rightRate == 0)
				return;
			if (leftRate == 0) {
				rightNum = allNum;
				return;
			}
			if (rightRate == 0) {
				leftNum = allNum;
				return;
			}
			double allRate = leftRate + rightRate;
			double everyRate = allRate / ((double)allNum);
			
			for (int i = 0; i < allNum; i++) {
				boolean isLeft = false;
				if (leftRate > rightRate) {
					isLeft = true;
				} else if (leftRate < rightRate) {
					isLeft = false;
				} else {
					Random random = new Random();
					isLeft = random.nextBoolean();
				}
				if (isLeft == true) {
					leftNum++;
					leftRate -= everyRate;
				} else {
					rightNum++;
					rightRate -= everyRate;
				}
			}
		}
	}
	
	
	
	public static void main(String[] args) {
		GoodListDistriCalcu gl = new GoodListDistriCalcu(7, "1,1,1,1,1,1,1", "1,2,4,5,9,20,15");
		BigDecimal sum = new BigDecimal(0);
		for (int i = 0; i < gl.hourRateArray.length; i++) {
			sum = sum.add(new BigDecimal(String.valueOf(gl.hourRateArray[i])));
		}
		log.info(sum + "");
		log.info("");
		log.info("");
		log.info("");
		int[] hourNumArray = gl.newGetGoodDistribute(16);
		int all = 0;
		/*for (int i = 0; i < hourNumArray.length; i++) {
			all += hourNumArray[i];
			log.info(hourNumArray[i] + "  " + (i % 24));
		}*/
		
		for (int i = 0; i < 7; i++) {
			int count = 0;
			for (int j = 0; j < 24; j++) {
				count += hourNumArray[i * 24 + j];
			}
			log.info(i + ":  " + count);
		}
		
		log.info("");
		log.info("");
		log.info("");
		log.info(all + "");
	}
}
