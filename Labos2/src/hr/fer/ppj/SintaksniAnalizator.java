package hr.fer.ppj;

import org.w3c.dom.ElementTraversal;

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
                while(!((line = br2.readLine()).isBlank()))
                    lines.add(line);
                analizator.input(lines);
                analizator.parsiraj();
                //analizator.parse();
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

    private Stack<String> stog= new Stack<>();
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
    }

    public void input(List<String> linije){
        ulaz = new ArrayList<>();
        for(String s : linije) {
            String parts[] = s.split(" ");
            ulaz.add(new Entry(parts[0], Integer.parseInt(parts[1]), parts[2]));

        }
    }

    public void parse() {
        LinkedList<String> stog = new LinkedList<>();
        LinkedList<String> ulazneLinije = new LinkedList<>();
        LinkedList<Integer> indentacija = new LinkedList<>();
        LinkedList<Integer> indentancijaUlaznih = new LinkedList<>();

        stog.push("<program>");  // Start symbol
        indentacija.push(0);  // Start with indentation level 0
        int pos = 0;

        while (stog.size()>0 || pos<ulaz.size()) {
            if(pos==ulaz.size() && stog.size() ==0){
                ErrorHandler("kraj",pos);
            }
            String tipUlaza;
            String vrhStoga;

            if(pos==ulaz.size()){
                tipUlaza = "EOF";
            }
            else tipUlaza = ulaz.get(pos).tipUlaznoPod();

            vrhStoga = stog.getFirst();
            if (vrhStoga.equals(tipUlaza)) {
                if(!tipUlaza.equals("EOF")) {
                    ulazneLinije.add(ulaz.get(pos).toString());
                    stog.removeFirst();
                    indentancijaUlaznih.add(indentacija.removeFirst());
                }
                if (pos < ulaz.size()) {
                    pos++;
                } else {
                    ErrorHandler("kraj",pos);
                }
                continue;
            }
            else if(vrhStoga.equals("$")){
                ulazneLinije.add(stog.removeFirst());
                indentancijaUlaznih.add(indentacija.removeFirst());
                continue;
            }

            boolean postoji = false;
            Map<String, String[]> tranzicije = tablica.get(vrhStoga);
            String[] prod;
            if( tranzicije != null){
                if(tranzicije.containsKey(tipUlaza)){
                    postoji=true;
                    prod = tranzicije.get(tipUlaza);
                    int trenutnaIndentacija = indentacija.removeFirst();
                    indentancijaUlaznih.add(trenutnaIndentacija);

                    for(int i =0;i<prod.length;i++){
                        indentacija.add(0,trenutnaIndentacija+1);
                    }
                    ulazneLinije.add(stog.removeFirst());
                    stog.addAll(0, List.of(prod));

                }
            }
            if(!postoji){
                ulazneLinije.clear();
                indentancijaUlaznih.clear();

                if(pos>=ulaz.size() || stog.getFirst().equals("KR_AZ")){
                    ErrorHandler("kraj",0);
                }else{
                    ErrorHandler(ulaz.get(pos).toString(),pos);
                }
            }
        }


        // Output parse tree with indentation
        for (int i = 0; i < ulazneLinije.size(); i++) {
            for (int j = 0; j < indentancijaUlaznih.get(i); j++) {
                System.out.print(" ");
            }
            System.out.println(ulazneLinije.get(i));
        }
    }

    public void parsiraj() {
        stog.push("<program>");  // Start symbol
        int pos = 0;
        Node root = new Node("<program>");
        generativnoStablo.push(root);  // Track root in the generative stack

        while (!stog.isEmpty() || pos< ulaz.size()) {

            if (stog.size() == 0 && pos == ulaz.size())
                ErrorHandler("kraj", pos);

            String tipUlaza;
            String vrhStoga;
            Node trenutniCvor;  // Peek to get current node without popping

            if (pos >= ulaz.size()) {
                tipUlaza = "EOF";
            } else
                tipUlaza = ulaz.get(pos).tipUlaznoPod();

            vrhStoga = stog.peek();
            trenutniCvor = generativnoStablo.peek();
            if (vrhStoga.equals(tipUlaza)) {
                if (!tipUlaza.equals("EOF")) {
                    trenutniCvor.setSadrzaj(ulaz.get(pos).toString());
                    stog.pop();
                    generativnoStablo.pop();
                }
                if (pos < ulaz.size())
                    pos++;
                else
                    ErrorHandler("kraj", pos);
                continue;
            } else if (vrhStoga.equals("$")) {
                stog.pop();
                generativnoStablo.pop();
                continue;
            }

            boolean postoji = false;
            Map<String, String[]> tranz = tablica.get(vrhStoga);
            String prod[];
            if (tranz != null) {
                if (tranz.containsKey(tipUlaza)) {
                    postoji = true;
                    prod = tranz.get(tipUlaza);
                    stog.pop();
                    generativnoStablo.pop();
                    for (int i = prod.length - 1; i >= 0; i--) {
                        if(!trenutniCvor.sadrzaj.equals(prod[i]) &&   ulaz.size() > pos ? !trenutniCvor.sadrzaj.equals(ulaz.get(pos).toString()) : true ) {
                            stog.add(prod[i]);
                            Node child = new Node(prod[i]);
                            trenutniCvor.insert(child);
                            generativnoStablo.push(child);
                        }
                    }
                }
            }
            if (!postoji) {
               // stog.clear();
               // generativnoStablo.clear();

                if (pos >= ulaz.size() || stog.peek().equals("KR_AZ"))
                    ErrorHandler("kraj", 0);
                else
                    ErrorHandler(ulaz.get(pos).toString(), pos);
            }
        }
            sb = new StringBuilder();
            root.printajPreorder(sb, 0);
            System.out.println(sb.toString());
    }


    public void ErrorHandler(String token,int pos){
        if(token.equals("kraj")){
            throw new IllegalArgumentException("err kraj\n");
        }
        else if(pos<ulaz.size()){
            Entry pogresniEntry = ulaz.get(pos);
            throw new IllegalArgumentException("err "+pogresniEntry.toString());
        }
        else{
            throw new IllegalArgumentException(token);
        }
    }



    SyntaxAnal(){
        // stvorena je samo instanca
    }

}

class Entry{
    private enum oznakaEntry {
        IDN,BROJ,KR_ZA,KR_AZ,EOF,OP_PRIDRUZI,OP_MINUS,OP_PLUS,OP_DIJELI,OP_PUTA,D_ZAGRADA,L_ZAGRADA,KR_OD,KR_DO
    };

    private oznakaEntry entry;
    private String sadrzaj;
    private int linija;

    Entry(String entry,int linija,String sadrzaj){
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
        for(int i=0; i<dubina;i++) ispis = ispis.append(" ");
        ispis = ispis.append(this.sadrzaj).append("\n");
        for(int i = nodes.size() - 1 ; i>=0 ; i--){
            nodes.get(i).printajPreorder(ispis,dubina+1);
        }
    }
}
