#!/usr/bin/env python
from __future__ import print_function

import os
import sys
import subprocess
import glob
import time
import datetime
import getopt
from tqdm.contrib.concurrent import thread_map
from tqdm import tqdm
from functools import partial
from pipes import quote

def convert_pdf(in_loc, out_dir):
    # name for output file
    base_name = os.path.splitext(os.path.basename(in_loc))[0] + '.txt'
    # name with full path
    out_loc = os.path.join(out_dir, base_name)
    # don't reprocess files you've already seen
    if os.path.isfile(out_loc):
        tqdm.write(os.path.basename(in_loc) + "\talready done")
        # print(os.path.basename(in_loc) + "\talready done")
        return
    # escape spaces, safer cmd
    cmd = 'pdf2txt.py -o {} {}'.format(quote(out_loc), quote(in_loc))
    # start timer
    before = time.time()
    # send command to pdf2txt
    p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    # to make sure we wait for this doc to finish
    (_, _) = p.communicate()
    p_status = p.wait()
    # end timer and report elapsed time
    after = time.time()
    # keeping track of how far we've gotten, and how long it took
    tqdm.write(os.path.basename(in_loc) + '\t' + str(datetime.timedelta(seconds = after - before)))
    # print(os.path.basename(in_loc) + '\t' + str(datetime.timedelta(seconds = after - before)))

# convert a directory of pdf files (1st arg) to text and put them in a different directory (2nd arg)
# source activate collab
# python textFromDir.py [-t threads] /path/to/pdfs /path/to/rawtext
# python textFromDir.py -t8 /home/dane/data/collab/pdf /home/dane/data/collab/raw
def main(argv):
    argv = argv[1:]
    opts, args = getopt.getopt(argv, 't:')
    n_threads = 1
    for flag, val in opts:
        if flag == "-t":
            n_threads = int(val)

    to_convert = glob.glob(os.path.join(args[0], '*.pdf'))
    print("Converting {} files".format(len(to_convert)))
    thread_map(partial(convert_pdf, out_dir=args[1]), to_convert, max_workers=n_threads)

if __name__ == '__main__': sys.exit(main(sys.argv))