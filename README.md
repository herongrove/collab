# Information Extraction for Policy Analysis

This project uses the [Odin extraction framework](https://arxiv.org/abs/1509.07513) to extract 
information relevant to policy analysis from text.

## What you'll need...
  3. [anaconda for Python 2.7](https://www.anaconda.com/distribution/)
  1. [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
  2. [sbt](http://www.scala-sbt.org/release/tutorial/Setup.html)

## Process
To run the shell for grammar testing, run `./shell` from the command line. To run the extractor over 
many documents, follow the steps below.

### Convert PDFs into txt documents
The main extraction system assumes text format. If you have (searchable) PDF format, you can use 
the script at `src/main/python/textFromDir.py` to do the conversion. The script uses python 
libraries (crucially, [pdfminer](https://euske.github.io/pdfminer/index.html)) that require 
installation. You can install via conda using
```
conda env create -f src/main/python/environment.yml
```
then activate the environment using
```
source activate collab
```
To actually run the converter, use something like
```
python ./src/main/python/textFromDir.py /path/to/pdf /path/to/raw
```
This system doesn't work well on tables yet.

### Clean up txt documents

Text from PDFs typically has mid-sentence line breaks that confuse the NLP preprocessors. To prepare 
the raw text for extraction, adjust `src/main/resources/reference.conf` to match your file 
locations. Specifically, you need to adjust `collab.data`. Then, run
```
sbt "runMain com.github.danebell.collab.korrect.KorrectDocuments"
```

### Run the extraction system

Adjust `collab.out` to be the directory where you want the relation extractions printed. Then, run 
```
sbt "runMain com.github.danebell.collab.ReadPapers"
```
This is best done on a computer with many threads and lots of memory. To make use of this memory, 
adjust `./.sbtopts` to an appropriate value, e.g. `-J-Xmx63G` for 63 GB of memory. 

If you re-run the system (for example, after making rule changes), the previous results will not be 
overwritten. This is to allow for easy restarting after crashes or other interruptions. If you want 
to overwrite old results, you must delete them first.

### Join the output

There's a script to join the output into a single document. Ensure that the `collab.data` location 
is set correctly, then run 
```
sbt "runMain com.github.danebell.collab.utils.MergeOutput"
```
For very large collections, it will be faster to use command-line functions, so a script is 
provided at `./merge.sh`. Note that you must run this script from the `collab.data` location.

## Citing

If you use this repository, please cite the following work:
> Bell, Emily V., Elizabeth A. Albright, and Dane Bell.  “Mitigating Hazard through Collaborative PolicyNetworks:  A  Study  of  Policy  Learning  in  Urban  Stormwater  Management.”   Association  for  PublicPolicy Analysis and Management Fall Research Conference, Denver, CO, November 2019.
