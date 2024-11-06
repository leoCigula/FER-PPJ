package hr.fer.ppj;

import javax.lang.model.element.NestingKind;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.*;
import java.util.*;

public class SintaksniAnalizator {

    public static void main(String args[]){

        //klasa za uvod u program
        SyntaxAnal analizator = new SyntaxAnal();

        // working directory
        //System.out.println(System.getProperty("user.dir"));

        //citanje gramatike iz filea
        try (BufferedReader br = Files.newBufferedReader(Paths.get("./gramatika.txt"))) {
            String line;
            List<String> lines = new ArrayList<>();
            while((line = br.readLine()) != null){
                lines.add(line);

            }
            analizator.setLinijeGramatike(lines);
            analizator.izgradnjaTablice();
            try(BufferedReader br2 = new BufferedReader(new InputStreamReader(System.in))){
                // is null za stdin za labos
                lines = new ArrayList<>();
                while(!((line= br2.readLine()).isBlank()))
                    lines.add(line);
                analizator.input(lines);
                analizator.parsiraj();
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        catch (Exception e){
            System.err.println("Doslo je do greske sa citanjem gramatike");
            e.printStackTrace();
        }


    }

}

class SyntaxAnal{

    private List<String> linijeGramatike;
    private List<String> sintaksneJedinke;
    private Map<String,Map<String,String[]>> tablica;
    private List<Entry> ulaz ;
    private StringBuilder sb;

    private Stack<String> stog= new Stack<String>();
    private Stack<Node> generativnoStablo = new Stack<Node>();


    public void setLinijeGramatike(List<String> linijeGramatike){
        this.linijeGramatike = linijeGramatike;
    }

    void izgradnjaTablice() {
        sintaksneJedinke = new ArrayList<>();
        tablica = new LinkedHashMap<>();
        for (String s : linijeGramatike) {
            //<lista_naredbi> ::= <naredba> <lista_naredbi> = {IDN KR_ZA}
            String parts[] = s.split("::=");
            // <lista_naredbi>
            //<naredba> <lista_naredbi> = {IDN KR_ZA}
            parts[0] = parts[0].trim();
            if (!sintaksneJedinke.contains(parts[0])) sintaksneJedinke.add(parts[0]);

            parts[1] = parts[1].trim();
            String drugidio[] = parts[1].split("=");
            String expr[] = drugidio[0].split(" ");
            String words[] = drugidio[1].replace(" {", "").replace("}", "").split(" ");
            //<naredba> <lista_naredbi>
            // {IDN KR_ZA}
            for (String word : words) {
                Map<String, String[]> tranzicije = new HashMap<>();
                if (tablica.containsKey(parts[0]))
                    tranzicije = tablica.get(parts[0]);
                tranzicije.put(word, expr);
                tablica.put(parts[0], tranzicije);
            }

        }
        System.out.println("Izgradena je tablica");
    }

    public void input(List<String> linije){
        ulaz = new ArrayList<>();
        for(String s : linije){
            String parts[] = s.split(" ");
            ulaz.add(new Entry(parts[0],parts[2],Integer.parseInt(parts[1])));
        }
    }

    public void parsiraj(){
        stog.push("EOF");
        stog.push("<program>");
        int pos = 0;
        Node korijen = new Node("<program>");
        Node root = korijen;
        generativnoStablo.push(korijen);

        while(!stog.isEmpty()){

            String vrhStoga = stog.pop();
            Node trenutniCvor = generativnoStablo.pop();


            if(sintaksneJedinke.contains(vrhStoga)){
                // ako je predstavlja sintaksnu jedinku ili eof
                if(pos>=ulaz.size()) break;
                if(tablica.get(vrhStoga)==null || !tablica.get(vrhStoga).containsKey(ulaz.get(pos).tipUlaznoPod())){
                    throw new IllegalArgumentException("err "+ulaz.get(pos).toString());
                }
                else{
                    // nadeni tranzicije
                    String[] prod = tablica.get(vrhStoga).get(ulaz.get(pos).tipUlaznoPod());
                    System.out.println();
                    if(prod.length >= 1 || !prod[0].equals("$")){
                        for(int i=prod.length-1; i >=0 ; i--){
                            stog.push(prod[i]);
                            Node dijete = new Node(prod[i]);
                            trenutniCvor.insert(dijete);
                            generativnoStablo.push(dijete);

                        }
                    }else{
                        trenutniCvor.insert(new Node("$"));
                    }
                }

            }
            else{
                // podatak sa stoga  == tip entry / token
                if(vrhStoga.equals(ulaz.get(pos).tipUlaznoPod())){
                    trenutniCvor.insert(new  Node(ulaz.get(pos).toString()));
                    pos++;
                }else{
                   // System.out.println("err "+ulaz.get(pos).toString().replace("\n",""));
                    throw new IllegalArgumentException("err "+ulaz.get(pos).toString().replace("\n",""));
                }

            }


        }
        if(stog.isEmpty() && pos == ulaz.size()) {
            korijen.printajPreorder(sb, 0);
            System.out.println(sb);
            return ;
        }
        else
            throw new IllegalStateException("err kraj");
    }



    SyntaxAnal(){
        System.out.println("Stvorena je instanca ");
    }

}

class Entry{
    private enum oznakaEntry {
        IDN,BROJ,KR_ZA,KR_AZ,EOF,OP_PRIDRUZI,OP_MINUS,OP_PLUS,OP_DIJELI,OP_PUTA,D_ZAGRADA,L_ZAGRADA,KR_OD,KR_DO
    };

    private oznakaEntry entry;
    private String sadrzaj;
    private int linija;

    Entry(String entry,String sadrzaj,int linija){
        this.entry= oznakaEntry.valueOf(entry);
        this.sadrzaj = sadrzaj;
        this.linija = linija;
    }

    public String tipUlaznoPod(){
        return entry.name();
    }

    @Override
    public String toString() {
        return entry.name() + " " + linija + " " +sadrzaj;
    }
    /*public void ispis(){
        System.out.printf("%s %s %d",token.name(),
    }
    */

}

class Node{
    String sadrzaj;
    ArrayList<Node> nodes;

    Node(String sadrzaj){
        this.sadrzaj = sadrzaj;
        nodes = new ArrayList<>();
    }

    void setSadrzaj(String sadrzaj){
        this.sadrzaj = sadrzaj;
    }

    void insert(Node sadrzaj){
        this.nodes.add(sadrzaj);
    }



    void printajPreorder(StringBuilder ispis, int dubina){
        for(int i=0; i<dubina;i++) ispis.append(" ");
        ispis.append(sadrzaj).append("\n");
        for(Node n : nodes){
            n.printajPreorder(ispis,dubina+1);
        }
    }
}
