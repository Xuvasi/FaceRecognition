package com.licence.eigenface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Creaza FaceBundle-uri cu imaginile din lista si incearca sa le potriveasca cu
 * imaginea data
 */
public class EigenFaceCreator {

	private File root_dir;
	private int FACES_NUMBER = 2;
	private FaceBundle[] b = null;
	/**
	 * Pragul nostru de potrivire a imagini. Tot ce se afla sub acest
	 * numar e considerat ca nefiind gasit in nici un spatiu al fetei.
	 */
	public static double THRESHOLD = 3.0;

	/**
	 * Distanta minima observata pentru imaginea data in spatiul fetei.
	 * 
	 */
	public double DISTANCE = Double.MAX_VALUE;

	/**
	 * Acest lucru determina daca trebuie activata cache-ul din spatiul fetei.
	 * Orice peste 0 inseamna da. Orice altceva inseamna nu.
	 */
	public int USE_CACHE = -1;

	public EigenFaceCreator() {

	}
	
	public void setFACES_NUMBER(int fACES_NUMBER) {
		FACES_NUMBER = fACES_NUMBER;
		Log.i("Numarul de fete", Integer.toString(fACES_NUMBER));
		
	}





	/**
	 * Potrivire cu imaginea data.
	 * 
	 * @return Identificatorul de imagine in spatiul fetei. Daca imaginea nu a
	 *         fost gasita (conform pragului), se returneaza nul
	 */
	public String checkAgainst(Bitmap bitmap) throws FileNotFoundException,
			IOException {

		String id = null;
		if (b != null) {
			double small = Double.MAX_VALUE;
			double[] img = readImage(bitmap);

			if (img != null) {

				for (int i = 0; i < b.length; i++) {
					b[i].submitFace(img);
					if (small > b[i].distance()) {
						small = b[i].distance();
						id = b[i].getID();
					}
				}
				DISTANCE = small;
				// if (small < THRESHOLD)
				// id = b[idx].getID();
			Log.i("Distanta", Double.toString(DISTANCE));
			}
		}
		return id;
	}

	/**
	 * Construim spatiul fetei din directorul dat. Trebuie sa existe acolo
	 * cel putin 2 imagini si fiecare imagine trebuie sa aiba aceleasi
	 * dimensiuni Pachetul cu spatiul fetelor este, de asemenea, stocat in acel
	 * director pentru marirea vitezei unei viitoare initializari.
	 * 
	 * @param n
	 *            Directorul in care se gasesc imaginile de instruire.
	 * @throws FileNotFoundException
	 *             Directorul n nu exista.
	 * @throws IOException
	 *             Probleme legate de citirea imaginii din directorul dat sau
	 *             salvarea in fisierul cache,.
	 * @throws IllegalArgumentException
	 *             Argumentele date sunt gresite.
	 * @throws ClassNotFoundException
	 *             Obiectele cache sunt expirate sau nu sunt versiunea
	 *             obiectului, spatiul fetelor.
	 * 
	 */
	public void readFaceBundles(String n) throws FileNotFoundException,
			IOException, IllegalArgumentException, ClassNotFoundException {

		root_dir = new File(n);

		File[] files = root_dir.listFiles();
		Vector filenames = new Vector();

		String[] set = new String[FACES_NUMBER];

		int i = 0;

		// Sorteaza lista cu numele fisierelor.
		for (i = 0; i < files.length; i++) {
			filenames.addElement(files[i].getName());
		}
		Collections.sort((List) filenames);

		b = new FaceBundle[(files.length / FACES_NUMBER) + 1];

		// Citim fiecare set de imagini.
		for (i = 0; i < b.length; i++) {
			for (int j = 0; j < FACES_NUMBER; j++) {
				if (filenames.size() > j + FACES_NUMBER * i) {
					set[j] = (String) filenames.get(j + FACES_NUMBER * i);
					 System.out.println(" - "+set[j]);
				}
			}
			b[i] = submitSet(root_dir.getAbsolutePath() + "/", set);
			System.out.println("Am citit tot");
		}
	}

	/**
	 * Trimite un set de imagini in director si construieste un obiect al spatiului-fetei.
	 * 
	 * @param dir
	 *            Directorul cu imagini redimensionate
	 * @param files
	 *            Sir cu numele fisierelor(ie: "image01.jpg").
	 * @throws FileNotFoundException
	 *             Directorul/fisierele nu exista.
	 * @throws IOException
	 *             Probleme legate de citirea imaginii din directorul dat sau
	 *             salvarea in fisierul cache .
	 * @throws IllegalArgumentException
	 *             Argumentele date sunt invalide.
	 * @throws ClassNotFoundException
	 *             Obiectele cache sunt expirate sau nu sunt versiunea
	 *             obiectului, spatiul fetelor.
	 * 
	 * 
	 */
	private FaceBundle submitSet(String dir, String[] files)
			throws FileNotFoundException, IOException,
			IllegalArgumentException, ClassNotFoundException {

		if (files.length != FACES_NUMBER)
			throw new IllegalArgumentException("Can only accept a set of "
					+ FACES_NUMBER + " files.");

		FaceBundle bundle = null;
		int i = 0;
		String name = "cache";
		// Presupunem ca toate numele sunt sortate.

		for (i = 0; i < files.length; i++) {
			name = name + files[i].substring(0, files[i].indexOf('.')); // Construim
																		// numele
																		// cache-ului
			Log.i("Nume cache", name);
		}
		// Verificam daca FaceBundle a fost slavat.

		File f = new File(dir + System.currentTimeMillis() + ".cache");
		if (f.exists() && (USE_CACHE > 0)) /* s-a salvat */
			bundle = readBundle(f);
		else {

			bundle = computeBundle(dir, files);
			if (USE_CACHE > 0)
				saveBundle(f, bundle);
		}

		return bundle;
	}

