package hr.fer.ppj;

import java.io.*;
import java.util.Scanner;
public class LeksickiAnalizator {

    public String keywords[]={"za","od"," do","az"};
    public static void main(String args[]){
        String ulaz;
        String program;

        try{

            BufferedReader citac = new BufferedReader(new InputStreamReader(System.in));
            StringBuilder sb = new StringBuilder();
            while(!((ulaz=citac.readLine()).isBlank())){
                    sb.append(ulaz.concat("\n"));
            }
            //program = new String(sb.toString());
            //System.out.println(program);

            //izbacivanje komentara
            int pos=0;
            while( sb.indexOf("//") > -1){
                    pos=sb.indexOf("\n",sb.indexOf("//"));
                    sb.delete(sb.indexOf("//"),pos+1);
                    //System.out.printf("%d %d", i, program.indexOf('\n', i))
            }
            //izbaceni komentari
            program = new String(sb.toString().trim());

            System.out.println(program);



        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


}