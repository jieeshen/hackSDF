/**
* Created with IntelliJ IDEA.
* User: JShen
* Date: 10/24/12
* Time: 11:12 AM
* This program reads a multi column text file contain the CAS in one column, it will get the mol object from JCHEM
* MolImporter and write a sdf with the other columns to be the properties
*/
import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.struc.Molecule;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;


public class cas2Sdf {

    public static void usage() throws Exception{
        System.out.println("Usage:");
        System.out.println("\tjava cas2sdf -i INPUTFILE -o OUTPUTSDFFILE [-cas CASCOLUMNNAME -s SEPARATOR -q]");
        System.out.println("\tparameters:");
        System.out.println("\t\t-i\tinput text file with multiple columns");
        System.out.println("\t\t-o\toutput sdf file");
        System.out.println("\t\t-cas\tthe column title of CAS in the input file [Default: \"CAS\"]");
        System.out.println("\t\t-s\tthe separator char in the input file [Default: \"\t\"]");
        System.out.println("\t\t-q\tusing Mysql to query Actor[Default is off]\n\n");
    }


    public static ArrayList<String []> readList(String fileName, String separator) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        String line = null;
        ArrayList<String[]> outList = new ArrayList<String[]>();
        int i=0;
        while((line=br.readLine())!=null){
            String[] data=line.split(separator);
            outList.add(data);
            i++;
        }
        return outList;
    }

    public static String casFormat(String casNo) throws Exception{
        String cas=new String();
        int cl=casNo.length();
        String cas3=casNo.substring(cl-1);
        String cas2=casNo.substring(cl-3,cl-1);
        String cas1=casNo.substring(0,cl-3);
        cas=cas1+"-"+cas2+"-"+cas3;
        return cas;
    }

    public static void main(String args[]) throws Exception{
        if (args.length<0 | args.length>9){
            usage();
            System.exit(1);
        }
        int i=0;
        String inputFile="C:\\JShen\\Research\\EDKB\\ER\\DECA\\121211\\uterotrophic_data_EDKB.txt";
        String outSdFile="C:\\JShen\\Research\\EDKB\\ER\\DECA\\121211\\uterotrophic_data_EDKB.sdf";
        String casTitle="CAS_NUMBER";
        System.out.println("xxxxxxxxxxxxx");
        String separator="\t";
        Boolean queryActor=Boolean.FALSE;
        ArrayList<String> passThroughOpts=new ArrayList<String>();
        String driver="com.mysql.jdbc.Driver";
        String url="jdbc:mysql://localhost:3306/actor_2010q4a";
        String user="root";
        String password="jsh1234";

        while (i<args.length){
            if (args[i].equals("-h")){
                i++;
                usage();
                System.exit(1);
            }else if (args[i].equals("-i")){
                i++;
                inputFile=args[i];
            }else if (args[i].equals("-o")){
                i++;
                outSdFile=args[i];
            }else if (args[i].equals("-cas")){
                i++;
                casTitle=args[i];
            }else if (args[i].equals("-s")){
                i++;
                separator=args[i];
            }else if (args[i].equals("-q")){
                queryActor=Boolean.TRUE;
            }else{
                passThroughOpts.add(args[i]);
            }
            i++;
        }

        ArrayList<String[]> dataMatrix=readList(inputFile,separator);
        String[] titles=dataMatrix.get(0);
        int casLoc=0;
        for(i=0;i<titles.length;i++){
            if (titles[i].equals(casTitle)){
                casLoc=i;
                break;
            }
        }
        int noInp=dataMatrix.size()-1;
        int noCas=0;
        int noUniqCas=0;
        int noIllCas=0;
        int noTransCas=0;
        MolExporter me=new MolExporter(outSdFile,"sdf");
        Molecule mol=new Molecule();
        String casStr=new String();
        HashSet<Integer> casSet = new HashSet<Integer>();
        String cas=new String();


        if (queryActor){

        }


        for(i=1;i<=noInp;i++){
            String[] dataString=dataMatrix.get(i);
            cas=dataMatrix.get(i)[casLoc];
            if (cas.matches("\\d+\\-\\d\\d\\-\\d")){
                noCas++;
                casStr=cas;
                cas=cas.replace("-","");
            }else if (!cas.matches("\\d+")){
                noIllCas++;
                continue;
            }else if (Integer.valueOf(cas)>999){
                noCas++;
                casStr=casFormat(cas);
            }else{
                noIllCas++;
                continue;
            }
            if (casSet.contains(Integer.valueOf(cas))){
                continue;
            }else {
                casSet.add(Integer.valueOf(cas));
                noUniqCas++;
                try{
                    mol=MolImporter.importMol(casStr);
                }catch(Exception e){
                    if (queryActor){
                        // set connection to mysql db.

                        Class.forName(driver);
                        Connection conn = DriverManager.getConnection(url, user, password);
                        Statement st= conn.createStatement();
                        // got the molecule information
                        // got the molecule information
                        String sqlquery_Mol="select a.smiles, b.casrn from compound a, generic_chemical b where a.compound_id=b.compound_id AND b.casrn="+casStr+";";
                        ResultSet rsM=st.executeQuery(sqlquery_Mol);
                        if (rsM.next()){

                        } else {
                            System.out.println("can not parse: " + casStr);
                            continue;
                        }
                        try{
                            mol=MolImporter.importMol(rsM.getString("smiles").replace("\n",""));
                        }catch(Exception e1){
                            System.out.println("can not parse: " + rsM.getString("smiles"));
                            continue;
                        }

                    }
                }

                for (int j=0;j<titles.length;j++){
                    try{
                        mol.setProperty(titles[j],dataMatrix.get(i)[j]);
                    }catch (Exception e){
                        mol.setProperty(titles[j],"");
                    }
                }
                noTransCas++;
                me.write(mol);
            }

        }
        me.close();
        System.out.println();
        System.out.println();
        System.out.printf("The total input entry is %d;\n", noInp);
        System.out.printf("The total input cas is %d;\n",noCas);
        System.out.printf("The total illegal cas is %d;\n",noIllCas);
        System.out.printf("The total unique cas is %d;\n",noUniqCas);
        System.out.printf("%d CAS have been transformed.\n",noTransCas);
    }
}
