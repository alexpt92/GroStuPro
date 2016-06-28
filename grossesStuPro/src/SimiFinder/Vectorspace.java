package SimiFinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

class Vectorspace {
	ArrayList<String> Vokabular = new ArrayList<String>();
	Vector<Double> DocVector;
	Vector<Double> AnfrageVector;
	ArrayList<Double> Simis = new ArrayList<Double>();
	Map<String, Vector<Double>> AllDocVec; //Key ist der stream
	
	public double Simiwert(Vector<Double> Q, Vector<Double> D){
		double Simi=0;
		for (int i=0; i<Q.size();i++){
			double q = (double) Q.get(i);
			double d = (double) D.get(i);
			Simi=Simi+(q*d);
		}
		//System.out.println(Simi);
		return Simi;
	}
	
	void Anfrage(int counter, Map<String, Term> globalMap, Map<String, Map<String, LinkedTerm>> localMap, String stream ){
		int N=counter;
		 //Anzahl der verschiedenen Begriffe aller Dokumente
		Vector<Double> AnfrageVector = new Vector<Double>();
		//int maxT = maxHaeufigkeit(localMap, stream);
		System.out.println(Vokabular.size());
		SetAllDocVector(N, localMap);
/*		for (int i=0;i<Vokabular.size();i++){
			//System.out.println(Vokabular.get(i));
			int nk= vorkommenGesamt(Vokabular.get(i),localMap); //Anzahl der Dokumente die den Begriff enthalten
			//System.out.println(nk);
			int tf=vorkommenImDokument(Vokabular.get(i),stream, localMap);
			if (tf==0)AnfrageVector.add(0.0); else
			AnfrageVector.add(wqk(tf,maxT, N, nk));
			System.out.println(i);
		}	//	System.out.println(AnfrageVector.firstElement());

			for (Iterator<Entry<String, Map<String, LinkedTerm>>> it = localMap
					.entrySet().iterator(); it.hasNext();){
				Map.Entry<String, Map<String, LinkedTerm>> entry = it.next();
				double W = Simiwert(AnfrageVector, getVector(N,globalMap,localMap, entry.getKey()));
				Simis.add(W);
				if (W>0.0){
				System.out.println(W);	
				}*/
			//}
		//	for (int i=1;i<=10;i++){
			//	System.out.println(Simis.get(i));
			//}
		
	}
	
	
	
	public int maxHaeufigkeit(Map<String, Map<String, LinkedTerm>> localMap, String stream){
		int max= 0;
		for (int i=0;i<Vokabular.size();i++){
		int tf=vorkommenImDokument(Vokabular.get(i), stream, localMap);
		if(tf>max)max=tf;
		}		return max;
	}
	
	
	public double wqk(int tfqk, int tfqi, int N, int nk){
		//if (tfqk==0) return 0;
		//else{
		double erg=(0.5+((0.5*tfqk)/(tfqi)))*Math.log(N/nk);
			return erg;
		}
	//}
	void getAllDocVector(){
		//liest aus AllDocVec.txt Map ein
}
	
	
	void SetAllDocVector(int counter, Map<String, Map<String, LinkedTerm>> localMap){
		System.out.println("in SADV");
		int N =counter; //Anzahl der Dokumente
		Vector<Double> DocVector= new Vector<Double>(); //Dokumentenvektor
		//vocabular(localMap, globalMap); //Anzahl der verschiedenen Begriffe aller Dokumente
		for (Iterator<Entry<String, Map<String, LinkedTerm>>> it = localMap
				.entrySet().iterator(); it.hasNext();){
			Map.Entry<String, Map<String, LinkedTerm>> entry = it.next();
			DocVector.clear();
			for (int i=1;i<Vokabular.size();i++){
				System.out.println(i);
				int nk= vorkommenGesamt(Vokabular.get(i),localMap); //Anzahl der Dokumente die den Begriff enthalten
				int tf=vorkommenImDokument(Vokabular.get(i),entry.getKey(), localMap); //Häufigkeit des Begriffs im Dokument
				if (tf==0){DocVector.add(0.0);}else{
				DocVector.add(matching(localMap, N, nk , tf ));}
			
		}AllDocVec.put(entry.getKey(), DocVector);
}
		try {
			PrintStream ps;
			ps = new PrintStream(new File("DocVectoren.txt"));

		for (Entry<String, Vector<Double>> entry : AllDocVec.entrySet()) {
			ps.println(entry.getKey() + " " + entry.getValue());		
			ps.close();
			} }catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("File printed.");
	}
	
