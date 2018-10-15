package com.chelseaurquhart.securejson;

import java.io.IOException;
import java.util.Iterator;

interface ICharacterIterator extends Iterator<Character> {
    Character peek() throws IOException;

    int getOffset();
}
