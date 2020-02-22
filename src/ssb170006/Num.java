package ssb170006;

/**
 * CS 6301: Implementation of data structures and algorithms
 * Long Project LP1: Integer arithmetic with arbitrarily large numbers
 *
 * @author Divyesh Patel (dgp170030)
 * @author Sankalp Bhandari (ssb170006)
 * @author Shraddha Bang (sxb180041)
 * Date: Sunday, Febuary 23, 2020
 */

import java.util.*;

public class Num implements Comparable<Num> {

    // defaultBase: Base in which input and output numbers are evaluated
    static long defaultBase = 10;
    // base: Base responsible for fast calculations. Larger the base, faster the computations.
    long base = 1000000000; //NOTE: Keep it in powers of 10 AND 10 <= base <= 10^9

    int digits = (int) Math.log10(base()); // base = 10^digits

    // Main attributes of a Num:
    private long[] arr; // array to store arbitrarily large integers
    // boolean flag to represent negative numbers
    private boolean isNegative;
    private int len; // actual number of elements of array that are used

    /**
     * Converts String s (a number) into Num. Initialize isNegative, arr, and len.
     *
     * @param s the input string
     * @throws NumberFormatException
     */
    public Num(String s) throws NumberFormatException {
        isNegative = false;
        /**
         * oriStrLen: original string length of s
         * trunStrLen: truncated string length after ignoring '-' sign and leading 0s
         * non0: index keeping track of first nonzero character
         * count0: total leading 0s in a string
         */
        int originalLen = s.length();
        int truncatedLen = originalLen, nonZeroCount = 0, zeroCount = 0;

        String validStr = "[-]?([0-9]+)"; // RegEx for valid String

        // When invalid string entered
        if (originalLen == 0 || s.isEmpty() ||
                (originalLen > 1 && !s.matches(validStr))) {
            throw new NumberFormatException("Invalid String \n");
        }
        // Assigning proper Sign
        this.isNegative = s.charAt(0) == '-';

        // Truncating '- sign' character, if present
        if (isNegative) {
            truncatedLen = originalLen - 1;
            nonZeroCount = 1;
        }


        // incrementing nonZero to keep track of first nonZero element.
        while (nonZeroCount < originalLen && s.charAt(nonZeroCount) == '0') {
            nonZeroCount++;
            zeroCount++;
        }

        // For -000000000000 to be just 0
        if (truncatedLen == zeroCount)
            isNegative = false;

        // Removing leading zeros
        if (zeroCount > 0)
            truncatedLen = truncatedLen - zeroCount;

        // When s is "0"
        if (truncatedLen == 0) {
            len = 1; // still a valid 'digit'
            arr = new long[1];
            arr[0] = 0;
        } else {
            // trunStrLen cannot be further truncated. So, assign a correct len
            this.len = (int) Math.ceil((double) truncatedLen / digits);
            arr = new long[len];
            int i = 0, k = 0;
            int index = originalLen - 1;
            char[] charDigit = new char[digits];
            String strDigit;

            // Extracting the numbers out of String
            while (nonZeroCount <= index && i < len) {

                k = digits - 1;
                // Copying chunk of characters of length 'digits' or less
                while (0 <= k && nonZeroCount <= index) {
                    charDigit[k--] = s.charAt(index--);
                }
                // if less then remaining characters are padded with '0'
                while (0 <= k) {
                    charDigit[k--] = '0';
                }
                // a new digit is added as a long in arr
                this.arr[i++] = Long.parseLong(String.valueOf(charDigit));
            }
        }
    }

    /**
     * Reads the number x (in defaultBase), to create Num in base = this.base
     * storing all it's digits long[] arr with least significant at arr[0] and so on.
     *
     * @param x the number
     */
    public Num(long x) {
        isNegative = false;

        // When x is Negative, making it positive with flag isNegative as true
        if (x < 0) {
            this.isNegative = true;
            x = -1 * x;
        }
        double lengthArr = 0;

        // When x = 0, lengthArr = -Infinity
        if (x == 0) {
            lengthArr = 1;
        }
        // No of digits in this.Num = log_base (x) + 1 = log(x)/log(base) + 1
        else {
            lengthArr = ((Math.log((double) x)) / (Math.log((double) base))) + 1;
        }

        this.len = (int) lengthArr; // Real to integer (floor function)
        arr = new long[this.len];

        long n = x;
        // Iteratively pass appropriate digit to this.arr (array of long)
        int i = 0;
        while (n > 0) {
            arr[i++] = n % base();
            n /= base();
        }
    }

    private Num() {

    }

    /**
     * Addition of two numbers Num num1 and Num num2.
     * Precondition: num1 and num2 must be in the same base.
     *
     * @param num1 the large number
     * @param num2 the large number
     * @return the large sum of num1 and num2
     */
    public static Num add(Num num1, Num num2) {
        Num out = new Num();

        // When (-) + (-) or (+) + (+)
        if (num1.isNegative == num2.isNegative) {
            out = out.unsignedAdd(num1, num2);
            out.isNegative = num1.isNegative;
        }
        // When you need to find the difference
        else {
            // When |a| < |b|
            int compare = num1.compareTo(num2);
            if (compare < 0) {
                out = out.normalSubtract(num2, num1); // find the difference
                out.isNegative = num2.isNegative; // give bigger number sign
            } else if (compare > 0) {
                out = out.normalSubtract(num1, num2); // find the difference
                out.isNegative = num1.isNegative; // give bigger number sign
            } else {
                return new Num(0);
            }
        }
        return out;
    }

    /**
     * Unsigned addition of two numbers Num x and Num y.
     * Precondition: x and y must be in the same base.
     *
     * @param x the large number
     * @param y the large number
     * @return the large sum of x and y
     */
    private Num unsignedAdd(Num x, Num y) {
        int ptrNum1 = 0, ptrNum2 = 0; // Pointers as indices of each Number array

        long carry = 0;
        long sum = 0;

        // trimming unnecessary leading zeros, if any.
        // Updating to appropriate a.len and b.len as well
        Num num1 = trimZeros(x);
        Num num2 = trimZeros(y);

        Num result = new Num();
        result.len = Math.max(num1.len, num2.len);
        result.arr = new long[result.len + 1]; // NOTE: out.arr initialized to long[result.len + 1]*

        int index = 0;

        // While both arrays have corresponding element
        while (ptrNum1 < num1.len && ptrNum2 < num2.len) {

            // Elementary way of adding digit by digit
            sum = num1.arr[ptrNum1++] + num2.arr[ptrNum2++] + carry;
            result.arr[index++] = (sum % base);
            carry = (sum / base); // would be 1 or 0
        }
        // Now, one of the number is exhausted

        // When b is exhausted, while a still has element(s)
        while (ptrNum1 < num1.len) {
            // same as above
            sum = num1.arr[ptrNum1++] + carry;
            result.arr[index++] = (sum % base);
            carry = (sum / base);
        }

        // When a is exhausted, while b still has element(s)
        while (ptrNum2 < num2.len) {
            // same as above
            sum = num2.arr[ptrNum2++] + carry;
            result.arr[index++] = (sum % base);
            carry = (sum / base);
        }

        // If there's still a carry forwarded
        if (carry > 0) {
            result.arr[index] = carry;
            result.len++;
        }
        // returning without any leading zeros
        return trimZeros(result);
    }

    /**
     * Trimming unnecessary leading zeros from x.
     * Keeping sign intact, updates 'num.len' and 'num.arr' from x
     *
     * @param x the input number
     * @return the trimmed number
     */
    private Num trimZeros(Num x) {
        Num num = new Num();
        num.isNegative = x.isNegative; // sign as it was

        int numLen = x.len; // initialized

        // reduce by 1 every-time you have a leading zero
        while (x.arr[numLen - 1] == 0) {
            numLen--;
            // When all zeros, eventually ended up making aLen = 0
            if (numLen == 0) {
                numLen++;
                break;
            }
        }
        // Initialize and returning appropriately
        num.arr = new long[numLen];
        System.arraycopy(x.arr, 0, num.arr, 0, numLen);
        num.len = numLen;
        return num;
    }

    /**
     * Subtraction of Num b from Num a.
     * NOTE: Left associativity is maintained.
     *
     * @param num1 the input number
     * @param num2 the input number
     * @return the difference
     */
    public static Num subtract(Num num1, Num num2) {
        Num result = new Num();

        // When num1 - num2 = (-) - (+) or num1 - num2 = (+) - (-)
        if (num1.isNegative != num2.isNegative) {
            result = result.unsignedAdd(num1, num2);
            result.isNegative = num1.isNegative;
        }
        // When num1 - num2 needs to find the difference |num1|-|num2|
        else {
            // When |num1| < |num2|
            if (num1.compareTo(num2) < 0) {
                result = result.normalSubtract(num2, num1); // find the difference
                result.isNegative = !num2.isNegative; // give bigger number sign* -(-num2) is +ve
            } else {
                result = result.normalSubtract(num1, num2); // find the difference
                result.isNegative = num1.isNegative; // give bigger number sign
            }
        }
        return result;
    }

    /**
     * Difference of two numbers ignoring the sign.
     * Precondition: x >= y
     *
     * @param x the greater number
     * @param y the smaller number
     * @return the difference
     */
    private Num normalSubtract(Num x, Num y) {

        int ptrNum1 = 0, ptrNum2 = 0; // Pointers as indices of array of numbers

        long borrow = 0;
        long digitDiff = 0;

        // Trimming unnecessary leading zeros, if any.
        // Updating to appropriate num1.len and num2.len as well
        Num num1 = trimZeros(x);
        Num num2 = trimZeros(y);

        Num result = new Num();
        result.len = Math.max(num1.len, num2.len);
        result.arr = new long[result.len]; // but result.arr initialized to long[result.len]* (not result.len + 1)

        int index = 0;

        // While both arrays have corresponding element
        while (ptrNum1 < num1.len && ptrNum2 < num2.len) {
            // Elementary way of subtracting digit by digit,
            // WITH subtracting a BORROW taken earlier
            digitDiff = num1.arr[ptrNum1++] - num2.arr[ptrNum2++] - borrow;

            // When you don't need to take a borrow
            if (digitDiff >= 0) {
                result.arr[index++] = digitDiff;
                borrow = 0;
            }
            // When you need to have BORROW
            else {
                result.arr[index++] = digitDiff + base;
                borrow = 1; // setting borrow = 1
            }
        }
        // When num2 is exhausted, while num1 still has element(s)
        while (ptrNum1 < num1.len) {
            // When there exists a borrow
            if (borrow == 1) {
                // When borrow is forwarded, as you can't borrow from ZERO
                if (num1.arr[ptrNum1] == 0) {
                    digitDiff = base - 1;
                    borrow = 1;
                }
                // borrow is subtracted
                else {
                    digitDiff = num1.arr[ptrNum1] - borrow;
                    borrow = 0; // No more borrows
                }

                result.arr[index++] = digitDiff;
                ptrNum1++;
            }
            // No borrow
            else {
                // just copying a to result
                result.arr[index++] = num1.arr[ptrNum1++];
            }
        }
        return trimZeros(result);
    }

    /**
     * Returns Num as a product of Num a and Num b.
     *
     * @param num1 first number
     * @param num2 second number
     * @return the product of numbers a and b
     */
    public static Num product(Num num1, Num num2) {
        Num result = new Num();

        // just getting the scalar product = |num1| * |num2|
        result = result.scalarProduct(num1, num2);

         /*
         Updating the appropriate sign
         - * - or + * +
         - * + or - * +
         */
        result.isNegative = num1.isNegative != num2.isNegative;

        return result;
    }

    /**
     * Returns scalar product of two numbers using Karatsuba Algorithm
     *
     * @param num1 the first number
     * @param num2 the second number
     * @return the product of num1 and num2
     */
    private Num scalarProduct(Num num1, Num num2) {
        Num result = new Num();

        // Largest length from two numbers
        int maxLen = Math.max(num1.len, num2.len);

        // When product num1 * num2 = 0 (ZERO), when either of two numbers is ZERO
        if ((num1.len == 1 && num1.arr[0] == 0) || (num2.len == 1 && num2.arr[0] == 0)) {
            result.len = 1;
            result.arr = new long[result.len];
            result.arr[0] = 0;
            return result;
        }

        // Base Condition: When both numbers are of unit length (have a single digit)
        if (num1.len == 1 && num2.len == 1) {
            long prod = num1.arr[0] * num2.arr[0]; // still in decimal
            long carry = prod / base;

            // Only single digit is sufficient
            if (carry == 0) {
                result.len = 1;
                result.arr = new long[result.len];
                result.arr[0] = prod;
            }
            // When need two digits for the product
            else {
                result.len = 2;
                result.arr = new long[result.len];
                result.arr[0] = prod % base;
                result.arr[1] = carry;
            }
        }

        // Karatsuba Algorithm: Fast Integer Multiplication, RT = 0(n^log2 (3))
        else {
            // a1, b1 being GREATER sub-halves and a2, b2 being LOWER sub-halves
            Num a1 = new Num();
            Num a2 = new Num(); // a[a2:a1]
            Num b1 = new Num();
            Num b2 = new Num(); // b[b2:b1]

            Num p;
            int zeros = 0;

            // Initializing appropriate a1[], a2[], b1[] and b2[]
            int l2 = maxLen / 2;
            int l1 = maxLen - l2; // NOTE: l1 = {l2+1 OR l2}

            a2.len = b2.len = l2;
            a1.len = b1.len = l1;

            a1.arr = new long[l1];
            a2.arr = new long[l2];
            b1.arr = new long[l1];
            b2.arr = new long[l2];

            // When unEqual lengths
            if (num1.len != num2.len) {
                zeros = num1.len - num2.len;
                // pad to a
                if (zeros < 0) {
                    // [It's out = p * b]
                    p = padZeros(num1, Math.abs(zeros));
                    fragment(p, a1, a2, num2, b1, b2); // p to [a1:a2] and b to [b1:b2]
                }
                // pad to b
                else {
                    // [It's out = a * p]
                    p = padZeros(num2, zeros);
                    fragment(num1, a1, a2, p, b1, b2); // a to [a1:a2] and p to [b1:b2]
                }
            }
            // When Equal length
            else {
                fragment(num1, a1, a2, num2, b1, b2); // a to [a1:a2] and b to [b1:b2]
            }
            // As of Now, we have proper fragments - [a1:a2] and [b1:b2]
            // ReC1: r1 = a1*b1
            // ReC2: r2 = a2*b2
            // ReC3: r3 = (a1+a2)*(b1+b2)

            // Recursive Call 1:
            Num r1 = new Num();
            r1 = scalarProduct(a1, b1);

            // Recursive Call 2:
            Num r2 = new Num();
            r2 = scalarProduct(a2, b2);

            // Recursive Call 3:
            Num r3 = new Num();
            r3 = scalarProduct(unsignedAdd(a1, a2), unsignedAdd(b1, b2));

            // CONQUER STEP:
            // a * b = shiftZeros(r1,(n/1+n/2)) + r2 + shiftZeros(r3-r2-r1, n/2)
            Num sum = unsignedAdd(r1, r2);
            Num r4 = normalSubtract(r3, sum);

            Num f1 = shiftZeros(r1, (maxLen / 2 + maxLen / 2));
            Num f2 = shiftZeros(r4, maxLen / 2);

            Num temp = unsignedAdd(f1, r2);
            result = unsignedAdd(temp, f2);
        }
        return result;
    }

    /**
     * NOTE:
     * 1. Though Karatsuba Algorithm RT = 0(n^log 3) sounds great, but it's implementation is
     * complicated, as you can see. The divide-and-conquer has a big recursive-overhead when
     * the numbers are not of equal length, as we create additional terms and more crossover
     * points, and only behaves better when numbers are of equal length.
     *
     * 2. The O(n^2) is simple and straight-forward. Karatsuba can perform faster than O(n^2) when
     * the Num has 10000 digits (arr.length = 10000) or more, which is equivalent to having a number
     * of decimal length = 10^(9 * 10000).
     *
     * 3. It is done for personal satisfaction. If you are aiming for efficiency, you may keep
     * the implementation simple with O(n^2) algorithm.
     *
     * 4. Thus, a complicated 0(n^log 3) doesn't necessarily beat O(n^2) algorithm.
     */

