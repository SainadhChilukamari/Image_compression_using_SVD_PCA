
import org.jblas.FloatMatrix;
import org.jblas.MatrixFunctions;
import org.jblas.ranges.RangeUtils;

import java.io.*;
import java.util.Scanner;

public class SVD {
    public static void main(String[] args) {
        if(args.length>0){
            int choice = Integer.parseInt(args[0]);
            switch (choice){
                case 1:
                    String fileName = "data/"+args[1];
                    convertToBinary(fileName);
                    break;
                case 2:
                    fileName = "data/"+args[1];
                    convertToAscii(fileName);
                    break;
                case 3:
                    String headerFile = "data/"+args[1];
                    String svdFile = "data/"+args[2];
                    int rank = Integer.parseInt(args[3]);
                    compressImage(headerFile, svdFile, rank);
                    break;
                case 4:
                    String filename = "data/"+args[1];
                    readCompressedBinImage(filename);
                    break;
                case 5:
                    String originalFile = "data/"+args[1];
                    String resultFile = "data/"+args[2];
                    String binFile = "data/"+args[3];
                    errorCalc(originalFile,resultFile,binFile);
                    break;
                default:
                    System.out.println("Input format is not correct");
                    break;
            }
        }
        else
            System.out.println("Input format is not correct");
    }

    public static void errorCalc(String originalFile, String resultFile, String binFile){
        PGMFile original = Utils.readPGMFile(originalFile);
        PGMFile result = Utils.readPGMFile(resultFile);
        FloatMatrix originalM = original.A;
        FloatMatrix resultM = result.A;
        FloatMatrix errorMatrix = MatrixFunctions.abs(resultM.sub(originalM));
        FloatMatrix mul = errorMatrix.mul(errorMatrix);
        System.out.println("\nError Calculations : ");
        System.out.println("Mean squared error: " + mul.sum() / (originalM.rows * originalM.columns));
        long s = new File(binFile).length();
        System.out.print("Compression rate : "+ 1.0*(original.size-s)/original.size+"\n");
    }

    public static void readCompressedBinImage(String filename){
        FloatMatrix[] compressedData = Utils.readCompressedBin(filename);
        compressedData[2] = compressedData[2].transpose();
        FloatMatrix resultantImage = compressedData[0].mmul(FloatMatrix.diag(compressedData[1])).mmul(compressedData[2]);
        resultantImage = resultantImage.mul(255);
        try {
            FileWriter fw = new FileWriter(new File(filename.replace("_b.pgm.SVD","_"+Utils.getRank(filename)+".pgm")));
            fw.write("P2"+"\n");
            fw.write(resultantImage.columns+" "+resultantImage.rows+"\n");
            fw.write("255"+"\n");
            for(int i=0;i<resultantImage.rows;i++){
                for(int j=0;j<resultantImage.columns;j++){
                    fw.write((int)resultantImage.get(i,j)+" ");
                }
            }
            fw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void compressImage(String headerFile, String svdFile, int k){
        SVDFile data = Utils.readSVDFiles(headerFile,svdFile);
        FloatMatrix[] compresSvd = new FloatMatrix[3];
        compresSvd[0] = data.svd[0].get(RangeUtils.all(), RangeUtils.interval(0, k));
        compresSvd[1] = data.svd[1].get(RangeUtils.interval(0, k), 0);
        compresSvd[2] = data.svd[2].get(RangeUtils.all(), RangeUtils.interval(0, k));
        Utils.saveCompressedFileToBin(compresSvd, compresSvd[2].rows, compresSvd[0].rows,  compresSvd[1].rows,headerFile.replace("_header.txt","_b.pgm.SVD"));
    }


    private static void convertToBinary(String fileName){
        try{
            String newFile = fileName.replace(".pgm", "_b.pgm");
            //source - https://stackoverflow.com/questions/3639198/how-to-read-pgm-images-in-java
            FileInputStream fileInputStream = new FileInputStream(fileName);
            Scanner scanner = new Scanner(fileInputStream);
            scanner.nextLine(); // skip line one
            int width = Integer.parseInt(scanner.next());
            int height = Integer.parseInt(scanner.next());
            int maxValue = Integer.parseInt(scanner.next());
            //to store bytes
            byte[] byteArray = new byte[width * height + 5];
            //convert width into 2 byte binary
            //source - https://stackoverflow.com/questions/1735840/how-do-i-split-an-integer-into-2-byte-binary
            byteArray[0] = (byte) (width & 0xFF);
            byteArray[1] = (byte) ((width >> 8) & 0xFF);
            //convert height into 2 byte binary
            byteArray[2] = (byte) (height & 0xFF);
            byteArray[3] = (byte) ((height >> 8) & 0xFF);
            //convert maxValue into 1 byte binary
            byteArray[4] = (byte) maxValue;
            //store pixels
            int count = 5;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int pixelValue = scanner.nextInt();
                    byteArray[count++] = (byte) pixelValue;
                }
            }
            //write byteArray data to newFile
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(newFile));
            try {
                dataOutputStream.write(byteArray);
                dataOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    private static void convertToAscii(String fileName){
        try {
            String newFile = fileName.replace("_b.pgm", "_copy.pgm");
            File file = new File(fileName);
            FileInputStream fileInputStream = new FileInputStream(file);
            //source -- https://howtodoinjava.com/java/io/how-to-read-file-content-into-byte-array-in-java/
            byte[] fileContent = new byte[(int) file.length()];
            fileInputStream.read(fileContent);
            fileInputStream.close();
            //source -- https://stackoverflow.com/questions/2885173/how-do-i-create-a-file-and-write-to-it-in-java
            PrintWriter writer = new PrintWriter(newFile);
            writer.println("P2");
            //source -- https://stackoverflow.com/questions/4768933/read-two-bytes-into-an-integer
            int width = ((fileContent[1] << 8) + (fileContent[0] & 0xff));
            int height = ((fileContent[3] << 8) + (fileContent[2] & 0xff));
            writer.println(width + " " + height);
            int maxValue = fileContent[4] & 0xFF;
            writer.println(Integer.toString(maxValue));
            //convert pixel values
            int count = 5;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int rowValue = (fileContent[count++] & 0xFF);
                    writer.print(rowValue + " ");
                }
                writer.println();
            }
            writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }







}
