package hr.fer.ppj;

import java.io.*;
import java.util.Scanner;
public class LeksickiAnalizator {

    public String keywords[]={"za","od"," do","az"};
    public static void main(String args[]){
        String ulaz;
        System.out.println("Pozdrav svijete");

        try{

            BufferedReader citac = new BufferedReader(new InputStreamReader(System.in));
            while(!((ulaz=citac.readLine()).isBlank())){
                System.out.println(ulaz);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
