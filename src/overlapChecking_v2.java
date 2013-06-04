/**
 * Created with IntelliJ IDEA.
 * Author: Jie Shen @ NCTR, FDA
 * Date: 7/19/12
 * Time: 5:16 AM
 * Description:
 */

import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.marvin.io.MPropHandler;
import chemaxon.reaction.Standardizer;
import chemaxon.struc.Molecule;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 *
 * Created with IntelliJ IDEA.
 * By: Jie Shen @ NCTR, FDA
 * Date: 7/18/12
 * This version only counts the overlaps of TOX21, ERAD12.
 *
 * Description: This is used to compare TOX21, ERDB12, and mols send to Jenifer.
 *
 * Created with IntelliJ IDEA.
 * By: Jie Shen @ NCTR, FDA
 * Date: 6/22/12
 * Time: 10:36 PM
 * <p/>
 * Description: This is used to compare TOX21, ERDB12, and mols send to Jenifer.
 */
public class overlapChecking_v2 {
    final static String SDF1="/home/hhong/edkb/ER/TOX21OLP/TOX21S_v2a_8193_22Mar2012.sdf";
    final static String SDF2="/home/hhong/edkb/ER/TOX21OLP/erad_120718_compact.sdf";
    final static String OUTFILE="/home/hhong/edkb/ER/TOX21OLP/Tox21withERAD.txt";

    public static String[][] readList(String fileName) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        String line = null;
        String[][] outList = new String[4356][100];
        int i=0;
        while((line=br.readLine())!=null){
            String[] data=line.split("\t");
            outList[i]=data;
            i++;
        }
        return outList;
    }


    public static void main(String args[]) throws Exception{
        MolImporter mi1 = new MolImporter(SDF1);
        MolImporter mi2 = new MolImporter(SDF2);
        Molecule mol;
        ArrayList<String> inchiList1 = new ArrayList<String>();
        ArrayList<Molecule> molList1 = new ArrayList<Molecule>();
        String[] titleArray1=new String[1000];
        ArrayList<String> inchiList2 = new ArrayList<String>();
        ArrayList<Molecule> molList2 = new ArrayList<Molecule>();
        String[] titleArray2=new String[1000];

        PrintWriter pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream(OUTFILE)),true);


        while((mol=mi1.read())!=null){
            titleArray1=mol.properties().getKeys();
            Standardizer standardizer = new Standardizer(new File("/home/hhong/edkb/ER/TOX21OLP/standardlize_config.xml"));
            standardizer.setFinalClean();
            standardizer.standardize(mol);
            String molinchikey= MolExporter.exportToFormat(mol, "inchikey").split("=")[1];
            inchiList1.add(molinchikey);
            molList1.add(mol);
        }
        while((mol=mi2.read())!=null){
            titleArray2=mol.properties().getKeys();
            Standardizer standardizer = new Standardizer(new File("/home/hhong/edkb/ER/TOX21OLP/standardlize_config.xml"));
            standardizer.setFinalClean();
            standardizer.standardize(mol);
            String molinchikey= MolExporter.exportToFormat(mol, "inchikey").split("=")[1];
            inchiList2.add(molinchikey);
            molList2.add(mol);

        }

        System.out.printf("Set1:%d\tSet2:%d\n", inchiList1.size(),inchiList2.size());
        mi1.close();
        mi2.close();

        //print title
        for (String t:titleArray1){
            pw.printf("%s\t", t);
        }
        //String[] subTitle2= Arrays.copyOfRange(titleArray2,0,titleArray2.length-2);
        String[] subTitle2= Arrays.copyOfRange(titleArray2,0,9);
        for (String t:subTitle2){
            pw.printf("%s\t",t);
        }
        //pw.printf("%s\n",titleArray2[titleArray2.length-1]);
        pw.printf("%s\n",titleArray2[10]);

        int countOLP=0;
        for (int i=0;i<inchiList1.size();i++){
            Molecule mol1=molList1.get(i);

            for (String prop:titleArray1){
                pw.printf("%s\t",MPropHandler.convertToString(mol1.properties(),prop));
            }

            if (inchiList2.contains(inchiList1.get(i))){
                Molecule mol2;
                countOLP++;
                int index=inchiList2.indexOf(inchiList1.get(i));
                mol2=molList2.get(index);
                for(String prop:subTitle2){
                    pw.printf("%s\t", MPropHandler.convertToString(mol2.properties(), prop));
                }
                pw.printf("%s",MPropHandler.convertToString(mol2.properties(),titleArray2[10]));
            }
            pw.printf("\n");
        }
        pw.close();

        System.out.printf("Overlap number is %d\n",countOLP);


    }
}


