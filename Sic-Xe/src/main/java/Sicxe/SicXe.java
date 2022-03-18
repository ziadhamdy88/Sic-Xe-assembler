package Sicxe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author magic
 */
public class SicXe {
    static String[] op_TAB ={ "ADD","ADDF","ADDR","AND","CLEAR","COMP", 
                               "COMPF","COMPR","DIV","DIVF","DIVR","FIX",
                               "FLOAT","HIO","J","JEQ","JGT","JLT",
                               "JSUB","LDA","LDB","LDCH","LDF","LDL",
                               "LDS","LDT","LDX","LPS","MUL","MULF",
                               "MULR","NORM","OR","RD","RMO","RSUB",
                               "SHIFTL","SHIFTR","SIO","SSK","STA","STB",
                               "STCH","STF","STI","STL","STS","STSW",
                               "STT","STX","SUB","SUBF","SUBR","SVC",
                               "TD","TIO","TIX","TIXR","WD"};
    
    static String[] op_TAB1 ={ "FIX","FLOAT","HIO","NORM","SIO","TIO"};
    
    static String[] op_TAB2 ={ "ADDR" , "CLEAR" ,"COMPR","DIVR","MULR","RMO","SHIFTL","SHIFTR","SUBR","SVC","TIXR"};
        
    static String[] opCode = { "18", "58", "90", "40", "B4", "28", "88", "A0", "24", "64", "9C", "C4", "C0", "F4", "3C",
                               "30", "34", "38", "48", "00", "68", "50", "70", "08", "6C", "74", "04", "E0", "20", "60",
                               "98", "C8", "44", "D8", "AC", "4C", "A4", "A8", "F0", "EC", "0C", "78", "54", "80", "D4",
                               "14", "7C", "E8", "84", "10", "1C", "5C", "94", "B0", "E0", "F8", "2C", "B8", "DC" };
    
    
    static int counter = 0;
    static String LitSymAddress = "";
    static String BaseAddress = "";
    static String BaseSymbol = "";
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        int TCount=0;
        int displacement;
        int checkpoint=0;
        int instcount = 0;
        int Address=0;
        int j = 0;        //FirstPassCounter
        
        String xbpe;
        String xbpetemp;
        String ni;
        
        ArrayList<String> SymbolName= new ArrayList<>();
        ArrayList<String> OCode= new ArrayList<>();
        ArrayList<String> LiteralName= new ArrayList<>();
        ArrayList<String> Modification = new ArrayList<>();
        ArrayList<Integer> SymbolAddress = new ArrayList<>();
        ArrayList<Integer> LiteralAddress = new ArrayList<>();
        ArrayList<Integer> LiteralSize = new ArrayList<>();
        ArrayList<Integer> StartAddress = new ArrayList<>();
        
