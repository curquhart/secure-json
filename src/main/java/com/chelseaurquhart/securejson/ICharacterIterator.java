package com.chelseaurquhart.securejson;

import java.util.Iterator;

interface ICharacterIterator extends Iterator<Character> {
    Character peek();

    int getOffset();
}
