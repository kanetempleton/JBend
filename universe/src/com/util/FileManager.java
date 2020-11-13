package com.util;

import java.nio.file.Files;
import java.nio.file.Paths;

public class FileManager {

    public static String fileDataAsString(String path)  {
        // path = path.replace("//","/");
        //String str = FileUtils.readFileToString(file);
        try {
            String str = Files.readString(Paths.get(path));
            return str;
        } catch (Exception e) {
            System.out.println("Error finding path "+path);
            e.printStackTrace();
        }
        return "DNE";
    }
}
