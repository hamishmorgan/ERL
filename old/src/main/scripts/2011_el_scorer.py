#!/usr/bin/python

import re
import sys
import os

class SystemOutputException(Exception): pass

def readLinking(goldStdFile):
    """
    Reads a file containing Entity Linking output according to the KBP format.
    Uppercases all IDs
    Returns a dictionary KB_ID -> set of doc_IDs
    """
    linking = dict()
    for line in open(goldStdFile):
        d = re.split("\s+", line.strip())
        mention = d[0].upper()
        kb_id   = d[1].upper()

        if kb_id in linking.keys():
            linking[kb_id].add(mention)
        else:
            linking[kb_id] = set([mention])
    return linking


def b2_correctness(el_a, el_b, system_el2kbid, gold_el2kbid):
    correct = False

    if(inSameSet(el_a, el_b, system_el2kbid) and 
       inSameSet(el_a, el_b, gold_el2kbid)
       ):
        correct = True

    return correct

def b3_correctness(el_a, el_b, system_el2kbid, gold_el2kbid):
    """
    A pair of elements (el_a, el_b) is considered 'correct' when they
    share the same cluster in the gold standard and
    share the same cluster in the system output and
    have the same linking (see 'sameLinking' documentation).
    """
    correct = False

    if(inSameSet(el_a, el_b, system_el2kbid) and 
       inSameSet(el_a, el_b, gold_el2kbid) and
       sameLinking(el_a, el_b, system_el2kbid, gold_el2kbid)  #THIS CONDITION DEPARTS FROM THE ORIGINAL BCUBED (extesion for the Entity Linking problem)
       ):
        correct = True

    return correct

def sameLinking(el_a, el_b, system_el2kbid, gold_el2kbid):
    """
    For two elements (el_a, el_b) that are be mapped to the Knwoledge Base in the gold standard
    'same linking' means that they share the same identifier, both in the gold standard and the system output.
    Elements that cannot be mapped to the KB are assigned a NILXXX identifier, where XXX is a numerical value.
    For those elements 'same linking' is satisfied when the element's label begins with 'NIL' in both gold
    standard and system output. That means that we check that the elements have been correctly identified
    as not mapped to the KB, but ignore the particular identifier assigned on the gold std. and sys. output sides.
    """

    sys_el_a_id = system_el2kbid[el_a]
    sys_el_b_id = system_el2kbid[el_b]
    gol_el_a_id = gold_el2kbid[el_a]
    gol_el_b_id = gold_el2kbid[el_b]

    if sys_el_a_id.startswith('NIL'): sys_el_a_id = 'NIL'
    if sys_el_b_id.startswith('NIL'): sys_el_b_id = 'NIL'
    if gol_el_a_id.startswith('NIL'): gol_el_a_id = 'NIL'
    if gol_el_b_id.startswith('NIL'): gol_el_b_id = 'NIL'

    #print system_el2kbid
    
    return sys_el_a_id == sys_el_b_id == gol_el_a_id == gol_el_b_id

def inSameSet(el_a, el_b, el2kbid):
    return el2kbid[el_a] == el2kbid[el_b]

def b2_recall(system_output, gold_standard, sys_el2kbid, gold_el2kbid):
    #print "\nElement recall:"
    return b3_precision(gold_standard, system_output, sys_el2kbid, gold_el2kbid)

def b3_recall(system_output, gold_standard, sys_el2kbid, gold_el2kbid):
    #print "\nElement recall:"
    return b3_precision(gold_standard, system_output, sys_el2kbid, gold_el2kbid)
    
def b3_precision(system_output, gold_standard, sys_el2kbid, gold_el2kbid):
    """
    Extension of the original BCubed clustering metric as described by Daniel Bikel, Vittorio Castelli and Radu Florian in their technical report.
    """
    el_pre_sums = 0.0
    num_elements = 0
    
    for kb_id in system_output.keys():
        mention_set = system_output[kb_id]

        num_elements += len(mention_set)
        
        for el_a in mention_set:
            num_correct = 0
            
            for el_b in mention_set:
                correct = b3_correctness(el_a, el_b, sys_el2kbid, gold_el2kbid)
                if(correct): num_correct +=1

            el_pre = num_correct / float(len(mention_set))
            
            el_pre_sums += el_pre
            
            #print "\t%s\t%.2f" % (el_a, el_pre)

    P = el_pre_sums / float(num_elements)
    
    return P


