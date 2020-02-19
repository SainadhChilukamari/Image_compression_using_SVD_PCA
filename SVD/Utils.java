
import org.jblas.FloatMatrix;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class PGMFile {
    String projectname;
    int height;
    int width;
    int maxValue;
    FloatMatrix A;
    long size;
}
class SVDFile {
    int height;
    int width;
    int maxValue;
    int k;
    FloatMatrix[] svd;
}


public class Utils {
    public static FloatMatrix readMatrixFromAscii(String filename){
        float[][] A = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(filename);
            Scanner scanner = new Scanner(fileInputStream);
            scanner.nextLine(); // skip line one => P2 line
            int width = Integer.parseInt(scanner.next());
            int height = Integer.parseInt(scanner.next());
            int maxValue = Integer.parseInt(scanner.next());
            A = new float[height][width];
            for (int i = 0; i < A.length; i++) {
                for (int j = 0; j < A[0].length; j++) {
                    A[i][j] = scanner.nextFloat();
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return new FloatMatrix(A);
    }

    public static PGMFile readPGMFile(String filename){
        PGMFile data = new PGMFile();
        try {
            FileInputStream fileInputStream = new FileInputStream(filename);
            Scanner scanner = new Scanner(fileInputStream);
            data.projectname = scanner.nextLine(); // skip line one => P2 line
            data.width = Integer.parseInt(scanner.next());
            data.height = Integer.parseInt(scanner.next());
            data.maxValue = Integer.parseInt(scanner.next());
            data.A = readMatrixFromAscii(filename);
            data.size = new File(filename).length();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return data;
    }

    static SVDFile readSVDFiles(String headerFile, String svdFile){
        SVDFile data = new SVDFile();
        try {
            FileInputStream fileInputStream = new FileInputStream(headerFile);
            Scanner headerScanner = new Scanner(fileInputStream);
            data.width = Integer.parseInt(headerScanner.next());
            data.height = Integer.parseInt(headerScanner.next());
            data.maxValue = Integer.parseInt(headerScanner.next());
            data.svd = new FloatMatrix[3];
        }
        catch(Exception e){
            e.printStackTrace();
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(svdFile);
            Scanner svdScanner = new Scanner(fileInputStream);
            float[][] U = new float[data.height][data.height];
            for(int i=0;i<U.length;i++){
                for(int j=0;j<U[0].length;j++){
                    U[i][j]=svdScanner.nextFloat();
                }
            }
            float[][] S = new float[data.height][1];
            for(int i=0;i<S.length;i++){
                S[i][0]=svdScanner.nextFloat();
            }
            float[][] V = new float[data.width][data.width];
            for(int i=0;i<V.length;i++){
                for(int j=0;j<V[0].length;j++){
                    V[i][j]=svdScanner.nextFloat();
                }
            }
            data.svd[0] = new FloatMatrix(U);
            data.svd[1] = new FloatMatrix(S);
            data.svd[2] = new FloatMatrix(V);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return data;
    }
    public static float convertByteArrayToFloatingPoint(byte[] _b) {
        int b = _b[0] << 8 | (_b[1] & 0xFF);
        int mantissa = b & 0x03ff;
        int exp = b & 0x7c00;
        if (exp == 0x7c00)
            exp = 0x3fc00;
        else if (exp != 0) {
            exp += 0x1c000;
            if (mantissa == 0 && exp > 0x1c400)
                return Float.intBitsToFloat((b & 0x8000) << 16 | exp << 13 | 0x3ff);
        } else if (mantissa != 0) {
            exp = 0x1c400;
            do {
                mantissa <<= 1;
                exp -= 0x400;
            } while ((mantissa & 0x400) == 0);
            mantissa &= 0x3ff;
        }

        return Float.intBitsToFloat(
                (b & 0x8000) << 16
                        | (exp | mantissa) << 13);
    }

    public static byte[] convertFloatToHalfPrecision(float a) {
        int intBits = Float.floatToIntBits(a);
        int n = (intBits & 0x7fffffff) + 0x1000;
        int b = adjust(intBits,n,0);
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (b >>> 8);
        bytes[1] = (byte) b;
        return bytes;
    }
    public static FloatMatrix[] readCompressedBin(String filename) {
        FloatMatrix[] svd = new FloatMatrix[3];
        File file = new File(filename);
        try {
            byte[] contents = Files.readAllBytes(file.toPath());
            int currentCount=0;

            byte[] _nextVal1 = {contents[currentCount], contents[currentCount+1]};
            int width=(int)convertByteArrayToFloatingPoint(_nextVal1);
            currentCount+=2;

            byte[] _nextVal2 = {contents[currentCount], contents[currentCount+1]};
            int height=(int)convertByteArrayToFloatingPoint(_nextVal2);
            currentCount+=2;

            byte[] _nextVal3 = {contents[currentCount], contents[currentCount+1]};
            int k=(int)convertByteArrayToFloatingPoint(_nextVal3);
            currentCount+=2;

            float[][] U = new float[height][k];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < k; j++) {
                    byte[] nextVal = {contents[currentCount], contents[currentCount+1]};
                    U[i][j]=convertByteArrayToFloatingPoint(nextVal);
                    currentCount+=2;
                }
            }
            svd[0]=new FloatMatrix(U);

            float[][] S = new float[k][1];
            for (int i = 0; i < k; i++) {
                byte[] nextVal = {contents[currentCount], contents[currentCount+1]};
                S[i][0]=convertByteArrayToFloatingPoint(nextVal);
                currentCount+=2;
            }
            svd[1]=new FloatMatrix(S);

            float[][] V = new float[width][k];
            for (int i = 0; i<width; i++) {
                for (int j = 0; j < k; j++) {
                    byte[] nextVal = {contents[currentCount], contents[currentCount+1]};
                    V[i][j]=convertByteArrayToFloatingPoint(nextVal);
                    currentCount+=2;
                }
            }
            svd[2]=new FloatMatrix(V);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return svd;
    }

    public static int getRank(String filename) {
        int k = -1;
        File file = new File(filename);
        try {
            byte[] contents = Files.readAllBytes(file.toPath());
            byte[] _nextVal3 = {contents[4], contents[5]};
            k=(int)convertByteArrayToFloatingPoint(_nextVal3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return k;
    }
    
    public static void saveCompressedFileToBin(FloatMatrix[] decomposedMatrix, int width, int height,  int k, String filename) {
        byte[] fileContent = new byte[2*k*(height + 1 + width)+6];
        int pointer = 0;

        byte[] _bytes = convertFloatToHalfPrecision(width);
        fileContent[pointer++] = _bytes[0];
        fileContent[pointer++] = _bytes[1];

        _bytes = convertFloatToHalfPrecision(height);
        fileContent[pointer++] = _bytes[0];
        fileContent[pointer++] = _bytes[1];

        _bytes = convertFloatToHalfPrecision(k);
        fileContent[pointer++] = _bytes[0];
        fileContent[pointer++] = _bytes[1];

        FloatMatrix U = decomposedMatrix[0];
        float[] uVals = U.transpose().toArray();
        for (int i = 0; i < uVals.length; i++) {
            byte[] bytes = convertFloatToHalfPrecision(uVals[i]);
            fileContent[pointer++] = bytes[0];
            fileContent[pointer++] = bytes[1];
        }

        FloatMatrix S = decomposedMatrix[1];
        float[] sVals = S.toArray();
        for (int i = 0; i < sVals.length; i++) {
            byte[] bytes = convertFloatToHalfPrecision(sVals[i]);
            fileContent[pointer++] = bytes[0];
            fileContent[pointer++] = bytes[1];
        }

        FloatMatrix V = decomposedMatrix[2];
        float[] vVals = V.transpose().toArray();
        for (int i = 0; i < vVals.length; i++) {
            byte[] bytes = convertFloatToHalfPrecision(vVals[i]);
            fileContent[pointer++] = bytes[0];
            fileContent[pointer++] = bytes[1];
        }

        File file = new File(filename);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileContent);
            fos.flush();
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static int adjust(int intBits,int n,int b){
        int s = intBits >>> 16 & 0x8000;
        if (n >= 0x47800000) {
            if ((intBits & 0x7fffffff) >= 0x47800000) {
                if (n < 0x7f800000)
                    b = s | 0x7c00;
                else
                    b = s | 0x7c00 |
                            (intBits & 0x007fffff) >>> 13;
            } else
                b = s | 0x7bff;
        } else if (n >= 0x38800000)
            b = s | n - 0x38000000 >>> 13;
        else if (n < 0x33000000)
            b = s;
        else {
            n = (intBits & 0x7fffffff) >>> 23;
            b = s | ((intBits & 0x7fffff | 0x800000)
                    + (0x800000 >>> n - 102)
                    >>> 126 - n);
        }
        return b;
    }



}