	/**
	 * Salveaza obiectul spatiul-fetei in fisierul f.
	 * 
	 * @param f
	 *            Fisierul in care salveaza.
	 * @param bundle
	 *            Obiectul spatiul-fetei.
	 * @throws FileNotFoundException
	 *             f e invalid.
	 * @throws IOException
	 *             Probleme de citire a datelor.
	 * 
	 */
	private void saveBundle(File f, FaceBundle bundle)
			throws FileNotFoundException, IOException {

		f.createNewFile();
		FileOutputStream out = new FileOutputStream(f.getAbsolutePath());
		ObjectOutputStream fos = new ObjectOutputStream(out);
		fos.writeObject(bundle);
		fos.close();
		Log.i("saved bundle ... ", f.getAbsolutePath());

	}

	/**
	 * Citeste obiectul salvat din fisier.
	 * 
	 * @param f
	 *            Fisierul din care citeste.
	 * @throws ClassNotFoundException
	 *             Obiectele cache sunt expirate sau nu sunt versiunea
	 *             obiectului, spatiul fetelor.
	 * @throws FileNotFoundException
	 *             f e invalid.
	 * @throws IOException
	 *             Probleme de citire a datelor.
	 */
	private FaceBundle readBundle(File f) throws FileNotFoundException,
			IOException, ClassNotFoundException {

		FileInputStream in = new FileInputStream(f);
		ObjectInputStream fo = new ObjectInputStream(in);
		FaceBundle bundle = (FaceBundle) fo.readObject();
		fo.close();
		System.out.println("read cached bundle..");

		return bundle;
	}

	/**
	 * Construieste spatiul-fetei din directorul dat.
	 * 
	 * @param dir
	 *            Directorul din care citeste.
	 * @param id
	 *            Numele fisierelor din care citeste.
	 * @throws FileNotFoundException
	 *             Directorul, dir, nu exista. Sau id[] nu exista
	 * @throws IOException
	 *             Probleme de citire a datelor din directorul dat
	 * @throws IllegalArgumentException
	 *             Imaginile au formatul gresit sau dimenisiunile gresite
	 */
	private FaceBundle computeBundle(String dir, String[] id)
			throws IllegalArgumentException, FileNotFoundException, IOException {

		xxxFile[] files = new xxxFile[FACES_NUMBER];
		xxxFile file = null;
		String temp = null;
		int width = 0;
		int height = 0;
		int i = 0;

		for (i = 0; i < files.length; i++) {
			temp = id[i].toLowerCase();
			temp = temp.substring(temp.lastIndexOf('.') + 1, temp.length());
			if (temp.equals("jpg") || temp.equals("jpeg"))
				file = new JPGFile(dir + id[i]);
			else if (temp.equals("ppm") || temp.equals("pnm"))
				file = new PPMFile(dir + id[i]);
			if (file == null)
				throw new IllegalArgumentException(id[i]
						+ " is not an image file!");

			files[i] = file;

			if (i == 0) {
				width = files[i].getWidth();
				height = files[i].getHeight();
			}
			if ((width != files[i].getWidth())
					|| (height != files[i].getHeight()))
				throw new IllegalArgumentException(
						"All image files must have the same width and height!");
		}

		// Construim o matrice big double[][] - MxN^2
		double[][] face_v = new double[FACES_NUMBER][width * height];
		System.out.println("Generating bundle of ("+face_v.length+" x "+face_v[0].length+"), h:"+height+" w:"+width);

		for (i = 0; i < files.length; i++) {
			// System.arraycopy(files[i].getDouble(),0,face_v[i],0,face_v[i].length);
			face_v[i] = files[i].getDouble();
		}
		Log.i("latime,inaltime, numar fete",Integer.toString(width)+" "+Integer.toString(height)+" "+Integer.toString(FACES_NUMBER));

		// Calculeaza!

		return EigenFaceComputation.submit(face_v, width, height, id,
				FACES_NUMBER, true);

	}

	public double[] readImage(Bitmap bitmap) {

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		int[] rgbdata = new int[width * height];

		bitmap.getPixels(rgbdata, 0, width, 0, 0, width, height);

		double[] doubles = new double[rgbdata.length];

		for (int i = 0; i < doubles.length; i++) {
			doubles[i] = (double) (rgbdata[i]);
		}

		return doubles;

	}

}