package com.sloppie.mediawellbeing.util;

import java.util.ArrayList;

/**
 * This is a Stack DataStructure that contains only unique elements in its body, as such:
 * if an element is {@link #push(BaseDataStructure)} a stack that already contained the element:
 * <ol>
 *     <li>The element is first sliced out</li>
 *     <li>The element is then added to the top of the stack</li>
 * </ol>
 * This is used to keep track of unique things like: Screens and Fragments that may appear in Stack
 * order but are unique.
 */
public class HybridStack {
    private final ArrayList<BaseDataStructure<String>> stack;

    public HybridStack() {
        stack = new ArrayList<>();
    }

    /**
     * This method is used to push unique values onto the HybridStack. If the stack already
     * contains a given value, it is sliced out and then pushed as a unique value to the top.
     * @param value
     */
    public void push(BaseDataStructure<String> value) {
        String key = value.getKey();
        if (stack.size() != 0) {
            // this index is used to find of there exists such a value in the stack,
            // if it does exist, this index is update with that value, otherwise, the index remains
            // as -1
            int index = -1;
            for (int i=0; i<stack.size(); i++) {
                String currentValKey = stack.get(i).getKey();
                if (currentValKey.compareTo(key) == 0) {
                    index = i;
                    break; // found an index
                }
            }

            // slice out the index of the previous element in the stack containing that value
            // and add a new member at the top that is this value.
            if (index > -1) {
                stack.remove(index); // remove element
            }

            // the value is then added afresh making sure tha the value is unique and now at the top
            // of the stack.
        }
        stack.add(value);
    }

    /**
     * This method is used to remove the element at the top of the stack,
     * @return the element that has been popped, it returns null if the stack does not contain any
     * elements.
     */
    public BaseDataStructure<String> pop() {
        if (stack.size() > 0) {
            return stack.remove(stack.size() - 1);
        }

        return null;
    }
}
