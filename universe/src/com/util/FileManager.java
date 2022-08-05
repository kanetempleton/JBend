package com.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class FileManager {

    public static String fileDataAsString(String path)  {
        // path = path.replace("//","/");
        //String str = FileUtils.readFileToString(file);
        try {
            String str = Files.readString(Paths.get(path));
            return str;
        } catch (Exception e) {
            System.out.println("[FileManager] Error finding path "+path);
            //e.printStackTrace();
        }
        return "DNE";
    }

    public static void writeFile(String file, String text) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(text);
            writer.close();
            System.out.println("Wrote to file: "+file);
        }
        catch (Exception ex) {
            System.out.println("Error writing file: "+file);
            ex.printStackTrace();
        }
    }
}
