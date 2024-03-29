package com.util;

import java.nio.file.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;

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

    public static String[] listFiles(String directory) {
        System.out.println("Listing files for "+directory);
        File folder = new File(directory);
        if (folder==null) {
            System.out.println("null");
        } else {
            System.out.println("good to go");
        }
        if (folder.listFiles()==null) {
            System.out.println("null File.listFiles");
        }else {
            System.out.println("good to go 2");
        }
        return Tools.string_array(folder.listFiles());
    }
}
