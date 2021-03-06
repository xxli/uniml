/**
 *
 */
package edu.hitsz.nlp.eisnerdep;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import edu.hitsz.nlp.eisnerdep.EisnerParser;


/**
 * @author tm
 *
 */
public class EisnerParser {

	public int maxSpanNum=2000;

		public EisnerParser(){

		}


		/**
		 * ����������ݣ�ǰ�����Ѿ��õ���Ҫ��ͳ�����HashMap
		 * @param outPath
		 * @param fileName
		 * @param size:���ӵ���Ŀ��size=0��ʾ���о���
		 */
		public void lexEisnerParse(String outPath,String fileName,String fileNameR,int size){
			StatMap newMap=new StatMap();
			newMap.readLexLink(outPath);
			Conll09File testFile=new Conll09File();
			testFile.readTestFile(outPath,fileName,size);
			//�ݹ����о���
			int sentenceNumber=testFile.sentenceNumber;
			for(int i=0;i<sentenceNumber;i++){
				System.out.println("The "+(i+1)+"th sentence is processing, Its length is "+
						testFile.totalSentence.get(i).sentenceLength);
				lexEisnerParseBySen2(testFile.totalSentence.get(i),newMap);
				//testFile.totalSentence.get(i).outputTrainSentence();
			}
			//�洢�ļ�
			String testFileO=outPath+fileNameR;
			String tempString;
			try{
				FileWriter outFileWriter=new FileWriter(testFileO);
				for(int i=0;i<sentenceNumber;i++){
					for(int j=0;j<testFile.totalSentence.get(i).sentenceLength;j++){
						tempString=j+1+"\t"+testFile.totalSentence.get(i).form[j]+"\t"
						+testFile.totalSentence.get(i).lemma[j]+"\t"
						+testFile.totalSentence.get(i).plemma[j]+"\t"
						+testFile.totalSentence.get(i).pos[j]+"\t"
						+testFile.totalSentence.get(i).ppos[j]+"\t_\t_\t"
						+testFile.totalSentence.get(i).head[j]+"\t"
						+testFile.totalSentence.get(i).phead[j]+"\t"
						+testFile.totalSentence.get(i).deprel[j]+"\t"
						+testFile.totalSentence.get(i).pdeprel[j]+"\n";
						outFileWriter.write(tempString);
					}
					outFileWriter.write("\n");
				}
				outFileWriter.close();
				System.out.println("store parsed test file done!");
			}catch (Exception e) {
	            e.printStackTrace();
			}
			System.out.println("Paring test file done!");
		}

