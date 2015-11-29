/**
 * Created by tyuyhnbnm on 11/25/2015.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Naive {
    private Scanner scanner;
    private String output;


    public Naive(String filepath) {
        try {
            scanner = new Scanner(new File(filepath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //    array_load, i1, Y, i
    public String array_load(String op, String dest, String variable,  String index) {
        String temp = "";

        // getting the correct address
        temp += "la $t1, " + variable + "\n";

        // add to the correct address by index
        if (!index.matches("\\d+")) {   // if variable
            temp += "la $t2, " + index + "\n";
            temp += "load $t2, 0($t2)";
            temp += "add $t1, $t1, $t2";
        } else {                        // if immd
            temp += "addiu $t1, $t1, " + index;
        }

        // array_loading
        temp += "load $t0, 0($t1)\n";

        // Store back the result to dest
        temp += "la $t1, " + dest;
        temp += "store $t0, $t1";
        return temp;
    }

    // TODO do i even need it?
    public String array_store(String op, String dest, String variable,  String index){
        String temp = "";

        return temp;
    }

    // assign, X, 100, 10
    // assign, sum, 0,
    public String assign(String op, String variable, String reg1, String reg2) {
        String temp = "";
        if (reg2.equals("")) { //  if not an array
            temp += "la $t0, " + variable + "\n";
            if (!reg1.matches("\\d+")) {
                temp += "la $t1, " + reg1;
                temp += "load $t1, 0($t1)";
            } else { // if immd
                temp += "addiu $t1, $zero, " + reg1;
            }
            temp += "store $t1, 0($t0)";
//            temp += "add $t1, $t1, $t2";
        } else {                // is an array_assign TODO

        }
        return temp;
    }

    public String binaryOp(String op, String reg1, String reg2, String dest) {
        String temp = "";

        if (!reg1.matches("\\d+")) {   // if variable
            temp += "la $t1, " + reg1;
            temp += "load $t1, 0($t1)";
            temp += "add $t1, $zero, $t1";
        } else { // if immd
            temp += "addiu $t1, $zero, " + reg1;
        }

        if (!reg2.matches("\\d+")) {   // if variable
            temp += "la $t2, " + reg2;
            temp += "load $t2, 0($t2)";
            temp += "add $t2, $zero, $t2";
        } else { // if immd
            temp += "addiu $t2, $zero, " + reg2;
        }

        temp += op + " $t0, $t1, $t2";

        temp += "store $t0, " + dest;

        return temp;
    }

    public String branchOp(String op, String reg1, String reg2, String branchtarget) {
        String temp = "";

        if (!reg1.matches("\\d+")) {   // if variable
            temp += "la $t1, " + reg1;
            temp += "load $t1, 0($t1)";
            temp += "add $t1, $zero, $t1";
        } else {
            temp += "addiu $t1, $zero, " + reg1;
        }

        if (!reg2.matches("\\d+")) {   // if variable
            temp += "la $t2, " + reg2;
            temp += "load $t2, 0($t2)";
            temp += "add $t2, $zero, $t2";
        } else {
            temp += "addiu $t2, $zero, " + reg2;
        }

        temp += op + " $t0, $t1, " + branchtarget;

        return temp;
    }

    public void generate() {
        //before main

        //after main
    }

    public String getSolution() {
        return output;
    }
}