def b2_precision(system_output, gold_standard, sys_el2kbid, gold_el2kbid):
    """
    Implementation of the original BCubed clustering metric
    """
    el_pre_sums = 0.0
    num_elements = 0
    
    for kb_id in system_output.keys():
        mention_set = system_output[kb_id]

        num_elements += len(mention_set)
        
        for el_a in mention_set:
            num_correct = 0
            
            for el_b in mention_set:
                correct = b2_correctness(el_a, el_b, sys_el2kbid, gold_el2kbid)
                if(correct): num_correct +=1

            el_pre = num_correct / float(len(mention_set))
            
            el_pre_sums += el_pre
            
            #print "\t%s\t%.2f" % (el_a, el_pre)

    P = el_pre_sums / float(num_elements)
    
    return P

def getMap(linking):
    el2kbid = dict()
    for kbid in linking.keys():
        mentions = linking[kbid]
        for m in mentions:
            el2kbid[m] = kbid
    return el2kbid

def kbp2010_microaverage(sys_el2kbid, gold_el2kbid):
    num_samples = len(gold_el2kbid.keys())
    num_correct_samples = 0

    for el in gold_el2kbid.keys():
        gold_kbid = gold_el2kbid[el]
        sys_kbid = sys_el2kbid[el]

        if gold_kbid == sys_kbid: num_correct_samples += 1

    #print num_correct_samples
    #print num_samples
    
    return num_correct_samples / float(num_samples)


def systemsRankingScript(goldStdFile, systemsDir):

    #print ("system\tKBP2010 micro-average\tB^2 Pre\tB^2 Rec\tB^3 Pre\tB^3 Rec") 
    print ("system\tB^3 Precision\tB^3 Recall") 
    
    gold_standard = readLinking(goldStdFile)
    gold_el2kbid = getMap(gold_standard)
    size = 0
    for s in gold_standard: size += len(s)

    gold_els = frozenset(gold_el2kbid.keys())
    
    for systemOutFile in os.listdir(systemsDir):
        
        system_output = readLinking(systemsDir+"/"+systemOutFile)
        
        sys_el2kbid = getMap(system_output)

        #Make sure that the system output includes all and only items in the reference
        sys_els = frozenset(sys_el2kbid.keys())
        diff = gold_els - sys_els
        try:
            if len(diff) > 0:
                raise SystemOutputException
        except SystemOutputException:
            print "[ERROR] The output in \""+systemsDir+systemOutFile+"\" is missing the following documents ("+str(', '.join(diff))+"). This system output wont be evaluated."
            continue

        diff = sys_els - gold_els
        try:
            if len(diff) > 0:
                raise SystemOutputException
        except SystemOutputException:
            print "[ERROR] The output in \""+systemsDir+systemOutFile+"\" contains documents not present in the gold standard ("+str(', '.join(diff))+"). This system output wont be evaluated."
            continue

        system_name = systemOutFile

        #kbp_micro_aver = kbp2010_microaverage(sys_el2kbid, gold_el2kbid)

        #b2_pre = b2_precision(system_output, gold_standard, sys_el2kbid, gold_el2kbid)
        #b2_rec = b2_recall(system_output, gold_standard, sys_el2kbid, gold_el2kbid)

        b3_pre = b3_precision(system_output, gold_standard, sys_el2kbid, gold_el2kbid)
        b3_rec = b3_recall(system_output, gold_standard, sys_el2kbid, gold_el2kbid)

        #print "%s\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f" % (system_name, kbp_micro_aver, b2_pre, b2_rec, b3_pre, b3_rec)
        print "%s\t%.2f\t%.2f" % (system_name, b3_pre, b3_rec)
 
if __name__ == "__main__":
    """
    version 0.1: initial script
    version 0.2: fixed bug in 'sameLinking' function (thanks to Timothy Nyberg) that prevented NIL elements with different identifiers on the gold std. and sys output sides from matching
    """

    if(not len(sys.argv) == 3):
        print "----------------------------------------"
        print "KBP2011 Entity Linking evaluation script"
        print "----------------------------------------"
        print "USAGE: ./el_scorer [gold_standard_file] [system_output_dir]"
        print " - gold_standard_file Ground truth for the test data."
        print " - system_output_dir  Directory with one or more system outputs for the"
        print "                      test data following the KBP format."
        print " Please note that 'NIL' element identifiers must follow the format NILXXX, where XXX is three digit number."
    else:
        goldStdFile = sys.argv[1]
        systemsDir = sys.argv[2]
        systemsRankingScript(goldStdFile, systemsDir)
    
