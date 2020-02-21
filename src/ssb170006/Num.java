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
        Num x = new Num(999);
        Num y = new Num("8");
        String[] strPost = {"63", "53", "-", "10", "+"};
        Num z = Num.evaluatePostfix(strPost);
        if (z != null) z.printList();
//        String[] strInfix = {"(", "(", "63", "-", "53", ")", "+", "10", ")"};
        String strInfix = "((63-53)+10)";
        Num a = Num.evaluateExp(strInfix);
        if (a != null) a.printList();
    }
}
