package com.licence.eigenface;

import android.util.Log;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * Calculeaza un "spatiu al fetei" folosit pentru recunoasterea fetei
 * 
 * Idee acestui algoritm provine de la Matthew A. La randul sau pornind de la
 * lucrarea lui, Alex P. Pentland, intitulata "Recunoastere faciala folosind
 * eigenfaces" (<a href="http://www.cs.ucsb.edu/~mturk/Papers/mturk-CVPR91.pdf">
 * http://www.cs.ucsb.edu/~mturk/Papers/mturk-CVPR91.pdf</a>) <br>
 * Modul de lucru al algoritmului consta in "tratarea problemei de recunoastere
 * faciala ca o problema 2-D, profitand de faptul ca fetele in mod normal sunt
 * in pozitie verticala si pot fi descrise de un set mic de caracteristici de
 * vizualizare 2-D. Imaginile fetei sunt proiectate intr-un spatiu caracteristic
 * ("spatiul fetei") care codeaza cel mai bine variatia dintre fetele cunoscute.
 * Spatiul fetei este definit de "eigenfaces" care sunt vectorii proprii ai
 * setului de fete; acestea nu corespund neaparat cu caracteristici izolate cum
 * sunt ochii, urechile, si nasul." (paragraf din abstractul lucrarii) <br>
 * <br>
 * Aceasta lucrare este publica fara licenta si fara garantie <b>NOTA</b>: Acest
 * pachet foloseste Jama pentru calculul valorilor proprii si vectorilor proprii
 * 
 * Care o puteti gasi la: <a
 * href="http://math.nist.gov/javanumerics/jama/">http:
 * //math.nist.gov/javanumerics/jama/</a><br>
 * <br>
 */
public class EigenFaceComputation {

