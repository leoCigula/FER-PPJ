package hr.fer.ppj;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.*;

public class SintaksniAnalizator {

    public static void main(String args[]){

        //klasa za uvod u program
        System.out.println("Pozdrav PPj lab 2");
        SyntaxAnal analizator = new SyntaxAnal();

        // working directory
        //System.out.println(System.getProperty("user.dir"));

        //citanje gramatike iz filea
        try (BufferedReader br = Files.newBufferedReader(Paths.get("./gramatika.txt"))) {
            String line;
            while((line = br.readLine()) != null){
                System.out.println(line);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }

}

class SyntaxAnal{

    SyntaxAnal(){
        System.out.println("Stvorena je instanca ");
    }

}
