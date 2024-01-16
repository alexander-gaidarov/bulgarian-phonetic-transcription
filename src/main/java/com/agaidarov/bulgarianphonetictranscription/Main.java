/**
 * 1/15/2024
 * 
 * @author - Alexander Gaidarov
 * 
 * This class serves as a demo for the methods in the PhoneticConverter class
 *
 */

package com.agaidarov.bulgarianphonetictranscription;


public class Main {

	public static void main(String[] args) {

		//links: if true, the "arc" in affricates is included. Some transcriptions don't include it, as it doesn't always display well
		PhoneticConverter converter1 = new PhoneticConverter(true, false);
		
		
		/* Transcribes the Bulgarian word for "cherry" into the International Phonetic Alphabet (IPA)
		 * 
		 * cyrillic: the Bulgarian word to be transcribed
		 * stressedIndex: the index of the stressed vowel in cyrillic
		 */
		String cherry = converter1.toPhonetic("череша", 3);
		System.out.println(cherry);
		
		
		/* Some Bulgarian words have 2 stresses, like "prettiest"
		 * 
		 * secondStress: the index of the second stressed vowel
		 * primary: if true, the second stress is transcribed as 'ˈ'; if false, the stress is secondary and is transcribed as 'ˌ'
		 */
		String prettiest = converter1.toPhonetic("най-красива", 1, 8, true);
		System.out.println(prettiest);
		
		//"light blue" is an example of a Bulgarian word with a secondary stress
		String lightBlue = converter1.toPhonetic("светлосин", 7, 2, false);
		System.out.println(lightBlue);
		
		
		//searchWebsite: if true, PhoneticConverter is allowed to search the website "https://slovored.com/search/accent/" for the stresses of words
		//This makes transcription much more accurate, but slower
		PhoneticConverter converter2 = new PhoneticConverter(false, true);
		
		
		/* If the user doesn't know the stress of a given word, they may use this method
		 * 
		 * If searchWebsite is true, the PhoneticConverter attempts to find the stress from the website, 
		 * in which case toPhonetic would return an array of length 1
		 * 
		 * Otherwise, this method returns an array of all possible transcriptions of cyrillic, depending on where the stress may lie
		 */
		String[] apartment = converter2.toPhonetic("апартамент");
		for (String transcription : apartment)
		{
			System.out.println(transcription);
		}
		
		/* The 1-argument version of the toPhonetic method can also transcribe multiple words at a time, always returning an array of length 1
		 * 
		 * If searchWebsite is false or toPhonetic fails to find stresses from website, the transcription might be slightly inaccurate
		 * If toPhonetic returns weird symbols, check if cyrillic contains unusual characters
		 */
		String[] purpose = converter2.toPhonetic("Тази програма прави фонетична транскрипция.");
		for (String transcription : purpose)
		{
			System.out.println(transcription);
		}
		
		//When transcribing a passage, toPhonetic doesn't stress clitics and performs sandhi (assimilation across words)
		String[] fromCity = converter2.toPhonetic("от град");
		System.out.println(fromCity[0]);
		
		//If the user doesn't want this to happen, they may set clitics to an empty array
		converter2.setClitics(new String[0]);
		fromCity = converter2.toPhonetic("от град");
		System.out.println(fromCity[0]);
		
		
		//This method returns the IPA equivalent of a cyrillic letter
		//stressed: most vowels have a different pronounciation depending on if they are stressed or not
		String d = converter2.convertLetter('д', true);
		System.out.println(d);
		
		
		//This method splits up a word into syllables
		String[] syllables = converter2.getSyllables("хладилник");		//refrigerator
		String dashed = "";
		for (String syllable : syllables)
		{
			dashed += "-" + syllable;
		}
		System.out.println(dashed.substring(1));
		
		
		//This method parses the website and returns the stressed version of "optimistic"
		String stressed = converter2.getStressed("оптимистичен");
		System.out.println(stressed);
	}

}