	/**
	 * Calculeaza "spatiul fetei folosit pentru recunoasterea faciala.
	 * Recunoasterea se efectueza de fapt intr-un obiect "FaceBundle", dar
	 * pregatirea unui astfel de obiect necesita o multime de calcule. Pasii
	 * sunt urmatorii:
	 * <ol>
	 * <li>Calculeaza o fata medie
	 * <li>Construirea unei matrici de covarianta
	 * <li>Calculeaza valori proprii si vectori proprii
	 * <li>Selecteaz doar {@link MAGIC_NR} cea mai mare valoare proprie
	 * (corespunzatoare vectorului propriu)
	 * <li>Calculeaza fata folosind vectorii nostri proprii
	 * <li>Calculeaza spatiul eigenfaces pentru imaginile noastre.
	 * </ol>
	 * De aici restul algoritmului (detectia fetei) trebuie sa fie apelata in
	 * {@link FaceBundle}.
	 * 
	 * @param face_v
	 *            matrice 2-D. Are 16 randuri. Fiecare coloana trebuie sa aiba
	 *            aceeasi lungime. Ficare rand contine imaginea intr-o
	 *            reprezentare vectoriala.
	 * @param width
	 *            Latimea imaginii se afla in randul vectorului face_v.
	 * @param height
	 *            Inaltimea imaginii se se afla in randul vectorului face_v.
	 * @param id
	 *            Sir reprezentand fiecare din cele 2 imagini.
	 * 
	 * @return Un "FaceBundle" folosit pentru recunoastere.
	 * 
	 */
	public static FaceBundle submit(double[][] face_v, int width, int height,
			String[] id, int facesNumber, boolean debug) {

		int length = width * height;
		int nrfaces = face_v.length;
		int i, j, col, rows, pix, image;
		double temp = 0.0;
		double[][] faces = new double[nrfaces][length];

		Log.i("Pachet FaceBundle",
				Integer.toString(length) + " " + Integer.toString(nrfaces)
				+ " " + Double.toString(facesNumber));
		// ImageFileViewer simple = new ImageFileViewer();
		// simple.setImage(face_v[0],width,height);

		double[] avgF = new double[length];

		/*
		 * Calculeaza fata medie a tuturor fetelor. 1xN^2
		 */
		for (pix = 0; pix < length; pix++) {
			temp = 0;
			for (image = 0; image < nrfaces; image++) {
				temp += face_v[image][pix];
			}
			avgF[pix] = temp / nrfaces;
		}
		
		// simple.setImage(avgF, width,height);

		/*
		 * Calculeaza diferenta.
		 */

		for (image = 0; image < nrfaces; image++) {

			for (pix = 0; pix < length; pix++) {
				face_v[image][pix] = face_v[image][pix] - avgF[pix];
				
			}
		}
		
		/* Copiaza vectorul fata (MxN^2). Il vom folosi mai tarziu */

		// for (image = 0; image < nrfaces; image++)
		// System.arraycopy(face_v[image],0,faces[image],0,length);
		System.arraycopy(face_v, 0, faces, 0, face_v.length);

		// simple.setImage(face_v[0],width,height);

		/*
		 * Calculculam matricea de covarianta. MxM
		 */

		Jama.Matrix faceM = new Matrix(face_v, nrfaces, length);
		Jama.Matrix faceM_transpose = faceM.transpose();

		/*
		 * Matricea de covarianta e MxM (nrfaces x nrfaces)
		 */
		Matrix covarM = faceM.times(faceM_transpose);

		double[][] z = covarM.getArray();
		System.out.println("Covariance matrix is " + z.length + " x "
				+ z[0].length);

		/*
		 * Calculam valorile proprii si vectorii proprii.Avand dimensiunea MxM.
		 */
		EigenvalueDecomposition E = covarM.eig();

		double[] eigValue = diag(E.getD().getArray());
		double[][] eigVector = E.getV().getArray();

		/*
		 * Avem nevoie doar de cele mai mari valori asociate valorilor proprii.
		 * Astfel le sortam (si pastram un index pentru ele)
		 */
		int[] index = new int[nrfaces];
		double[][] tempVector = new double[nrfaces][nrfaces]; /*
		 * vector propriu
		 * temporar *
		 */

		for (i = 0; i < nrfaces; i++)
			/* Enumereza toate intrarile */
			index[i] = i;

		doubleQuickSort(eigValue, index, 0, nrfaces - 1);

		// Pune indexul invers
		int[] tempV = new int[nrfaces];
		for (j = 0; j < nrfaces; j++)
			tempV[nrfaces - 1 - j] = index[j];
		/*
		 * for (int j = 0; j< nrfaces; j++) {
		 * System.out.println(temp[j]+" (was: "
		 * +index[j]+") "+eigValue[temp[j]]); }
		 */
		index = tempV;

		/*
		 * Pune valorile proprii sortate in coloanele corespunzatoare.
		 */
		for (col = nrfaces - 1; col >= 0; col--) {
			for (rows = 0; rows < nrfaces; rows++) {
				tempVector[rows][col] = eigVector[rows][index[col]];
			}
		}
		eigVector = tempVector;
		tempVector = null;
		eigValue = null;
		/*
		 * Inmultim faceM (MxN^2) cu noul vector propriu (MxM), si obtinem
		 * eigenfaces (MxN^2)
		 */
		Matrix eigVectorM = new Matrix(eigVector, nrfaces, nrfaces);
		eigVector = eigVectorM.times(faceM).getArray();

		/* Normalizam matricea vectorilor proprii. */

		for (image = 0; image < nrfaces; image++) {
			temp = max(eigVector[image]); // Maximul
			for (pix = 0; pix < eigVector[0].length; pix++)
				// Normalizare
				eigVector[image][pix] = Math.abs(eigVector[image][pix] / temp);
		}

		/*
		 * Si acum calculam wk - "spatiul fetei"
		 * 
		 * Aici este locul in care folosim vectorul fetelor copiat
		 */

		double[][] wk = new double[nrfaces][facesNumber]; // M randuri, 11
		// coloane

		/*
		 * Calculam wk.
		 */

		for (image = 0; image < nrfaces; image++) {
			for (j = 0; j < facesNumber; j++) {
				temp = 0.0;
				for (pix = 0; pix < length; pix++)
					temp += eigVector[j][pix] * faces[image][pix];
				wk[image][j] = Math.abs(temp);
			}
		}

		FaceBundle b = new FaceBundle(avgF, wk, eigVector, id);

		/*
		 * //This is what you would use to recognize a face ...
		 * 
		 * double[] inputFace = new double[length]; // So we are trying to
		 * recognize the 14th image..
		 * System.arraycopy(faces[14],0,inputFace,0,length);
		 * 
		 * // This is done for virgin images, not ones that we already
		 * subtracted. // for ( pix = 0; pix < inputFace.length; pix++) { //
		 * inputFace[pix] = inputFace[pix] - avgF[pix]; //}
		 */
		/*
		 * double[] input_wk = new double[MAGIC_NR]; for (j = 0; j < MAGIC_NR;
		 * j++) { temp = 0.0; for ( pix=0; pix <length; pix++) temp +=
		 * eigVector[j][pix] * inputFace[pix];
		 * 
		 * input_wk[j] = Math.abs( temp ); }
		 */
		/*
		 * Find the minimun distance from the input_wk as compared to wk
		 */
		/*
		 * int idx = 0; double[] distance = new double[MAGIC_NR]; double[]
		 * minDistance = new double[MAGIC_NR];
		 * 
		 * for (image = 0; image < nrfaces; image++) { for (j = 0; j < MAGIC_NR;
		 * j++) distance[j] = Math.abs(input_wk[j] - wk[image][j]); if (image ==
		 * 0) System.arraycopy(distance,0,minDistance,0,MAGIC_NR); if
		 * (sum(minDistance) > sum(distance)) { idx = image;
		 * System.arraycopy(distance,0,minDistance,0,MAGIC_NR);
		 * 
		 * } }
		 */

		/*
		 * Normalize our minimum distance.
		 */
		/*
		 * divide(minDistance, max(minDistance)+1); double minD =
		 * sum(minDistance);
		 * System.out.println("image is idx "+idx+" distance from face: "+minD);
		 * 
		 * 
		 * //ImageFileViewer simple = new ImageFileViewer(); int[] bb = new
		 * int[length];
		 * 
		 * temp = max(eigVector[0]);
		 * 
		 * for ( i = 0; i < width*height; i++) { bb[i] = (int) (255*(1 -
		 * eigVector[0][i] / temp )); }
		 * 
		 * simple.setImage(bb,width,height);
		 */
		return b;
	}