		/**
		 * ����ÿ�����ӵ�parser
		 * ��Eisner 2000��������ͬ
		 * @param sentence
		 * @throws CloneNotSupportedException
		 */
		public void lexEisnerParseBySen1(Conll09Sentence sentence,StatMap newMap){
			int sentenceLength=sentence.sentenceLength;
			ArrayList<EisnerSpan>[][] allSpan=new ArrayList[sentenceLength][sentenceLength+1];
			for(int i=0;i<sentenceLength;i++)
				for(int j=i+1;j<sentenceLength+1;j++){
					allSpan[i][j]=new ArrayList<EisnerSpan>(maxSpanNum);
				}
			EisnerSpan span=new EisnerSpan(sentence);
			EisnerSpan spanN=new EisnerSpan(sentence);
			for(int i=0;i<sentenceLength;i++){
				if((spanN=span.seed(sentence, i))!=null){
					EisnerSpan spanLinkLeft=spanN.linkLeft(sentence, newMap);
					if(spanLinkLeft!=null){
						//spanLinkLeft.outputSpan(sentence);
						if(!allSpan[i][i+1].contains(spanLinkLeft))
							allSpan[i][i+1].add(spanLinkLeft);
					}
					EisnerSpan spanLinkRight=spanN.linkRight(sentence, newMap);
					if(spanLinkRight!=null){
						//spanLinkRight.outputSpan(sentence);
						if(!allSpan[i][i+1].contains(spanLinkRight))
							allSpan[i][i+1].add(spanLinkRight);
					}
					EisnerSpan spanNoLink=spanN.noLink(sentence);
					if(spanNoLink!=null){
						//spanNoLink.outputSpan(sentence);
						if(!allSpan[i][i+1].contains(spanNoLink))
							allSpan[i][i+1].add(spanNoLink);
					}
				}
			}
			for(int width=2;width<sentenceLength+1;width++){
				System.out.println("\t"+width+"th width is iterating......");
				for(int i=0;i<(sentenceLength+1-width);i++){
					int k=i+width;
					EisnerSpan newSpan=span.linkInf(sentence);
					allSpan[i][k].add(newSpan);
					allSpan[i][k].add(newSpan);
					for(int j=i+1;j<k;j++){
						//int leftNum=allSpan[i][j].size();
						int rightNum=allSpan[j][k].size();
						//��Ϊֻ��ǰ�������simple==1��span������spanҪ��simple==1
						for(int m=0;m<3;m++){
							for(int n=0;n<rightNum;n++){
								if(allSpan[i][j].size()>m&&allSpan[i][j].get(m).simple==1
										&&allSpan[i][j].get(m).isSpan()&&allSpan[j][k].size()>n
											&&allSpan[j][k].get(n).isSpan()){
									if((spanN=span.combine(sentence, allSpan[i][j].get(m), allSpan[j][k].get(n),newMap))!=null){
										EisnerSpan spanLinkLeft=spanN.linkLeft(sentence,newMap);
										if(spanLinkLeft!=null){
											//spanLinkRight.outputSpan(sentence);
											if(allSpan[i][k].get(0).weight<spanLinkLeft.weight)
												allSpan[i][k].set(0,spanLinkLeft);
										}
										EisnerSpan spanLinkRight=spanN.linkRight(sentence,newMap);
										if(spanLinkRight!=null){
											//spanLinkRight.outputSpan(sentence);
											if(allSpan[i][k].get(1).weight<spanLinkRight.weight)
												allSpan[i][k].set(1,spanLinkRight);
										}
										//���i=0����δ�ﵽ��󳤶ȣ��򲻱���simple��=0��
										if(i!=0||k==sentence.sentenceLength){
											EisnerSpan spanNoLink=spanN.noLink(sentence);
											if(spanNoLink!=null){
												//spanNoLink.outputSpan(sentence);
												if(!allSpan[i][i+1].contains(spanLinkLeft))
													allSpan[i][k].add(spanNoLink);
											}
										}
									}
								}
							}
						}
					}
					if(i==0&&k!=sentence.sentenceLength)
						for(int l=0;l<k-1;l++){
							int tempSize=allSpan[l][k].size();
							EisnerSpan span0=new EisnerSpan(sentence);
							EisnerSpan span1=new EisnerSpan(sentence);
							try{
								span0=(EisnerSpan) allSpan[l][k].get(0).clone();
								span1=(EisnerSpan) allSpan[l][k].get(1).clone();
							}catch (CloneNotSupportedException e) {
								e.printStackTrace();
							}
							allSpan[l][k].clear();
							allSpan[l][k].trimToSize();
							allSpan[l][k].add(span0);
							allSpan[l][k].add(span1);
						}
					if(allSpan[i][k].size()>maxSpanNum&&k!=sentence.sentenceLength)
					{
						EisnerSpan span0=new EisnerSpan(sentence);
						EisnerSpan span1=new EisnerSpan(sentence);
						try{
							span0=(EisnerSpan) allSpan[i][k].get(0).clone();
							span1=(EisnerSpan) allSpan[i][k].get(1).clone();
						}catch (CloneNotSupportedException e) {
							e.printStackTrace();
						}
						int spanSize=allSpan[i][k].size();
						ArrayList<EisnerSpan> newArrayList=new ArrayList<EisnerSpan>(spanSize);
						for(int m=2;m<spanSize;m++)
							newArrayList.add(allSpan[i][k].get(m));
						allSpan[i][k].clear();
						Comparator myComp = new EisnerSpanComparator();
						Collections.sort(newArrayList,myComp);
						allSpan[i][k].add(span0);
						allSpan[i][k].add(span1);
						for(int m=0;m<maxSpanNum-1;m++)
							allSpan[i][k].add(newArrayList.get(m));
						allSpan[i][k].trimToSize();
						newArrayList=null;
					}
				}
				//
			}
			if(allSpan[0][sentenceLength].get(0)==null){
				System.out.println("span is empty!");
				System.exit(0);
			}
			//�������������ŵ�һ����
			Comparator myComp = new EisnerSpanComparator();
			Collections.sort(allSpan[0][sentenceLength],myComp);
			if(allSpan[0][sentenceLength].get(0).weight<allSpan[0][sentenceLength].get(0).weightThres)
			{
				for(int i=0;i<sentenceLength;i++){
					sentence.head[i]=Integer.toString(0);
					sentence.phead[i]=Integer.toString(0);
				}
			}
			else{
				for(int i=0;i<sentenceLength;i++){
					int tempHead=Integer.parseInt((allSpan[0][sentenceLength].get(0).head.get(i)).toString())+1;
					if(tempHead==sentenceLength+1)
						tempHead=0;
					sentence.head[i]=Integer.toString(tempHead);
					sentence.phead[i]=Integer.toString(tempHead);
				}
			}
			//
			allSpan=null;
			/*
			long startTime=System.currentTimeMillis();   //��ȡ��ʼʱ��
			long endTime=System.currentTimeMillis(); //��ȡ����ʱ��
			System.out.println("��������ʱ�䣺 "+(endTime-startTime)+"ms");
			*/
		}



