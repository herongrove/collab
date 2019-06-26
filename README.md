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
locations. Specifically, you need to adjust `collab.raw` and `collab.clean` to be the directories 
where your raw (that is, not yet cleaned up) text files and your cleaned-up text files will reside, 
respectively. Then, run
```
sbt "runMain com.github.danebell.collab.korrect.KorrectDocuments"

```

### Run the extraction system

Adjust `collab.out` to be the directory where you want the relation extractions printed. Then, run 
```
sbt "runMain com.github.danebell.collab.ReadPapers
```
This is best done on a computer with many threads and lots of memory. To make use of this memory, 
adjust `./.sbtopts` to an appropriate value, e.g. `-J-Xmx63G` for 63 GB of memory. 