	public Vector<Double> getVector(int counter, Map<String, Term> globalMap, Map<String, Map<String, LinkedTerm>> localMap, String stream){
		int N =counter; //Anzahl der Dokumente
		Vector<Double> DocVector= new Vector<Double>(); //Dokumentenvektor
		//Anzahl der verschiedenen Begriffe aller Dokumente
		for (int i=1;i<Vokabular.size();i++){
			int nk= vorkommenGesamt(Vokabular.get(i),localMap); //Anzahl der Dokumente die den Begriff enthalten
			int tf=vorkommenImDokument(Vokabular.get(i),stream, localMap); //Häufigkeit des Begriffs im Dokument
			DocVector.add(matching(localMap, N, nk , tf ));
			
		}
		return DocVector;
	}

	
	public double matching(Map<String, Map<String, LinkedTerm>> localMap, int counter, int nk, int tf){//berechnet die Relevanz (wdk) eines Dokumentes für k
		double Nenner=0;		//Summe von i=1 bis t (tf*log(N/ni))^2
		
		for (int i = 1; i<Vokabular.size(); i++){	
			Nenner=Nenner+Math.pow(tf*Math.log(counter/vorkommenGesamt(Vokabular.get(i), localMap)),2);
		}
		double wdk = (tf*Math.log(counter/nk))/(Math.sqrt(Nenner));
		return wdk;
}
	
	
	public int vorkommenGesamt(String k, Map<String, Map<String, LinkedTerm>> localMap){ 
		//gibt die Anzahl der Dokumente wieder, die k enthalten
		int v= 0;	
		for (Iterator<Entry<String, Map<String, LinkedTerm>>> it = localMap
				.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Map<String, LinkedTerm>> entry = it.next();
			for (Iterator<Map.Entry<String, LinkedTerm>> tmpIterator = entry.getValue().entrySet().iterator(); tmpIterator.hasNext();){
				Entry<String, LinkedTerm> entry2 = tmpIterator.next();
	if (entry2.getValue().globalTerm.term.equals(k)){

			v++;
				}}}
		
		return v;

	}
	
	
	public int vorkommenImDokument(String k, String stream, Map<String, Map<String, LinkedTerm>> localMap){ 
		int h=0;	//Häufigkeit vom Begriff k im Dokument
		for (Iterator<Entry<String, Map<String, LinkedTerm>>> it = localMap
				.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Map<String, LinkedTerm>> entry = it.next();
			if (h>0)break;
			if (entry.getKey().equals(stream)){
				System.out.println("yes");
				for (Iterator<Map.Entry<String, LinkedTerm>> tmpIterator = entry.getValue().entrySet().iterator(); tmpIterator.hasNext();){
					Entry<String, LinkedTerm> entry2 = tmpIterator.next();
					System.out.println("for2");
				if (entry2.getValue().globalTerm.term.equals(k)){
					System.out.println("if");
					//return
					 h= entry2.getValue().localTerm.counter.getVal();
					 break;
				}
				}
			}
		}
			
			return h;
	}
	
	void vocabular(Map<String, Map<String, LinkedTerm>> localMap, Map<String, Term> globalMap){		
		try{
			
			String line;
			BufferedReader br = new BufferedReader(new FileReader("vokabular.txt"));
			
			while ((line = br.readLine()) != null){
				if (!line.equals(" "))Vokabular.add(line);
			}
			br.close();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
			
	
}
}
	
class Vocabular{
	void Setvocabular(Map<String, Map<String, LinkedTerm>> localMap, Map<String, Term> globalMap){		
//gibt die Anzahl der verschiedenen Begriffe aller Dokumente zurück & erstellt das ListArray Vokabular.
		ArrayList<String> Vokabular = new ArrayList<String>();
		for (Iterator<Entry<String, Map<String, LinkedTerm>>> it = localMap
				.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Map<String, LinkedTerm>> entry = it.next();
			for (Iterator<Map.Entry<String, LinkedTerm>> it2 = entry.getValue()
					.entrySet().iterator(); it2.hasNext();) {
				Entry<String, LinkedTerm> entry2 = it2.next();
		Vokabular.add(entry2.getValue().globalTerm.term);
		}}
		try {
			PrintStream ps = new PrintStream(new File("vokabular.txt"));
			for (int i=0; i<Vokabular.size();i++ ) {
				ps.println(Vokabular.get(i));
			}
			System.out.println("File printed.");
			ps.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}	