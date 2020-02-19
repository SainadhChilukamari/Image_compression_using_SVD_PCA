
import org.jblas.FloatMatrix;
import org.jblas.Singular;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class DecomposeMatrix {

    public static FloatMatrix[] decompose(FloatMatrix A){
        A=A.div(255);
        FloatMatrix[] matDecomposition = Singular.fullSVD(A);
        return matDecomposition;
    }

    public static void main(String args[]){
        String filename = "data/"+args[0];
        PGMFile data = Utils.readPGMFile(filename);
        FloatMatrix[] decomposedMatrices = decompose(data.A);
        saveMatricesToFile(decomposedMatrices,data.height,data.width,data.maxValue,filename);
    }

    public static void saveMatricesToFile(FloatMatrix[] matrices, int height, int width,  int size, String filename){
        try {
            File headerFile = new File(filename.replace(".pgm","_header.txt"));
            FileWriter writeHeader = new FileWriter(headerFile);
            writeHeader.write(width+" "+height+"\n");
            writeHeader.write(String.valueOf(size));
            writeHeader.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        FloatMatrix U = matrices[0];
        FloatMatrix S = matrices[1];
        FloatMatrix V = matrices[2];
        List<String> data = new ArrayList<>();
        data.add(U.toString("%.3f", "", "", " ", " "));
        data.add(S.toString("%.0f", "", "", " ", " "));
        data.add(V.toString("%.3f", "", "", " ", " "));
        File file = new File(filename.replace(".pgm","_SVD.txt"));
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(String.join("\n", data).getBytes());
            outputStream.flush();
            outputStream.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
