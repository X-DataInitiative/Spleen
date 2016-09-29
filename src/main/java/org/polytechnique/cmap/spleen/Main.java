package org.polytechnique.cmap.spleen;

import java.io.*;
import java.util.*;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.sun.org.apache.xpath.internal.operations.Bool;

import static java.util.Arrays.copyOfRange;

public class Main {

    public static void main(String[] args) throws IOException {

        System.out.println(args[1]);
        System.out.println(args[2]);

        String inputPath = "src/main/resources/Results.csv";


        if(new File(args[1]).exists()){
            inputPath = args[1];
        }

        String outputPath = args[2];

        Integer nbKnots = Integer.parseInt(args[3]);

        Boolean normalizeOrigin = false;

        CSVReader reader = new CSVReader(new FileReader(inputPath));
        List<String[]> rows = reader.readAll();

        System.out.println("##########################################################");
        System.out.println("##########################################################");
        System.out.println("LOOKING FOR MAX DEPTH " + inputPath);
        System.out.println("##########################################################");
        System.out.println("##########################################################");


        int globalMaxKnot = 0;

        for(int i=1;i<rows.size();i++){
            String[] rowI = rows.get(i);
            int knotCount = Integer.parseInt(rowI[4]);
            int maxKnot = (int) Double.parseDouble(rowI[4 + knotCount]);
            if (maxKnot > globalMaxKnot) {
                globalMaxKnot = maxKnot;
            }
        }

        System.out.println(globalMaxKnot);

        System.out.println("##########################################################");
        System.out.println("##########################################################");
        System.out.println("LOADING RESULT PARAMETERS IN " + inputPath);
        System.out.println("##########################################################");
        System.out.println("##########################################################");


        List<String[]> result = new ArrayList<String[]>();
        int knotCountIndex = 4;
        int firstKnotIndex = knotCountIndex + 1;
        int firstEstimateIndex = knotCountIndex + nbKnots +1;

        for(int i=1; i<rows.size(); i++){
            String[] row = rows.get(i);
            String moleculeName = row[0];

            int knotCount = Integer.parseInt(row[knotCountIndex]);
            int maxKnot = (int) Double.parseDouble(row[knotCountIndex + knotCount]);

            String[] knots =  Arrays.copyOfRange(row, firstKnotIndex, firstKnotIndex + knotCount);
            String[] estimates =  Arrays.copyOfRange(row, firstEstimateIndex, firstEstimateIndex + knotCount);

            double[] ai;
            double[] bi;
            if(normalizeOrigin){
                ai = new double[knotCount+1];
                bi = new double[knotCount+1];
                ai[0] = 0.0;
                bi[0] = 0.0;
                for(int j=0; j<knotCount; j++){
                    ai[j+1] = Double.parseDouble(knots[j]);
                    bi[j+1] = Double.parseDouble(estimates[j]);
                }
            } else {
                ai = new double[knotCount];
                bi = new double[knotCount];
                for (int j = 0; j < knotCount; j++) {
                    ai[j] = Double.parseDouble(knots[j]);
                    bi[j] = Double.parseDouble(estimates[j]);
                }
            }

            System.out.println(moleculeName);
            System.out.println(Arrays.toString(ai));
            System.out.println(Arrays.toString(bi));
            System.out.println("##########################################################");

            CubicSplineFast spline = new CubicSplineFast(ai, bi);
            String[] resultLine = new String[globalMaxKnot+1];
            resultLine[0] = moleculeName;

            for(int j=0; j<globalMaxKnot; j++){
                if(j < maxKnot){
                    resultLine[j+1] = Double.toString(spline.interpolate(j));
                }else{
                    resultLine[j+1] = Double.toString(0.0);
                }
            }
            //System.out.println(maxKnot);
            //System.out.println(Arrays.toString(resultLine));
            result.add(resultLine);
        }

        System.out.println("##########################################################");
        System.out.println("##########################################################");
        System.out.println("WRITING RESULTS IN " + outputPath);
        System.out.println("##########################################################");
        System.out.println("##########################################################");

        CSVWriter writer = new CSVWriter(new FileWriter(outputPath));
        for(String[] resultLine: result){
            writer.writeNext(resultLine);
        }
        writer.close();
    }
}