        File file = new File ("Instructions.txt");
        FileWriter Writer = new FileWriter(new File("LiteralTable.txt"));
        try (Scanner scanner = new Scanner (file))
        { 
            while (scanner.hasNext())
            {
                
                String Line = scanner.nextLine();
                String [] Arr = Line.split("\\t");
                //Arr = Line.split("\\t");
                if (Arr[0].equals("LTORG"))
                {
                    int i = 0;
                    while (i<LiteralSize.size())
                    {
                        LiteralAddress.add(Address);
                        Address=Address+LiteralSize.get(i);
                        StartAddress.add(Address);
                        LiteralSize.remove(0);
                        i++;
                    }
                }
                else if(Arr[0].equals("BASE"))
                {
                    BaseSymbol = Arr[1];
                }
                else if(Arr[0].equals("RSUB"))
                { 
                }
                else if (Arr[1].equals("START"))
                {
                    String tempy="";
                    if (Arr[0].length()<6)
                    {
                        tempy = Arr[0];
                        while(tempy.length()!=6)
                        {
                            tempy = tempy+"X";
                        }
                    }
                    Address = Integer.parseInt(Arr[2],16);
                    SymbolName.add(tempy);
                    SymbolAddress.add(Address);
                    SymbolAddress.get(0);
                    StartAddress.add (Address);
                    StartAddress.add (Address);
                }
                else if (IsInstruc(Arr[0]))
                {
                    if ( Arr[1].charAt(0)== '=')
                    {
                        LiteralName.add(Arr[1]);
                        String [] Arr2 = Arr[1].split("'"); 
                        if (Arr2[0].charAt(1)=='X')
                        {
                            LiteralSize.add((Arr2[1].length()+1)/2);
                        }
                        else if(Arr2[0].charAt(1)=='C')
                        {
                            LiteralSize.add(Arr2[1].length());
                        }
                    }
                    if(CalcInstruction(Arr[0]) == 4)
                    {
                        Modification.add("M." + String.format("%06x",Address+1) + ".05");
                    }
                    else if ( (CalcInstruction(Arr[0])==3) && Arr[1].matches("[0-9]+"))
                    {
                        Modification.add("M." + String.format("%06x",Address+1) + ".03");
                    }        
                    Address = Address + CalcInstruction(Arr[0]) ;
                    StartAddress.add(Address);
                }
                else if (IsInstruc(Arr[1]))
                {
                    SymbolName.add(Arr[0]);
                    SymbolAddress.add(Address);
                    if ( Arr[2].charAt(0)== '=')
                    {
                        LiteralName.add(Arr[2]);
                        String [] Arr2 = Arr[2].split("'");
                        if (Arr2[0].charAt(1)=='X')
                        {
                            LiteralSize.add((Arr2[1].length()+1)/2);
                        }
                        else if(Arr2[0].charAt(1)=='C')
                        {
                            LiteralSize.add(Arr2[1].length());
                        }
                    }
                    if(CalcInstruction(Arr[1]) == 4)
                    {
                        Modification.add("M." + String.format("%06x",Address+1) + ".05");
                    }
                    else if ( (CalcInstruction(Arr[1])==3) && Arr[1].matches("[0-9]+"))
                    {
                        Modification.add("M." + String.format("%06x",Address+1) + "03");
                    }
                    Address = Address + CalcInstruction(Arr[1]) ;
                    StartAddress.add(Address);
                }
                else if(Arr[1].equals("EQU"))
                {
                    String[] Arr3 = Arr[2].split(" ");
                    int length = Arr3.length;
                    if(Arr[2].equals("*"))
                    {
                        SymbolAddress.add(Address);
                    }
                    else
                    {
                        if( length == 1)
                        {
                            int n ;
                            if(SymbolName.contains(Arr[2]))
                            {
                                n=SymbolName.indexOf(Arr[2]);
                                SymbolAddress.add(SymbolAddress.get(n));
                            }
                            else
                            {
                                SymbolAddress.add(Integer.parseInt(Arr[2]));
                            }
                        }
                        else
                        {
                            int result;
                            int n;
                            if(SymbolName.contains(Arr3[0]))
                               {
                                   n=SymbolName.indexOf(Arr3[0]);
                                   result=SymbolAddress.get(n);
                               }
                            else
                               {
                                   result=Integer.parseInt(Arr3[0]);
                               }
                            for(int i =1; i<Arr3.length; i=i+2)
                            {
                                if(SymbolName.contains(Arr3[i+1]))
                                {
                                    n=SymbolName.indexOf(Arr3[i+1]);
                                    if(Arr3[i].equals("+"))
                                        result+=SymbolAddress.get(n);
                                    else
                                        result-=SymbolAddress.get(n);
                                }
                                else
                                {
                                    if(Arr3[i].equals("+"))
                                        result+=Integer.parseInt(Arr3[i+1]);
                                    else
                                        result-=Integer.parseInt(Arr3[i+1]);
                                }
                            }
                            SymbolAddress.add(result);
                        }
                    }
                    SymbolName.add(Arr[0]);    
                }
                
                else if (Arr[1].equals("BYTE"))
                {
                    SymbolAddress.add(Address);
                    SymbolName.add(Arr[0]);
                    String [] Arr1 = Arr[2].split("'");
                    switch (Arr1[0]) {
                        case "C":
                            Address = Address + Arr1[1].length();
                            break;
                        case "X":
                            Address = Address + ((Arr1[1].length()+1)/2);
                            break;
                        default:
                            Address = Address + 1;  //////////////////////////////////////////////////
                            break;
                    }
                    StartAddress.add(Address);
                }
                else if (Arr[1].equals("WORD"))
                {
                    SymbolAddress.add(Address);
                    SymbolName.add(Arr[0]);
                    Address = Address + 3;
                    StartAddress.add(Address); 
                }
                else if (Arr[1].equals("RESW"))
                {
                    SymbolAddress.add(Address);
                    SymbolName.add(Arr[0]);
                    Address = Address + Integer.parseInt(Arr[2])*3;
                    StartAddress.add(Address);  
                }
                else if (Arr[1].equals("RESB"))
                {   
                    SymbolAddress.add(Address);
                    SymbolName.add(Arr[0]);
                    Address = Address + Integer.parseInt(Arr[2]); 
                    StartAddress.add(Address);
                }
                else if (Arr[0].equals("END"))
                {
                    if(LiteralSize.size()>0)
                    { 
                        int i = 0;
                        while (i<LiteralSize.size())
                        {
                            LiteralAddress.add(Address);
                            Address=Address+LiteralSize.get(i);
                            StartAddress.add(Address);
                            i++;
                        }
                        for (i=0;i<LiteralSize.size()+1;i++)
                        {
                            Writer.write(LiteralName.get(i)+"\t"+Integer.toHexString(LiteralAddress.get(i))+"\n");   
                        }
                    }
                }
            }
        }
        Writer.close();  
        
        
        FileWriter Typer = new FileWriter(new File("FIrstPass.txt"));
        while(j <SymbolName.size()&&j<=SymbolAddress.size())
        {
            Typer.write( SymbolName.get(j) + "\t" + Integer.toHexString(SymbolAddress.get(j)) + "\n" );
            j++;
        }
        Typer.close();
        
        
        File BaseFile = new File ("FirstPass.txt");
        try(Scanner BaseScanner = new Scanner (BaseFile))
        {
            while (BaseScanner.hasNext())
            {
                String BaseLine = BaseScanner.nextLine();
                String[] BaseArr = BaseLine.split("\t");
                if (BaseSymbol.equals(BaseArr[0]))
                {
                    BaseAddress = BaseArr[1];
                    break;
                }   
            }   
            BaseScanner.close();
        }
        
        
        FileWriter HTE_Writer = new FileWriter(new File("HTE.txt"));
        try (Scanner scanner = new Scanner (file))
        {
            File LitFile = new File("LiteralTable.txt");
            File SymFile = new File("FirstPass.txt");
            while (scanner.hasNext())
            {
                String Line = scanner.nextLine();
                String [] Arr = Line.split("\\t");///////////////////////
                
                if (Arr[0].equals("LTORG"))
                {
                }
                else if(Arr[0].equals("BASE"))
                {
                    
                }
                else if(Arr[0].equals("RSUB"))
                {
                    
                }
                else if (Arr[1].equals("START"))
                {
                    instcount++;
                    HTE_Writer.append("H." + SymbolName.get(0) + "." + String.format("%06x",StartAddress.get(0)) + "." + String.format("%06x",(Address - StartAddress.get(0))));
                }
                else if (IsInstruc(Arr[0]))
                {
                    TCount++;
                    Scanner LiteralScanner = new Scanner (LitFile);
                    Scanner SymbolScanner = new Scanner (SymFile);
                    if(CalcInstruction(Arr[0]) == 1)
                    {
                        OCode.add(CheckOP(Arr[0]));
                    }
                    else if (CalcInstruction(Arr[0])==2)
                    {
                        String[] Registers = Arr[1].split(",");
                        OCode.add(CheckOP(Arr[0])+CalcReg(Registers[0])+CalcReg(Registers[1]));
                    }
                    else if (CalcInstruction(Arr[0])==4)
                    {
                        String t1 ;
                        if(Arr[1].charAt(0) == '@')
                        {
                            t1 = Arr[1].replace("@", "");
                            Arr[1]=t1;
                            ni = "2";
                        }
                        else if (Arr[1].charAt(0)=='#')
                        {
                            t1=Arr[1].replace("#", "");
                            Arr[1]=t1;
                            ni = "1";
                        }
                        else
                        {
                            ni = "3";
                        }
                        while (LiteralScanner.hasNext())
                        {
                            String LiteralLine = LiteralScanner.nextLine();
                            String [] Arry = LiteralLine.split("\\t");
                            if (Arry[0].equals(Arr[1]))
                            {
                                LitSymAddress = Arry[1];
                            }   
                        }
                         while (SymbolScanner.hasNext())
                        {
                            String SymbolLine = SymbolScanner.nextLine();
                            String [] Arry = SymbolLine.split("\\t");
                            if (Arry[0].equals(Arr[1]))
                            {
                                LitSymAddress = Arry[1];
                            }   
                        }
                        if("".equals(LitSymAddress))
                        {
                            LitSymAddress = Arr[1];
                        }
                        OCode.add(String.format("%02x", Integer.parseInt(CheckOP(Arr[0]),16) + Integer.parseInt(ni,16)) + "1" + String.format("%05x", Integer.parseInt(LitSymAddress,16)));
                        LitSymAddress = "";
                    }
                    
                    else if(CalcInstruction(Arr[0])==3)
                    {
                        String[] Checkx = Arr[1].split(",");
                        if (Checkx.length>1)
                        {
                            xbpetemp = Checkx[0];
                            while (LiteralScanner.hasNext())
                            {
                                String LiteralLine = LiteralScanner.nextLine();
                                String [] Arry = LiteralLine.split("\\t");
                                if (Arry[0].equals(xbpetemp))
                                {
                                    LitSymAddress = Arry[1];
                                }   
                            }
                             while (SymbolScanner.hasNext())
                            {
                                String SymbolLine = SymbolScanner.nextLine();
                                String [] Arry = SymbolLine.split("\\t");
                                if (Arry[0].equals(xbpetemp))
                                {
                                    LitSymAddress = Arry[1];
                                }   
                            }
                            if("".equals(LitSymAddress))
                            {
                                LitSymAddress = xbpetemp;
                            }
                            
                            
                            if (((Integer.parseInt(LitSymAddress,16) - (StartAddress.get(instcount + 1))) < 4095) ) //Pc Relative
                            {
                                xbpe = "a";
                                displacement = (Integer.parseInt(LitSymAddress, 16) - StartAddress.get(instcount + 1));
                            }
                            else    //Base Relative
                            {
                                xbpe = "c";
                                displacement = (Integer.parseInt(LitSymAddress, 16) - Integer.parseInt(BaseAddress,16) );

                            }
                            
                            OCode.add(String.format("%02x", Integer.parseInt(CheckOP(Arr[0]),16) + 3) + xbpe + String.format("%03x", displacement & 0xfff));
                        }
                        else
                        {
                            xbpetemp = Checkx[0];
                            if(xbpetemp.charAt(0)=='@')
                            {
                                ni = "2";
                                String t2 = xbpetemp.replace("@", "");
                                xbpetemp = t2;
                            }
                            else if(xbpetemp.charAt(0)=='#')
                            {
                                ni = "1";
                                String t2 = xbpetemp.replace("#", "");
                                xbpetemp = t2;
                            }
                            else
                            {
                                ni = "3";
                            }
                            
                            while (LiteralScanner.hasNext())
                            {
                                String LiteralLine = LiteralScanner.nextLine();
                                String [] Arry = LiteralLine.split("\\t");
                                if (Arry[0].equals(xbpetemp))
                                {
                                    LitSymAddress = Arry[1];
                                }   
                            }
                            while (SymbolScanner.hasNext())
                            {
                                String SymbolLine = SymbolScanner.nextLine();
                                String [] Arry = SymbolLine.split("\\t");
                                if (Arry[0].equals(xbpetemp))
                                {
                                    LitSymAddress = Arry[1];
                                }   
                            }
                            if("".equals(LitSymAddress))
                            {
                                    LitSymAddress = xbpetemp;   
                            } 
                            if (((Integer.parseInt(LitSymAddress,16) - (StartAddress.get(instcount + 1))) < 4095) && !LitSymAddress.equals(xbpetemp)) //Pc Relative
                            {
                                xbpe = "2";
                                displacement = (Integer.parseInt(LitSymAddress, 16) - StartAddress.get(instcount + 1));
                            }
                            else if (!LitSymAddress.equals(xbpetemp))    //Base Relative
                            {
                                xbpe = "8";
                                displacement = (Integer.parseInt(LitSymAddress, 16) - Integer.parseInt(BaseAddress,16) );
                            }
                            else
                            {
                                xbpe = "0";
                                displacement = Integer.parseInt(LitSymAddress);
                            }
                            OCode.add(String.format("%02x", Integer.parseInt(CheckOP(Arr[0]),16) + Integer.parseInt(ni,16)) + xbpe + String.format("%03x", displacement & 0xfff));
                            LitSymAddress = "";
                        }
                        LiteralScanner.close();
                        SymbolScanner.close(); 
                    }
                    instcount++;
                }    
                else if (IsInstruc(Arr[1]))
                {
                    TCount++;
                    Scanner LiteralScanner2 = new Scanner (LitFile);
                    Scanner SymbolScanner2 = new Scanner (SymFile);
                    if(CalcInstruction(Arr[1]) == 1)
                    {
                        OCode.add(CheckOP(Arr[1]));
                    }
                    else if (CalcInstruction(Arr[1])==2)
                    {
                        String[] Registers = Arr[2].split(",");
                        OCode.add(CheckOP(Arr[1])+CalcReg(Registers[0])+CalcReg(Registers[1]));
                    }
                    else if (CalcInstruction(Arr[1])==4)
                    {
                        if(Arr[2].charAt(0) == '@')
                        {
                            String t2 = Arr[2].replace("@", "");
                            Arr[2]=t2;
                            ni = "2";
                        }
                        else if (Arr[2].charAt(0)=='#')
                        {
                            String t2 = Arr[2].replace("#", "");
                            Arr[2]=t2;
                            ni = "1";
                        }
                        else
                        {
                            ni = "3";
                        }
                        while (LiteralScanner2.hasNext())
                        {
                            String LiteralLine2 = LiteralScanner2.nextLine();
                            String [] Arry = new String [4];
                            Arry = LiteralLine2.split("\\t");
                            if (Arry[0].equals(Arr[2]))
                            {
                                LitSymAddress = Arry[1];
                            }   
                        }
                         while (SymbolScanner2.hasNext())
                        {
                            String SymbolLine2 = SymbolScanner2.nextLine();
                            String [] Arry = new String [4];
                            Arry = SymbolLine2.split("\\t");
                            if (Arry[0].equals(Arr[2]))
                            {
                                LitSymAddress = Arry[1];
                            }   
                        }
                        if("".equals(LitSymAddress))
                        {
                            LitSymAddress = Arr[2];
                        }
                        OCode.add(String.format("%02x", Integer.parseInt(CheckOP(Arr[1])) + Integer.parseInt(ni,16)) + "1" + String.format("%05x", Integer.parseInt(LitSymAddress,16)));
                        LitSymAddress = "";
                    }
                    else if(CalcInstruction(Arr[1])==3)
                    {
                        String[] Checkx = Arr[2].split(",");
                        if(Checkx.length>1)
                        {
                            xbpetemp = Checkx[0];
                            while (LiteralScanner2.hasNext())
                            {
                                String LiteralLine2 = LiteralScanner2.nextLine();
                                String [] Arry = LiteralLine2.split("\\t");
                                if (Arry[0].equals(xbpetemp))
                                {
                                    LitSymAddress = Arry[1];
                                }   
                            }
                            while (SymbolScanner2.hasNext())
                            {
                                String SymbolLine2 = SymbolScanner2.nextLine();
                                String [] Arry = SymbolLine2.split("\\t");
                                if (Arry[0].equals(xbpetemp))
                                {
                                    LitSymAddress = Arry[1];
                                }   
                            }
                            if("".equals(LitSymAddress))
                            {
                                LitSymAddress = xbpetemp;
                            }
                            if (((Integer.parseInt(LitSymAddress,16) - (StartAddress.get(instcount + 1))) < 4095) ) //Pc Relative
                            {
                                xbpe = "a";
                                displacement = (Integer.parseInt(LitSymAddress, 16) - StartAddress.get(instcount + 1));
                            }
                            else    //Base Relative
                            {
                                xbpe = "c";
                                displacement = (Integer.parseInt(LitSymAddress, 16) - Integer.parseInt(BaseAddress,16) );
                            }
                            OCode.add(String.format("%02x", Integer.parseInt(CheckOP(Arr[1]),16) + 3) + xbpe + String.format("%03x", displacement & 0xfff));
                        }
                        else
                        {
                            xbpetemp = Checkx[0];
                            if(xbpetemp.charAt(0)=='@')
                            {
                                String t2 = xbpetemp.replace("@", "");
                                xbpetemp=t2;
                                ni = "2";
                            }
                            else if(xbpetemp.charAt(0)=='#')
                            {
                                String t2 = xbpetemp.replace("#", "");
                                xbpetemp=t2;
                                ni = "1";
                            }
                            else
                            {
                                ni = "3";
                            }
                            while (LiteralScanner2.hasNext())
                            {
                                String LiteralLine2 = LiteralScanner2.nextLine();
                                String [] Arry = LiteralLine2.split("\\t");
                                if (Arry[0].equals(xbpetemp))
                                {
                                    LitSymAddress = Arry[1];
                                }   
                            }
                            while (SymbolScanner2.hasNext())
                            {
                                String SymbolLine2 = SymbolScanner2.nextLine();
                                String [] Arry = SymbolLine2.split("\\t");
                                if (Arry[0].equals(xbpetemp))
                                {
                                    LitSymAddress = Arry[1];
                                }   
                            }
                            if("".equals(LitSymAddress))
                            {
                                LitSymAddress = xbpetemp;
                            }
                            if (((Integer.parseInt(LitSymAddress,16) - (StartAddress.get(instcount + 1))) < 4095) && LitSymAddress!=xbpetemp ) //Pc Relative
                            {
                                xbpe = "2";
                                displacement = (Integer.parseInt(LitSymAddress, 16) - StartAddress.get(instcount + 1));
                            }
                            else if (!LitSymAddress.equals(xbpetemp))   //Base Relative
                            {
                                xbpe = "8";
                                displacement = (Integer.parseInt(LitSymAddress, 16) - Integer.parseInt(BaseAddress,16) );
                            }
                            else
                            {
                                xbpe = "0";
                                displacement = Integer.parseInt(LitSymAddress, 16); 
                            }
                            
                            OCode.add(String.format("%02x", Integer.parseInt(CheckOP(Arr[1]),16) + Integer.parseInt(ni,16)) + xbpe + String.format("%03x", displacement & 0xfff));
                           }
                    }
                    LiteralScanner2.close();
                    SymbolScanner2.close();
                    instcount++;
                }
                else if (Arr[1].equals("BYTE"))
                {
                    String t2="";
                    String[] ByteCheck = Arr[2].split("'");
                    switch (ByteCheck[0]) {
                        case "X":
                            OCode.add(ByteCheck[1]);
                            break;
                        case "C":
                            char[] Tempo = ByteCheck[1].toCharArray();
                            for(int i=0;i<Arr[1].length()-1;i++)
                            {
                                t2=t2+String.format("%02X", (int)(Tempo[i]));;
                            }   OCode.add(t2);
                            break;
                        default:
                            OCode.add(Integer.toHexString(Integer.parseInt(ByteCheck[0])));
                            break;
                    }
                    TCount++;   
                }
                else if(Arr[1].equals("WORD"))
                {
                    OCode.add(Integer.toHexString(Integer.parseInt(Arr[2])));
                    TCount++;
                } 
                if ((TCount==10)||Arr[1].equals("RESW")||Arr[1].equals("RESB")||Arr[0].equals("END"))
                {
                    if(OCode.size()>0)
                    {
                        HTE_Writer.write("\nT."+String.format("%06x",(StartAddress.get(instcount)-StartAddress.get(checkpoint))));
                    }
                    while((OCode.size()!=0))
                    {
                        HTE_Writer.write("."+OCode.get(0));
                        OCode.remove(0);
                    }
                    checkpoint  = instcount;
                    TCount = 0;
                }
                if(Arr[0].equals("END"))
                {
                    while (!Modification.isEmpty())
                    {
                        HTE_Writer.write("\n"+Modification.get(0));
                        Modification.remove(0);
                    }
                    HTE_Writer.write("\nE."+String.format("%06x",StartAddress.get(0)));    
                }
            }
            HTE_Writer.close();
        }
    }
    /***********************functions**********************/
    public static String CheckOP (String Instruc)
    {
        String temp= Instruc;
        if(temp.charAt(0)== '+')
        {
            temp = temp.replace("+", "");
        }
        int i=0;
        boolean flag = false;
        while(flag == false&& (i <= 59 ))
        {
            if (temp.equals(op_TAB[i]))
                flag = true;
            else
                i++;
        }
        return opCode [i];
    }
    public static int CalcInstruction( String Instruc )
    {
        if (Instruc.charAt(0)== '+')
            return 4;
        for (int i = 0;i<6;i++)
        {
            if (Instruc.equals(op_TAB1[i]))
                return 1;
        }
        for (int i=0;i<10;i++)
        {
            if(Instruc.equals(op_TAB2[i]))
                return 2;
        }
        for (int i=0;i<59;i++)
        {
            if(Instruc.equals(op_TAB[i]))
                return 3;
        } 
        return 0;
    }
    public static boolean IsInstruc (String Instruc )
    { 
        for (String i : op_TAB)
       {
            if (Instruc.equals(i))
                return true;
       }
            if (Instruc.contains("+"))
                return true;
        return false;
    }
    public static String CalcReg (String Reg)
    {
        switch (Reg) {
            case "A":
                return "0";
            case "X":
                return "1";
            case "L":
                return "2";
            case "B":
                return "3";
            case "S":
                return "4";
            case "T":
                return "5";
            case "F":
                return "6";
            case "PC":
                return "8";
            case "SW":
                return "9";
        }
        return "0";
    }    
}