    /**
     * Karatsuba Algorithm Helper method:
     * Fragments a to a1 and a2, and b to b1 and b2.
     * Precondition: a and b are of same length n.
     *
     * @param num1      the first number
     * @param upperNum1 the higher significant half (length = n - n/2)
     * @param lowerNum1 the lower significant half (length = n/2)
     * @param num2      the second number
     * @param upperNum2 the higher significant half (length = n - n/2)
     * @param lowerNum2 the lower significant half (length = n/2)
     */
    private void fragment(Num num1, Num upperNum1, Num lowerNum1, Num num2, Num upperNum2, Num lowerNum2) {
        int numLen = num1.len;
        int lowerLen = numLen / 2;
        int upperLen = numLen - lowerLen;
        // Consider a = 12345 = [5,4,3,2,1]. So, l2 = 2.
        // a1 = 123 = [3,2,1] and a2 = 45 = [5,4]
        System.arraycopy(num1.arr, 0, lowerNum1.arr, 0, lowerLen);
        System.arraycopy(num2.arr, 0, lowerNum2.arr, 0, lowerLen);
        System.arraycopy(num1.arr, lowerLen, upperNum1.arr, 0, upperLen);
        System.arraycopy(num2.arr, lowerLen, upperNum2.arr, 0, upperLen);
    }

    /**
     * Pad 'zeros' no of zeros in front of Num a, and
     * updating the length return new paddedNum
     *
     * @param num   the Number required padding
     * @param zeros is the no of zeros to be padded
     * @return the padded number
     */
    private Num padZeros(Num num, int zeros) {
        // No padding needed
        if (zeros == 0) return num;

        Num paddedNum = new Num();
        int pLen = num.len + zeros; // NOTE: default number in java is ZERO* ;)
        paddedNum.arr = new long[pLen];
        int index = 0;

        // Copying each element from front until there is one in a.arr
        for (long element : num.arr) {
            paddedNum.arr[index++] = element;
        }
        // Appropriate Sign and length of paddedNum
        paddedNum.isNegative = num.isNegative;
        paddedNum.len = pLen;

        return paddedNum;
    }

    /**
     * Shifts digits of Number a with d digits to give a Number
     * equivalent to a with 'd' zeros behind.
     *
     * @param num The number to be shifted
     * @param e   the number of zeros to be appended
     * @return the output Num = a * 10^e
     */
    private Num shiftZeros(Num num, int e) {
        Num result = new Num();

        result.len = num.len + e; // NOTE: default number in java is ZERO* ;)
        result.arr = new long[result.len];
        result.isNegative = num.isNegative; // Appropriate sign

        int numLen = num.len;

        // Copying digits from a to out
        // assigned to index: d to (a.len + d - 1)
        System.arraycopy(num.arr, 0, result.arr, e, numLen);

        return result;
    }

    /**
     * POWER: using Divide-and-Conquer - Recursion
     * Returns a^n
     *
     * @param num the number as Num
     * @param e   the exponent in long
     * @return num^e
     * @throws ArithmeticException on undefined cases
     */
    public static Num power(Num num, long e) throws ArithmeticException {
        Num result = new Num();
        Num one = new Num(1);
        Num zero = new Num(0);
        Num minusOne = new Num(-1);

        //When num is zero
        if (num.compareTo(zero) == 0) {
            if (e <= 0) {
                throw new ArithmeticException("power(0," + e + ") is Undefined!");
            } else {
                return zero;
            }
        }
        // When power is ZERO

        if (e == 0) {
            return one;
        }
        // When power is Negative
        if (e < 0) {
            if (num.isNegative && num.compareTo(one) == 0) {
                if (e % 2 == 0) return one; // power(-1, negativeEven)
                else return minusOne; // power(-1, negativeOdd)
            }

            if (num.compareTo(one) == 0) return one; // power(1,anyInteger)

            if (num.compareTo(zero) > 0 && num.isNegative) {
                if (e % 2 == 0) return zero; // power(negative, negativeEven)
                else return minusOne; // power(negative, negativeOdd)
                //Note: Assuming num is non negative
            }

            if (num.compareTo(zero) > 0) return zero; // power(positive,negative)

        }

        // When power is Positive
        if (e > 0) {

            if (num.compareTo(one) == 0 && num.isNegative) { // a = -1
                if (e % 2 == 0) return one; // power(-1, positiveEven)
                else return minusOne; // power(-1,positiveOdd)
            }

            if (num.compareTo(one) == 0) return one; // power(1,positive)

            // When a is Negative
            if (num.compareTo(zero) > 0 && num.isNegative) {
                if (e % 2 == 1) result.isNegative = true; // power(negative, positiveOdd)
                result = result.simplePower(num, e);
                return result;
            }

            // When a is Positive
            else return result.simplePower(num, e); // power(positive, positive)
        }
        return result;
    }

    /**
     * Power Helper method: a != {0, 1, -1} and n is positive
     *
     * @return returns power(num,e) = num^e
     */
    private Num simplePower(Num num, long e) {

        if (e == 0) return new Num(1); // a^0 = 1

        if (e == 1) return num;

        Num halfPower = simplePower(num, e / 2); // saving (4 - 1) recursive calls

        // When n is Positive and EVEN
        if (e % 2 == 0) {
            return product(halfPower, halfPower);
        } else {
            // When n is Positive and ODD
            Num temp = product(halfPower, halfPower);
            return product(num, temp);
        }
    }

    /**
     * Division using Binary Search method
     *
     * @param num1 the dividend
     * @param num2 the divisor
     * @return the integer quotient
     * @throws ArithmeticException when exception arise.
     */
    public static Num divide(Num num1, Num num2) throws ArithmeticException {

        Num result = new Num();
        Num zero = new Num(0);
        Num one = new Num(1);
        Num minusOne = new Num(-1);
        Num two = new Num(2);

        // Division by ZERO
        if (num2.compareTo(zero) == 0)
            return null;

        // Numerator is ZERO
        if (num1.compareTo(zero) == 0)
            return zero;

        // Denominator is one/ minusOne
        if (num2.compareTo(one) == 0) {
            // assigning correct sign to 'out'
            result.isNegative = (num2.isNegative) != num1.isNegative;

            result.len = num1.len;
            result.arr = new long[result.len]; // Copying a to out
            System.arraycopy(num1.arr, 0, result.arr, 0, num1.len);
            return result;
        }

        // When division is by 2
        if (num2.compareTo(two) == 0) {
            result.isNegative = (num2.isNegative) != num1.isNegative;
            result = num1.by2();
            return result;
        }

        // |a| < |b|, causing division either ZERO or minusOne
        if (num1.compareTo(num2) < 0) {
            if (num1.isNegative == num2.isNegative) return zero;
            else return minusOne;
        }

        // |a| == |b|
        if (num1.compareTo(num2) == 0) {
            if (num1.isNegative == num2.isNegative) return one;
            else return minusOne;
        }

        // Binary Search begins
        Num low = new Num(1);
        Num high = new Num();

        // Copying a to high
        // high.isNegative = a.isNegative; // DONT NEED THIS*
        high.len = num1.len;
        high.arr = new long[high.len];

        System.arraycopy(num1.arr, 0, high.arr, 0, num1.len);

        // mid = (low + high) / 2.
        Num mid = new Num();

        while (low.compareTo(high) < 0) {

            Num sum = result.unsignedAdd(low, high);
            mid = sum.by2();

            if (mid.compareTo(low) == 0)
                break; // When mid = low

            Num prod = result.scalarProduct(mid, num2);

            if (prod.compareTo(num1) == 0)
                break; // When mid*b = a

            else if (prod.compareTo(num1) > 0)
                high = mid;
            else
                low = mid;
        }

        // When a, b has different signs
        if (num1.isNegative != num2.isNegative)
            mid.isNegative = true;

        return mid;
    }

    /**
     * Returns modulo: num1 mod b
     *
     * @param num1 the first operand
     * @param b    the second operand
     * @return the modulo
     */

    public static Num mod(Num num1, Num b) throws ArithmeticException {
        Num zero = new Num(0);
        Num one = new Num(1);

        // When Undefined modulo operation
        if (num1.isNegative || b.isNegative || b.compareTo(zero) == 0)
            return null;

        // NOTE: b wouldn't be minusOne
        if (b.compareTo(one) == 0)
            return num1;

        Num quotient = divide(num1, b);
        Num product = product(quotient, b);

        Num result = subtract(num1, product);

        return result;
    }

    /**
     * Returns square root of num Num using Binary Search
     *
     * @param num the number
     * @return the square root
     * @throws ArithmeticException for undefined numbers
     */
    public static Num squareRoot(Num num) throws ArithmeticException {

        Num zero = new Num(0);
        Num one = new Num(1);

        // When num is NEGATIVE
        if (num.isNegative)
            return null;

        if (num.compareTo(zero) == 0)
            return zero; // When num is ZERO

        if (num.compareTo(one) == 0)
            return one; // When num is ONE

        Num low = zero;
        Num high = new Num();

        // Copying num to high
        high.isNegative = num.isNegative;
        high.len = num.len;
        high.arr = new long[high.len];

        System.arraycopy(num.arr, 0, high.arr, 0, num.len);

        Num mid = new Num();
        Num sum = new Num(0);

        // Quite similar to divide
        while (low.compareTo(high) < 0) {

            sum = add(low, high);
            mid = sum.by2();

            if (low.compareTo(mid) == 0)
                return mid;

            Num prod = product(mid, mid);

            if (prod.compareTo(num) == 0)
                return mid;

            else if (prod.compareTo(num) < 0)
                low = mid;
            else
                high = mid;
        }
        return mid;
    }

    /**
     * Compare "this" to "other": return +1 if this is greater, 0 if equal, -1 otherwise.
     * NOTE: Compares ONLY Magnitude of each number, NOT according to the sign.
     *
     * @param other the other number
     */
    public int compareTo(Num other) {

        // When lengths are unequal
        if (this.len != other.len) {
            return (this.len > other.len) ? 1 : -1;
        }
        // When same length, look for greatest highest significant digit
        else {
            int index = this.len - 1;
            while (index >= 0) {
                // Inequality check: this < other
                if (this.arr[index] != other.arr[index])
                    return (this.arr[index] < other.arr[index]) ? -1 : 1;
                index--;
            }
        }
        // When Equal Numbers
        return 0;
    }

    /**
     * Output using the format "base: elements of list"
     * For example, if base = 100, and the number stored corresponds to -10965,
     * then the output is "100: - [65, 9, 1]"
     */
    public void printList() {
        System.out.print(base() + ": ");

        if (this.isNegative) System.out.print("- ");
        System.out.print("[");
        int i = 0;
        while (i < this.len) {
            if (i == this.len - 1) System.out.print(this.arr[i]);
            else System.out.print(this.arr[i] + ", ");
            i++;
        }
        System.out.print("]");
        System.out.println();
    }

    /**
     * Returns this number in DECIMAL
     */
    public String toString() {

        // To get the number in base()
        this.arr = convertToOriginalBase();
        this.len = this.arr.length;

        StringBuilder sb = new StringBuilder();
        long temp, countDigits = 0, maxNo = base - 1, append0 = 0;

        if (isNegative) sb.append("-");

        // Append zeros if required to each 'digit'
        for (int i = len - 1; i >= 0; i--) {
            countDigits = 0;
            if (i == len - 1) {
                sb.append(arr[i]);
                continue;
            }

            // Counting no of digits(base=10) in each digit(our base)
            temp = arr[i];
            while (temp < maxNo && temp > 0) {
                temp = temp / 10;
                countDigits++;
            }
            append0 = digits - countDigits;

            // Actually appending the zeros
            while (append0 > 0) {
                sb.append("0");
                append0--;
            }
            sb.append(arr[i]);
        }

        return sb.toString();
    }

    // Returns base
    public long base() {
        return base;
    }

    // Return number equal to "this" number, in base = newBase
    public Num convertBase(int newBase) {
        Num nBase = new Num(newBase);
        Num zero = new Num(0);

        Num result = new Num();
        result.isNegative = this.isNegative;

        // len1 = this.len = log_thisBase (N) = log(N)/log(thisBase)
        // len2 = log_newBase (N) = log(N)/log(newBase)
        // So, len2 = len1 * (log_newBase (thisBase)) = len1 * log(thisBase)/ log(newBase)

        double lgBase1 = Math.log10(this.base);
        double lgBase2 = Math.log10(newBase);

        double l2 = (lgBase1 / lgBase2) * this.len;
        result.len = (int) l2 + 1; // Safe side + 1, will trimZeros later

        result.arr = new long[result.len];

        // Copying this number to numCopy
        Num numCopy = new Num();
        numCopy.len = this.len;
        numCopy.arr = new long[this.len];
        System.arraycopy(this.arr, 0, numCopy.arr, 0, this.len);

        int index = 0;

        // Computing each digit using mod-divide methods
        while (numCopy.compareTo(zero) > 0) {
            Num remainder = mod(numCopy, nBase);
            String remStr = remainder.toString();
            result.arr[index++] = Long.parseLong(remStr);
            Num quotient = divide(numCopy, nBase);
            numCopy = quotient;
        }

        result = zero.trimZeros(result);
        result.base = newBase;
        return result;
    }

    /**
     * Convert the base of Num to Num.base (if it was static)
     *
     * @return the newly converted Num
     */
    private long[] convertToOriginalBase() {

        Num num = new Num(this.arr[len - 1]);
        Num base = new Num(this.base());

        // When the base is same
        if (num.base == this.base)
            return this.arr;

        // using Horner's method to
        // convert from this.base() to our original base()
        for (int i = len - 2; i >= 0; i--) {
            num = product(num, base);
            num = add(num, new Num(arr[i]));
        }

        this.base = num.base;
        return num.arr;
    }

    /**
     * Divide by 2, for using in binary search
     *
     * @return a new Num half of calling Num
     */
    public Num by2() {
        Num half = new Num();
        long carry = 0;
        Num two = new Num(2);
        Num zero = new Num(0);

        if (this.compareTo(two) < 0)
            return zero;

        // When the most significant digit = 1, the half.len = this.len - 1
        if (this.arr[len - 1] == 1) {
            half.len = this.len - 1;
            carry = 1; // Planning to start from index = half.len - 1 = this.len - 2, WITH a carry
        }
        // When most significant digit > 1
        else half.len = this.len;

        half.isNegative = this.isNegative;
        half.arr = new long[half.len];

        // Assigning correct digits of half.arr from index = most to least significant
        int index = half.len - 1;

        while (index >= 0) {
            long sum = 0; // to store the proper digit to be halved.

            // When there exits a carry
            if (carry == 1) {
                sum = this.arr[index] + base;
            } else {
                sum = this.arr[index];
            }

            half.arr[index--] = sum / 2; // the proper halved digit
            carry = (sum % 2 == 1) ? 1 : 0;
        }
        return half;
    }

    // Evaluate an expression in postfix and return resulting number
    // Each string is one of: "*", "+", "-", "/", "%", "^", "0", or
    // a number: [1-9][0-9]*. There is no unary minus operator.
    public static Num evaluatePostfix(String[] expr) {

        Num result = new Num();
        if (expr.length == 0) return null;

        Stack<String> operand = new Stack<>();
        Set<String> uniqueOperators = new HashSet<>();
        String num1, num2;
        int expLenIdx = 0;

        uniqueOperators.add("+");
        uniqueOperators.add("-");
        uniqueOperators.add("*");
        uniqueOperators.add("/");
        uniqueOperators.add("^");
        uniqueOperators.add("%");

        while (expLenIdx < expr.length) {
            if (!uniqueOperators.contains(expr[expLenIdx]))
                operand.push(expr[expLenIdx]);

            else {
                num1 = operand.pop();
                num2 = operand.pop();
                operand.push(result.evaluate(num1, num2, expr[expLenIdx]).toString());
            }
            expLenIdx++;
        }
        //System.out.println(operand.peek());
        result = new Num(operand.pop());
        return result;
    }

