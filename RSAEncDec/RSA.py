import argparse
import copy
import math
import pickle
import random
from itertools import combinations


def int2baseTwo(x):
    """x is a positive integer. Convert it to base two as a list of integers
    in reverse order as a list."""
    # repeating x >>= 1 and x & 1 will do the trick
    assert x >= 0
    bitInverse = []
    while x != 0:
        bitInverse.append(x & 1)
        x >>= 1
    return bitInverse


def modExp(a, d, n):
    """returns a ** d (mod n)"""
    assert d >= 0
    assert n >= 0
    base2D = int2baseTwo(d)
    base2DLength = len(base2D)
    modArray = []
    result = 1
    for i in range(1, base2DLength + 1):
        if i == 1:
            modArray.append(a % n)
        else:
            modArray.append((modArray[i - 2] ** 2) % n)
    for i in range(0, base2DLength):
        if base2D[i] == 1:
            result *= base2D[i] * modArray[i]
    return result % n


def string2numList(strn):
    """Converts a string to a list of integers based on ASCII values"""
    return [ ord(chars) for chars in pickle.dumps(strn) ]


def numList2string(l):
    """Converts a list of integers to a string based on ASCII values"""
    return pickle.loads(''.join(map(chr, l)))


def numList2blocks(l, n):
    """Take a list of integers(each between 0 and 127), and combines them
    into block size n using base 256. If len(L) % n != 0, use some random
    junk to fill L to make it."""
    # Note that ASCII printable characters range is 0x20 - 0x7E
    returnList = []
    toProcess = copy.copy(l)
    if len(toProcess) % n != 0:
        for i in range(0, n - len(toProcess) % n):
            toProcess.append(random.randint(32, 126))
    for i in range(0, len(toProcess), n):
        block = 0
        for j in range(0, n):
            block += toProcess[i + j] << (8 * (n - j - 1))
        returnList.append(block)
    return returnList


def blocks2numList(blocks, n):
    """inverse function of numList2blocks."""
    toProcess = copy.copy(blocks)
    returnList = []
    for numBlock in toProcess:
        inner = []
        for i in range(0, n):
            inner.append(numBlock % 256)
            numBlock >>= 8
        inner.reverse()
        returnList.extend(inner)
    return returnList


def encrypt(message, modN, e, blockSize):

    numList = string2numList(message)
    numBlocks = numList2blocks(numList, blockSize)
    return [modExp(blocks, e, modN) for blocks in numBlocks]


def decrypt(secret, modN, d, blockSize):

    numBlocks = [modExp(blocks, d, modN) for blocks in secret]
    numList = blocks2numList(numBlocks, blockSize)
    return numList2string(numList)

def block_size(val):
    try:
        v = int(val)
        assert(v >= 10 and v <= 1000)
    except:
        raise argparse.ArgumentTypeError("{} is not a valid block size".format(val))
    return val

if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("-m", "--message", help="Text message to encrypt")

    parser.add_argument("-b", "--block-size", type=block_size, default=15,
        help="Block size to break message info smaller trunks")

    args = parser.parse_args()

    n = 0xb405c40e08150ea58684b203002ccbd72385c9c10b74cb2010b1258e4b38d472da2c2c640ee92fb2d6742e027769749fa998e77a4f9ed406620c336e803a3fe3
    e = 0x10001
    d = 0x6fc1c895616519239f1fcf96d74bf7fb861cef43fd85b2f82404a528f1ffc4bac0080443831eda34d68779d25609831678991fd724137f60dd67f08223dde3b9

    if args.message is not None:
        message = args.message
    else:
        raise argparse.ArgumentTypeError("NULL message")

    print "\n\nMessage : {}".format(message)
    print "-"*80
    cipherText = encrypt(message, n, e, 15)
    print "Ciphertext :  {}".format(cipherText)
    print "-"*80
    decrypted = decrypt(cipherText, n, d, 15)
    print "Decrypted : {}".format(decrypted)
