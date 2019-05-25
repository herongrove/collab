#!/usr/bin/env python
from __future__ import print_function

import os
import sys
import subprocess
import glob
import time
import datetime
from pipes import quote

# convert a directory of pdf files (1st arg) to text and put them in a different directory (2nd arg)
# source activate collab
# python textFromDir /home/dane/data/energy/pdf /home/dane/data/energy/raw
def main(args):
    toconvert = glob.glob(os.path.join(args[1], '*.pdf'))
    print("Converting {} files".format(len(toconvert)))
    for inloc in toconvert:
        # keeping track of how far we've gotten
        print(os.path.basename(inloc), end='\t')
        # name for output file
        basename = os.path.splitext(os.path.basename(inloc))[0] + '.txt'
        # name with full path
        outloc = os.path.join(args[2], basename)
        # escape spaces, safer cmd
        cmd = 'pdf2txt.py -o {} {}'.format(quote(outloc), quote(inloc))
        # start timer
        before = time.time()
        # send command to pdf2txt
        p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        # to make sure we wait for this doc to finish
        (output, err) = p.communicate()
        p_status = p.wait()
        # end timer and report elapsed time
        after = time.time()
        print(str(datetime.timedelta(seconds = after - before)))

if __name__ == '__main__': sys.exit(main(sys.argv))