package org.greencodeinitiative.creedengo.java.checks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class AvoidGettingSizeCollectionInForLoopIgnored {
    AvoidGettingSizeCollectionInForLoopIgnored() {

    }

    public void badForLoop() {
        final List<Integer> numberList = new ArrayList<Integer>();
        numberList.add(10);
        numberList.add(20);

        final Iterator<Integer> it = numberList.iterator();
        for (; it.hasNext(); ) { // Ignored => compliant
            System.out.println(it.next());
        }
    }


}