	/**
	 * Gasim diagonala matricii.
	 * 
	 * @param m
	 *            numarul de linii si coloane trebuie sa fie acelasi
	 * @return diagonla matricii
	 */
	static double[] diag(double[][] m) {

		double[] d = new double[m.length];
		for (int i = 0; i < m.length; i++)
			d[i] = m[i][i];
		return d;
	}

	/**
	 * Impartim fiecare element v cu b. Nu verificam impartirea cu 0.
	 * 
	 * @param v
	 *            vector care contine numere.
	 * @param b
	 *            scalar folosit pentru impartirea fiecarui element din vectorul
	 *            v
	 * 
	 * @return returneaza un vector care are fiecare element impartit la b,
	 *         scalar.
	 * 
	 */
	static void divide(double[] v, double b) {

		for (int i = 0; i < v.length; i++)
			v[i] = v[i] / b;

	}

	/**
	 * Suma vectorului.
	 * 
	 * @param a
	 *            vector cu numere
	 * @return a scalar cu suma fiecarui element din vectorul a
	 */
	static double sum(double[] a) {

		double b = a[0];
		for (int i = 0; i < a.length; i++)
			b += a[i];

		return b;

	}

	/**
	 * Maximul vectorului a.
	 * 
	 * @param a
	 *            vector
	 * 
	 * @return returneaza suma tuturor elementelor din a
	 */
	static double max(double[] a) {
		double b = a[0];
		for (int i = 0; i < a.length; i++)
			if (a[i] > b)
				b = a[i];

		return b;
	}

	/**
	 * Sortare rapida intr-un vector cu un index.
	 * 
	 * @param a
	 *            matricea cu numere.Aceasta va fi modificata si sortata
	 *            crescator
	 * @param index
	 *            indexul numerelor corelat la locatia originala.
	 * @param lo
	 *            indexul de start. De obicei 0.
	 * @param hi
	 *            indexul de stop. De obicei lungimea lui a(a.length())
	 */
	static void doubleQuickSort(double a[], int index[], int lo0, int hi0) {
		int lo = lo0;
		int hi = hi0;
		double mid;

		if (hi0 > lo0) {

			/*
			 * Stabilirea arbitrara a elemetului de partitie ca punct de mijloc
			 * al matricii
			 */
			mid = a[(lo0 + hi0) / 2];
			// bucla in matrice pana cand indicii se incruciseaza
			while (lo <= hi) {
				/*
				 * primul element care este mai mare sau egal cu mijlocul
				 * matricii incepand cu indexul din stanga
				 */
				while ((lo < hi0) && (a[lo] < mid)) {
					++lo;
				}

				/*
				 * primul element care este mai mic sau egal cu mijlocul
				 * matricii incepand de la dreapta
				 */
				while ((hi > lo0) && (a[hi] > mid)) {
					--hi;
				}

				// daca indecsi nu sau intersectat, schimba
				if (lo <= hi) {
					swap(a, index, lo, hi);
					++lo;
					--hi;
				}
			}
			/*
			 * Daca indexul drept nu a ajuns in partea stanga a matricii trebuie
			 * sa sortam partitia stanga.
			 */
			if (lo0 < hi) {
				doubleQuickSort(a, index, lo0, hi);
			}
			/*
			 * Daca indexul stang nu a ajuns in partea dreapta a matricii
			 * trebuie sa sortam partitia dreapta
			 */
			if (lo < hi0) {
				doubleQuickSort(a, index, lo, hi0);
			}
		}
	}

	static private void swap(double a[], int[] index, int i, int j) {
		double T;
		T = a[i];
		a[i] = a[j];
		a[j] = T;
		// Index
		index[i] = i;
		index[j] = j;
	}
}
