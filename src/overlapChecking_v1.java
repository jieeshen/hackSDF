import chemaxon.formats.MolExporter;
import chemaxon.formats.MolImporter;
import chemaxon.reaction.Standardizer;
import chemaxon.struc.Molecule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * By: Jie Shen @ NCTR, FDA
 * Date: 6/22/12
 * Time: 10:36 PM
 * <p/>
 * Description: This is used to compare TOX21, ERDB12, and mols send to Jenifer.
 */
public class overlapChecking_v1 {
    final static String SDF1="c:\\Temp_data3\\TOX21S_v2a_8193_22Mar2012.sdf";
    final static String SDF2="c:\\Temp_data3\\erdb_12_symyx.sdf";
    final static String TEXT3="c:\\Temp_data3\\ER_invitro_4355_all.txt";
    final static String OUTFILE="c:\\Temp_data3\\tox21s_erdb_jenifer.sdf";

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
        ArrayList<String> inchiList2 = new ArrayList<String>();
        ArrayList<Molecule> molList2 = new ArrayList<Molecule>();
        ArrayList<String> inchiList3 = new ArrayList<String>();
        ArrayList<Molecule> molList3 = new ArrayList<Molecule>();
        ArrayList<Molecule> allMols = new ArrayList<Molecule>();
        while((mol=mi1.read())!=null){
            Standardizer standardizer = new Standardizer(new File("C:\\Temp_data3\\standardlize_config.xml"));
            standardizer.setFinalClean();
            standardizer.standardize(mol);
            String molinchikey= MolExporter.exportToFormat(mol, "inchikey").split("=")[1];
            inchiList1.add(molinchikey);
            molList1.add(mol);
        }
        while((mol=mi2.read())!=null){
            Standardizer standardizer = new Standardizer(new File("C:\\Temp_data3\\standardlize_config.xml"));
            standardizer.setFinalClean();
            standardizer.standardize(mol);
            String molinchikey= MolExporter.exportToFormat(mol, "inchikey").split("=")[1];
            inchiList2.add(molinchikey);
            molList2.add(mol);

        }

        String[][] mol3array = readList(TEXT3);
        for (int i=1;i<mol3array.length;i++){
            String[] data = mol3array[i];
            mol = MolImporter.importMol(data[5]);
            Standardizer standardizer = new Standardizer(new File("C:\\Temp_data3\\standardlize_config.xml"));
            standardizer.setFinalClean();
            standardizer.standardize(mol);
            String molinchikey= MolExporter.exportToFormat(mol, "inchikey").split("=")[1];
            inchiList3.add(molinchikey);
            molList3.add(mol);
        }
        System.out.printf("Set1:%d\tSet2:%d\tSet3:%d\n", inchiList1.size(),inchiList2.size(),inchiList3.size());
        mi1.close();
        mi2.close();

        for (int i=0;i<inchiList1.size();i++){
            Molecule mol1=molList1.get(i);
            mol1.setProperty("tox21s","1");
            if (inchiList2.contains(inchiList1.get(i))){
                Molecule mol2;
                int index=inchiList2.indexOf(inchiList1.get(i));
                mol2=molList2.get(index);
                for(String key:mol2.properties().getKeys()){
                    mol1.setProperty(key,mol2.getProperty(key));
                }
                mol1.setProperty("nctr_erdb","1");
                allMols.add(mol1);
                inchiList2.remove(index);
                molList2.remove(index);
            }else if (inchiList3.contains(inchiList1.get(i))){
                Molecule mol3;
                int index=inchiList3.indexOf(inchiList1.get(i));
                mol3=molList3.get(index);
                for(String key:mol3.properties().getKeys()){
                    mol1.setProperty(key,mol3.getProperty(key));
                }
                mol1.setProperty("Jenifer","1");
                allMols.add(mol1);
                inchiList3.remove(index);
                molList3.remove(index);
            }else{
                allMols.add(mol1);
            }
        }
        for (int i=0;i<inchiList2.size();i++){
            Molecule mol2=molList2.get(i);
            mol2.setProperty("nctr_erdb","1");
            if (inchiList3.contains(inchiList2.get(i))){
                Molecule mol3;
                int index=inchiList3.indexOf(inchiList2.get(i));
                mol3=molList3.get(index);
                for(String key:mol3.properties().getKeys()){
                    mol2.setProperty(key,mol3.getProperty(key));
                }
                mol2.setProperty("Jenifer","1");
                allMols.add(mol2);
                inchiList3.remove(index);
                molList3.remove(index);
            }else {
                allMols.add(mol2);
            }
        }
        for (int i=0;i<inchiList3.size();i++){
            Molecule mol3=molList3.get(i);
            mol3.setProperty("nctr_erdb","1");
            allMols.add(mol3);
        }
        System.out.println(allMols.size());
        MolExporter me = new MolExporter(OUTFILE,"sdf");
        for (Molecule m:allMols){
            me.write(m);
        }



    }
}
