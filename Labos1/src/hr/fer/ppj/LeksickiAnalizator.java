package hr.fer.ppj;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

class Lex {

    public Lex(StringBuilder sb, List<String> keywords, List<Character> znakovi) {
        this.sb = sb;
        this.keywords = keywords;
        this.znakovi = znakovi;

    }

    StringBuilder broj,sb,idn;
    String program;
    int cnt=1;
    List<String> keywords;
    List<Character> znakovi;


    public void izbacivanjeKom() {
        int pos = 0;
        while (sb.indexOf("//") > -1) {
            pos = sb.indexOf("\n", sb.indexOf("//"));
            sb.delete(sb.indexOf("//"), pos);
        }
        program = new String(sb.toString().stripTrailing());
    }

    public void izbacivanjeDelim() {
    // nema potrebe
    }

    public void ispis(String br, String id){
        if(!br.isBlank() || !id.isBlank()){
            if(!br.isEmpty()){
                System.out.printf("BROJ %d %s\n",cnt,br.toString());
            }else{
                System.out.printf("IDN %d %s\n",cnt,id.toString());
            }
        }
    }
    public void analiza(){
        for(String s : program.split("\n")) {
            // System.out.println(s);
            for (String rijeci : s.split("\\s+")) {
                if(s.isBlank())
                    break;

                if(keywords.contains(rijeci))
                    System.out.printf("KR_%s %d %s\n", rijeci.toUpperCase(),cnt,rijeci);
                else{
                    char ar[] = rijeci.toCharArray();
                    broj = new StringBuilder();
                    idn  = new StringBuilder();
                    for(int j =0 ; j<ar.length ; j++){
                        if(znakovi.contains(ar[j])){
                            ispis(broj.toString(),idn.toString());
                            broj=new StringBuilder();
                            idn = new StringBuilder();
                            switch (ar[j]) {
                                case '+' :
                                    System.out.println("OP_PLUS "+cnt+ " +");
                                    break;
                                case '-':
                                    System.out.println("OP_MINUS "+cnt+" -");
                                    break;
                                case '/':
                                    System.out.println("OP_DIJELI "+cnt+" /");
                                    break;
                                case '*':
                                    System.out.println("OP_PUTA "+cnt+" *");
                                    break;
                                case '=':
                                    System.out.println("OP_PRIDRUZI "+cnt+" =");
                                    break;
                                case '(':
                                    System.out.println("L_ZAGRADA "+cnt+" (");
                                    break;
                                case ')':
                                    System.out.println("D_ZAGRADA "+cnt+" )");
                                    break;
                            }
                        }
                        else{
                            if(Character.isDigit(ar[j]) && idn.toString().isBlank())
                                broj.append(ar[j]);
                            else if (Character.isDigit(ar[j]) && !idn.toString().isEmpty()){
                                idn.append(ar[j]);
                            }
                            else if(!(broj.toString().isEmpty()) && Character.isLetter(ar[j])) {
                                ispis(broj.toString(), idn.toString());
                                broj = new StringBuilder();
                                idn.append(ar[j]);
                            }
                            else if(Character.isLetter(ar[j])){
                                idn.append(ar[j]);
                            }
                        }


                    }
                    ispis(broj.toString(), idn.toString());
                    //   System.out.printf("RAZNO %d %s\n",cnt,rijeci);
                }
                //slj red

            }
            cnt++;
        }
    }
}

class LeksickiAnalizator{

        public static void main(String args[]){
            List<String> keywords = Arrays.stream("za,od,az,do".split(",")).collect(Collectors.toList());
            List<Character> znakovi = Arrays.asList('+','-','*','/','=','(',')');
            try{
                String ulaz;
                BufferedReader citac = new BufferedReader(new InputStreamReader(System.in));
                StringBuilder sb = new StringBuilder();

                // na null ako je sprut, !isBlank ako je testing ovako
                while((ulaz=citac.readLine()) != null){
                    sb.append(ulaz.concat("\n"));
                }

                Lex lex = new Lex(sb,keywords,znakovi);

                lex.izbacivanjeKom();
                lex.analiza();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }