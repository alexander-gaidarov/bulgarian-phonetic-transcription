/**
 * 1/15/2024
 * 
 * @author - Alexander Gaidarov
 * 
 * This class contains all the methods needed to perform a phonetic transcription written in the 
 * International Phonetic Alphabet (IPA) from Bulgarian cyrillic.
 * 
 * If the user wishes to use this class independently in their own projects, they need to use the 
 * jsoup library (for parsing websites). In this project, it is listed as a dependency in the pom.xml file.
 * 
 */


package com.agaidarov.bulgarianphonetictranscription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class PhoneticConverter {

	private boolean links;
	private boolean searchWebsite;
	
	//If true, then multiple words are being transcribed at once and must take into account clitics and sandhi
	private boolean passage;
	
	private boolean devoice;	//if true, word-final devoicing of obstruents occurs; if false, then sandhi is overriding that
	private boolean dashed;		//if true, the word being transcribed has a '-' 
	
	//In English loan words that start with an 'у', it is transcribed to 'w'.
	//If the word has one of these prefixes, replace 'y' with 'w'
	private String[] loanWords = {"уи", "уеб", "уейлс", "уест", "уо"};
	
	//Obstruents form 8 minimal pairs, dividing them into voiced and voiceless consonants
	private String[] voiced = {"д", "з", "б", "г", "в", "ж", "дж", "дз"};
	private String[] voiceless = {"т", "с", "п", "к", "ф", "ш", "ч", "ц", "х"};		//'x' doesn't have a counterpart
	
	//Clictics are short words that don't have a stress when pronounced together with other, 
	//longer words (e.g. "спи' му се"). There can be multiple clictics next to each other
	private String[] clitics = {"му", "те", "ти", "ги", "им", "си", "се", "го", "я", "и", 
			"съм", "е", "сме", "сте", "са", "бях", "бе", "ме", "ми", "й", "ни", "ви", "хем",
			"без", "в", "вдън", "во", "връз", "всред", "във", "въз", "не", "я", "че", "ту",
			"до", "за", "зад", "из", "край", "към", "на", "над", "низ", "о", "от", "под", "пред",
			"през", "при", "с", "след", "сред", "със", "у", "чрез", "а", "ако", "ала", "ама", 
			"ами", "да", "дето", "и", "или", "като", "ни", "нито", "но", "па", "пък", "та", "то", "ща"};

	
	/*
	 * links: if true, "links" are included for affricates
	 * Sometimes affricates (ц [t͡s], ч [t͡ʃ], дж [d͡ʒ], дз [d͡z]) are transcribed with a "link" above the two sounds
	 * 
	 * searchWebiste: if true, allows program to search the website slovored.com/search/accent
	 * This makes transcription accurate, but slows down the program
	 */
	public PhoneticConverter(boolean links, boolean searchWebsite) 
	{
		this.links = links;
		this.searchWebsite = searchWebsite;
		
		Arrays.parallelSort(clitics);	//sorts clitics, so that Arrays.binarySearch can be used later
		
		passage = false;
		devoice = true;
		dashed = false;
	}
	
	/* 
	 * cyrillic: Bulgarian word written in the Bulgarian Cyrillic alphabet
	 * stressedIndex: the index of cyrillic that has the stressed vowel (set to -1 if the word doesn't have a stress)
	 * 
	 * Returns the phonetic transcription of the word written in the International Phonetic Alphabet (IPA)
	 */
	public String toPhonetic(String cyrillic, int stressedIndex)
	{
		if (cyrillic.contains(" "))
		{
			System.out.println("WARNING: the argument \"cyrillic\" takes a SINGLE word");
			return toPhonetic(cyrillic)[0];
		}		
		
		if (stressedIndex != -1)	//allows exceptions for short words without a stress (e.g. "в", "с")
		{
			//Check if stressedIndex is actually pointing to a vowel
			//If not, print warning and run the other toPhonetic method and return the first variant
			try {
				char vowel = cyrillic.charAt(stressedIndex);
				boolean check = isVowel(vowel);
				
				if (!check)
				{
					if (!passage)		//Happens every once in a while with stresses gotten from website
						System.out.println("WARNING: " + stressedIndex + " is not an index of a vowel in word " + cyrillic);
					return toPhonetic(cyrillic)[0];
				}
			} catch (IndexOutOfBoundsException e) {
				System.out.println("WARNING: index " + stressedIndex + " is out of bounds for word " + cyrillic);
				return toPhonetic(cyrillic)[0];
			}
		}
		
		//Shouldn't really happen in this method (okay in the toPhonetic with 4 arguments)
		if (cyrillic.contains("-"))		//e.g. "по-добре"
		{
			String[] parts = cyrillic.split("-");
			
			//Either part before or after '-' has a stress, so assuming the other isn't stressed
			if (stressedIndex < parts[0].length())		//part before '-' is stressed
				return toPhonetic(parts[0], stressedIndex) + "-" + toPhonetic(parts[1], -1);
			
			//Part after '-' is stressed
			stressedIndex -= parts[0].length() + 1;
			return toPhonetic(parts[0], -1) + "-" + toPhonetic(parts[1], stressedIndex);
		}
		
		cyrillic = cyrillic.toLowerCase();
		cyrillic = phonotation(cyrillic);		//rewrites the word the way it is pronounced
		
		//For English loan words, replace 'y' with 'w' here
		if (cyrillic.charAt(0) == 'у')
		{
			for (int i = 0; i < loanWords.length; i++)
			{
				if (cyrillic.startsWith(loanWords[i]))		//if they have the same root/prefix
				{
					cyrillic = "w" + cyrillic.substring(1);
					i = loanWords.length;
				}
			}
		}
				
		//Temporarily transcribe "дж" as 'j' (so that the two letters can't be split into separate syllables)
		while (cyrillic.contains("дж"))
		{
			int rareIndex = cyrillic.indexOf("дж");
			cyrillic = cyrillic.substring(0, rareIndex) + "j" + cyrillic.substring(rareIndex + 2);
			
			//Changing length of cyrillic, so need to update stressedIndex
			if (stressedIndex > rareIndex)		//if "дж" was before stressed vowel
				stressedIndex--;
		}
			
		//The "d͡z" sound occurs only at the start of borrowed words. 
		//Otherwise, it is usually the 'д' and 'з' sounds separately, as in "подземен"
		if (cyrillic.startsWith("дз") && links)
		{
			cyrillic = "d͡z" + cyrillic.substring(2);
			stressedIndex++;
		}
		
		int length = 0, insertionIndex = -1;		//used to determine where to put stress mark
		if (stressedIndex != -1)	//if cyrillic has a stress
		{
			//Stress mark (ˈ) is inserted before the syllable that contains the stressed vowel
			//However, no stress mark is needed if cyrillic has only 1 vowel, unless it is dashed ("по", "най")
			
			int vowelCount = 0;
			for (int i = 0; i < cyrillic.length(); i++)
			{
				if (isVowel(cyrillic.charAt(i)))
					vowelCount++;
			}
			
			if (vowelCount > 1 || dashed)
			{
				String[] syllables = getSyllables(cyrillic);
				
				for (String syllable : syllables)
				{
					if (length + syllable.length() >= stressedIndex)
					{
						insertionIndex = length;
						break;
					}
					else
						length += syllable.length();
				}
			}
		}
		
		//Keep track of letter after current letter, because it affects how 'л' and 'н' are transcribed
		char nextLetter, letter;
		boolean nonvelarized;	//'л' --> 'l' before vowels 'и' or 'е', 'ɫ' everywhere else
		boolean curved;		//'н' --> 'ŋ' if preceding 'к' or 'г', 'n' everywhere else
		boolean stressed;	//catch-all for nonvelarized, curved, and stressedIndex == i
		
		//Transcribe cyrillic into IPA
		String transcribed = "";
		for (int i = 0; i < cyrillic.length(); i++)
		{
			if (i == insertionIndex)
				transcribed += "ˈ";		//inserts the stress mark
			
			letter = cyrillic.charAt(i);
			stressed = (i == stressedIndex);
			
			if (letter == 'л' || letter == 'н')
			{
				//Get next letter
				if (i < cyrillic.length() - 1)	//if not last letter
					nextLetter = cyrillic.charAt(i + 1);
				else
					nextLetter = 0;
				
				//Determine how 'л'/'н' is transcribed
				nonvelarized = false;
				curved = false;			
				
				if ((letter == 'л' && nextLetter == 'и') || (letter == 'л' && nextLetter == 'е'))
					nonvelarized = true;
				if ((letter == 'н' && nextLetter == 'к') || (letter == 'н' && nextLetter == 'г'))
					curved = true;
				
				stressed = (nonvelarized || curved);
			}
			
			transcribed += convertLetter(letter, stressed);		//transcribe letter
		}
		
		dashed = false;
		
		return transcribed;
	}
	
	/*
	 * This is a copy of toPhonetic with 2 extra arguments describing the second stress in a Bulgarian word
	 * Use this method for words with 2 stresses (e.g. "прабаба", "по-добре", "светлосин")
	 * 
	 * secondStress: index of the second stress in cyrillic
	 * primary: true if the second stress is primary (transcribed as 'ˈ'); false if it's secondary ('ˌ')
	 */
	public String toPhonetic(String cyrillic, int stressedIndex, int secondStress, boolean primary)
	{
		if (cyrillic.contains(" "))
		{
			System.out.println("WARNING: the argument \"cyrillic\" takes a SINGLE word");
			return toPhonetic(cyrillic)[0];
		}
		
		//Check if both stress indexes are pointing to vowels
		char vowel1 = 0, vowel2;
		try {
			vowel1 = cyrillic.charAt(stressedIndex);
			vowel2 = cyrillic.charAt(secondStress);		//second vowel 
			boolean check1 = isVowel(vowel1), check2 = isVowel(vowel2);
			
			if (!check1 || !check2)		//if one or both indexes aren't pointing to a vowel
			{
				int badIndex = check1? secondStress : stressedIndex;
				System.out.println("WARNING: " + badIndex + " is not an index of a vowel in word " + cyrillic);
				return toPhonetic(cyrillic)[0];
			}
		} catch (IndexOutOfBoundsException e) {
			
			int badIndex = (vowel1 == 0)? stressedIndex : secondStress;
			System.out.println("WARNING: index " + badIndex + " is out of bounds for word " + cyrillic);
			return toPhonetic(cyrillic)[0];
		}
		
		if (cyrillic.contains("-"))		//e.g. "по-добре"
		{
			dashed = true;
			
			//Find which index is larger and which is smaller
			int larger = secondStress, smaller = stressedIndex;
			if (stressedIndex > secondStress)	//if the order is in reverse
			{
				//Switch them around
				larger = stressedIndex;
				smaller = secondStress;
			}
			
			String[] parts = cyrillic.split("-");
			
			//See in which parts of cyrillic the stresses lie and call toPhonetic again accordingly
			if (smaller < parts[0].length() && larger > parts[0].length())	//both parts of the word are stressed
				return toPhonetic(parts[0], smaller) + "-" + toPhonetic(parts[1], larger - parts[0].length() - 1);
			else if (smaller < parts[0].length())		//if both stresses fall before '-'
				return toPhonetic(parts[1], stressedIndex, secondStress, primary) + "-" + toPhonetic(parts[0], -1);
			else		//if both stresses fall after '-'
			{
				stressedIndex -= parts[0].length() + 1;
				secondStress -= parts[0].length() + 1;
				return toPhonetic(parts[0], -1) + "-" + toPhonetic(parts[1], stressedIndex, secondStress, primary);
			}
		}
		
		cyrillic = cyrillic.toLowerCase();
		cyrillic = phonotation(cyrillic);
		
		if (cyrillic.charAt(0) == 'у')
		{
			for (int i = 0; i < loanWords.length; i++)
			{
				if (cyrillic.startsWith(loanWords[i]))
				{
					cyrillic = "w" + cyrillic.substring(1);
					i = loanWords.length;
				}
			}
		}
				
		while (cyrillic.contains("дж"))
		{
			int rareIndex = cyrillic.indexOf("дж");
			cyrillic = cyrillic.substring(0, rareIndex) + "j" + cyrillic.substring(rareIndex + 2);
			
			//Changing length of cyrillic, so need to update indexes of stressed vowels
			if (stressedIndex > rareIndex)
				stressedIndex--;
			if (secondStress > rareIndex)
				secondStress--;
		}
			
		if (cyrillic.startsWith("дз") && links)
		{
			cyrillic = "d͡z" + cyrillic.substring(2);
			stressedIndex++;
			secondStress++;
		}
		
		//Stress mark ('ˈ'/'ˌ') is inserted before the syllable that contains the stressed vowel
		int length = 0, insertionIndex1 = -1, insertionIndex2 = -1;		//used to determine where to put stress marks
		String[] syllables = getSyllables(cyrillic);
		
		for (String syllable : syllables)
		{
			length += syllable.length();

			if (insertionIndex1 == -1 && length > stressedIndex)
				insertionIndex1 = length - syllable.length();
			else if (insertionIndex2 == -1 && length > secondStress)
				insertionIndex2 = length - syllable.length();
		}
		
		char nextLetter, letter;
		boolean nonvelarized;
		boolean curved;
		boolean stressed;
		
		String transcribed = "";
		for (int i = 0; i < cyrillic.length(); i++)
		{
			//Insert stress mark if at insertion index
			if (i == insertionIndex1)
				transcribed += "ˈ";
			else if (i == insertionIndex2)
			{
				if (primary)
					transcribed += "ˈ";
				else
					transcribed += "ˌ";		//secondary stress
			}
			
			letter = cyrillic.charAt(i);
			stressed = (i == stressedIndex) || (i == secondStress);
			
			if (letter == 'л' || letter == 'н')
			{
				if (i < cyrillic.length() - 1)
					nextLetter = cyrillic.charAt(i + 1);
				else
					nextLetter = 0;
				
				nonvelarized = false;
				curved = false;			
				
				if ((letter == 'л' && nextLetter == 'и') || (letter == 'л' && nextLetter == 'е'))
					nonvelarized = true;
				if ((letter == 'н' && nextLetter == 'к') || (letter == 'н' && nextLetter == 'г'))
					curved = true;
				
				stressed = (nonvelarized || curved);
			}
			
			transcribed += convertLetter(letter, stressed);
		}
		
		return transcribed;
	}
	
	
	/*If the user doesn't specify where the stress is, there may be several possible
	  phonetic transcriptions, as most vowels are transcribed differently depending on whether
	  or not they are stressed.
	 
	  This method will first check if the argument cyrillic already contains stress marks.
	  If it doesn't, it will try to search the website "https://slovored.com/accent", which gives 
	  the stresses of words. In both cases, the output array will have length 1 (since the stresses will be known).
	  Otherwise, the output array will have length equal to the number of syllables of the word.
	  
	  cyrillic: Bulgarian word(s) written in the Cyrillic alphabet (can be a longer String 
	  containing many words separated by spaces)
	 */
	public String[] toPhonetic(String cyrillic)
	{
		cyrillic = cyrillic.toLowerCase();
		
		String transcription;
		
		if (cyrillic.contains(" "))		//if input is more than one word
		{			
			passage = true;
			
			//Get the stress of every word all at once by putting the whole string into readWebsite
			char mark = "а̀".charAt(1);
			if (searchWebsite && !cyrillic.contains("" + mark))		//makes sure cyrillic doesn't have stresses already
			{
				//The website cannot handle these symbols
				cyrillic = cyrillic.replace('—', '+');
				cyrillic = cyrillic.replace('„', '+');
				cyrillic = cyrillic.replace('“', '+');
				
				cyrillic = readWebsite(cyrillic.replace(' ', '+'));		//the URL has '+' instead of ' '
			}
			
			String[] words = cyrillic.split(" ");
			
			char[] sonorants = {'м', 'н', 'л', 'р', 'й', 'ь'};		//used for sandhi
			Arrays.sort(sonorants);
			
			transcription = "";
			
			//Sandhi: the pronunciation of a word might change depending on the following word
			String word, nextWord, cluster = "", newCluster;
			int letterIndex;
			char first, last;
			boolean v;
			for (int i = 0; i < words.length; i++)
			{
				word = words[i];
				if (i < words.length - 1)	//if word isn't the last word
					nextWord = words[i+1];
				else
					nextWord = "";
				
				if ((Arrays.binarySearch(clitics, word) >= 0 || Arrays.binarySearch(clitics, nextWord) >= 0) 
						&& nextWord.length() > 0)
				{
					letterIndex = word.length() - 1;
					last = word.charAt(letterIndex);	//last letter of first word
					first = nextWord.charAt(0);		//first letter of second word
					
					//Prepositions ending in a voiced consonant don't get devoiced if next word starts with a vowel
					v = (word.equals("в") || word.equals("във"));	//"в" and "във" are exceptions
					if (getObstruentIndex(voiced, "" + last) >= 0 && isVowel(first) && Arrays.binarySearch(clitics, word) >= 0 && !v)
						devoice = false;
					
					//"във" doesn't get devoiced at the end of the word if the next one starts with 'в'
					if (word.equals("във") && first == 'в')
						devoice = false;
					
					//Next word cannot start with a vowel, sonorant, or 'в'
					if (!isVowel(first) && Arrays.binarySearch(sonorants, first) < 0 && first != 'в')
					{
						//Perform sandhi here by calling assimilate. The cluster will be the consonants at the
						//end of one word and those at the start of the next word
						
						//Add consonants at the end of word to cluster
						while (getObstruentIndex(voiceless, "" + last) >= 0 || getObstruentIndex(voiced, "" + last) >= 0)
						{
							cluster = last + cluster;
							
							letterIndex--;
							if (letterIndex >= 0)
								last = word.charAt(letterIndex);
							else
								last = 0;
						}
						
						letterIndex = 0;
						
						//Add consonants at the start of next word to cluster
						while (cluster.length() > 0 && (getObstruentIndex(voiceless, "" + first) >= 0 || 
								getObstruentIndex(voiced, "" + first) >= 0))	
						{
							cluster += first;
							
							letterIndex++;
							first = nextWord.charAt(letterIndex);
							
							devoice = false;
						}
						
						newCluster = assimilate(cluster);
						
						//letterIndex now becomes the index in cluster where the two words are split by a space (assuming cluster doesn't get longer)
						letterIndex = cluster.length() - letterIndex;
						
						//If 'ч' was voiced to "дж" (couldn't find examples of 'ц' --> "дз") and 'ч' was in first word, then index must be increased
						//to accomodate for extra letter
						if (newCluster.length() > cluster.length() && cluster.indexOf('ч') <= letterIndex)		
							letterIndex++;
						
						//Replace end of word with beginning of the new cluster
						if (!devoice)
							word = word.substring(0, word.length() - letterIndex) + newCluster.substring(0, letterIndex);
						
						cluster = "";
					}
				}

				transcription += toPhonetic(word)[0] + " ";	//appends the first transcription for each word
				//System.out.println(transcription);
				devoice = true;
			}
			
			passage = false;
			
			//Return only 1 transcription "option" (too many possibilities otherwise)
			String[] output = {transcription.trim()};
			return output;
		}
		
		//Search cyrillic for stress mark
		int[] stresses = getStressIndex(cyrillic);
		if (stresses.length > 0)
		{
			if (stresses.length == 1)	//if only 1 stress
			{
				//Remove stress mark and set stressedIndex to index of the vowel right before the mark
				cyrillic = cyrillic.substring(0, stresses[0]) + cyrillic.substring(stresses[0] + 1);
				int stressedIndex = stresses[0] - 1;
				
				if (passage && (Arrays.binarySearch(clitics, cyrillic) >= 0))
					transcription = toPhonetic(cyrillic, -1);	//if cyrillic is a clitic in a passage, it has no stress
				else
					transcription = toPhonetic(cyrillic, stressedIndex);
			}
			else	//cyrillic has 2 stresses
			{
				//Remove stress marks
				cyrillic = cyrillic.substring(0, stresses[0]) + cyrillic.substring(stresses[0] + 1);
				cyrillic = cyrillic.substring(0, stresses[1]) + cyrillic.substring(stresses[1] + 1);
				
				//Adjust stress indexes to point at vowel before each mark
				int stressedIndex = stresses[0] - 1;
				int secondStress = stresses[1] - 1;
				
				transcription = toPhonetic(cyrillic, stressedIndex, secondStress, true);
			}
			
			//Stress is known, so only 1 possible transcription (the correct one)
			String[] output = {transcription};
			return output;
		}
		
		//Find where the vowels are in word
		ArrayList<Integer> vowelIndexes = new ArrayList<>();
		for (int i = 0; i < cyrillic.length(); i++)
		{
			if (isVowel(cyrillic.charAt(i)))
				vowelIndexes.add(i);
		}
		
		//No stress marks found, so search website
		if (searchWebsite && vowelIndexes.size() > 1)
		{
			String stressed = readWebsite(cyrillic);
			
			//If the website has trouble finding a stress, readWebsite will output cyrillic without stresses (its input)
			char mark = "а̀".charAt(1);
			if (stressed.contains("" + mark))		//makes sure the word was stressed
			{
				//Check that stress index is pointing to a vowel
				stresses = getStressIndex(stressed);
				if (vowelIndexes.contains(stresses[0] - 1))
					return toPhonetic(stressed);
			}
		}
		
		//In the cases of one-letter words (e.g. "в", "с") or clitics in a passage, cyrillic has no stress
		if ((passage && (Arrays.binarySearch(clitics, cyrillic) >= 0)) || !devoice || vowelIndexes.size() == 0)
		{
			String[] output = {toPhonetic(cyrillic, -1)};
			return output;
		}
		
		//Make an array containing transcriptions for all possible stress locations
		String[] transcriptions = new String[vowelIndexes.size()];
		for (int i = 0; i < vowelIndexes.size(); i++)
		{
			//Gets the transcription for one possible stress index
			transcriptions[i] = toPhonetic(cyrillic, vowelIndexes.get(i));
			
			if (passage)	//if cyrillic is in a passage, only 1st transcription option is used
				break;
		}
		
		return transcriptions;
	}
	
	
	//Outputs the word(s) with stress marks by parsing the website slovored.com/search/accent
	//unstressed: Bulgarian word(s) without stress marks (written in cyrillic alphabet)
	public String getStressed(String unstressed)
	{
		if (!searchWebsite)
		{
			System.out.println("WARNING: This PhoneticConverter is set to not search websites");
			return unstressed;
		}
		
		//The website cannot handle these symbols
		unstressed = unstressed.replace('—', '+');
		unstressed = unstressed.replace('„', '+');
		unstressed = unstressed.replace('“', '+');
		
		unstressed = unstressed.replace(' ', '+');		//replaces spaces with '+' to put into URL
		
		//Make sure there are no stress marks beforehand
		char mark1 = "а̀".charAt(1), mark2 = "а́".charAt(1);
		if (!(unstressed.contains("" + mark1) || unstressed.contains("" + mark2)))	//if there are no stress marks
			return readWebsite(unstressed);
		
		return unstressed;
	}
		
	
	//This method splits up word (written in Cyrillic) into syllables and returns an array 
	//containing each syllable in order.
	public String[] getSyllables(String word)
	{
		if (word.contains(" "))
		{
			System.out.println("WARNING: cannot get syllables for more than one word at a time");
			return new String[0];
		}
		
		if (word.contains("-"))		//e.g. "по-добре"
		{
			//Get the syllables from each part of the word
			String[] syllables1 = getSyllables(word.substring(0, word.indexOf("-")));
			String[] syllables2 = getSyllables(word.substring(word.indexOf("-") + 1));
			
			//Put all the syllables together in a bigger array
			String[] bigger = new String[syllables1.length + syllables2.length];
			for (int i = 0; i < syllables1.length; i++)
			{
				bigger[i] = syllables1[i];
			}
			for (int i = 0; i < syllables2.length; i++)
			{
				bigger[i + syllables1.length] = syllables2[i];
			}
			
			return bigger;
		}
		
		word = word.toLowerCase();
		
		//This method might be called individually/externally (separately from toPhonetic),
		//so might need to transcribe "дж" to "j" and then reverse it at the end
		word = word.replace("дж", "j");
		
		//Find where the vowels are in word
		ArrayList<Integer> vowelIndexes = new ArrayList<>();
		for (int i = 0; i < word.length(); i++)
		{
			if (isVowel(word.charAt(i)))
				vowelIndexes.add(i);
		}
		
		//If word starts or ends in a cluster of 2 vowels, they must be in the same syllable (e.g. "уе-ди-не-ния")
		if (vowelIndexes.size() >= 2 && vowelIndexes.get(1) == 1)
			vowelIndexes.remove(0);
			
		//Remove last vowel index if word ends in two vowels
		if (isVowel(word.charAt(word.length() - 2)) && isVowel(word.charAt(word.length() - 1)))
			vowelIndexes.remove(vowelIndexes.size() - 1);
		
		String[] syllables = new String[vowelIndexes.size()];
		
		//Consonant clusters (or simply consonant, if any) between vowels (as well as start and end of word)
		String[] clusters = new String[vowelIndexes.size() + 1];
		
		clusters[0] = word.substring(0, vowelIndexes.get(0));	//first "cluster"
		for (int i = 1; i < vowelIndexes.size(); i++)	//middle "clusters"
		{
			clusters[i] = word.substring(vowelIndexes.get(i - 1) + 1, vowelIndexes.get(i));
		}
		
		//Last cluster
		clusters[vowelIndexes.size()] = "";
		if (vowelIndexes.get(vowelIndexes.size() - 1) != word.length() - 1)		//if last letter isn't a vowel
			clusters[vowelIndexes.size()] = word.substring(vowelIndexes.get(vowelIndexes.size() - 1) + 1);
		
		//Piece together syllables
		String syllable;
		int length, splitIndex;		//length of cluster to be split up
		for (int i = 0; i < syllables.length; i++)
		{
			syllable = clusters[i];		//consonants before vowel
			syllable += word.charAt(vowelIndexes.get(i)); 	//appends vowel
			
			length = clusters[i + 1].length();
			if (length > 0 && clusters[i + 1].charAt(length - 1) == 'ь')
				length--;	//ensures that 'ь' is always before 'o' and that "ьо" is considered a vowel
			
			//Split up following cluster
			splitIndex = length / 2;
			if (i == syllables.length - 1)		//if this is the end of the word
				splitIndex = length;
			syllable += clusters[i + 1].substring(0, splitIndex);	//appends consonants after vowel
			clusters[i + 1] = clusters[i + 1].substring(splitIndex);
			
			syllable = syllable.replace("j", "дж");
			
			syllables[i] = syllable;
		}
		
		return syllables;
	}
	
	/* 
	 * Converts a Cyrillic letter into its IPA equivalent
	 * 
	 * If letter is one of the vowels а, о, у, ъ, ю, or я, it has a different transcription depending on if it's stressed or not.
	 * The consonants 'л' and 'н' have two possible transcriptions; use argument "stressed" to determine which to use
	 */
	public String convertLetter(char letter, boolean stressed)
	{
		switch (letter) {
			case 'а':
				if (stressed)
					return "a";
				return "ɐ";
				
			case 'б':
				return "b";
				
			case 'в':
				return "v";
				
			case 'г':
				return "g";
				
			case 'д':		//"дж" and "дз" are already transcribed to "d͡ʒ" and "d͡z"
				return "d";
				
			case 'е':
				return "ɛ";
				
			case 'ж':
				return "ʒ";
				
			case 'з':
				return "z";
				
			case 'и':
				return "i";
				
			case 'й':
				return "j";
				
			case 'к':
				return "k";
				
			case 'л':		//'л' --> "l" (stressed = true) or "ɫ" (stressed = false)
				if (stressed)
					return "l";
				return "ɫ";
				
			case 'м':
				return "m";
				
			case 'н':		//'н' --> "ŋ" (stressed = true) or "n" (stressed = false)
				if (stressed)
					return "ŋ";
				return "n";
				
			case 'о':
				if (stressed)
					return "ɔ";
				return "o";
				
			case 'п':
				return "p";
				
			case 'р':
				return "r";
				
			case 'с':
				return "s";
				
			case 'т':
				return "t";
				
			case 'у':		//'у' is already transcribed to 'w' in English loan words
				if (stressed)
					return "u";
				return "o";
				
			case 'ф':
				return "f";
				
			case 'х':
				return "x";
				
			case 'ц':
				if (links)
					return "t͡s";
				return "ts";
				
			case 'ч':
				if (links)
					return "t͡ʃ";
				return "tʃ";
				
			case 'ш':
				return "ʃ";
				
			case 'щ':
				return "ʃt";
				
			case 'ъ':
				if (stressed)
					return "ɤ";
				return "ɐ";
				
			case 'ь':
				return "j";
				
			case 'ю':
				if (stressed)
					return "ju";
				return "jo";
				
			case 'я':
				if (stressed)
					return "ja";
				return "jɐ";
			
			case 'j':		//Previously transcribed "дж" to 'j'
				if (links)
					return "d͡ʒ";
				return "dʒ";
		}
		
		//If letter was already transcribed
		return "" + letter;		//makes letter a String
	}
	
	
	//If user wants to update the array of English loan words/prefixes, they may do so with this method
	public void setLoanWords(String[] newWords) {
		loanWords = newWords;
	}
	
	/*
	 * If user wants to update the array of enclitics and proclitics, they may do so with this method
	 * 
	 * If user doesn't want program to take into account clitics and sandhi for passages, they may set argument
	 * to empty array.
	 */
	public void setClitics(String[] clitics)
	{
		Arrays.parallelSort(clitics);
		
		this.clitics = clitics;
	}
	
	/*
	  The website slovored.com/search/accent finds the stresses of words, taking as input 1 or multiple words.
	  This method uses the library Jsoup to parse websites.
	  If the website can't find the stresses of the word(s), this method returns the argument word.
	 */
	private String readWebsite(String word)
	{
		try {
			//Load the website (this takes a while)
			org.jsoup.nodes.Document doc = Jsoup.connect("https://slovored.com/search/accent/" + word).get();
			Elements elements = doc.getAllElements();
		
			//Line 143 contains the stressed word
			String line = elements.get(143).text();
			return line;
			
		} catch (IOException e) {
			System.out.println("Couldn't load website for word " + word);
			return word;
		}
	}
	
	/* 
	 * Some letters in Bulgarian words are pronounced differently:
	 * - Word-final devoicing of obstruents (e.g. град --> 'grat, where д is pronounced like т)
	 * - Regressive assimilation in consonant clusters (e.g. изток --> 'istok, where з is pronounced like с)
	 * - т and д are usually not pronounced when inside of a consonant cluster (e.g. вестник --> 'vɛsnik)
	 * 
	 * word: Bulgarian word written in Cyrillic
	 * Returns word spelled as it would be pronounced (also in Cyrillic)
	 */
	private String phonotation(String word)
	{		
		//If word ends in a voiced consonant(s), that letter gets devoiced (replaced by its voiceless equivalent)
		if (word.endsWith("дж") && devoice)		//it's never "дз"
			word = word.substring(0, word.length() - 2) + 'ч';
		else if (devoice)
		{
			String last = word.substring(word.length() - 1);		//last letter
			int index = getObstruentIndex(voiced, last);
			if (index >= 0)		//if last letter is in voiced
				word = word.substring(0, word.length() - 1) + voiceless[index];
		}
		
		String cluster = "";	//current consonant cluster
		String output = "";
		char letter;
		for (int i = 0; i < word.length(); i++)		//loops through word
		{
			letter = word.charAt(i);
			if (isVowel(letter))
			{
				//Cluster ends; need to analyze it and possibly alter it
				cluster = analyzeCluster(cluster);
				
				output += cluster + letter;
				cluster = "";	//starts a new cluster
			}
			else	//if letter is a consonant
				cluster += letter;
		}
		
		//Last cluster
		if (cluster.length() > 0)
			output += analyzeCluster(cluster);
		
		return output;
	}
	
	
	//Performs assimilation and removes 'т' and 'д' from inside clusters
	private String analyzeCluster(String cluster)
	{		
		//Replace 'щ' with "шт" (if present); will replace back to 'щ' at the end
		cluster = cluster.replace("щ", "шт");
		
		char letter;
		if (cluster.length() > 2)
		{
			//Remove consonants 'т' and 'д' from inside clusters, unless it is followed by 'р'
			for (int i = 1; i < cluster.length() - 1; i++)		//loop in case cluster is 4+ letters long
			{
				letter = cluster.charAt(i);
				if ((letter == 'т' || letter == 'д') && cluster.charAt(i + 1) != 'р')
					cluster = cluster.substring(0, i) + cluster.substring(i + 1);	//remove letter
			}
		}
		
		if (cluster.length() > 1)		//Assimilation
			cluster = assimilate(cluster);
		
		cluster = cluster.replace("шт", "щ");
		
		return cluster;
	}
	
	
	//Changes a consonant cluster, replacing an obstruent with its voiced/voiceless alternative
	private String assimilate(String cluster)
	{
		//Progressive assimilation (rare): св --> сф, and possibly other examples
		cluster = cluster.replace("св", "сф");		
		
		String type = "", nextType = "neither";	//stores what type of consonant follows (voiced, voiceless, or neither)
		//Couldn't find any examples of "дж"/"дз" followed by a voiceless consonant, so it isn't considered
		
		char letter;
		String newLetter;
		for (int i = cluster.length() - 1; i >= 0; i--)		//loops through cluster backwards
		{
			letter = cluster.charAt(i);
			
			//Get the current consonant's type
			int index = getObstruentIndex(voiced, "" + letter);	//makes letter a String
			if (index >= 0)
				type = "voiced";
			else
			{
				index = getObstruentIndex(voiceless, "" + letter);
				if (index >= 0)
					type = "voiceless";
				else
					type = "neither";
			}
			
			//If type and nextType are different and not "neither", perform regressive assimilation
			if (!type.equals(nextType) && !type.equals("neither") && !nextType.equals("neither"))
			{
				//Get replacement letter
				if (nextType.equals("voiced") && index < 8)
					newLetter = voiced[index];
				else
					newLetter = voiceless[index];
				
				//Change letter to make it have the same type as following letter
				cluster = cluster.substring(0, i) + newLetter + cluster.substring(i + 1);
			}
			
			nextType = type;
			
			if (letter == 'в')		//exception: do not voice before 'в'
				nextType = "neither";
		}
		
		return cluster;
	}
	
	
	//Replacement for Arrays.binarySearch, since I can't sort the arrays voiced and voiceless 
	//beforehand (obstruents form pairs that share indexes in each array)
	private int getObstruentIndex(String[] obstruents, String letter)
	{
		for (int i = 0; i < obstruents.length; i++)
		{
			if (obstruents[i].equals(letter))
				return i;
		}
		
		return -1;
	}
	
	
	//Returns true if argument vowel is in array vowels, false otherwise (i.e. consonant)
	private boolean isVowel(char vowel)
	{
		//'ю' and 'я' are included for the purpose of splitting into syllables and for stresses
		char[] vowels = {'а', 'и', 'е', 'о', 'я', 'ъ', 'у', 'ю'};	//ordered by frequency

		for (int j = 0; j < 8; j++)		//loops through vowels
		{
			if (vowel == vowels[j])
				return true;
		}
		return false;
	}
	
	
	//This method returns an array containing the indexes of the stress marks in a word
	private int[] getStressIndex(String word)
	{
		char mark1 = "а̀".charAt(1), mark2 = "а́".charAt(1);	//two possible stress marks
		if (word.contains("" + mark1) || word.contains("" + mark2))
		{
			char mark = word.contains("" + mark1)? mark1 : mark2;
			
			int stressedIndex = word.indexOf(mark);
			if (word.indexOf(mark, stressedIndex + 1) > 0)		//if there is a second stress
			{
				int secondStress = word.indexOf(mark, stressedIndex + 1);
				
				int[] output = {stressedIndex, secondStress};
				return output;
			}
			else	//only 1 stress
			{
				int[] output = {stressedIndex};
				return output;
			}
		}
		
		return new int[0];		//no stress found
	}
	
}
