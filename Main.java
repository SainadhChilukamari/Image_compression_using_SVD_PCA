import java.io.*;
import java.util.*;
//program that uses command-line arguments that separates the tasks
public class Main {
    static String fileName; //fileName from command line
    static int choice; //choice to perform particular task from command line
    static String newFile; //newFile name for converted file
    static String headerFile; //headerFile name
    static String svdFile; //svdFile name
    static String rank; //rank of matrix
    public static void main(String[] args) throws IOException{
        if(args.length > 0){
            choice = Integer.parseInt(args[0]);
            switch (choice){
                case 1:
                    fileName = args[1];
                    convertToBinary(fileName);
                    break;
                case 2:
                    fileName = args[1];
                    convertToAscii(fileName);
                    break;
                case 3:
                    headerFile = args[1];
                    svdFile = args[2];
                    rank = args[3];
                    break;
                case 4:
                    break;
                default:
                    System.out.println("Input format is not correct");
                    break;
            }
        }
        else
            System.out.println("Input format is not correct");
    }
    //program that converts ascii .pgm file to binary .pgm file
    private static void convertToBinary(String fileName) throws FileNotFoundException{
        newFile =  fileName.replace(".pgm", "_b.pgm");
        //source - https://stackoverflow.com/questions/3639198/how-to-read-pgm-images-in-java
        FileInputStream fileInputStream = new FileInputStream(fileName);
        Scanner scanner = new Scanner(fileInputStream);
        scanner.nextLine(); // skip line one
        int width = Integer.parseInt(scanner.next());
        int height = Integer.parseInt(scanner.next());
        int maxValue = Integer.parseInt(scanner.next());
        //to store bytes
        byte[] byteArray = new byte[width*height+5];
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
        for(int i=0; i<height; i++){
            for (int j=0; j<width; j++){
                int pixelValue = scanner.nextInt();
                byteArray[count++] = (byte) pixelValue;
            }
        }
        //write byteArray data to newFile
        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(newFile));
        try {
            dataOutputStream.write(byteArray);
            dataOutputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //program that converts binary .pgm file to ascii .pgm file
    private static void convertToAscii(String fileName) throws IOException{
        newFile = fileName.replace("_b.pgm", "_copy.pgm");
        File file = new File(fileName);
        FileInputStream fileInputStream = new FileInputStream(file);
        //source -- https://howtodoinjava.com/java/io/how-to-read-file-content-into-byte-array-in-java/
        byte[] fileContent = new byte[(int)file.length()];
        fileInputStream.read(fileContent);
        fileInputStream.close();
        //source -- https://stackoverflow.com/questions/2885173/how-do-i-create-a-file-and-write-to-it-in-java
        PrintWriter writer = new PrintWriter(newFile);
        writer.println("P2");
        //source -- https://stackoverflow.com/questions/4768933/read-two-bytes-into-an-integer
        int width = ((fileContent[1] << 8) + (fileContent[0] & 0xff));
        int height = ((fileContent[3] << 8) + (fileContent[2] & 0xff));
        writer.println(width+" "+height);
        int maxValue = fileContent[4] & 0xFF;
        writer.println(Integer.toString(maxValue));
        //convert pixel values
        int count = 5;
        for(int i=0; i<height; i++){
            for (int j=0; j<width; j++){
                int rowValue = (fileContent[count++] & 0xFF);
                writer.print(rowValue + " ");
            }
            writer.println();
        }
        writer.close();
    }
}
