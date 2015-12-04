/**
 * Created by tyuyhnbnm on 11/25/2015.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Naive {
    private static boolean main_found = false;
    private Scanner scanner;
    private String output;
    boolean func_done = false;

    private FunctionTable funcTable;

    private List<String> irCode;
    //$t0 = dest
    //$t1 = source 1
    //$t2 = source 2
    public static final String[] regs = {"$t0", "$t1", "$t2", "$t3" , "$t4", "$t5"};

    //floating point registers
    public static final String[] fregs = {"$f0", "$f1", "$f2"};

    //for label for array loads
    public static final String arrayForLabel = "__arrforlabel__";
    public static final String arrayForEndLabel = "__arrforendlabel__";
    public int arrLabelCount = 0;

    //mips code list
    private List<String> mipsCode;

    //foatmap
    private Map<String,String> floatMap;

    //int map
    private Map<String, String> intMap;

    //array map
    private Map<String , String> arrayMap;
    private Map<String, pair> arrayFloatMap;
    private Map<String, pair> arrayIntMap;

    //array set-nouse
    private Set<String> arraySet;

    //var and float set
    private Set<String> varSet;
    private Set<String> floatSet;

    //float label for contants
    private static String floatValLabel = "floatVal";
    private int floatlabelcount = 0;

    private class pair{
        String size, val;

        public pair(String _size, String _val){
            size = _size;
            val = _val;
        }

        public String getVal(){
            return val;
        }

        public String getSize(){
            return size;
        }

    }

    //Naive main
    public Naive(List<String> _irCode) {

        irCode = _irCode;

        floatMap = new HashMap<String, String>();
        floatlabelcount = 0;
        arrLabelCount = 0;
        mipsCode = new ArrayList<String>();
        varSet = new HashSet<String>();
        arrayMap = new HashMap<String,String>();
        floatSet =new HashSet<String>();
        arraySet = new HashSet<String>();
        intMap = new HashMap<String,String>();
        arrayFloatMap =  new HashMap<String,pair>();
        arrayIntMap = new HashMap<String,pair>();


    }



    // assign, X, 100, 10
    // assign, sum, 0,

    /*.data
    array:   .word  0:100       # array of 100 integers

        .text
            li   $8, 0          # $8 is the index, and loop induction variable
            li   $13, 100       # $13 is the sentinel value for the loop
    for:    bge  $8, $13, end_for
            la   $9, array      # $9 is the base address of the array
            mul  $10, $8, 4     # $10 is the offset
            add  $11, $10, $9   # $11 is the address of desired element
            li   $12, 18        # $12 is the value 18, to be put in desired element
            sw   $12, ($11)
            add  $8, $8, 1      # increment loop induction variable
            b    for
    end_for:
*/
    public List<String> assign(String op, String variable, String reg1, String reg2) {

        List<String> retStr = new ArrayList<String>();
        boolean floatop = false;

        //check if the IRCODE is for fixed point or floating point
        if(reg1.contains(".") || reg2.contains(".") || reg1.matches("f[0-9]+") || reg2.matches("f[0-9]+") || variable.matches("f[0-9]+")){
            floatop = true;
        }

        //if only integer values
        if(!floatop){
            //If no array
            if(reg2.equals("")){

                varSet.add(variable);
                if (!reg1.matches("\\d+")) {   // if variable
                    //load variable in reg1
                    varSet.add(reg1);
                    retStr.add("lw " + regs[1] + ", " + reg1);
                } else { // if immd
                    retStr.add("addiu " + regs[1] + ", $zero, " + reg1);
                }

                retStr.add("sw " + regs[1] + "," + variable);
            }

            //if array
            else{
                arrayMap.put(variable,reg1);
                arraySet.add(variable);

                retStr.add("li " + regs[1] + ", 0");
                retStr.add("li " + regs[2] + ", " + reg1);
                String tempLabelBegin = arrayForLabel + Integer.toString(arrLabelCount++);
                String tempLabelEnd = arrayForEndLabel + Integer.toString(arrLabelCount++);

                //For label
                retStr.add(tempLabelBegin + ":" + " bge " + regs[1] + ", " + regs[2] + ", " + tempLabelEnd);
                retStr.add("la " + regs[3] + ", " + variable);
                retStr.add("mul " + regs[4] + ", " + regs[1] + ", 4");
                retStr.add("add " + regs[5] + ", " + regs[4] + ", " + regs[3]);
                retStr.add("li " + regs[6] + "," + reg2);
                retStr.add("sw " + regs[6] + ", (" + regs[5] + ")");
                retStr.add("add " + regs[1] + ", " + regs[1] + ", 1");
                retStr.add("b " + tempLabelBegin);
            }
        }

        else{
            //If no array
            if(reg2.equals("")){
                floatSet.add(variable);
                if (!reg1.contains(".")) {   // if variable

                    //load variable in reg1
                    floatSet.add(reg1);

                    retStr.add("l.s " + fregs[1] + ", " + reg1);
                } else { // if immd
                    //Add to the float map
                    String tempfloatlabel = floatValLabel + Integer.toString(floatlabelcount++);
                    floatMap.put(tempfloatlabel,reg1);
                    floatSet.add(tempfloatlabel);

                    //load the float value addres
                    retStr.add("l.s " + fregs[1] + ", " + tempfloatlabel);
                }

                retStr.add("s.s " + fregs[1] + "," + variable);
            }

            //if array
            else{

                if(reg2.contains(".")){
                    arrayFloatMap.put(variable,new pair(reg1.trim(),reg2.trim()));
                }
            }

        }

        return retStr;
    }

    public List<String> binaryOp(String op, String reg1, String reg2, String dest) {
        List<String> temp = new ArrayList<String>();
        boolean floatop = false;

        //check if the IRCODE is for fixed point or floating point
        if(reg1.contains(".") || reg2.contains(".") || reg1.matches("f[0-9]+") || reg2.matches("f[0-9]+") || dest.matches("f[0-9]+")){
            floatop = true;
        }

        //if the instruction is a float op
        if(floatop){
            //if floatlit
            if(reg1.contains(".")){
                //Add to the float map
                String tempfloatlabel = floatValLabel + Integer.toString(floatlabelcount++);
                floatMap.put(tempfloatlabel,reg1);
                floatSet.add(tempfloatlabel);

                //load the float value addres
                temp.add("l.s " + fregs[1] + ", " + tempfloatlabel);
            }
            //if a variable
            else{
                floatSet.add(reg1);
                //load the float value address
                temp.add("l.s " + fregs[1] + ", " + reg1);
            }

            //if floatlit
            if(reg2.contains(".")){
                //Add to the float map
                String tempfloatlabel = floatValLabel + Integer.toString(floatlabelcount++);
                floatMap.put(tempfloatlabel,reg2);
                floatSet.add(tempfloatlabel);

                //load the float value address
                temp.add("l.s " + fregs[2] + ", " + tempfloatlabel);
            }
            //if a variable
            else{
                floatSet.add(reg2);
                //load the float value address
                temp.add("l.s " + fregs[2] + ", " + reg2);
            }

            temp.add(op + ".s " + fregs[0] + ", " + fregs[1] + ", " + fregs[2]);
            floatSet.add(dest);
            temp.add("s.s " + fregs[0] + ", " + dest);
        }

        //Its fixed point arithmetic
        else{
            if (!reg1.matches("\\d+")) {   // if variable

                //load variable in reg1
                varSet.add(reg1);

                temp.add("lw " + regs[1] + "," +  reg1 );
                temp.add("add " + regs[1] + ", $zero ," + regs[1]);
            } else { // if immd
                temp.add("addiu " + regs[1] + ", $zero, " + reg1);
            }

            if (!reg2.matches("\\d+")) {   // if variable

                //load variable in reg1
                varSet.add(reg2);

                temp.add("lw " + regs[2] + ", " + reg2 );
                temp.add("add " + regs[2] + ", $zero ," + regs[2]);
            } else { // if immd
                temp.add("addiu " + regs[2] + ", $zero, " + reg2);
            }

            //The operation
            temp.add(op + " " + regs[0] + ", " + regs[1] + ", " + regs[2]);

            //Store the result
            temp.add("sw " + regs[0] + ", " + dest);

            varSet.add(dest);
        }

        return temp;
    }

    public List<String> branchOp(String op, String reg1, String reg2, String branchtarget) {
        List<String> retStr = new ArrayList<String>();
        String mipsOp = "";

        switch (op){
            case("breq"):{
                varSet.add(reg1);
                varSet.add(reg2);

                //if digit
                if(reg1.matches("\\d+")){
                    retStr.add("li " + regs[1] + ", " + reg1);
                }
                else{
                    retStr.add("lw " + regs[1] + ", " + reg1);
                }
                //if digit
                if(reg1.matches("\\d+")){
                    retStr.add("li " + regs[2] + ", " + reg1);
                }
                else{
                    retStr.add("lw " + regs[2] + ", " + reg1);
                }


                mipsOp =  "beq";

                retStr.add(mipsOp + " " + regs[1] + ", " + regs[2] + ", " + branchtarget);
                break;
            }
            case("brneq"):{
                varSet.add(reg1);
                varSet.add(reg2);

                //if digit
                if(reg1.matches("\\d+")){
                    retStr.add("li " + regs[1] + ", " + reg1);
                }
                else{
                    retStr.add("lw " + regs[1] + ", " + reg1);
                }
                //if digit
                if(reg1.matches("\\d+")){
                    retStr.add("li " + regs[2] + ", " + reg1);
                }
                else{
                    retStr.add("lw " + regs[2] + ", " + reg1);
                }

                mipsOp =  "bne";

                retStr.add(mipsOp + " " + regs[1] + ", " + regs[2] + ", " + branchtarget);
                break;
            }

            case("brlt"):{
                varSet.add(reg1);
                varSet.add(reg2);

                //if digit
                if(reg1.matches("\\d+")){
                    retStr.add("li " + regs[1] + ", " + reg1);
                }
                else{
                    retStr.add("lw " + regs[1] + ", " + reg1);
                }
                //if digit
                if(reg1.matches("\\d+")){
                    retStr.add("li " + regs[2] + ", " + reg1);
                }
                else{
                    retStr.add("lw " + regs[2] + ", " + reg1);
                }

                //set $t0 if reg1 < reg2
                retStr.add("slt " + regs[0] + ", " + regs[1] + ", " + regs[2]);

                mipsOp =  "bne";

                //branch if equal to 1
                retStr.add(mipsOp + " " + regs[0] + ", " +  " $0 , " + branchtarget);
                break;
            }

            case("brgt"):{
                varSet.add(reg1);
                varSet.add(reg2);

                //if digit
                if(reg1.matches("\\d+")){
                    retStr.add("li " + regs[1] + ", " + reg1);
                }
                else{
                    retStr.add("lw " + regs[1] + ", " + reg1);
                }
                //if digit
                if(reg1.matches("\\d+")){
                    retStr.add("li " + regs[2] + ", " + reg1);
                }
                else{
                    retStr.add("lw " + regs[2] + ", " + reg1);
                }

                //set $t0 if reg1 < reg2
                retStr.add("slt " + regs[0] + ", " + regs[1] + ", " + regs[2]);

                mipsOp =  "be";

                //branch if equal to 1
                retStr.add(mipsOp + " " + regs[0] + ", " +  " $0 , " + branchtarget);
                break;
            }


            case("brgeq"):{
                varSet.add(reg1);
                varSet.add(reg2);

                //if digit
                if(reg1.matches("\\d+")){
                    retStr.add("li " + regs[1] + ", " + reg1);
                }
                else{
                    retStr.add("lw " + regs[1] + ", " + reg1);
                }
                //if digit
                if(reg1.matches("\\d+")){
                    retStr.add("li " + regs[2] + ", " + reg1);
                }
                else{
                    retStr.add("lw " + regs[2] + ", " + reg1);
                }

                //set $t0 if reg1 < reg2
                retStr.add("sub " + regs[0] + ", " + regs[1] + ", " + regs[2]);

                mipsOp =  "bgez";

                //branch if equal to 1
                retStr.add(mipsOp + " " + regs[0] + ", " + branchtarget);
                break;
            }

            case("brleq"):{
                varSet.add(reg1);
                varSet.add(reg2);

                //if digit
                if(reg1.matches("\\d+")){
                    retStr.add("li " + regs[1] + ", " + reg1);
                }
                else{
                    retStr.add("lw " + regs[1] + ", " + reg1);
                }
                //if digit
                if(reg1.matches("\\d+")){
                    retStr.add("li " + regs[2] + ", " + reg1);
                }
                else{
                    retStr.add("lw " + regs[2] + ", " + reg1);
                }

                //set $t0 if reg1 < reg2
                retStr.add("sub " + regs[0] + ", " + regs[1] + ", " + regs[2]);

                mipsOp =  "blez";

                //branch if equal to 1
                retStr.add(mipsOp + " " + regs[0] + ", " + branchtarget);
                break;
            }

            default: {
                break;
            }

        }

        return retStr;
    }

    public List<String> callOp(String op, String funcName, List<String> paraList){
        List<String> retStr = new ArrayList<String>();

        if(op.trim().equals("call")){
            retStr.add("addi $sp, $sp, -4");
            retStr.add("sw $ra, 4($sp)");

            if(funcName.contains("printf")){
                retStr.add("l.s $f12, " + paraList.get(0));
                floatSet.add(paraList.get(0));
            }
            else{
                //passed values, arguments values
                int size =4;
                if(paraList.size()<4){
                    size = paraList.size();
                }

                for(int params = 0 ; params < size ; params++) {
                    retStr.add("lw $a" + Integer.toString(params) + ", " + paraList.get(params));
                    varSet.add(paraList.get(params));
                }
            }

            retStr.add("jal " + funcName);
            retStr.add("lw $ra, 4($sp)");
            retStr.add("addi $sp, $sp, 4");


        }

        return retStr;
    }

    public List<String> callrOp(String op, String retVar, String funcName, List<String> paraList){
        List<String> retStr = new ArrayList<String>();


        if(op.trim().equals("callr")){



            retStr.add("addi $sp, $sp, -4");
            retStr.add("sw $ra, 4($sp)");

            //passed values, arguments values
            int size =4;
            if(paraList.size()<4){
                size = paraList.size();
            }

            for(int params = 0 ; params < size ; params++){
                retStr.add("lw $a" + Integer.toString(params) + ", " + paraList.get(params));
                varSet.add(paraList.get(params));
            }
            retStr.add("jal " + funcName);
            retStr.add("sw $v0" + retVar);

            retStr.add("lw $ra, 4($sp)");
            retStr.add("addi $sp, $sp, 4");
            varSet.add(retVar);
        }

        return retStr;
    }

    public List<String> arrayStoreOp(String op, String var, String index, String val){
        List<String> retStr = new ArrayList<String>();

        boolean floatop = false;

        //check if the IRCODE is for fixed point or floating point
        if(val.contains(".")){
            floatop = true;
        }

        if(op.trim().equals("array_store")){

            //if index is digit
            if(index.matches("\\d+")){
                retStr.add("li " + regs[1] + ", " + index);
            }
            else{
                retStr.add("lw " + regs[1] + ", " + index);
                varSet.add(index);
            }

            if (floatop){
                //get offset
                retStr.add("multi " + regs[1] + ", " + regs[1] + ", 4");

                //get base addr
                retStr.add("la " + regs[2] + ", " + var);

                //get final addr
                retStr.add("add " + regs[2] + ", " + regs[1] + ", " + regs[2]);

                //Add to the float map
                if(val.contains(".")){
                    String tempfloatlabel = floatValLabel + Integer.toString(floatlabelcount++);
                    floatMap.put(tempfloatlabel,val);
                    floatSet.add(tempfloatlabel);
                    //load the float value addres
                    retStr.add("l.s " + fregs[1] + ", " + tempfloatlabel);
                }
                else{
                    retStr.add("l.s " + fregs[1] + ", " + val);
                    floatSet.add(val);
                }




                //Store the value
                retStr.add("s.s " + fregs[1] + ", (" + regs[2]+")");

            }

            else{
                //get offset
                retStr.add("multi " + regs[1] + ", " + regs[1] + ", 4");

                //get base addr
                retStr.add("la " + regs[2] + ", " + var);
                arraySet.add(var);

                //get final addr
                retStr.add("add " + regs[2] + ", " + regs[1] + ", " + regs[2]);

                //check val
                //if index is digit
                if(val.matches("\\d+")){
                    retStr.add("li " + regs[1] + ", " + val);
                }
                else{
                    retStr.add("lw " + regs[1] + ", " + val);
                    varSet.add(val);
                }

                retStr.add("sw " + regs[1] + ", 0(" + regs[2] + ")");
            }

        }

        return retStr;
    }

    public List<String> arrayLoadOp(String op, String var, String arrVar, String index){
        List<String> retStr = new ArrayList<String>();

        boolean floatop = false;

        //check if the IRCODE is for fixed point or floating point
        if(var.matches("f[0-9]+")){
            floatop = true;
        }

        if(op.trim().equals("array_load")){

            //if index is digit
            if(index.matches("\\d+")){
                retStr.add("li " + regs[1] + ", " + index);
            }
            else{
                retStr.add("lw " + regs[1] + ", " + index);
                varSet.add(index);
            }

            if (floatop){
                //get offset
                retStr.add("multi " + regs[1] + ", " + regs[1] + ", 4");

                //get base addr
                retStr.add("la " + regs[2] + ", " + arrVar);

                //get final addr
                retStr.add("add " + regs[2] + ", " + regs[1] + ", " + regs[2]);

                //load arr val
                retStr.add("lw " + fregs[1] + ",  0(" + regs[2] + ")");

                //Store the value
                retStr.add("s.s " + fregs[1] + ", " + var);
                floatSet.add(var);
            }
            else{
                //get offset
                retStr.add("multi " + regs[1] + ", " + regs[1] + ", 4");

                //get base addr
                retStr.add("la " + regs[2] + ", " + arrVar);
                arraySet.add(arrVar);

                //get final addr
                retStr.add("add " + regs[2] + ", " + regs[1] + ", " + regs[2]);

                //load arr val
                retStr.add("lw " + regs[1] + ",  0(" + regs[2] + ")");

                //Store the value
                retStr.add("sw " + regs[1] + ", " + var);
                varSet.add(var);
            }
        }

        return retStr;
    }

    public List<String> generate() {
        //before main
        String data, inst, func;

        boolean main = false;

        for(String line : irCode){
            line = line.trim();

            if(line.contains(":")){
                if(line.contains("main:")){
                    main = true;
                }
                mipsCode.add(line);
            }





            else if(main){
                String[] words = line.split(",");
                String op = words[0].trim();

                List<String> tempMips;

                switch (op){
                    case "add":
                    case "sub":
                    case "div":
                    case "mult":
                    case "or":
                    case "and":{
                        if(words.length == 4){
                            tempMips = binaryOp(op, words[1].trim(), words[2].trim(), words[3].trim());
                            mipsCode.addAll(tempMips);

                        }
                        break;
                    }

                    case "assign":{
                        if(words.length == 3){
                            tempMips = assign(op, words[1].trim(), words[2].trim(), "");
                        }
                        else{
                            tempMips = assign(op, words[1].trim(), words[2].trim(), words[3].trim());
                        }

                        mipsCode.addAll(tempMips);
                        break;
                    }

                    case "array_load":{
                        tempMips = arrayLoadOp(op, words[1].trim(), words[2].trim(), words[3].trim());
                        mipsCode.addAll(tempMips);
                        break;
                    }

                    case "array_store":{
                        tempMips = arrayStoreOp(op, words[1].trim(), words[2].trim(), words[3].trim());
                        mipsCode.addAll(tempMips);
                        break;
                    }

                    case "call":{

                        List <String> paramList = new ArrayList<>();

                        for(int index = 2; index < words.length; index++){
                            paramList.add(words[index].trim());
                        }

                        tempMips = callOp(op, words[1].trim(), paramList);
                        mipsCode.addAll(tempMips);
                        break;
                    }

                    case "callr":{

                        List <String> paramList = new ArrayList<>();

                        for(int index = 3; index < words.length; index++){
                            paramList.add(words[index].trim());
                        }

                        tempMips = callrOp(op, words[1].trim(), words[2].trim(), paramList);
                        mipsCode.addAll(tempMips);
                        break;
                    }

                    case "breq":
                    case "brneq":
                    case "brleq":
                    case "brgeq":
                    case "brlt":
                    case "brgt":{

                        tempMips = branchOp(op, words[1].trim(), words[2].trim(), words[3].trim());
                        mipsCode.addAll(tempMips);
                        break;
                    }


                    case "return":{

                        if(words[1].trim().equals("")){
                            mipsCode.add("jr $ra");
                        }
                        else{
                            if(words[1].matches("\\d+")){
                                mipsCode.add("li $v0, " + words[1].trim());
                                mipsCode.add("jr $ra");
                            }
                            else{
                                mipsCode.add("lw $v0, " + words[1].trim());
                                varSet.add(words[1].trim());
                                mipsCode.add("jr $ra");
                            }
                        }

                    }

                    default:{
                        break;
                    }
                }
            }

            else{
                String[] words = line.split(",");

                String op = words[0].trim();

                if(op.equals("assign")){
                    if(words.length == 4){
                       if(words[3].contains(".")){
                           arrayFloatMap.put(words[1].trim(),new pair(words[2].trim(),words[3].trim()));
                       }
                       else{
                           arrayIntMap.put(words[1].trim(), new pair(words[2].trim(),words[3].trim()));
                       }
                    }
                    else{
                        if(words[2].contains(".")){
                            floatMap.put(words[1].trim(), words[2].trim());
                        }
                        else{
                            intMap.put(words[1].trim(), words[2].trim());
                        }
                    }
                }

                else{
                    List<String> tempMips;

                    switch (op){
                        case "add":
                        case "sub":
                        case "div":
                        case "mult":
                        case "or":
                        case "and":{
                            if(words.length == 4){
                                tempMips = binaryOp(op, words[1].trim(), words[2].trim(), words[3].trim());
                                mipsCode.addAll(tempMips);

                            }
                            break;
                        }

                        case "assign":{
                            if(words.length == 3){
                                tempMips = assign(op, words[1].trim(), words[2].trim(), "");
                            }
                            else{
                                tempMips = assign(op, words[1].trim(), words[2].trim(), words[3].trim());
                            }

                            mipsCode.addAll(tempMips);
                            break;
                        }

                        case "array_load":{
                            tempMips = arrayLoadOp(op, words[1].trim(), words[2].trim(), words[3].trim());
                            mipsCode.addAll(tempMips);
                            break;
                        }

                        case "array_store":{
                            tempMips = arrayStoreOp(op, words[1].trim(), words[2].trim(), words[3].trim());
                            mipsCode.addAll(tempMips);
                            break;
                        }

                        case "call":{

                            List <String> paramList = new ArrayList<>();

                            for(int index = 2; index < words.length; index++){
                                paramList.add(words[index].trim());
                            }

                            tempMips = callOp(op, words[1].trim(), paramList);
                            mipsCode.addAll(tempMips);
                            break;
                        }

                        case "callr":{

                            List <String> paramList = new ArrayList<>();

                            for(int index = 3; index < words.length; index++){
                                paramList.add(words[index].trim());
                            }

                            tempMips = callrOp(op, words[1].trim(), words[2].trim(), paramList);
                            mipsCode.addAll(tempMips);
                            break;
                        }

                        case "breq":
                        case "brneq":
                        case "brleq":
                        case "brgeq":
                        case "brlt":
                        case "brgt":{

                            tempMips = branchOp(op, words[1].trim(), words[2].trim(), words[3].trim());
                            mipsCode.addAll(tempMips);
                            break;
                        }


                        case "return":{

                            if(words[1].trim().equals("")){
                                mipsCode.add("jr $ra");
                            }
                            else{
                                if(words[1].matches("\\d+")){
                                    mipsCode.add("li $v0, " + words[1].trim());
                                    mipsCode.add("jr $ra");
                                }
                                else{
                                    mipsCode.add("lw $v0, " + words[1].trim());
                                    varSet.add(words[1].trim());
                                    mipsCode.add("jr $ra");
                                }
                            }

                        }

                        default:{
                            break;
                        }
                    }
                }

            }
        }

        List<String> revMipsCode = new ArrayList<String>();

        revMipsCode.addAll(generateData());

        revMipsCode.add(".text");
        revMipsCode.add(".globl main");

        revMipsCode.addAll(mipsCode);

        revMipsCode.addAll(generateLib());

        return revMipsCode;
    }

    private List<String> generateData(){

        List<String> retStr = new ArrayList<>();
        retStr.add(".data");

        for (String key: intMap.keySet()){
            varSet.remove(key);
            retStr.add(key + ": .word " + intMap.get(key));
        }

        for (String var : varSet){
            if(!var.matches("\\d+")){
                retStr.add(var + ": .word 0");
            }

        }

        for (String key: floatMap.keySet()){
            floatSet.remove(key);
            retStr.add(key + ": .float " + floatMap.get(key));
        }

        for (String s: floatSet){
            if(!s.contains(".")){
                retStr.add(s + ": .float 0.0");
            }
        }

        for (String key: arrayIntMap.keySet()){
            String tempStr = "";
            for(int i=0; i< Integer.parseInt(arrayIntMap.get(key).getSize()) ; i++){
                tempStr = tempStr + " " + arrayIntMap.get(key).getVal();
            }
            retStr.add(key + ".word: " + tempStr);
            arrayMap.remove(key);
        }

        for (String key: arrayMap.keySet()){
            retStr.add(key + ": .space " + arrayMap.get(key));
        }

        for (String key: arrayFloatMap.keySet()){
            String tempStr = "";
            for(int i=0; i< Integer.parseInt(arrayFloatMap.get(key).getSize()) ; i++){
                tempStr = tempStr + " " + arrayFloatMap.get(key).getVal();
            }
            retStr.add(key + ": .float " + tempStr);
        }

        return  retStr;
    }

    private List<String> generateLib(){
        List<String> retStr = new ArrayList<>();
        //printi
        retStr.add("printi:");

        retStr.add("li $v0, 1");

        retStr.add("syscall");

        retStr.add("jr $ra");



        //printf
        retStr.add("printf:");

        retStr.add("li $v0, 2");

        retStr.add("syscall");

        retStr.add("jr $ra");


        return  retStr;
    }

}