		/**
		 * ����ÿ�����ӵ�parser����ͬ�ĵ��˳��
		 * ijk----------------------------------------------- ��ʼ״̬
		 * -----------------------ijk------------------------ |�ݣ�k������
		 * ----------------i-------jk------------------------ |�飺i������
		 * ----------------i----j---k------------------------ |��j������
		 * ----------------ij-------k------------------------ |��
		 * ij-----------------------------------------------k ��ֹ״̬
		 * @param sentence
		 * @throws CloneNotSupportedException
		 */
		public void lexEisnerParseBySen2(Conll09Sentence sentence,StatMap newMap){
			int sentenceLength=sentence.sentenceLength;
			ArrayList<EisnerSpan>[][] allSpan=new ArrayList[sentenceLength][sentenceLength+1];
			for(int i=0;i<sentenceLength;i++)
				for(int j=i+1;j<sentenceLength+1;j++){
					allSpan[i][j]=new ArrayList<EisnerSpan>(maxSpanNum);
				}
			EisnerSpan span=new EisnerSpan(sentence);
			EisnerSpan spanN=new EisnerSpan(sentence);
			//���ڽڵ�(i,i+1)����span
			for(int i=0;i<sentenceLength;i++){
				if((spanN=span.seed(sentence, i))!=null){
					EisnerSpan spanLinkLeft=spanN.linkLeft(sentence, newMap);
					if(spanLinkLeft!=null){
						//spanLinkLeft.outputSpan(sentence);
						if(!allSpan[i][i+1].contains(spanLinkLeft))
							allSpan[i][i+1].add(spanLinkLeft);
					}
					EisnerSpan spanLinkRight=spanN.linkRight(sentence, newMap);
					if(spanLinkRight!=null){
						//spanLinkRight.outputSpan(sentence);
						if(!allSpan[i][i+1].contains(spanLinkRight))
							allSpan[i][i+1].add(spanLinkRight);
					}
					EisnerSpan spanNoLink=spanN.noLink(sentence);
					if(spanNoLink!=null){
						//spanNoLink.outputSpan(sentence);
						if(!allSpan[i][i+1].contains(spanNoLink))
							allSpan[i][i+1].add(spanNoLink);
					}
				}
			}
			//��ʼ�ݹ�
			for(int k=2;k<sentenceLength+1;k++){
				System.out.println("\t"+k+"th position is iterating......");
				for(int i=k-2;i>=0;i--){
					//System.out.println("i "+i+",k "+k);
					EisnerSpan newSpan=span.linkInf(sentence);
					allSpan[i][k].add(newSpan);
					allSpan[i][k].add(newSpan);
					for(int j=k-1;j>i;j--){
						//System.out.println("i "+i+",k "+k+",j "+j);
						//int leftNum=allSpan[i][j].size();
						int rightNum=allSpan[j][k].size();
						//��Ϊֻ��ǰ�������simple==1��span��������spanҪ��simple==1
						for(int m=0;m<3;m++){
							for(int n=0;n<rightNum;n++){
								if(allSpan[i][j].size()>m&&allSpan[i][j].get(m).simple==1
										&&allSpan[i][j].get(m).isSpan()&&allSpan[j][k].size()>n
											&&allSpan[j][k].get(n).isSpan()){
									if((spanN=span.combine(sentence, allSpan[i][j].get(m), allSpan[j][k].get(n),newMap))!=null){
										EisnerSpan spanLinkLeft=spanN.linkLeft(sentence,newMap);
										if(spanLinkLeft!=null){
											//spanLinkRight.outputSpan(sentence);
											if(allSpan[i][k].get(0).weight<spanLinkLeft.weight)
												allSpan[i][k].set(0,spanLinkLeft);
										}
										EisnerSpan spanLinkRight=spanN.linkRight(sentence,newMap);
										if(spanLinkRight!=null){
											//spanLinkRight.outputSpan(sentence);
											if(allSpan[i][k].get(1).weight<spanLinkRight.weight)
												allSpan[i][k].set(1,spanLinkRight);
										}
										//���i=0����δ�ﵽ��󳤶ȣ��򲻱���simple��=0��
										if(i!=0||k==sentence.sentenceLength){
											EisnerSpan spanNoLink=spanN.noLink(sentence);
											if(spanNoLink!=null){
												//spanNoLink.outputSpan(sentence);
												if(!allSpan[i][i+1].contains(spanLinkLeft))
													allSpan[i][k].add(spanNoLink);
											}
										}
									}
								}
							}
						}
					}
					//ֻ�������ŵ�ǰmaxSpanNum��span
					if(allSpan[i][k].size()>maxSpanNum)
						if(k!=sentence.sentenceLength){
							EisnerSpan span0=new EisnerSpan(sentence);
							EisnerSpan span1=new EisnerSpan(sentence);
							try{
								span0=(EisnerSpan) allSpan[i][k].get(0).clone();
								span1=(EisnerSpan) allSpan[i][k].get(1).clone();
							}catch (CloneNotSupportedException e) {
								e.printStackTrace();
							}
							int spanSize=allSpan[i][k].size();
							ArrayList<EisnerSpan> newArrayList=new ArrayList<EisnerSpan>(spanSize);
							for(int m=2;m<spanSize;m++)
								newArrayList.add(allSpan[i][k].get(m));
							allSpan[i][k].clear();
							Comparator myComp = new EisnerSpanComparator();
							Collections.sort(newArrayList,myComp);
							allSpan[i][k].add(span0);
							allSpan[i][k].add(span1);
							for(int m=0;m<maxSpanNum-1;m++)
								allSpan[i][k].add(newArrayList.get(m));
							allSpan[i][k].trimToSize();
							newArrayList=null;
						}
						else{
							int spanSize=allSpan[i][k].size();
							ArrayList<EisnerSpan> newArrayList=new ArrayList<EisnerSpan>(spanSize);
							for(int m=2;m<spanSize;m++)
								newArrayList.add(allSpan[i][k].get(m));
							allSpan[i][k].clear();
							Comparator myComp = new EisnerSpanComparator();
							Collections.sort(newArrayList,myComp);
							for(int m=0;m<maxSpanNum-1;m++)
								allSpan[i][k].add(newArrayList.get(m));
							allSpan[i][k].trimToSize();
							newArrayList=null;
						}
				}
				//ֻ����ǰ����simple=1�Ľڵ㣬��Ϊk֮ǰ��span��ȫ�����������span��combineʱ��ֻ��simple=1
				if(k!=sentence.sentenceLength)
					for(int l=0;l<k-1;l++){
						int tempSize=allSpan[l][k].size();
						EisnerSpan span0=new EisnerSpan(sentence);
						EisnerSpan span1=new EisnerSpan(sentence);
						try{
							span0=(EisnerSpan) allSpan[l][k].get(0).clone();
							span1=(EisnerSpan) allSpan[l][k].get(1).clone();
						}catch (CloneNotSupportedException e) {
							e.printStackTrace();
						}
						allSpan[l][k].clear();
						allSpan[l][k].trimToSize();
						allSpan[l][k].add(span0);
						allSpan[l][k].add(span1);
					}

			}
			if(allSpan[0][sentenceLength].get(0)==null){
				System.out.println("span is empty!");
				System.exit(0);
			}
			//�������������ŵ�һ����
			Comparator myComp = new EisnerSpanComparator();
			Collections.sort(allSpan[0][sentenceLength],myComp);
			if(allSpan[0][sentenceLength].get(0).weight<allSpan[0][sentenceLength].get(0).weightThres)
			{
				for(int i=0;i<sentenceLength;i++){
					sentence.head[i]=Integer.toString(0);
					sentence.phead[i]=Integer.toString(0);
				}
			}
			else{
				for(int i=0;i<sentenceLength;i++){
					int tempHead=Integer.parseInt((allSpan[0][sentenceLength].get(0).head.get(i)).toString())+1;
					if(tempHead==sentenceLength+1)
						tempHead=0;
					sentence.head[i]=Integer.toString(tempHead);
					sentence.phead[i]=Integer.toString(tempHead);
				}
			}
			//
			allSpan=null;
			/*
			long startTime=System.currentTimeMillis();   //��ȡ��ʼʱ��
			long endTime=System.currentTimeMillis(); //��ȡ����ʱ��
			System.out.println("��������ʱ�䣺 "+(endTime-startTime)+"ms");
			*/
		}


		public static void main(String[] args){

			String outPath="E:\\codespace\\workspace\\laparser\\";
			String trainName="CoNLL2009-ST-English-train.txt";
			String devName="CoNLL2009-ST-English-development.txt";
			String testName="CoNLL2009-ST-English-Joint.txt";
			String testoodName="CoNLL2009-ST-English-Joint-ood.txt";
			String trainNameP="preprocess-"+trainName;
			String devNameP="preprocess-"+devName;
			String testNameP="preprocess-"+testName;
			String testoodNameP="preprocess-"+testoodName;
			String trainNamePR="result-"+trainNameP;
			String devNamePR="result-"+devNameP;
			String testNamePR="result-"+testNameP;
			String testoodNamePR="result-"+testoodNameP;
			Conll09File trainFile=new Conll09File();
			EisnerParser parser=new EisnerParser();
			//parser.extLexMap();
			parser.lexEisnerParse(outPath,devNameP,devNamePR,5);


		}
	}


