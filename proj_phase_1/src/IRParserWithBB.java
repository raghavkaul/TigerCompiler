/**
 * Created by harshitjain on 11/28/15.
 */
/*
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class IRParserWithBB {
    private Scanner irFileScanner;
    private File irFile;
    private Map<Integer, BasicBlock> blockMap;
    private Map<String, Integer> blockLabelToNo;

    public IRParserWithBB(String infileName) {
        irFile = new File(infileName);
        try {
            irFileScanner = new Scanner(irFile);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }

        blockMap = new HashMap<>();
    }

    public void parse(){


        boolean prevInstrWasBranch = true;
        Map<Integer, String> instrMap = new HashMap<Integer, String>();

        BasicBlock block = new BasicBlock("",0);
        // For each leader its basic block consists of itself and all
        // instructions until the next leader or end of IR, exclusive
        for (int lineNo = 0, blockNo = 0; irFileScanner.hasNextLine(); lineNo++) {
            String line = irFileScanner.nextLine();

            //Remove and leading and trailing whitespaces
            line = line.trim();

            if(prevInstrWasBranch || isLabel(line)){

                //If first block
                if(blockNo==1){
                    //Check if line is branch
                    if(isBranchInstr(line)){
                        blockNo++;

                        //Add this block to the block map
                        String label = "$label" + Integer.toString(blockNo);
                        block = new BasicBlock(label, blockNo);
                        blockMap.put(new Integer(blockNo),block);
                        blockLabelToNo.put(label,blockNo);
                        block.add(lineNo,line);

                        //Make next instruction part of new block
                        prevInstrWasBranch = true;

                        //Get child for this block
                        String childLabel = getChildLabel(line);

                        //If Not null
                        if(childLabel!=null){
                            if(!blockLabelToNo.containsKey(childLabel)){

                                //Child block does not exist create
                                blockNo++;
                                BasicBlock tempBlock = new BasicBlock(childLabel,blockNo);
                                blockMap.put(new Integer(blockNo),tempBlock);
                                blockLabelToNo.put(childLabel,blockNo);
                                tempBlock.addParent(blockNo-1);

                            }
                            else{
                                Integer childBlockNo = blockLabelToNo.get(childLabel);
                                BasicBlock childBlock = blockMap.get(childBlockNo);
                                //Add this block as childs parent
                                childBlock.addParent(blockNo);
                            }

                            //Get child block no.
                            int childBlockNo = blockLabelToNo.get(childLabel);

                            //Add to current block
                            block.addChild(childBlockNo);
                        }
                    }

                    //If its a label
                    else if(isLabel(line)){
                        String label = line.split(":")[0].trim();
                        block = new BasicBlock(label, blockNo);
                        blockMap.put(new Integer(blockNo),block);
                        blockLabelToNo.put(label, blockNo);
                        block.add(lineNo,line);
                    }
                    //If its a simple instr it will have a temp label
                    else{
                        String label = "$label" + Integer.toString(blockNo);
                        block = new BasicBlock(label, blockNo);
                        blockMap.put(new Integer(blockNo),block);
                        blockLabelToNo.put(label, blockNo);
                        block.add(lineNo,line);
                    }
                }

                //If a new label
                else if (isLabel(line)){
                    String label = line.split(":")[0].trim();

                    //If block was previously added by a branch
                    if(blockLabelToNo.containsKey(label)){
                        Integer tempBlockNo = blockLabelToNo.get(label);
                        BasicBlock tempBlock = blockMap.get(tempBlockNo);
                        tempBlock.add(lineNo,line);

                        //Add current block as parent
                        tempBlock.addParent(blockNo);
                    }

                    //Create a new block
                    else{
                        blockNo++;
                        BasicBlock tempBlock = new BasicBlock(label, blockNo);
                        blockMap.put(new Integer(blockNo),tempBlock);
                        blockLabelToNo.put(label, blockNo);
                        tempBlock.add(lineNo,line);

                        //Add current block as parent
                        tempBlock.addParent(blockNo-1);
                    }

                }



                //if not first block
                if(blockNo != 0){
                    String label;
                    if(isLabel(line)){
                        label = line.replace(":", "");
                    }
                    else{

                    }

                }
            }



            if (defineNewBlock || isLabel(line)){
                defineNewBlock = false;
                blockNo++;

                if(isLabel(line)){
                    String label = line.replace(":", "");
                    block = new BasicBlock(label, blockNo);
                    blockMap.put(blockNo,block);
                }
                else{
                    String label = "$label" + Integer.toString(blockNo);
                    block = new BasicBlock(label, blockNo);
                    blockMap.put(blockNo,block);
                }
                block.add(lineNo,line);

                //If not the first block
                if(blockNo!=1){
                    block.addParent(blockNo-1);
                }

            }


            else if(isBranchInstr(line)){
                defineNewBlock =true;
                block.addChild(blockNo);
            }


            if (isBranchInstr(line)) {
                // We've found a leader, implying a new block
                blockNo++;
            }


        }
    }

    private boolean isBranchInstr(String instr) {
        final String[] branchInstrs = {"goto", "breq", "brneq","brlt",
                "brgt", "brgeq", "brleq", "return", "call"};

        for (String branchInstr : branchInstrs) {
            if (instr.contains(branchInstr)) return true;
        }

        return false;
    }


    private boolean isLabel(String instr){
        return instr.contains(":");
    }

    private String getChildLabel(String instr){
        if(isBranchInstr(instr)){
            if(instr.contains("goto")){
                return instr.split(",")[1].trim();
            }
            else if(instr.contains("br")){
                return instr.split(",")[3].trim();
            }
            else if(instr.contains("call")){
                return instr.split(",")[1].trim();
            }
            else if(instr.contains("return")) {
                return null;
            }
        }
        return null;
    }

}
*/