    // Evaluate an expression in infix and return resulting number
    // Each string is one of: "*", "+", "-", "/", "%", "^", "(", ")", "0", or
    // a number: [1-9][0-9]*. There is no unary minus operator.
    public static Num evaluateExp(String expression) {

        Num result = new Num();

        Stack<String> operator = new Stack<>(); // Stack for operators
        Stack<String> operand = new Stack<>(); // Stack for operands

        String operand1 = null; // first Number
        String operand2 = null; // second Number
        String operation = null; // operator

        // Set<String > uniqueOperators=new HashSet<String>();
        // HashMap to store <operator, priority> pairs
        Map<String, Integer> uniqueOperators = new HashMap<>();
        uniqueOperators.put("+", 3);
        uniqueOperators.put("-", 3);
        uniqueOperators.put("*", 2);
        uniqueOperators.put("/", 2);
        uniqueOperators.put("^", 1);
        uniqueOperators.put("%", 2);
        uniqueOperators.put("(", 4);
        uniqueOperators.put(")", 4);

        expression = expression.replaceAll(" ", "");

        ArrayList<String> expr = new ArrayList<>();
        int i = 0;
        char[] exp = expression.toCharArray();
        while (i < exp.length) {
            if (uniqueOperators.containsKey(String.valueOf(exp[i])) || exp[i] == '(' || exp[i] == ')') {
                expr.add(String.valueOf(exp[i++]));
            } else {
                StringBuilder tempSB = new StringBuilder();
                while (i < exp.length && !(uniqueOperators.containsKey(String.valueOf(exp[i]))
                        || exp[i] == '(' || exp[i] == ')')) {
                    tempSB.append(exp[i++]);
                }
                expr.add(tempSB.toString());
            }
        }
        i = 0;
        // Iterating through given input: String[] as expression
        while (i < expr.size()) {
            // When we get a number -> pushing it to the operand stack
            if (!uniqueOperators.containsKey(expr.get(i)))
                operand.push(expr.get(i));

                // When it's a closing parenthesis pop(or evaluate) until we reach opening parenthesis
            else if (expr.get(i).equals(")")) {

                while (!operator.isEmpty() && !operator.peek().equals("(")) {
                    operand1 = operand.pop();
                    operand2 = operand.pop();
                    operation = operator.pop();
                    operand.push(result.evaluate(operand1, operand2, operation).toString());
                }
                operator.pop();
            } else {
                // When operator stack is empty, i.e nothing remains to evaluate -> push(operator)
                if (operator.isEmpty() ||
                        expr.get(i).equals("(") ||
                        uniqueOperators.get(expr.get(i)) < uniqueOperators.get(operator.peek()))  // When the priority  of the new operator is more than operator.peek -> push(operator)
                    operator.push(expr.get(i));

                    // else pop until we get lesser
                else {
                    while (!operator.isEmpty() &&
                            uniqueOperators.get(operator.peek()) <= uniqueOperators.get(expr.get(i))) {
                        operand1 = operand.pop();
                        operand2 = operand.pop();
                        operation = operator.pop();
                        operand.push(result.evaluate(operand1, operand2, operation).toString());
                    }
                    operator.push(expr.get(i));
                }
            }
            i++;
        }
        while (!operator.isEmpty()) {
            operand1 = operand.pop();
            operand2 = operand.pop();
            operation = operator.pop();
            operand.push(result.evaluate(operand1, operand2, operation).toString());
        }

        result = new Num(operand.pop());
        return result;
    }

    /*
     * Perform the required Arithmetic operations operand1-operator-operand2
     * And operator must be binary, like +,-,*,/,%,^
     */
    private Num evaluate(String operand1, String operand2, String operator) {

        Num result = null, num1, num2 = null;
        long num3 = 0;

        if (operator.equals("^")) {
            num1 = new Num(operand2);
            num3 = Long.parseLong(operand1);
        } else {
            num1 = new Num(operand1);
            num2 = new Num(operand2);
        }

        switch (operator) {
            case "+":
                result = add(num2, num1);
                break;
            case "-":
                result = subtract(num2, num1);
                break;
            case "*":
                result = product(num2, num1);
                break;
            case "/":
                result = divide(num2, num1);
                break;
            case "^":
                result = power(num1, num3);
                break;
            case "%":
                result = mod(num2, num1);
                break;
        }

        return result;
    }

