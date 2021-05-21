#!/usr/bin/env python
from __future__ import print_function

import os
import sys
import glob

import PyPDF2

# convert a directory of pdf files (1st arg) to text and put them in a different directory (2nd arg)
# source activate collab
# python countPages.py /home/dane/data/collab/pdf
def main(args):
    toconvert = glob.glob(os.path.join(args[1], '*.pdf'))
    print("Counting pages in {} files".format(len(toconvert)))
    with open("pageCounts.tsv", "w") as out_file:
        for inloc in toconvert:
            # keeping track of how far we've gotten
            print(os.path.basename(inloc), end='\n')
            # name for output file
            basename = os.path.splitext(os.path.basename(inloc))[0]
            with open(inloc, "rb") as pdf_file:
                try:
                    pdf_reader = PyPDF2.PdfFileReader(pdf_file)
                    num_pages = pdf_reader.numPages
                except:
                    num_pages = "NA"
                out_file.write("%s\t%s\n" % (basename, num_pages))

if __name__ == '__main__': sys.exit(main(sys.argv))