    public static void main(String[] args) {
        Num x = new Num("37082461354993496737406781501224794023294786904491506442652762570704013970102913557558274581981035181659515021255283490818930764270315070649158271310557463507167805162236800349193017225094855488191883209112057478471913659555419220190686782401072330647607828817854513961339395054814867683462487553988821998364230030870079802721873444342496150476056156337780052939689860675751202433832396735739326816141386254668867960317658937511956799536936068846505528329411865711017076488367450471713387305859538681166461211037014489001457764899580498799147393605498938036450779981630272625746211343878621035489705771728104643747936633981350518723564818807501940841030485264126343295101185207696234109769634464354237274416515259037342112301638923297366840026989469822671591504722385600368746364184168661372185615620667647035734071773822093171454686796521781912974848571606816250883949591403543442612630954194669143967743748032035627731482953400322005413592171385193291238006022638443145845759607505042273426003907039442580129906024986275458339587262766326157845132672680089833724365459338116715871295607731755149197031209690647340650960565087204944538594927195100281022453688852064272611500214637006099443586803401483200665535340913118908998689642093292563286953053471974755201420755637429738547612968182799036829564151072056371100155967394660466404353026387978516234978317334361498663544476862682372438511044370818468332713324625290169011895117495406789120230358007078384724106013319061168954620310473706978486161617063362906340401163582608251249591374719776298244799822365706556443853172436648548981155408571657821672971174064380330208158471365405150901528636986653608262634750330388214266960392758367600965306424151361780759402241781907338332957873790917798473060874627990384924229844401876471249618876927878877653508525820763079767357838090626131833197232985271887489426493105730095897877385232195933643138652336112649334101485595356826199271131311098900610186100103534534851361680519396485106004837078397184");
//        Num x = new Num("3000");
        System.out.print("Length: " + x.len + " isNegative: " + x.isNegative + " x: \t");
        x.printList();
        System.out.println(x);
        System.out.println();

        Num y = new Num("774126011101378011753825226770413322200310840565044607271408248939705851813308143895737712585540022153030784666629479629671141932075035690388120760274847766844895311013289551777860558186964666295330185606100035942580715265029824631051535546668277547955647825155260390576445669717413507392449218428637406873297613411260202529283261229274130176677834126274155754574168388508021114101567014366020404101178870491828807377302744506266309395530262984329134092800092572481229233100153744066000477638316263534377532090525841595696677242895150625845156825008912107092463707472325627494978897137538353179631101321545745447610368117840946019385545054872185823663299155940497672933159284366154743749751457957620201480064622088858328481786230012925666232381680101502698471464678227026128396231070465293974330848");
//        Num y = new Num(800);
        System.out.print("Length: " + y.len + " isNegative: " + y.isNegative + " y: \t");
        y.printList();
        System.out.println(y);
        System.out.println();

        Num z = Num.add(x, y);
        System.out.print("Length: " + z.len + " isNegative: " + z.isNegative + " x + y: \t");
        z.printList();
        System.out.println(z);
        System.out.println();

        z = Num.subtract(x, y);
        System.out.print("Length: " + z.len + " isNegative: " + z.isNegative + " x - y: \t");
        z.printList();
        System.out.println(z);
        System.out.println();

        z = Num.product(x, y);
        System.out.print("Length: " + z.len + " isNegative: " + z.isNegative + " x * y: \t");
        z.printList();
        System.out.println(z);
        System.out.println();

        z = Num.divide(x, y);
        System.out.print("Length: " + z.len + " isNegative: " + z.isNegative + " x / y: \t");
        z.printList();
        System.out.println(z);
        System.out.println();

        z = Num.power(x, 3);
        System.out.print("Length: " + z.len + " isNegative: " + z.isNegative + " x ^ 3: \t");
        z.printList();
        System.out.println(z);
        System.out.println();

        z = Num.squareRoot(x);
        System.out.print("Length: " + z.len + " isNegative: " + z.isNegative + " sqrt(x): \t");
        z.printList();
        System.out.println(z);
        System.out.println();

        z = Num.mod(x, y);
        System.out.print("Length: " + z.len + " isNegative: " + z.isNegative + " x % y: \t");
        z.printList();
        System.out.println(z);
        System.out.println();

        z = x.convertBase(12);
        System.out.print("Length: " + z.len + " isNegative: " + z.isNegative + " convertBase(12): \t");
        z.printList();
        System.out.println(z.base);
        System.out.println(z);

        String strIn = "( ( 98765432109876543210987654321 + 5432109876543210987654321 * 345678901234567890123456789012 ) * 246801357924680135792468013579 + 12345678910111213141516171819202122 * ( 191817161514131211109876543210 - 13579 * 24680 ) ) * 7896543 + 157984320";
        Num inFix = Num.evaluateExp(strIn);
        System.out.println("Infix evaluation: \t" + inFix);
        inFix.printList();

        //String[] strPost = {"63","53","-","10","+" };
        //String[] strPost = {"9","5","3","*","+","2","*","1","8","6","4","*","-","*","+","7","*","1","+"};
        String[] strPost = {"98765432109876543210987654321", "5432109876543210987654321", "345678901234567890123456789012", "*", "+", "246801357924680135792468013579", "*", "12345678910111213141516171819202122", "191817161514131211109876543210", "13579", "24680", "*", "-", "*", "+", "7896543", "*", "157984320", "+"};
        System.out.println("Postfix evaluation: \t" + Num.evaluatePostfix(strPost).toString());

//        Num test = new Num ("3727640477087217810300483049622606327914378468665807564626675120034372233462717080985039873199271949794205954639021269434029801876379530229498064912634351622100323910172807013534844835181777388312097756106414492636072630423470346056703563291269899679186634058472093693686760838330731921395845929346051398332612852560785759651212527473493997994194359834815651743192159129379131254748298219082030722667669724131403270910890277730375677818219712121851869204740085547771640811868259419767707510089321641444142996854213750198520401592754964036054542476285449925586109335740615631209545283656632117662413339989186198422306726369682453629123952701969275806375238859206480146862650082842493153603297786390269796251046071779852455929738902495695068623667636531569680007042702746846514041678833060321715551874036234760888040976120532852329059760648271060235301534246821092921372523458515826089759646566821356862487585228341976928362663996633158848122205192742700525900210518138286280827671027502277457649065351125592167891454296040203758738272181350113047922784524395943260143413745034270822519302606444068749213335707093676093681775559815885381104694873138997022650720946147018184642860215033086907797725430522922239349423582544205083166916184764055165574623780894071636636484162994693220684228033782482764477199383257733784134285961256660790876506335090470974847118082682876697444426027170898557088499411025827616205504712215153120229964786180257887122541854110096587060721821070245916293904811953097531820929988101017145375191193082310886405449941932978012012112924973435191024503687450734022702204941955474549151438304734185787751267833384761807865657584770151186956377690166454945144341261404917893675409330444514858059734071370497793083113482771902198354953970532592345996579378811438371222895797896858567228698079149309915201511744434064766335357896760236499074969010478449454116801896017572574407389844063127491566757361585955223832529537071532146604598615992333458687322304415008834456843923880063810726041573774214238640018166401950902412750452740862601979908180192255669252987820720906266140856107148533735643525900727279338065874047206935624385970115482201187315620997637976096331574596213910452610164434997620008623613742958060374558238902977172407486757443185486491690312244078364458059492928862417908761819384248172288202477853382973548198168464198147237481419383959875445049953984043371311457997550503326652643439145506113109198489974730370190136998481791529694254300895607753805447498034403701999492133171954455624014581312738372835539978614312565649601931317143698382955395324780222259543107719687555254218355760548402362887211541802147758643752854222337778914257606950324175953268067089693593089062154214512598757774633369119485490745696406162957620042617063145686050624770023192010711854958194484776263826477548305170460827844789240700191203993970604461413554886428087275206492099837997779841133591009999230823177479456063914309405151905018179887195888080627151185076308188513079109634476709799721148310353846133953334086386888248877778637773563539882017769313276049213359765879136836566905644435306138663738573783192348350238464628299210613148836629368582194671351795511258302523399500298712701567554880708082484106508450521207660334285928807671369022054748006393785598998987315594759897457592440654201350433934185275698730108650861071228646760158443982219210313001797007656097281392563026243823590327157316070410742093732790520388954049214567515588089102227755771945584340276611681502716347782049218519842716619326864025810139373641064021480802722856330717723691767981328879081123360621044046016132184424159425945705793031695715434563407847633411754850602878260899591484535910523062906127564217153884785675395558407292797487313248239186494418090631591363188276151287958971452958242638788722691767414590625896890459317256324847163758321785121408749655150224482856286899416728745973116045852530473589631542687758885012232445072498372599475865265339100851123081350979315980454655835529409047324513491617915332665117506444429011535352067610001027958988076920587235224750502753222871783846917007457974536051555788612674210931388780866480733444089670245122213175491971249355181475143440522763149232356289166964934071169210314034174826840150578787536349612660461320782742762574589259500725509320190629540791525252303531580833584685545468257574222963361898125532525715414539313244319486523865241923789202580536584441002310258588111780770062276168442337560111658217030893234745147974416065015021069900930556647222483900672598758777482640241072420965083069527696679439793488270487838039216468551073324311300791305805923692137705257630470735362104653665374480897478090970563600478341054958285144084822599544103825838731581297228281491395048932560672764778516966883454585854259429371503653632049044636264225705926383007085766216757743795613471420135458933281607877502900404448127073338623861135483097978145213934181371580177128567270684037671740105166235055066815261389424191045041088309445015009406961018895722837587847224995220908509990740541759211015673098049625740691444806621655256868606458067616050180686418571746510905116386716163729962912008151754141923095809188652060470572163509997411402123720007491350148058209159122416492512379375247928287875586555430154241365628705935625016271493437676169260323090570633401305052884365042484365904386128926698873442539222724517115722743979990790317711250981903133837765342286022904708455438788434059106177091285063068453974962711732395244267138273754693038060113656349506950745348636010876199204012103022266712218910651633534458881199971607909522106690269653968433725981856790418523183421187276466591014054510322536538631341138037130573702131631478155307149459010682325281999245456315895928638358548326225767485470452029822983033891776027268359878164452633125058810740892646838138728194945793294521539887228694992805722288478409483002982881257058395301583629205597425601671682733665858885714117862844957714658967964977129020868106626832381427385992787529931910962937313878942070235041918184120078829647340745783496655343262617493812879320316334633569912386161563877572398085400043381316538256723903036689821442463381700870587944667803858901975249735112387827823967814621140048078706206255826538660527642133692768843815044666367417280419919548825995392214415703556750071314226962276535998654924146492571993103508517429049349148583534241387835958254326456707605227726863755102599114926133291037174456281752491152893017651605977756188886292853785288130514793108209651402996168377223780987094708726476287219208040794417032714590079888077104849597764058565410810220947029614318954861300666952741589151114439397446602108962190940808906384696552958280854143236043643818419897271951429606857748906612098898065762642202678541643830837261109576134825974688765177073436733673256695908934276900956594369789205189195894517769605543231014339659976968652253943783887417601534180831562055271087803763742351262627091869375476929132756060717525040247441544417085805655813451883549096288671129747238103464228921767157918236240996152060290757527160999410489250351415117529682220192121124318701284472348124401295323398084278210041824689875103400624876405743349393563951808548649964381172502107982209385905608125182636783243936665898641422118944783272927833462600801592602700713998439130562752960166479150870484691853094015808475717850799719615720721662274978999889963971184287300529281829257386932633684096117677682928935805356278185660824553734447320732848738622357280706669465481847717852287913251774877929186869744336511688289715209134186941078440045112138638365740927241778210515188099740369304559456566220854399768525729393065019405293917828586721210547996555602214778617232443827410578457297304210616446664661157330828594847187811079832708585701027254193752449904680989496348356938286058751896977664556914375008898594519370445576528157046497638537467312833479123736108032363106756949435202185980928589865072136696633813217021989935816833506086332522768383147280611766481095389697673860804779835332245184111358420653111785752308483342132534631628473195932147388124061772365583803594252452710080382223173873675961194293666666135282965306087831012035144077544642530623402793697805176084961040390202120068840058605360898586113107109884820242951067021992734251487805644352580300986155181417150545499631380258171013089708846715584200029690188892660912719104175317154120605449549061514323048916421028747561692196308125552265029930792149416853129304847427273075060539223374220774365778797416285248684638939374458363388822092751420306513139362989543511905454166743511110607607789666854988681036407285713752726671126545887704802725693561636707092030395502550993799993097037959194051236032358685522690280843820507299004970782534724545143786379053821831015117366874854500232685970699776324803335010691703559441419740187154102213698751626042833618473158594368949023698436663746459228576904184425335879044427096370970429444610421996706925759774546031706504562141777458412392772528270171873002519032042015044273426215479812499313970790730276833722613646445359574482737870481713699865155061752069553647216909549089151787573977737876975148580480653416449894657182851096767788804815803434555940822174343922169536607937132319344280049879319335971694438947024332990146472984064266131033189581962051591368874823035181212325689441097024110786570060974815597042622471006101845227821402167893481542163955335008671971415060070371855813143054978858046434299137730905923945795144625910608922916210145007923070210678946290960490757276491908046041727498376405335022438217492469455085648355239352213272404808970221171919201472939547169731360215503784241183993760917491701187435629354503125387425777328183153039719448351868080002838601641169701028898674443841294990802378509206273533740174309727018165743152292686406613547444184359503003377567782412461401572779683936837300353002136642014868925060336758068956296744433740024811828348467756833182595892100117699183409812747964321085914157274322006632639437455119944835628945453788179542750126053446677672879913620574518895079789858274858638225231440036340999482735826863172834940408707330770482853064424038007628640129170344660635764540143046365535467760265565521640095954807590535647618389216118408173477525419545687627763468823827328916586395304995153123306610232402211298566151087041354392532044846666478454513777056518073629632571172322007203240818462587313798042034349227332527192228466392263605747333880361046094830002205931065644611494669371588833847837572340381216034925981340240576951702270009724745654873436325624183329391710854451979284586920171256941875697084307414921190604409485373405669377292153110364851224273986512315422886936882116143101836458233399965831702887706014596291006657630980974912754890032949833819914833564594738442797824500870792926998318597934321877408202063389733714333324909005692715052154414382958134159819248941828974895812790499899245257569494889096191157858042868950438866759891868241642661530428012392079350501832018354848746046304183241988427043407580529829978152617965708092701343879355332865036908665954174941550733431996235156997481012252780905980434930158666405501869861300227791757529102429682015227501780058350407513256863518386072616313734446398512914908882424829362864431187052578921013409615083953459388854737008407088890076496439098900401585411734705780950614427441483197153626183552009243979847388952532211644407169187547120938450240535644125828539911422562234377223358703442069344355148611421272314206442151761657629601897355817034844685388676494068301898597350969385495837865678975786289548326805570669081724728473378314447013987146067093646080558571039740840238801637891488518501903070617350865589081694457473666255825651614583206038043397231450306869271467411623932603165893377397963270909322444133331775553415046468004923690780177636185283650423351169619280264413731657969159218366093949600388556572858529257329455426222029896951971356567750145280431188679127021521130062717405597795946337235283778583359744534111167842414996153135343091469750785150974550197803395964202032199582398714805210300110471869668141599251898495719894956176791054578792082445267676330174624236143142399102864767127536252744429156440742422037113285350276674451593892963313660948886914403533170215128843031180391319240831221720958504029964981301037513191002727922780983057734908978360579277796227981544763957739264822658526582066369687646090070372425135227141593784158393651902011977139253705842437474232998757434772571365352284594794832365951671376789246216974539528993796640651428622363961451054680550258743565901126308368110536176659756778988961189021536978385477833695193558545402623959818580370554824185813647266344543347529531524074313478273036098561936795107723120691530299300886060498922230206952322928083063548194592347786425411618238661586897232489114142787079390999834566147027004302125441062575267280868060540545459962269873828508244046117227878065136475044080255574344078710775101536270044668069227219758381544229155546527456535877369918905344172128613752645279981845775090702487020417743355559638220450289974118345726913642074117984737334568348073051238239803015105821519274645808376270965183781957931204473630618727438898669638494879899705451412972595546835532840721090214858887985153400885471426309369845213404411687541575039995826719373807448915031697734676193585769220212824806922597519963790145948829989710835547194028741358117221150828274707298427324936498062438311053696533784758314285137515502408245909625813299236856729607287109019688017468376884972785595846495367081632194886372234606326389095597632517171240361765400778884134354970257689237921065673819958980957841323830386167838918482650863879860922429198111196131984829674260105147144994003693304951731794288773932621321533562401499727970162147060741604399081920398727869410891885428114765589163317255785098279040783247610924640149012424980923583939944022817953544987611113862613231111734203215113729520161231439150575937249997601570272847260106437382957015378029131414484838837605329010292802499291802691846192426299383449539143767733047367370843683692839171416454768803035041692939765513767194647159132323059703112334729899268130987829794434142949739385748849129443804913421444803867206286208178728270208539006167601997237074613416464116770011447255067795650659942386822623653359616278425440313765336351214424512408228916205162168415567240444330872212484582192155244932795697059868371344087038565668145458939652412093037211157257503382357330349402639084713452340194363880767409527266969134815190534758866159122189291871458128026946110811410491299658510365247633548944771295208523848694746323593541446102181705105779901094987207084799117349828879518111774759252403496293937730824927439645855092917735296975750638454063450525933414030962303798570644310546697358437057026575578540067162139307948395361829163479663593540133521742620609196849907761329695733489671815609648823125788642255381932204983396526907268226239332719397413494184941113579756897934542168170380455335943534358815442779422479219532930063257372791793114069361808850160473952573578716755543222828081952766237689615392283533827488771998920974979188350521583774010809897645080609479084725970110165371448257842049940129005668476558408825368452195241776555291865404116398581206701186108770995480454380813743471768084065786994416417715700194557916647285640596943680298369928639759915561669780101313978330496330136596777676308542420478692805889947552540242952489403953303868415423703928503137380375818690421684029414176453661330017635708203200569742808814216228505125657110685631491909826118568813782920311512429905125661517836083536685820991759401962108803447490301328457137425834105326432004601233271368953716393992003713630325539050851850110962980879562356896353698564431979600805525754970985375864709678315544073913232237466198087078101129595773204656172922146190151696972776864252028919529116096477222053682943671171060291229526834791582623393993155938278890999826533295263357252166086379234783582936300382800367490743635044411158295924513986847439868060706757973354295838658587076258750721303692430868419269133518657408818248598622616331222863038256514676844586627936809729979403789007218428297962748519623280361837567183314865769282041194248437621352257231663495296599273345016653273111256886904372713870584495435463384376944979542583081538289994996472448487412817654570473934149078095521785953797254152235882610801724162886248240234694202518553603116183026360670795288423374479132546372040882231758979588739489807457346404867234941278200237275529143004077914971587472179628428741766296566403156886627907137731184517857492015232744560551113676556827687191924837974804763644899820124219205862847327256588499498408889382701814364487266603209786254372125247204551574626008333054376446570350873297021794799189846865119033201101454230610668529751560753994839701066860886248441961817122543019936630041146883099437847812878449419634673999942274612789814110911713492997713801733252144467837603289790467570994399647299786215829110660973696040695230382602994067456125949004563076376219432365985070083303154096042920759430528460656564493839259144467636047362495859066023181941221223779345860982736328872197087515650107794818369659070629719539981614518006743540362699728558425646633447860890461493867250306978491297623884799441105375349214153433249971240622917699307221391471884773349234872851118589615830417274987386773021608498680314924569086795722975715592287051079437559060615027833753701581578090215507018017870875860947189173688376174098907036494243508970407259215540750676063133063337599463582752423622755651707048683473182977573305667853448396916414921050813517841708747620449670531347153277597243370025215588632405060582919387421864108357585956522637833097328843253627660130792273770621246579478175357002524279558412111749677128049827745446205966636108480169009862675450712563084171485587526361193174855681778500449740857450624541865602087053246633878640295833586030204744218463402044356683855817510732578641000550591770982123559803131606162470671488747859260178890108622032280082961576982617302910193386329685858304382797828169695215075268281681198218864170887005138310039745045141089909383037513831512202096525929452569141170233487640490229739112901211611022240691066561635895091578107656150676141730990604322502820392585320076899790149452077432609945262832341874101844887518494417509918892705992744180781381114930820340735926342195799043056107577156493417334415934795178257134708489410933276395497246390118047353532745130531070487446498276015825876293725589488288116165905404445704752322839042497949050702711071594298447648169337345985192062217189764998993198083311207372827576938720907558325796021688273518665482056001244414936216941077324475226045178745579259285838923147510008567246396653132321397207074660292142534558432901612469966565417460525124936742621939129455194789736220806637699339720579895151693951412474444773275889338366661971524849894224154735707781686983636994404613977889081154641873632273966298481675538236272753726374529603468600207341677445119420238767025033090205242835359354506366910502580760723157225077484322497140030950922893795931391624340294553931319513616300064902989578652032503360202743636555043094457847571090139791181457729331750087664437852363320243313990772366452145600737628105822756100648743767683865500169160769240829099019163754870467043281914519423753138946993043639096579273476813295828573707236544670377461229229010055535192044974572209740633348876294194633440961081513259152836862764928980366903872694989731794427145042921710889253337285825162561193063786404928285252865260843427743548724634535578333851097909754041438069922434096472369227297522123333073721426569741638868989267390317863989187824531544098562781390756457508159956898737460790272888338264588008102193139779348313410210736972425165632483598870681982917052712667463226962075818200718559152872534417230714802249023608938789202373655729642555589618019924658273210936788981582695574847239235211259521423742935728495744360978405932198494040785542757788517428551425645719309184208594900972695519429941876270516979185487014407418191727811905696314684704526922369928951908279855104859411032471783342574182380754867863370362352690573943704343456821546886510660019093569283025787247025257259058842068780432878895946463880454187182527163921825518941410327930837900005439585758799443220005030481377256265575754562024928959382921941629472498281717528831021872411225596493912666039419687927139414969868358292294428277923321301099119339927195473221343712893981757876310574570412042921463028075922807582535264045602331476705756384212724847734434656580851818775423940351439148328555377095218666351877930375774515432430364158914882641346502740385929945155401442371809308907494064377931233510077601215545201783510931068931391577525197917114273907030055212894838613867120480136635147001085649223274795446806854177013919911403587583662772639102442403757565961579588658060654163294746027172987595640645940835178522613864630626640386733091778615230058964380460309439898686754060183757335989261908979345373228129155482429243995399016719619679266653751630788061731090347862979874746067587418926592965771674509671520420290397142571244533035249008236587189309594599726019153595699902390889831796836120814353103494171619618749237731524939953563738553605216564700253790661338718078391160181405749990386445664951762584517063129694409714106821410593928255469137999693853346696799824113425997878508821672345713571691650356800959211228268548587346737360752313972810703183904318845656433634667749673971838793392200928078656376596324575278980786778331605806036268516819449503734904241210560512168381341790493788329458948657287424262474844608963587757920034400984085506135743586583244834334002326628693153883485163939249912226259840676155247860204172553581797959374374652470391467419717691857818755298465395790465084877014508374063911398529616174709823496846563112954527409982910924661913183316995183496927896118291330105813136864629363813184663867728667571889888597190534082235800779682425095187303629154216507206155580420487870773556831591457880673406578859304700732173123193062292283948422002120792459699472500109713054478207078223074645524048924064281551530516190727587923691602757130048355890648708782928721658196320584869028539538521134491941983505971938087587528072101218295358448420820564340228987140920766749204028277653811962334380366311902783967323812239357804918267371804060770571423072798554853556737468181531403893341568103676902018106304630763526502243531782610394277312273832760257960874647501003604677755082950617798686047900020966931549896170630681660942697256028753613746383474235246609958478883073549870309432429800185646167718528342567090455249343528432197677462943378536457149538151999514276431414306611021879580539020980695475095537423540363106871438911440965081067217432575189402137931550025736966078022541336476353385582002316439715220914694102044349792722968963208565786489174041417798625412495140971726132917551190730300229622642566202839991307982680430488008490067512935880925208017485406488346682742262854664779278837394311265494130003201094228100822349450194028313115921126274153975510662375733286924487522571794498101246673972554418257564723114269821644397369918285115632385203151682120011272996867308895500893748143376137476028188321526642218793888261420250034942669028577468986409657130667052592462219896519731451090836031555595020842989501976366185310473876489157882695241566867726366231058800215648497043310638775766020399597362448408122041488399877657519108242885313129157950153120130645133741102664110435628197861564827817967538618197581132897163794400686261627089431789647446373343105065513108783449622928600974872625509726753110846386641236614389152876286424353421996686097989001002688059343681437377264707397561004987926250213595245340101989271278126262460390332844147101089680378063452585204770618906145153043944254917397825753265463049935723864044967746397248052894910651511839613131730189912381747716478901119199886714816687105453962761070623002203253475130758662916470438137113229454044462231724427990632066385054419574975073519503889669676558121545453101659777191151063310424232617092159659286210532821464441259793608518291164230680045360594123230936371931771848798561120103549515739943850204902553124874844943502956517641206837327407165030159354715722496676176791785376405110748002810987368637535606170076937246355203657569034986240164704199799689257021996251434217077466941300024473656010199301331704063402101811871339787212443000654536697113024302210689557455283745575301564617840080333844282994251198243318722218640897011786240199811942060570123443890174572927732539030696489005936754597851263153052434196837982188530582320881648917345086883444764210598776131055950813193353019968624479659722228191623435779175523750771993575390956764109758710731351135833922902454215426979835112254741794596898142898118365528380193157372287694851440840505222358618916245767778516879821558123194020725254602966429936154245545150856668730725035280370847770977640482998910983761097404054750205859995212957347719387862861913340702021149974565521472908508532218457076445817518809995403299075723116408907498088789660348029469114766713724320716585845224695130173174959325694982209408140085138379681117354196922600813879502359085925872175255345238816095620042194255883408070310784573108907224126873633801937541273668342078932829214830295989836887170875882702241176859917548853406920624977086406333183291107186009413306997340905460194527318010776423271269946314689503537726251998835406183371641080783503867894557868819860513758077889859824401115476046527833623013541617856183950784023960424584149707936603928715367169848180460561665275668001479533755108605528947257378925219977750745073430773201850788323897070528501286057433545538264087108709332646475231607908401207728949710866714295924981615944692664065802509067619453679910691120631584545066636958446966138115682546622587628781023704809259319600702354661969028080175503443194871676189645924514201502701562585351313418020595407868702994239383394224686157420125860968355831535312630372003049027418372114858091593339781221054934716579193221531614117936784435534104366731838187145806218392487714209761416338340279773289578707426701695572065330084549756981664267872868775792266876173802809458867668469296856578078484814346389577647544389801652759801011858334758623364225373542923549432315825548843840561325427848080122446427315292838220198098324641924544158255972138766714372163431007465331129277000237956972340283351193637213889337913396385588261920732147041427368660856064283351609446203055189448838148723999701650017960286486344238259508804272582769873048949743264426691725874839811655437359065314571363989462548768232062858396152769349330063145055049903077922292329161632726610093607756696783892878637052770452904881783116038689517151439450246790974475753161610220318327270128069118427658852739553176437818323294991639795905357602642139038413057314911250734647720616619459950361546230444012756773367618639956972400644149674890318134310595235810854157728957596733366293836395136128658567340574270546822125699153296360853180678650576625789927760560846886690192185114476525929056503362873112593735714887293334926160522343593000400029947734925441122408757402676125789787841640478478936428190741701164075860717476545528941257495941902210540066410091757268995395271967522052121475981621829527807748335346560362625383742678112031806008581124325167063873723666855163200678350207438878929122140959170954109010141032173417469630745698756002647025245690107783237968139456009584324022761386684730191771538507314486270506037768139096824119056346832345848062181896155175454857241081545378783215949118093169596280367488342173674488131975094739796057128920077132935111771059078275759622961793864946481035385486225410398436990174177332912361780862779587303478366833439408186972991485643723694208784817242051628288007438262231053342797668951609942755897744489577041872291947465959863103142501143069350223400917912758336225402758106117486040075376362129643909105885232612818831191580755671984317797524729278772085912456563048168935878511506858779136409864686798316542493711488014106715905112617075941246475395155551146380198375931579724644598736430714292226175541818410562117319222047040562099880379382544570211774632389580364576298596180078710180445850980740636900298070366571050773863685685708165013992913206377788800568806223861628835100077928213188225409638458621835798930904281677224000066634604206876029372041781910873819991031534088556142139701389388404537196459732795204004098875715925145655814035353856787961220940708097693437215389192461854076986203585631955071611649450699760878268194590363837447641085689357861955451742224671634098546746299695278205162978066439336031279119487908582689908230362746106247979270389685196437479291308916142555364689916198698564914647729518481449151046018781057624239517032119729439577440228557900674932116634155660220504996801938944763750830528765330849000365706652753736197330684251301542784708641160401672813525481893300055687226231131567001528240453281538408832116710562834249453562249898033437199824067312344890086659663215916699017924239536676924079484418364225763139737266013140755298976780735188511194338649472081121733763117797414036440510361559193510980295882844858043633069362502307778010411729771507448601009187115179446057216992556521456209228026751060945409882433112580004202061884278629753146479769168001895880443781459507459991151842787696481629006195958464654005452023944501388050858956183911685740168566077921970793895892201948640500363242940730314604287636925103788816917849523410160388634209265478410846134584959894483131221117079785928743173282923632845988573592016469956304981039523646076302075707085623266685815664128945714659368073173295638660543737034689226903841275424928010984594181590496150956083699607380991369590883888062305190632213706903649887906130697028796505402071343941840807448985104639328976711367605333194908602344893498598367150743843422974643065843296685946046437818953001076699753568156745623610268153009234038010541981478241471750393559731554768990583776321480054415707706086410384480264948965717511721135585311247708161040719978469980813280565223204244230156597215035785592146618064752003582424404443213469983102145079054024470876795621700137417827993367421138027576848860053468250370146965311797445771188683534948736062809521602691372985634969134616121210936352166791080293360584457950502078185332326013479956146826813832943091714275588135183941181506428453087717518342016200970341747864626270210760173709377489599277009298332163945966483020452670626265496026849572211690926524922529382130487087767045194782490107751737417098715832302725674058484995863348107224540471608878222426050574136612740076690849258094148448387231958687221718482773436725818913754045461656607169162368192538224414390560050364990071330591036833245771053036124964638240740072763038826422115780469832418900958410933725891541422434260931662938022449676117589324010008030551125105862469099820205809614446225812380403525202452466976013207942985708822978873865518633160826688344563593433932949439473346624958875249286132834679687663239536041407764027602801125661988924671454561017273278533818958875343607561332443575162558169940215559361349906583247136649420197262225004153045274171424294138900509170809018499576457224288746966075372644966387388665155498659651198018330025928281056844638034086366315855363388327867382189715998518618710742272351596672776702981273840798027746570791757296926005488345872306414898061162791025412651260768443570076641204781081870901779494289519452459458574492964119984992593790203248944415627457719222637753957430891967713228106924004759304127357187369626131333829568047270277649301591994018327971712527657955002990247522727408311279321564731516817148531717147552924042541547585517477202534132086357703093906800739847120833821484576573429236834746200849100034733891798334791234893327583731355476774014284484917017150072836464586048874497889536648390775918770706490216840177055840475517241578960894540598286752059119392714995453620478000773235289438862112503187558658625017315906750897059030671861227890163934717957690388289242059799435625885670368003071586179786920996846658479931911830301293254884163640380688153222259872889052344080604791646248073152753100200608320787541377164402222687229502682505054253969609217995732312177990629714910353063236963916591853166155049088753265751285056007172997039490790996502587634059941757624836231527498521490130490342562101690893715624607415755744259053686273497857556409417762744130629951059705739870017525120164488102842583476180764552541879487323112656934106145398152350682157419323339700220269547126594650252361042598175420672208781483034802357245600773044908142539381933048990531092641207929474852123423346315707355309584263511039575604809630917795322933047571426729679536641729067260632546370115480834727197746081138920966020377118010844157447565278961059747407295363951241127082221251511818913007465186511310505951034258940620734466914338427810725434045329273506129634142365552216820121028956412161039297939916291739740211976035514800085641599937967828899415738166005367096872892767578080087717483346929549310666008804700868225264909035285975778186211913457454366329233042917852605244605319345108915271134386584968104781123721761883018599239464825074212082077925277000567097027676044249505269830859358682924073037630900578294096515579618414888431476981745280805207613863884876236666131239254649849820317268009494526016714847941335000084655920632786897520642625394453936745311115090310182264099311364858984623613629604108658066071708991503395135225001708826829027047301038222043000951528489236588365501078060931273273719454625632585679393726346574249304569528226645702553448363041548894903683689251747142184236032292059753473165861033287718819283947404847431225526781736573055343472775433772728989103664791561542634557530873600402943618829664563103004941138871743515889784627576343147397507190432120275319555587777465542000310804704106332914246800220223590512382539380164489979346616421730701784301793516911739623368275621994823334314325705946645851466239264675640306349659167083665362174272048160002690129475480256446463652978695078378367905763513474403977570027585061978389446562933069376735453079328739435147461332772755308467007682944081796022492268996925640054445133858935127209620196458561964486239905403591310202074471841475240829463996327924599158260151694187967298695564451843887177632891173477769293623874711139081797283584815605426621781029835873437400698961315857882344519995684218740330420483069232906097054102003871220660462969974546028237201536138716599911339323327873370372834824523157173768959476707098582077243472153068107635368866343250252977895948315105401233207508542979689293073807330046498413637597015643710516681509475066369728081574671815505476889268539362529712695755269699324194397483342620390078309327598214506496114466336141564674400015260193873819512582077368680243481188022620715871185694115158148387116750387286379813056117309770444726928036014046106994894716638906893175743211761864280947464715869029901203285359546152482765369507201700260024812760348671505185784782372828055604366663218655519591229182810820026969012995733249993511363934772440358461570138177369363443521126061925661354484957928317925588963740919451835098053730148669595933915187720788385288773189488081209879087227498036330430359045322850379426769462645293282131349138303155152172045219923219606178617617612644313111985699388423009982767857229789266035829918152835558878774481128648984750839632421126063843994724315008985226257085303742491225043661331682024586170899730244007603669659906638117548150628532975722543688913676995845092570256001391241033827921567851980571250572373561871812998885302756467957181813706510489564678471914684744252496478713597978710438127401840588118859227761909825455691596809042968723103836019886716950771564604208760236292919249964693045891377332514369377288898365992445821766923878969047150609892180519034518024303095987896474563195361743195259265074460622636636442849177963002499942699453680692636326057178783922848567695165321191097177362758965470471851776088180104911273178240877462953223247427706277429668024850952739218695012228187261572435963214494718427887300853838159821243709831128027171475639004634073139777393813854787929166802830793846039003908236565809119179380422050276470794408853211229943265740800446010219408714700796467798790414880070562439480307779315852800850823102904953286433532609382921270322638671648891731424618914928347205512916279771649784766356913841042839464781060517147773729195811464313096268623871972898024757557268095446054422817479168766270764363755843315893640180242157374275653521459034520352848909935495579792668418459140411672604447136832844563860526989072517923276465466744052927156667230016503525706872746662344476796633245601045243777181432565762120783535040106410432404124864373763857153853369268648949284659645562901616379068649109569716108752135886585581924840891791118044730127035105395991403390051825629428319211949516187654177051943186410557231362210104994438586149171481504556966560133974369540575194832101750419629320195065576129907505764343657776995357355375006162969255068916497578633371499658953488570057835068687591933539086635982249785725952001025636743204554345415472973041088103158698068316183655658865393268311397378490541803212669806377450738043720908931341795283222547449877309907107024005757312317979340039476382270422359906088598108428652099141707170913164868170365216572641202314939499044449293736766555072454234264024633758252049825085051787110142315556694779251459974288929628302493750551080780471340369997063743433591764294267131324284022401461886510226608485671869545981238480269348435378638775329203311381681899995532114747070261889036705906899630949086775685752843951786073110026257240068827297295224719155336987902709615317295891126505699728996484274068767212135548001382150718157496421079168252131940514888453093497278519685027219372111658051165619763879206048285206015558535032076306020607556954018176697254754344163829681133106387742913075139135517620172184232888692157492181599649758390650574243700678174862799670347278027410737434400493453420616429849308726007720381545047767781392134498666954948308633976515142343321023492446711872135425472762121432975736076087582952656278978268376788092195491149860393922362249868834309675857147634876558417341697693182842810566539216661217592833087941072749515662888231454377102410176717679277814429144252967247566664484097385527137350302458913161784767337234507025371553284470009429864184668109372370011029856793836844575266751460184023021546813324475499224907267719406783828139425544962581291772550790635185357659483711354913514117927557545390534388198395104211261320415765544138388348149061364049258357926246604856934107072217665655821468980922407234047324896973518237006761596930591900585624166678152884679876250383304978016915989161362316620399258988826938343582252873026844127005159338115957529950947006765879405836571206038442286161910195546799899948587016498388369426883836122569845934168492489612917857142911229784401442250554835091785138620444783278106482246703048990495036669861456260956973254393673416941322563526003593785443283333964970963065010489390782967194327786964905480819649566067234405755531736384197001025342411820353051676609572474393012480593791521303189119951363188765246311216964182784606179354824724186887609431112902786694587457224051718167033641084439405129226048788990143887102666164159361382504133630846501878890073310748131970531899837611170109256703010276505304361904916961978853043266592362529286912926655840724052271358574976622477633898307891683143051121378633705699241433873530942619744774115827160233177085288456639124026340006191866157161225212967454521431498165616799278969823868139286995053914509492240009134622055849101836755265328950313820814789229913079616317761311825530454906888490887524299154818449764500502142922516066411939827914779826539399712685660345118528489542669407020477760067927990596545643872412420956570333678871737559988669654244435046014695635756491322797151929889217062718095673064645505056928100561179053204263082995617648989534750049524701524914662608769462246037738234173795767912344090515860945203541499291245319052733414549876706495610437756727101011593280875666344926356234015944420477769499293337543237754518697402928583417930046279074307951298790571499223750117457435652275055720332777442179070834712049886370203808998815101798815611348893352117015514331682988401998401033331179670546242008872223078158279911199190153820149226900614899699741217569984587744056122556478203336223247198069161609973313420959066867066882384169177789465660417814958734564325700146328625935025361008470499575405680031601106881494096281511709691932645782927600713300812178635747524244821013750686267923826846409110189435342117452891927915271304532352428026337171424972838754506545019312529931334560189795409568287677705473308601825577123788255309202793664524126097435459192908083326440225318754787833939187143277485280852766288942953556583873423846755906025114957048325778386818385764653555833066876971486098321079477010822927327767388495382072654571309508390433696334853818702261624022009202363085658341987179209297250953742692189189478257347790132600873854758420171071605494126962607669428648110128412566431903879598653602841280693412242939270835084514649949290810832777005660103187636091718284266986874520490902006024246451558505778276476626534472940274750570624791180347621964328454957691383500844356117135790492983288863616817100502252606881813974554308097753009168892463878400162667548830778456199240176247550160337885391557907570914370440701959268911733258139882841670994952657470997658268828494601111015028065491246139595997100697080164540577423509960605610349750360829543263883074247531646920987331125512285136585114199431221026605414753719457905799395694143963306310876204261446142957447883104925153014675992022397510100435368981031545688329922578655074807923503117413101347539050097737025456222822690007365071756184400996639968552914127634170650405653881822898523020365107982674550837326453334784860553109108271242789664094735753537478937067551037627684316920728044474767967493576995979829210920524242063257262023497786517956180668986098178627755212795137014276863863845767759741843958581480869057795348861517838437726931558776217492436372545269737640741594407099252931628155810647138869083875114008518843193204686764105948231318583931051795068708133002032503812979470780904119123067164201300528583873885386496021909171451873555424304487851942502014861189254238127753907858371991223929002243784220017152739435639449749711650810145289666365874549923904284118376848368268419850369005294508692701023586129752839478837492467128378407616611085574737431622779544130025429841729478051822645362549685680797403306231697947803923793795018174425828395211668846935236982784918940531618453763482377346370448869290416304625464290640480799480712633748843305384129168975143399394712753162338937101048326523819959576281665608650125538018402233337628502779296957215049129440359465430348043336766385443547986763230757697009728865537329308393953727704484545204576873020969485925190111875743357792116025329642193748104045353708677965365207566594680555595506969969777196296626325912459743975278530763874396559643028763107559604981307621550143426535699564630739410525318595134383393324639963365178143918820686485439179476476683066210141619534617067802431570115828271862278861307163630989605904717994692805553532174745024143828997637783449782206994507692869318027641737973409284793302638773373775647101219716250339106757591393510098317901634900902242424219387102490432204539268033432878966100130432246149440077109692760577148628764547878128638584621358513466592480316104900246663261469497663122462373061136455959752694152801971498795666865201402347946037638221888202155741462234257351862273476499136544294699106924580864476828049060765402234922539957862532994630089408377073269816479074135007151945566382631569876589804196112798619111377539066298908554182508245667294302623087994619226986378906942095394838122720311610164566386409301719751190115735370098891391821745539298285795488545573263801077443533980769259136435352422074517520348594568292654446054326023214654509092152363846835066648328716236678105844146699467845909002034601188512652196027177419361801751488713340138856539684355041587926874974422162307664754063183112551513520579439317866036166372803281148486302246798320958336300997737882590647703130294010385862652685758831382464432432246570914215305628600084723165836397490023604833882654378495218290451313434879718918182786374003894376239129520203824389394818863339013814926661174585307988749945316758690877231879031858616067868009269541433665075328379003708885750097949100010886524855939466490390755950859060925354613776176834255208481210488428755216519248356486800579060880471900134285313587043154798172868870214041296756475819041115737585407905961631013856121495594145681677318272808802137067001030676413504844418565958954916906306085219646125563575528585558983454526943391998864031894763740984115481570149239243941851687865318927937596835601171602225193527788435916998485793020982440758927132005971662665217162583588367328961780316795397286039283872241989546953948917352226079879134956284650624770451968087514133813070391599818227273887926052071128395525914741262399751543094861779353891904419765012074225455378476460105338090712071024686376695722783638123063388056308048771912212424332672556972163828430513850331021783709404276616107551617185970030812235255872982301745929476116414776503798692941678307800189042738728427552015044449415067347067044571168171987394421090666460889998480611187193194012109877749557897522396903330763263548835706442327955264589964646204561562653250383047354098176363437941097146290637422554343697547057483821820172041898001560226069776449135546574357893420001465673354665640580167484534053629565686609979518555607470675243812878723761998941206197079252382814820627127406770932347249510005776685354987113068968866643410226229935635901826583154539513380876750552495524550691028798687706518534012242261859110873917639639262702849793161104151103586021514943643439399828445525866563210137292638520420037213470466312320866274547664538147723423289417608680652031812710273491105937063418127339606828379266609307479011810117386781282161307183762672183582101893721713235558940241654942938371774640396494318688837221369202093303964242235124418553833074674346224001273504388256240440254294956040319189815521015488119239685867130121977627809030794745959945795540809083448366676271857032024162114493669280857974546791025642041676332800685910199155446436323559225726260123814055932198335642678184011687750832949655750671233956300485719010292632435551800030497367830290344541502569331724633182508883393181788730044395269641457799134187130761293899718303376253272975647370322130415327280330100452742011490566884296662842502383539024525751465908625761831951367214971548853959781205850422388297065824359136909287458773476707760447699702010438873825795349334266046209951684234275384688021551062310482311672663380652507916583648294894647286135125880222458215470053197075003791048759604650780954506958852875691156431848663810254039100962627606314547316778752592887250062130166686194135804102331222015235996040802527397291234030747363763771290303217863969704207086545973022956975737791574344428177142305445295425326279166515678866937168221898442371118066319965397241973159589760296433009609538975813715451826853912743956390310866071111360120297108214284336741095778106938260435174645970821517549184000898024588794745127375703851517371435222802767339578730874810133705320283955557291111496646475788490718343758628539975592773543638449945817023011076655226435247797761133498361935094627460237525619089499835875291133296834326698417604109963822618787176316347161392316085632787963228118522730268758146492807312623340679893759788054554335823809584702000506420672562956931697794507047174587857112176285261007563680241969748754086824928531944973912317997776207085344947865559339870006705750483374377567167381591677320642584700272672452291991204636091721788172369483238835085628304905882976670618413361910517249963844691304079863876978228516543496205225116197414699974504305977694867657151898162691391475889441002060428217022881910047381832493167077144957430758679140627005244332039397779886748397656966927855117133349300492623892068923322385471767376531540239301403073149190355659159528326664872860619260813908079064849453353459523527510410015843215483289105520349508454193748773503038648901772235659019947204860102529235655521528984005767420618169383549386018032128851120924616194447932941182815870695711960511054026288221444197714362033544273489572886952643041969469513973119784913989843839770399695860463663445719449767879766939772408037144955720218286734264715442537270461139516550790667239742239029101575285259025514474934314397623765165709576405455892657921896041018921961108975637531835687478692805185118448148299444181764811143864427040598380710288441019788905286943644942161051345306991964574552193920233026778986699931805833119643177042943723604889048067403495780102764127073761027001601979969311944502537328663069857890636960199385927309381555897952997380867739815362669366241188393682047225038496881886899857882267157074759937466651368826046065425402462340730038206074819966350495384341752888931374005151304550414276908888171730290772275680137185030899803172796873719491842487425951312908022913523254723850157038156234104053087800737484968997639541261341404062307569903036493050427130613651287311666218988189014921810249332162987324741860338411938484556287638974982243148451803616376148812512890215428228557552672984177828899409273321505867335664467334127385383836763778297975937450876365495052446351451583251523869882137453866863703009942090243666651958179316722547195535422874220686894546963677090238788102834588048168159735726939262080702298875469675656136472392804428496376642107266329503622224086920588334899564029113510902395927186878205964463609238197213441976629039142251166808903300542650519961662572865863486129122889452514446015938312483450713639153888147911386950046420969859895653049269027405312568242886569867837711814955660388263789838371576698868070550868133220451741708521682732437525808553612357090174538026990408321193822425118693367294808706772299634660950254690129419441457367997569627704383693945612127537868586770246531603666684939036779358085303954811674011103696379415856240828081007373823435594302467581172191883089446645838975205760142846407963993358821436805460552694482263679800726962255425174914003446267126991320294417855106502839983816899588361706544183777980531182362103352897399888075541635233911358004187536052027267710065421508949015983481768923178162945920653312645302300727239320188642565253716062402158359205585253125319520796981240619960353042574056452185106247832163577000761039979737072338486837192153920150772780703710857113293140824706439696172713290928548692739311313105348787201677599659279895686068175720128998874803683712564351124416917694217936658514659492607749707266195533296851127430868968776727856689865519377776272970341768398564892923260319571639417081407160136250944321050289065387153890048942251503778938149042714846274356834106296408040778465757271112360089855160988664438377645403977081683045602954905017122809704850357600861698454844199723533356495990622381967427136427345171698728004839200774505828508629104115077693715541790327814407434439547623247248516291263071929238984372306200683429055441501038513295818030455027730532813991911727550753054283547650408326556998669327008256370842762683200406184484540626414488344775235125024237372736027724373392382825556437322529812555101534239115652094824148723902973961217180242628602221130741934559692776059238218155908433837420207778383791749288554498618251781453431858178498770644758180195535859591754727913077369059957623358304981876804338462613903083180144618552183303694666070984496530753193461803462696822634930853463491247378557980939904610908774021980761086900434175735529189860537515453574993009091069185266742098144709099924047428596178694115719592109242883207947006419726187994434000069278928912037477193327278292124342067072331727592565884634638778957818182274206058067180266151878196452917265286681443485478299882739172069638630576983813584005192175919858039449274825561063961686818481954189163267646208151064910343058470349517430653408319089148934425850454949442565754605871901522079688342674492642684531277144339057516057746769183840105379598600224508743561894781147659553396250042238149785647147683508445470073413651778112524692055716606482887495373751955182713372899329447129604019583437706254095153815193261403357922065313526724348494297389398666093537721413905371327118477929041617756595410596677169578173847840733591997961499134125459135763073920029288219604067220919318436789076711508844313777298021906839993077583911867005713697965748519382281601552349784227111261318990949787896535124628709662278567714465203625013293271023981269345901862487561403223014193744706471411947630731269774770742370262989935536196796820013004607306231045591973261000287743817939390500013700478604203525609038258222699612106327300804820358259441229493469606092529739198852482167621427213098602573943228719055172095087266389140672884169293243246832955336143457849516568892989091839278350440362713383605229868581120892600065788068396742013713833621714084840614497314898377625357886899689815482035125323241091691399747736087180028105375325214537113807320221404796849871174430254271610685056624578327620591821669954617691501109280972529189859917038516273655842522652621655947027474957642287699316134480216752855880341370015370302298796777750366813571412135084803233146779406712617863629394244712488098873963805087469027976385363326691589319088953576492647582581158990933401839380039752262394741041826438533638107214759357741167007866202138049043521398088516140731812236876965766192027575927725056536423102321202195702569049340033696601373959308858151126629515359825779752902802903870274488443608781228671053553939471038024858534664683362189054649161006035932392172920425768132979097302606184432349751517342405285814932277975586316845145353600327076754181316870864966907873490067672973064278731037366278129137963431399452639930472980775087111184484123183977179651391506654578700855967871533706617280309930097502648155837669698807169406280553850831576496429741681136970031051185661107131086903340799112825054395773387109234047660771078547272130618334998096469598440114180575273750768570291194224668159194657544737025570323750565939885709620766253965330909010841355390605889722089679816301429759348192509837952117678689604953013191046756636210715473608596067715546266770136954176945440599619555291731640583151682521377695530042126715626481618518455283311522627586162257261188951417152299696138837542413547383002992850313104932087397015984613669431248117761458730765409887417761829433451576155378907479750646420828352174135076993375597641701082428676699251218110662862880315650468782767096242887834977241008209010864620870533423076883295613228330907202378757372401991613369276983331214775266638183493293939655068749096736091601073739074760345851057570301234263314862582536973887917107727561474809233195592129159836036081094835508710889260096822169669497228428326555151494153950793882519939147657178047583034007092680711591743995079485897742748332057196397279828709159270001174518604317993573092434779402966418227932372955760687761159301677807513164507117175117998392563119670868663343511832279956306504326560314125286569069318108255720173270033694116342513469415241056162373137638671239669267315074644672414426527359556876947040857882212691467079476367068745797701594518134084213687806502768048645077880862546138471990539072750410245509456815457764279936919054371188682831249562970882104988012354910811142854540266397138956785835810166408988219042903917880352114222408964753063822426900513859518532597384081000039397419400378345282799824782529518828832312830104965555838934802854236364050113770698175499137696214331634612005121977159134148245425989471855163163336807736130715352354851124146998367437660017833396472614379171379209152440590355330954561008163219216485559325115814935966360139169770558661757136898833315048900204974270895212880856249462719019913994772105439333339403405034625887490998125490566932774856814561654593840352533413217959675475637451737821924823266722189874351255542372354414059810253768661137223189229255336889288591003559570700043947434848171860280916333692003885228250388500208352189685134935807560017878841711930022899831059452956668851384709376890826029592177598778523649746595110348798898420841418207941530161942181945241419847000971784125157452542125037962616246848631953784909940349805800099564956103450971165282235544395108206103038429242281809273003224386476334908857269189298225201650708299425364981437886601749705724362514143174233510625596513883690171517056662817781701508207757643085793411828689533823057093010379503482566425030750237527214714215756979163513930075301345510366334061732279497650211556487682409398154096103621532160230491225309550381284132137608270583857005147393569698028710721535579767931789547676337380340914109036516350989245672336699007927370263098982183188832050548481626648858530566083515237000139639986201740903356537549854801285681891378299458495189929801095645623670543213832479625775052271506626316223057663059992658096370641765965624554767274675147089993577169993421198678040824851561909529688660321966477330939790531121844420854430503201989055977535004460044735629325116451829665450792612318190780098263721628173516293683356178776092325593245727868434331824230485555583796164755353142345970511043089796109164152126542314466018347824747208846765292360323963451990152148442293353938257563375943705825436639042406622858385415563731234893805835009360549491835350658084151572723102752905633584927441225875028825644011060320869985784329247803773881458096247041553335676207433691278766134362044596441990614981217212074215068444482271616128290077128880847105127213064090989555607480625252473404625302505068969948298256031926812278434729114473208145353163648962152079264925253117518941515685024912736226364510075121389584374248969866660561508318135062825503639859584492022302142120643730610848905888598759012527195442310657923800806516896700276482757437697415290726206168543711695317193110096673822511982010691165628680912955695213157239457551799388479856962438660231986928601903111433861997562850685393556296902548732120260462817744886175695497886430760201333131600818922721967621574100679482176993389205368654039962004358621905353727283696080953005304064963376813084753921083488510372805567342254073328138974984477029290160986107918849698021364100617901905960115738491800366173408075161382907673615842204345023089226629619947206183344872098355344437994284074917680733306768504109825426876578840983940682998505572584401155365748405532163405545132880644160958487023410713725222647139030074118896968409838022621598035750319219157392609096132430241487497823728489336050590515791602296390112897832441257279885571754735081843033431838937118561543839679416320067802301636168010693028151042319460277655777636005583483108907680805093699658793976309415702230941208062789871408887140519556990531025098923642077192501872940191923028819972001485501125684744675007965367631373641122532874652484745955123418712734816800309473692562858902992754575124919318040690760596782459252251380134037013306665672006637902292249601839836281442461155487463193466960123597767101182542098264123381269762085418957653766256759856099733608835399638680763074407915774104727107188879799005737479345270542029237299830282861622727294854451453263133111690360424849044360569843230079846179155045370771804304346501475751357822338223306674043431848216790898829353461429998195820712149851262517749889817090173403670700400822729815325456189314792839814453124692623052569459170172919773281273063225677036288096260326226570592135353483238033770110054563126948542540426101973835364455404148246117656363106269841914686499448331799150626183927833393467697461156141638149222691192476805169537752854405300516183959128661166029883136655108657404453733412454106995760637626257394029180694739720018119683063901383675787724092001618619611297081553315312970888727813483210713742725865368005916541912060163309537032340683938080280209830058451214669156008053856735041801052111536138482165710001616645449359879628614948481115815424020705582110660605643162875030948714881687402226944022574150075798096027467654132081423630683516078690285714890407182231330256689045975704272097999037753657527313517239942458247926569267080701265984196035537531265105761298435928145676947522304645337686581801101172614751233850314992892142965142343629951451896865991695123136052278037072346883351911119959097949989433972505024623871677879590164104397823966556541225679285298651883167399293918023690497167153916631128507668296401312993205097852899186432652117535584860420780322998310045726020194438799371438877559013828716886385214841542058131795366647422675559899009188515451820477379093650022648763332489093199197784452894912996720655957503327868375049255218303509110717229521566138026322658812586874830116265433935338771493461948672234534697605561594686757857905267062362082376085790233361106805405532197348764285573232322926424706894770121685718560430853746909487771218562001704678174416419393302598961285134638188089848636346941976309770118433179372185456517406618110604739621062506169193530993141874897022268144756627956205170753985797347117214109103436361498777285744361466956390464182813479596722869867233669470049623801960266112132910790615947378664963603688050920738556734740900421654192041335216693590423584649832379355545095675706095365767061720555892793227387023828623037433843376300514726039284571889201047224795179902002909802294672923393585293889743556793731427771524229962632420602667368176192174703548324293359843704211249073481846778949530822370497468709544757931090704804024341663219263908066578778254099693947428647180012227788739503063087078817691474702345633250960240395565235610474909207451787906446871429844606217792539929894146710748364670395520690902810328375239995179151585492649526933903722022983699717481844202716750436745683368438899326148817972828311870215247428387727984556160995224943827945052785114613458043441088034015013529621431680788690961113456924980549407773207582908548278198956339140885209537161401097462998215395247920606771139933297745311802839939922314286425648761966756476344769765806813752860536579108899900992224010670961823545711982432998134689729719964948741942983376914362008915633604082085743080408592360040534391479459610243245004999887305937144473736617504192539225032147989873071952737141651771004831642515756242396724378341506263525941389776738855912534880789773749592623032990418995684461406838536289343195287104329241367471954546078328349873178196804048873990808784399346378895572645018778722618430125511495823108219260334680962265416433389977099506013521307287300923646312885023711258031580709622678905397515824556632577209194620805555208451739757423331136051161663397220127004154478388330599825413345019218024142928536464106297711253144931927988048853610919683134639694673565070657580549680048915381846696004016044574017599890409578220601765364431048842843101818304663238678369660359705117136751961115554925828236511185395149116081400069845542402566727463162388360768220509187284019376806784496953321710304586670913475486745064936518257512992480604682844594988599241147653940918687844986146149603325678645888354421995518608080700594410791468020614678452202350469686483597701698871940291386133124318721195934983691210232434711050055565705532676556259754704821647696394725230242656324252830803973979173757232780612203604191438638046129930911300840696588768201272682418962674159797887268109899379434189966822929505981485056962346628602136774618762123116906515241296471512299526892346494963487150844119645573270760415250779879228952065234454387641873500952732536899619282182321978226147628805377344198356274757039315242109105327231667349106842684637328253608402756897391094902435869355734799136969101467225658861721445830458501165251518276970380486404996056642404521612959617600446004473768915529485364767187670406024151372786014248445220276958031769099300315416082545136796012400094083863704282005494164665812606171128583432855303178405301398271667917391181172736260851870096378655324770049239919774003274313665290519877637692914503344826213860446835304591551498883963014234948067563645288239380117515219673468864070780444964569999805528810175286608148600294209106658390361870223113521187494683790924736620823906514732355606651897749764712036064894243852501990283345377169491452846438805070734769831489657489940943684869894952530802833528097486812370167855473870305166485595512705345699801381570271565127922149376177357700534988126104531757811083739356398241330369017592366235251589317312522683416937590625520784178956990839393547987102316981266405904084696618638314147217467551170957449743202387413986490110569766001505436359226237682712112220998139208792696779628167163648755578669128371094599107986527303265566836343309756900938962750979778391209037600498094794243605270275748468778297682858242975319879597637215366701316191659446226508630020959808774857016720279819310226265842632056434423484803634591616676989783940524553829392974874456482909795039224767080920241503370604089660496006863971400987187294390767448526578797085917673191978735410465782318267582462702702572799106351893224941116590036202813100801121239207860771695480906320585337358420249174516166477309027547390486562973900390583391279632582616986179881982388905658745850614890244133633202516822496780579732259831695178626153587357001730068906801065214447449091541212588581867874050968636619534813947967647873833942886488078400533403775069452631719908340987045201511459949812117687457785188244548074108029201899455798752851455251674061303699668880808949343687030864718180255391066637637080350663679121000139685390945483296890653073663543629589184325215425236512985125184653404742419583109709223181574644213628198329865332054631067054865261088752185196994522440674048725880952552687496505822812071187048605763254417649853923957484049820183698255965506408839841368726244711366106627939441319838042382575279819496014229816492775454612903564271881546226146464286773273815837800241550251875998127338193885195919055408871647347173748925481510630381456427428296373649534997291619049701853234809421415253821483297568466430121522558128685641880677001791842164593293334477556126254727842598436629489612123705851972006267306989406477362382021497929716601110932551472518505829187438154376595272034895592278178861600261995427112527813587985780215491701775436475009040346081101391226963063672020611871424521404338294781365806917752720479215901722676593795778705251861934513677690177588917047144956204017820475448156255530828283339826505840519657055719558095661072023131359316725142698619969431611317426450822106859659995423358447138560091574318246406502945392601427537952854120842919229818608627398163293866494311335820258872711750907303300872064621518319560892998312979372988587787300916759856361190711975542771694732852626278623626485306311302829792810018524794775997935704109198544227870594371799315781120303138533203024169101579432625247928174575632794887411038727780687782370312289736235707792260110198881635836448290690269298537049786299504089151280547956930215893087064384304783568281438975784384179963238246047004902496280606623464020369492485827105434758982262203246878483836664194305297244476967859400984995720041878684786743153321228332232446624920976276068005214455132328043216198682636066686660513865178559983508500301363174651963576161147948074702593730586334906894492811250622611044336618672211193328907367530584992075359604359484304453617136712389165942489203918549577423166128428455547580864838326956515282026601358358645699358883725413970255189976645760139034090087285991689250112245820944160862346915071007948353916656661660467424321567699535261104409712748466552974275520448553101325139557097186368148535858630592934220831895417239512722359494490457343241041281918000814757564162154645966546155621493292990091699574293936532733562198370105704875165543462599663998725682932549077274673799424424171639461989611314954814401759225576506694266898659218698755144662016702937953232551833770698749449240592248933336851979813319608226779390380937534558603017100772338834035880132991613300123221739269414910345167299937239117774694336433701458180784544567987209209488537196349870694007525908788315881395632276016154359298360635511363263367649843934921739879426621980249143660458771814434679539643088188465898880523322946856365033849765296522356079519115802161545262559982692104865778387696685676970422747820360469454089247725251435977888429644124044795294678324396017292701571856516761749106945824047417262873807851645024642024465605216954787213250962568798587874313112891943553299542017707782917498150019922112688666117028678807923391805818853961336232255639624713971931805896693244586516034850276945085779759660149603544216387216552154905493377470347312701027359369182498649718113592132508348886886196017062930735370912121516886371828733237485470637162708634553074301053508488830077569963298187418282168186493674473935204810769182683245064669223800992279753882517802081810041094089562723548489680308028964073321829162154677432261943358170896425390810801729263730611237894578940493454026200623302380218690696990333370180943378724374960021256581858891125452509308886246506360125695135494332624898863094506603772189495177416889595597098926649049268619484201486048415192785375976797810744277100806745348144095517209662703603761464984847263410115055627586075654180562550857980505649684638319304978500561915380657495227107988078671752014949179612878468012407548800718194724190786850678717245973023667885817001156408263689834960931978579923229994328150627664048494606466907591253442789634699238844276501309090893204005730019795166084450370743479842221969780787391263734406672587471848800393434538156311158199943619125509676813612045351780280345350555491291501266303481714306682583473692830709044946806382375270940760409757347879273846230501835211135463795072670816510626496600215023789220939488090305665258132167547128988751692356772372337902045935400749341774793451583494573854021763657384753043987874719930916561042358415946942973579804646284470379530153007166377244032928289898852454787742706721938617731348026618612123454854618756344862415228600716254107392556485156134919689185022345575133445185239313233826332694533519825203660499542211708542216641344768898485970812183691107846853735405084483924819690472707512801431415745927576405591677562409069661997754764531054811882750246841264304559735091840165573906261559017482401983542372233659592428606415206874397981759193214579070330708095440628986603888398085026825938037044716755411708280487414413341865383397310074696950668537671774720292526972756528940407296799793583085192559076674714645158266097417047144827593163741025349501235071659998316179558987225629293246811693963278918596697500539155252169595380659216350196565591458348642052311361186520348079311755525997917673638533012547485151783525782116796050013647842387108412333017248394754659225276071706342222040981300537625392835243839545112315471184390481306704531527784760298200959303320600361672065447997003779649098815217303710065811431690686118373901855012494153933566896515234710406867409840034918036152105146092083500391247657967548701266066417890065773856273634059396352415324975236151848062639909943358986650010790578608647564681468023895541129866528376760040484314066946654211364837812790551702719394047446936675221681861977132009009276695940904090301418243589414276530214056112928701292398791628604906498537041271532725562936260974306742895133499875003194782382467691184761270035867169078681212150822803796480895383293957594643290276884560197496548402762611288918587951672867813195690601118443049939830659177490593862471299076530926750289076738283613803723815616244601169235853416262212822551298024836195746248880635109915696991462780568152487231531524129457803017057273205831132490731796903800411456282216458382215085377666632816848275839343841515169280209412334858052527479143027722725093992932933917276709553706632418689063586424908555211348553372714858231272522006711371151182470104026116188542892383598859657716615294926265815992862348868631214556612068689788017299825922755549329463974045573130006571105561007013553968260537549312480431474086880467671677442344388222774727965588693804166774652845004976460370528400949637744435689919534873500412468056011242088863920642016761857620845289839434963842296563562186688160486043380362039559629662072300781923657858372954149598436695316703250415783746030129943909360175067110215541434568455238219196175211967061622800159374142470069943491630516916965494539516174642431779009755091927421438348535848195902340091702135363652852653059319842529975850059393171027633000954437757688035962476021349009241991086996960250526309974123440402835556837143956006990820527947368103011148718138072876662339285327271183605664575455774423791292703730371529275953809905117454976962200579192190497169843130863928372536461211710586929239386911060034052956873511589906362129260550666354683328745701404516761261155992547871802689133968305846925035888305209844359663837499521440556599903462916674984160462683891180244763572892483083027440385040719419411148594351474457055851894113853604832572632258671740134220406485059922928312745153303673609655294308789549130185743404915436314628728019583535330884818615528036365696948898422161771565737903048708577252484311769338471403082821717128836686237670957637380996782369438305207102078943687459159450343341054553661691811609476725828455149698788546901201109587584288229887420571400214230871198471716102158849706602525631847417801393039848384661032504037540275981723236267777244952740008520157437747550246397158971709204916742503638126541035093014492637323448389652764637755056791764341852604237905142025649515629696514820930657749534219638993094265346269037681732246959975519207597976685920116412788085191145542716205845264774020397364919652543973688012078290439301875657509929415449705662703920351698791584747667961647298898329941279039283901378482588003531086991401635939780481809495201914467269371145167613652214596486664426395081484190244734066238531325869019021019405972823717991658061365661229501024748166203910748170477213287210035630868531090198577912970001512983118747226101818139940715705013273716738533610708806095237203374963254781395209207946945893163329314442401495826009338656659448455699226653282020741885180330283716018965198715402179709606263515443691240096040280878688236093498640332938848017856873055225250484152949489592609067179989292896263656233137692199918695462486741581174990455724982618331221684513491132165933233768032827132286684224834791867641358257557966487005918979895274491222913836018376834889832868283340157380538298679641243454464644461603807462999610019893907436676539904695734725617203446274418268052133074285714351763730517624066014836458864114717951069583044931748835333903993323224577422545561178537791907416550546155902161734759839487018375711201369804786622248382423623957213160124139123799125075366318339060779189956506987514184422344703797850177738210515683726018887758507457076936546631691815229091433464699358890656511236718026699237315465657169257686332867774108846619789291723438157565871996161034832948122471632856050580700173196795621346140843149468495320419855301635474065287912319203660451353731188395857314964519287873235130858895583991090887687279406265096884474255017267095115608604572416401708444477904498718917852060637487235261466972398875887613865436884548143979670786355747125581999814827709631445422075832336559414938469751309429354667883130153643371478476496859356222160483582015324059012972687993205195451459280281237924864130953851411422031647279807694595071924220250067925490820164040986052098942828673513376449703989251108386480663802067990841079591654501633095894304383284790109547143919005356531717570219781825178542554825444783218161181350501384951739210640163419928610343109411202348136935315920597570597057268356167762251522887013693819357801397499994743359869536490318173613305278653263113332160858686463650233568603720375376727124576641241364835097103956301331809024021260983578917035122189190407235057850597862033496136420189506888163662616344485883897749293631771076923397518787402793547647031783250360138765806589556942980253013634803413064314332268037894660554875887688961400131701225160330567798127562587734366987417938500736388346173396722535217746027742030299403446446780089166945087061181601631214608784997304178591188308533305589033187011945761079053660953173874210337185112111255437406330761625512716378671499700237100145322407292417707529464110964605207276090797289561372595220516592197192605663868932452085253675932547069437421826744851191284616340065639559595957001439817235331257305258608487154913508882510869871685871473982615241207235881341114280508753580866328061477383957931786755821733515889609593598998333492809146797171450100021387847620994131480085785487019751274172142360398039743093780457770982572646184960702671288763373893223152171762420605214494440067487183130984929258735582928040574331840812988417953562314723681468913100768691011489364505805351262316448173333201834118365015598512407449654277688257671087354268711887323987152765396745789181756273202284147703604707200314112298943353905231616662604930801771867121632717340121759880778724020614238283832885467463531469837379676559828451258722908817457141690343103678544579954204862331810908373752553234344165528980809147256754062161477870414564865792862195239935659773832751357295854775404604914673004435000824544985590120154967698681508860628035600546065440837420566395740300968209575967284152644426865520689254962382982650612160929198809150190732159103040865272899929303414242765301978368294946352037485087027062932927172799345336242207336435297565026975003139878468124410326904008834599456779738565462837767634275354411177982414680736884239193411074702455995915168996359964539640279375598305399329032243534208684928011171105680483527889747140727709781279591339635374983581311446450601336404622293455625485913570823512756773529852610132630342253764987536983206864719145415526790122892941535482032273236689446413172749832886819994062705248649430749688114856639698796106182434772344713555482182436437571414163495483779014867448466213185468263793441716656102977200892744624411313137194481250716500340459223128984117355873445849080258181793540898987708963678523220301811976995839945562394085565054972191293769602609295382181648709783293613379109900089126859929916306095309007071814023014059947833049155267370851006876151653259698007112523136368864477808411074007387851571221098621039117332552359481945388171145081353306287140217426495675165443865080776281275450894777149829156847379990765694957866019711366950718558747690816641716931520969673788975746917311797453713069754359467554418883409920036500075416627460499514480308291301650006415540556479911570206611074849502065842668075322739457550267312628687361521322011874986639730835657057894250654317438610736305880156367915934880492597456069450193814513458388694324746697671652645234170245545061862635923223029392269934986835853332269771636628947661765422400672327030559390277777678947820604764751915067653314567718358331265681995106225133192384414644550444628368894712325367549993194154786515131616129030649358709923297782583231385794919688842918516255705356943424299294945100835697329454842531426353298599148066896331151798984002573545826591281954783533689100687933601678443788114265097852896087472478701310744650429400536836817620117126550380578796074975891022644035699953092834566550522455064919668426381931301825797420339002414079949648534248080522991714075314279484685933615457025715075701363221970154752958447489460714504997479954402848909451620826177900959893454369026227278264100227481194706138108623921122914920197568146969577916675862748665009285047710184389460705260643600079737804977569218021451721529298474717087939225323262678032457572723825354824454320826112141408298834895244451499676812482516022098493616335353022588401683515810125975277571669840826804783254075778645552270031063523811508514670799930522026259717383334179852946217331801684416312341024200170488996132867936057834722272883776044044649150215545976866978420021852665199518299001572296037085809943260819686352910387610862454141106765902842778967084945721313889295381368910359896849362565605496521523644048706461979045142555378437656658566568968678230771895126264280439751795314731827582131847320549692159191820026273542656341718453888623263188717723952208949111088306502831178519073535807012400021571406644119288096387029996596794778088443329627473933688508360009625992261305986275614873387665626190168024518542368016136348374023605815089288705253690776072793851877211668550045834890286112890239312219110450900105820863745381710266721478852906058015571367270180463978607299429185703215812809210817971591441156720139651565634331856107090759919967046893847760763833738344321022880627859521365416392984999916801825013231385802206924189491960389435576538243625374092784559197567039707224084259540946478464215349601451247369342849735471246631743326277108129862603181498045782903693068389435239011312955931568388478301771170878866078289960547789896765651931789757211313547660960207474899557876333047577866533422496285343671410565823933531635030884189151200837213825717258700710749953248208522537003579834262501446155270413547453590690101412268878372323775226179980022943035376527519393399981986173892466503602323060809369205715668049579173405006143209644127422260176283512363875975755539585484304238415063369602600760991139922653852056927311095880139272417274270615481560516942837768962256668983228167003771319685106860857823535811761508435981765180111503072286079218598775102235303580778412641365714928465871327604743109753235873336221999630699248339787869279804034337569356905445926224503479343412883756113199917185189491808488219805383323478719582828577805039423921476982263801915235942242525146363063527433662792451786642328644808359636439363080511660001779026873724804120661479689512267251726676319719163259858143389633286137787307738695522144354157069051256634362811580946985171220904705566439355643002442392213771420758281268448724028486375461576733059201715421769004450871703468947461232967042638810512505206592057739689811310513311826962750292195148635873462762197713107604361220205465686662478945074101608620817716783146703007242844520700928572556773781938061785427856080192440151018232490093029432222255295393768221557713669968292543990370286562062644002921301891261091721368770983388372213895406776914041997760073321040348838766987763833867007155053962518392218213706106443346522251884785017048557754277874694575110783771901245424304074962398908127280335779829320228027978345109131732337594034445748353754322432872747356462869112883851218931891183500331781958035796328787933985750615180858576581329315468794370785227097337889715999688919739011894540805376131858479910707129690732415507742815427582255871840953004742610019499235555058761200238010009160657735483446051682724245112774705283315954181398922395026621481144624938076377585800659225397969392787160856030755055513740481336187137815412388010675726313562349821733836447274926664681290487182553949139331381166527474974019489410698767596621852983526378023937066800212611880236680546987128327990687680033381861532442588507027533805778830566374716087150414445981926924503137856519033855849605488157627825471274965547815038105804985405107258168975202534834165119678386766315364003721073829947631629429933302056061754113826546727614738588262012780346062755401956815348234360121989919298977957648112936435573830398598478186587679859321891970169868849839288440506528890349499485361264901839379326074180787439812721292828140846817150863580695033781040442255803577436820018415919256761383522309608199096951970725167538715020500637445094608459520470545710778236029077599784478793694328581916858329182137112934879144334792467426668624330793937593840348269494929477751863680682515945134102789207955720542359258367615160757871585702501109161850761689997991262025253554000919017464525120479194919590790644781418933475106862066657947963391255500801307843749901262701318634958939412086499932291248602681428420120186585072980470148939155887467544757116139451165127220557338639725037490374630247613728545371716565844381910598387448666445041531490631367689809222986077316052637162746831788339950621191523531930203642371368404404039172317221005602794040897821439910920641117892416797000388255031030517386027847856071293464380189195354589503225005129854358451335292038443456144111012691619335942251472018983230633197225819715072968433728259076848688728285822385467779061954861694843948386191840244408501290586752403302265392567191823967838662453272379702754573217362910810120305567620234037975841654280875513219545335225141463139498570324017765229064687081874839436117548709316459091767514188529443709379497656373231561588585383377919719488912256749260141386877412978458507000718662578227340830204070040289987882832120986033281640027923025166672676115711442810206800043693329616748923209603802400477551424610648040057504535677345691388060710966716776428220016149293251022713480072124046684620535079090919580011805898063609471186816792283255829605977923465704411873039494088962410247624156889075367220980798991074067861797875627990929508997444808307630873948369841349238527589203450810770215445910778543171528950905694392852334535895686332836297530433556627002716878166311509279050251778749625594639776364528140346041750640889819304761202946225370882945550219618297977105306984461047696181513337394253039474141165072125526291126254408565731855859527136954854987469477943887613639092017295375253577082693946957619544254208291173136565755318068901607563958721017635718174336983095806106115324543062435709288862134568361991566469424571593257219640019513793164653414865452183474971042034079088501315688732581880505719969952832057073101657369860759871907813515694057406356825273428127612947611432607171600667500379946843992162977152790061739071159005650307203281465772537709662580900187906583050611940244006556764879430059481520689434883493634436161614936768305103870063298496872714969829209885646421099704316728821830606217278041926414026350427617670732183287285010425882299038989979333625733304139712356302520422771318547115879549094234806792088631049524081103227480015215862889494091118584143722983512178712811182742373999156494753023452301010865035654354750977435141266954917700083156891285682997007668503117125184015281511875457399609942608395838929387271292168466105081532602045138777938709169500015569004461017369484271208507626847754989792545352975122897881775015328739860701477773448532279374577351781892031988761986127358786811677284925138736568556164507209899701788729701381584190926442215210808045390969445856091825636550230402656578703813765277854250974456611369797929307851205757267849697172766389938940825307249747562720290086000422373787016052280293437270765988235986312496920397466207099550367887379805646087997163112865985583027131058999555536440196245780619598580054520447101981688604619816008762386039350665633850190915807318384382498369306918480496194492111400338226966267757975889478414215857700054439813670401657474137802122582926123406834506962665200855711507756990749103649861367799539394346903714805890892346870955614322703987959383790357636926851479184251866628554553921106709480277466546476579831915993715505174982669264463468450214008526505047520478073973529779515135720295930642019935260783263788627667109757588990237226286172226033798782827715050751690445898107799575432105126453116487380333851477238111037489899742458858534797471996647695089109901194042669182980286331442749191327893870569819370037273117640146801322415291242946041978882936226079743545429436349672651202427097833044124079959501171777510953182034435170571398639890861164921102354975603191622309273271620622030867034924361395722337448703337379468302586000783972812983019563753230607994415395235497044632009731291287205254454477537119243245333545034997017588203969905793808105840591403030621026225817880015064361162415468083230162748760176888445030259502835565671953623294619868499186340631491301762710007005111381004166812614132047022750209261996882398699060954926876353057580889241750678752309493282003656952487207489730373838142018621861115110867997185852321813719730887568578893725996654265066460654378023153894739045835079411113298584316665668014518800875147015480807903649565827993647943018996105623286827549444904633496963026227289360696214445918596422143016001621166688480367862614219930482500398272294384602772412112171201966371988721592673663853689228317467363424715782051694311034571109332911951229558697172386489987097150271047947898065236834408214632699556481612022465884736203494081237280041336264155278447540454347032098486635879951669322044221098348451065976924912771689925245918989583485551246895370798975516067618163474141443296542451921074561859172419004739488156319337259339158978766657342916287668743259125078115347043395288911485223776707204729688568004308434448416413776611269472079661102192815047398356670901976069512664944218278534196340044887995005292483021466534218576363572035802194059205492615443211698891308826506482773820952102323108945897907495605245867188184108981873562855990636680412206942216469535500373931863613032044018276267794536440245316860087897128374233245581330262531828516243722339653155165473109785855690992952334002379533380181005139384625264069698201032333124261000560434997387522587701056554597498944528304717280931293306975365469706573253658649130543149394897169266361563852022616933543436075722768794643025746314991006441536269876175592251946905724436824363474062456722985824716273957160521931893391278087565443597792305647554169804961185748393807778432531763796119446184752771742348790071971218916780219910480522676261561699157062837032747866336282183062963503813430750704092764031560142455991588090065442802920402830091410500057284426143700170599570389072834543951218882494112523833879958269003923992519258869115509739516028667707926731513153708282091646920275937235055547284237506015627225008153591092830474811126153496870655594854935302427400605169084701606121584716608712960806429484310766378198163910479308587305125039802493748175541650435064378040379040440252946458993290388971687167631537189624019742206677235776125293050070682188762850270018550079059719894634618602799956434703766923601307290376843261624863984547429929542156052699964266551359781115578256621797921224394233882903393538535248076584714010960665239383519503094468558864651566297783583361690552710679327952996493098647547110942309690869780643500219475245958980096959018733957086242519056094236631659584833706989966225736578993636196663192282189117027148949624229626565600356114168312052179105402071466240653365507004142237893337105703854811172249696318091103525938705754633348567260951523431726721019784973928709060042759770139661525869079971325442224539067064590803124109695187281333132981553482560752969472140781086717058707521864743865518141472322728400014826527775620115327277849830778855233133724001266019754853340463010567920130862647788495795307551767254347600168893567109186485611308630119617807948911088195195610690190351837684009350406946543182111840520564606558386436511370438077035081137832037001283312139421416821866818172209321619773080275560392015822051508809109250347900434912164219515739608640929071329310974848772235275533969518890107652693074105929731194471255633202221601821502525927312347276601328888031211692393647592270302869787560221230548986619496489988606297242998519386400341210794165641950135292835214585916641799272445693117101124218114758312477633534124462403516053055711295820162459848495403186728674376565273906800265862723679646145051949665164602457358747115750208378624939875018620781588935000533756529007368257626336696631674553550971601136175216141825441327143234060728476764875825019752308950424733519134209340349858833247864148634733791028883803301576107194281898979435390409301560825765740726847082850472805119990200993139861090946365235303081123491108050228795923751262540658593196306775774021628169074131187612490434533046499944651978653121688261997838156199170413919454705955056330899702240200714653628120191946492598448601788927263545640573861709098494004253736005547156156823504013604809514032734559480899242131372077452643566798459858579547756469905751481744727360089958851839296882053235625459426525079466284964301225429666543942482223425948952828467811416624008989435856097967113496490876466953755091109516959989100411597495079924283798274768158742591543405398187353722844223450886296303391099895792276402172048652816797143581010150480338913635329571284674859383992564449666494740257879096021787010094098251072828194372504860898041400085145512015881478941768566928330100344241857980303170821491164924263765532788269734508811623050290183570437531415324735330526352313249529760017789901585222555457869558594402955377247198452172406725564993100742352847230902818797453445184542460142351603193916582436303991615778637569386901310766999340444563072334611518838100365610093874270679997593712424336286341181435207697230112781179792638895152022917367665727686139829246934819194874649018172481565033708483272592702973602317779171347340493285051976029037630747686115419428162032830226834280279784849927118941502658699770998250406337940052504139333006337942305517943309027611553227293614287078139385791360974396095719883734829666234164234309765283162821414081651760472017877206388299001002535800436034045830764473001208235662002062566024668987315778565785594726145808660298344804541102635618096439446628222022986482962562559176826835849735709256623474973889247872633427980482845930628856346841502645810605201189439879300224354215760899460744350915148064617784300961328387610906548222605258430402117804823204351511227938421852237008509770333547497507019076165972736558475120886095982438154933006928202391367594043979891388443738933133423077797524664184295000538691823342161096946833250199055166462484383482343964895317452467033460390821635512024336095426134945708927931132534934878448680092801004644262816583339041397402934998945128910728438219492068795918570937190950302533774281226333416461380418486893240999446639559331115476955072087732831663012884647680682132049315227862507230517252041381179837877589176310102286153219489110935142642260765154236028504931215081461194940628679789187514667488251813354384264867553152551828489217731641984764930640377542752520313253695248744152615403089566563662537856556072398924008255778322960595490620125162197814860218054673493658563070260166896740964655160924276304499274835123444431915420036093516067853157617221998923052000302073054280696889936750183779961176637806277376823503052543907126226933093083114591136582258156817369014704971609521766057786755620441987444995648430737335103523521324894741191740875659389425538216986252907198093099782630943451986300784455386913649009964913530869440567444575720604316865669876185303470115510340599851284028294815186609264327016108395551136440087282389707646407591739562008704526864379944082768250548080779285608203510043772523896427599936440103555779487900139276712293609257212423582417416028206120343013093876711812012121540389564496339018838666228340371692497019942059929663797575608611129462007148717415518476077088271336454194727958307951588548787463696242755772297371752990556114534394583804619779431773470960088204830682918134229473421983312104321340745028168838676609631624317893163173189972621693983950141462522829238097854549382335665435100596907428124015315656268670240967671850359749609619555194876677357585803930416253132065580944370354654967323100230898031860790410296262729768557577625789008024301643436613184657775650222584111642808674974897745911911144857117133628025280222042400885334906079200915370193425319289788633747365920317186519301578165811070852556145676933445102026019997395089303785066018221995139037074544702760082229102346357338693437707058230602237880194820345016552613115514085191197771204409737357146043372782114624824093860529856751498872352899131803698118996286926638324375576360439953436722373924917535572810425753752498517943660397123865903790538852843932454215754051215923788869224110844970485697535781024331255071846845912726116406970281161253062731526611748703522352790276648287528154980936851823973199135671217266813467478197010139194999676304753009228685242040161008315141557161183469553921947675039743621539012213543930818894753451703523436893622007783171391226865716668441034074589047887814414142794319307496525252418474151335293737840977868156164900921017823974053390897611644620843421556281535264653033974959715544366248765465547946744770570731730575689132134143122957088995384631308975791736814929046492246012965338685809896177264545711955154810238511438491507724680735961292309272026458552937998125040786176744695562380773115568841585370393183545821553554886028172348766957070845465453096604894973360238457785462959152900695963281692232615288478955397767492817974373817460270208249082006604080758687690112424067494784187541967088127163311376234469270111279958747420477804560192219874097229984211248704481570193427671870498374491320499107692671396394722775450080711289253761252923392917781716440508658983977667624315263817835019982532981287138448394185200535613762617613920183870841380722921021396155302398271695889629657709447610192538969622818841817788556373789494138733740920487854939473154420954321946753231506509246006017768252488429051308317462366789588354288445741868125043033437260953362979305586681070868596217261259442147773392758262815951722716229150684636285155459931103071189760968943100886092482079098801190575879678144945788508311739379743781611630462566981327345734810290926171948198970907450371568039890551578820434658407600025312732938811995077911030493186659332461624962648392515611835516921968159032578449962616889352328677028440526976763274309698662109394079070528478977148112596066181601272532572599337446370671805645008691663457979057360368154608288940161838330203744959920990013676188228782170791357438276737631146424846722653133349842446764987674796538047290791400677311526241093714340325307090741928144061302152113177150354478355439967749220929506268779984272415031945990332126200187528709641885169129575476875630550173124701800287779297660728850307499548109622114766104661660347524046303730830994699203871016357226314390797533989101927218860604452300748619444788855416756260290285520869842384413500165776539111258618028558350391020014612820567389925094277986312084016411901403597319643752241604631261448934281671825556392674577716105297934939403617041022195618988917667106464891489947479097044521054156565811043048047876627466471598443078733405034681639940703551080671715302639285955934505978920956942187249113421541201891515051994807659847710498426816542239883077658920580415302936812841436326740423074934045760336523513465374725202283768401632347952184438905678268100285163241121373350371273714419711557739949967326551465994679468998240094178642524360319998594340485589217747500015929372471868111895485092999166507384753081460520842787995257055361664848489061794709603099622733087647261770646028842960425732693917031478679933519694446183339474228052738001150254739211621193972112885643787907612688270021187087409482261961240299972202891181770911800070698209248789362598918865890021357132301593704317412136066844219954385676815658868986837788384296172428756036706656977209458780165024653353641440136317533273914031983581314216014970416651475656403952596847631809478542392889947250801872530502487806426180985456209456353946755172464219900181238604567992378125516452465148502894614373236900559723753451141891358573379258057095484390983351489069588671290458021370608597920156930603575567227206886807535793616626871534687637547515425225269278651869653359728939870031899350674047873691963354625671632546055077948296629034352861572400095733219784865458748109597948221430914048379901422945917138776078782608742181825975617621948798469206855927902237532688684197853844679520508024413398255301159985700342031752191709206535792468672134255895734463429778942250325002555429171023081739970054171196612886742110423297108944751560903944994467886076779282310074865091723093870820670529269086189195453487491082336649655190473716948548915197909007558780741057713087957384523434355071242978372537600883098480102107716650134875634858067741860352976321960110984874664737416920398845866877419888837030185535980698319014778260616652348308799941467031962462064182955565639117534271188434765272240098206994457801806573157885117098326161291634293206334870280049035594189072578235045660206902986839045062177567290684187740116726431859002950193816272407377952152004141267470988350355777829317580495052698324230480840242863822216092850001376207319981933472065815647066707610953979858450755683279606734287456134314138737385423273729417581449281827685559731137970804795387092858707908067587620416903976442682979057017647818785498274196325258801310166800437757894736516294107784703336906909693680477407268654638047571932972849705251196031078653975692058938889406646711136280077650595691553684600474335037165499180878558552715166113836059664242401654580515789861510907146242445005698604271106644334856636074616321617697474077834826936823511986210922965987770196845755239421165739804029756607403940664342719523037025036897187768924267465989813796965501029955787954996887224214168547179675008622627185497438750329300326925467068735344732063633681417783625475294406737703520994655238860977113177618497612406223320830314361339147656504473810583968703788318416929791661575996348427374520243574157879378517238475064199856691907587634009398256746824260504542242631013884362370919571207484233535296153840049732854460726972117723759876377266183708506501911505640687289143796748164718969708764312552887615030346347032916757801962079717808923160492724489515790279466458246353364101208503467753399466580853544242342646678465017726896599622361669409929681333210294505611326317847128361071346847757507662020235267713254434753140870629150316981093347995405341285625393216778036332267090516905993364573785814606387061970689019400680803817809940063232322044949949682924837201498454124208444517972902000412668247143478703767370810634053947746936563193878615658825353631315407932502190939022601742202112598458031932991887164294015671539004990610056935696283633786297541643973109517778233328126186936460756922505951300173063826006071634014142421599040388173213893315152409430481371695312799477030296154764258073487070390675390196200829149280703220227546673920950018595073633313287703825130662287202951035984934211024680376254413411235831329390089964512309557958198373829889667765877999098650794808447328436999931264314537004871397765664282517118379708249416011868170256246426443013754932838138125664106882387403158895003489763945228316807828723149269620379161273540105426852384686928747873596741397820918509579929358727699097156002725675143635779546574305987858587401301664922202495797557305036529411603545721606674909823207046139416128223559882336620649451887860670947884221792523730015610419233467134667736027420057326345379402770069243808458151310947143770474830597140877622887824243486401815312958186443551097387283392325078357952046230654598053679734601588540362620060479060276536459132372546565653123695579913597058101759487603816507383345010831310713571273100072982602421100695156412162269202715892999818873081420337072130923425280303530718797127238912121542355786365190536642567735049973793744859795609123200878649687290292247483381436507006989060705972140073934734136927984163026545752293115660552974362253649933129658343062817540463504632690739478897848551329348902386945617721924667993029500725191348276888037985118447713897337452970689778856589045715285285524186663534554756828863552740827658996160733964104606176466756542873805113004382716144204993931655640443136613377434500229188123058962989657125203019921393161876801906859273769284446701929272051052021370458761779423068827913780382582415228516372232302364992439337568127670252686773717511227790224016332599819851902485443492943488249165128443962988214205624260789805459791798114252680268000832613972242890012991838260368460730961265546720725356478381840645992536200188245048848123313010232520413751240480242492260841635811788929096259313076771964448117007849282344410739505013608281128305917798175123783497920845230103058119047790235820272098066659138927403365336671644732431207246054428917895218549334958752149440466270384723153808014485954805055885438262211559900077869834684602299978408525238338019903249939423346238135179040556978131673335056016721125363128704798524153550994604347456471160504845865764241992556826852201695226154928931956741052737081712080780882950263237454804052686202735000606286798731614244406695642946685930834723438343486898669481058616545797274747756976840746832295636045708482941269533203946411189181670021047584140171158429367454221984344964379254848504922121919301621909116530342827134748794674827545702887127205074683270263134983789686493477182220370219975270216224368920037526752907224528483438402109979388457588280936842841292427586517041874773854315265047848841623539610044780052998916082168391000048059814100937905067962903803587697830721561649415209565214564863203152316354832042648928799523877174778278679520747447432976658168656909056501596450918589856246982487577175700241458896655460786578102636192135180492217256350975597616404393856163291077164898346720331463455155218572939731883843071328091886539234185289242588401826215576426072860220981910609939585683772069927592057129627438923321165518208642275802283832877106931810662115165646789070432358620350248722303297672086831189338364635073268692532303272914957906847237116843560641455781281887355438549162246400003207232406566535682214334541758137175045804365663471947710403054049003766355502754564595966019522583933640918436836353553521178682857170128240373922323367997636222625406940291823810813889557739121872850911832222757005745474728101979922818626388306181960879379196778091354410405347214239545522342672137330468094450100165596683759625211635341600537818847040402835480034356228587564293812827226356546714784807017592791759910332673027391436135732227027789335164058323269022888279938049614925478567219740365700691504904901048186863470364286802438920420474355320638418061952106025112027553316794833396633280272922361951817345452023355689681305840842113986499495401685425785041756657801628282781542537931041218941452243134979664742170191176693108375517769513751702490329607878273606301620910983087800087381985821733128955216805782676747117481124263084526286341504341827882429142700575414032825543260673788859016763073589054569243442527446375037331540195072134320832219643028662086040707004618435926338254083155807217835382264336317187110006919186174308916806007576953791147313659952588988468683275253627487119827193400726085084798992781099866341928482926894133141461341090814981625144269735910143649904342584892992663172215924720915910285153346718617044099509298204233100208463936564218328796140277237834817035163346468911590698214976638355042411173093202482942268884270301679292453873307120914644912927935903354194832033524487958724021120129686717016780294628980657659781247704907191442317574548697997074158738052932212266681508290115099675085177402621230726080604335061865493782906635713386630724898686019124115121234450286707440565351815339023936526312641874175721409838242635836488030784994686599157917133585451974907676066212306761970282082176339643671633920044465209341071848062490377851397845988974450127975450273268592008009109856322568359882509524106794320684655136209350215051011600124427963719809785940023232961564265390858454947197397902880892736838707903302141410394143656383906953872204234570939302368077662202078313183372096195716367393089111299360895832216271767619835683465794245406407619794388620122118107266593027972833719455550065305716476609562616368604236619433536688898051493836777197494076528393492098696697021461012632481458796601988499984291571669636587088346184551482985609438354105948330365104865467894715691305553032194249692656422503482856907648252175686095209226219211188043261559306059525108556070896180189413983371860908567619835936063393301043658355864852278630928795784825512218422106004465682880119956668775757523511922129782856482574333947790831771639068688161061085614880802920502936442715903249686830924782136815832113937365792625462415306671728028665108768109445017555259888186352988317860141653254190427601948758072439949933848955198528792385559968694938537590817374168403241949407309748214814211131356468122669446745178779897630029927514790323739345369315035706604309594373287203446894941922011437485751323401004993830720191844096719466907149702175037587617843180379213913598871786774760742670221648451194030329922574419239948480096408892621132998662495153068231010610366473832997626020201061510813579172884364418459462970016204514");
//        test.printList();
//        System.out.println(test.convertBase(9223));
    }
}

